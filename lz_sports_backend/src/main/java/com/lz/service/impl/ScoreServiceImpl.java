package com.lz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.NotificationType;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.RegistrationDTO;
import com.lz.dto.ScoreUpsertDTO;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.Score;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.service.NotificationService;
import com.lz.service.ScoreService;
import com.lz.vo.RegistrationListExportVO;
import com.lz.vo.ScoreExportVO;
import com.lz.vo.ScoreImportFailureVO;
import com.lz.vo.ScoreImportResultVO;
import com.lz.vo.ScoreImportVO;
import com.lz.vo.ScoreVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private final RegistrationMapper registrationMapper;
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertScore(ScoreUpsertDTO dto) {
        Registration registration = registrationMapper.selectById(dto.getRegistrationId());
        validateRegistrationForWrite(registration);
        Score existing = getOne(new LambdaQueryWrapper<Score>().eq(Score::getRegistrationId, dto.getRegistrationId()));
        if (existing != null && Boolean.TRUE.equals(existing.getIsPublished())) {
            throw new BusinessException("已发布的成绩不可修改");
        }
        baseMapper.upsertByRegistration(
                registration.getId(),
                registration.getEventId(),
                registration.getItemId(),
                registration.getAthleteId(),
                dto.getScoreValue(),
                dto.getScoreRank(),
                dto.getRemark()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScore(Long scoreId, ScoreUpsertDTO dto) {
        Score score = getById(scoreId);
        if (score == null) {
            throw new BusinessException("成绩不存在");
        }
        if (Boolean.TRUE.equals(score.getIsPublished())) {
            throw new BusinessException("已发布的成绩不可修改");
        }
        dto.setRegistrationId(score.getRegistrationId());
        upsertScore(dto);
    }

    @Override
    public PageResult list(int currentPage, int pageSize, String eventName, Long eventId, Long itemId, Boolean onlyPublished) {
        Page<ScoreVO> page = new Page<>(currentPage, pageSize);
        IPage<ScoreVO> result = baseMapper.selectScorePage(page, eventId, itemId, eventName, onlyPublished);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishScores(Long eventId) {
        List<Score> scores = list(new LambdaQueryWrapper<Score>()
                .eq(Score::getEventId, eventId)
                .eq(Score::getIsPublished, false));
        if (scores.isEmpty()) {
            throw new BusinessException("没有可发布的成绩");
        }
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Score> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Score::getEventId, eventId);
        updateWrapper.eq(Score::getIsPublished, false);
        updateWrapper.set(Score::getIsPublished, true);
        updateWrapper.set(Score::getPublishedAt, now);
        update(updateWrapper);

        List<Long> athleteIds = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                        .select(Registration::getAthleteId)
                        .eq(Registration::getEventId, eventId))
                .stream()
                .map(Registration::getAthleteId)
                .distinct()
                .collect(Collectors.toList());
        Event event = eventMapper.selectById(eventId);
        String title = "成绩已发布";
        String content = "赛事《" + (event == null ? eventId : event.getEventName()) + "》成绩已发布，请前往查看";
        notificationService.batchSendNotification(athleteIds, title, content, NotificationType.RESULT_PUBLISHED);
    }

    @Override
    public ScoreImportResultVO importScores(MultipartFile file, Long eventId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空", 400);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("上传文件大小不能超过10MB", 400);
        }
        ScoreImportResultVO result = new ScoreImportResultVO();
        try (var inputStream = file.getInputStream()) {
            List<ScoreImportVO> rows = EasyExcel.read(inputStream).head(ScoreImportVO.class).sheet().doReadSync();
            int rowNumber = 1;
            for (ScoreImportVO row : rows) {
                rowNumber++;
                try {
                    validateImportRow(row, eventId);
                    ScoreUpsertDTO dto = new ScoreUpsertDTO();
                    dto.setRegistrationId(row.getRegistrationId());
                    dto.setScoreValue(row.getScoreValue());
                    dto.setScoreRank(row.getScoreRank());
                    dto.setRemark(row.getRemark());
                    upsertScore(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getFailures().add(new ScoreImportFailureVO(rowNumber, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Excel导入失败", 400);
        }
        return result;
    }

    @Override
    public void downloadTemplate(Long eventId, HttpServletResponse response) {
        List<RegistrationDTO> registrations = registrationMapper.selectRegistrationList(eventId).stream()
                .filter(item -> RegistrationStatus.CONFIRMED.getStatus().equals(item.getRegistrationStatus()) || RegistrationStatus.APPROVED.getStatus().equals(item.getRegistrationStatus()))
                .toList();
        writeTemplateResponse(eventId, response, registrations, "成绩导入模板");
    }

    @Override
    public void exportScores(Long eventId, HttpServletResponse response) {
        List<ScoreVO> all = baseMapper.selectPublicByEvent(eventId);
        Event event = eventMapper.selectById(eventId);
        String eventName = event == null ? "赛事" : event.getEventName();
        Map<String, List<ScoreVO>> grouped = all.stream().collect(Collectors.groupingBy(ScoreVO::getItemName, LinkedHashMap::new, Collectors.toList()));
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = URLEncoder.encode(eventName + "_成绩_" + time, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ExcelWriter writer = EasyExcel.write(response.getOutputStream(), ScoreExportVO.class).build();
            try {
                int idx = 0;
                for (Map.Entry<String, List<ScoreVO>> entry : grouped.entrySet()) {
                    WriteSheet sheet = EasyExcel.writerSheet(idx++, entry.getKey()).build();
                    List<ScoreExportVO> sheetRows = entry.getValue().stream().map(item -> {
                        ScoreExportVO vo = new ScoreExportVO();
                        vo.setRank(item.getScoreRank());
                        vo.setName(item.getAthleteName());
                        // Assuming deptName contains the full path "College-Major-Class" or just "Class"
                        // We put it in deptName and let college be empty, or we can parse it.
                        // Wait, in ScoreMapper.xml, we will return the full path for deptName? 
                        // Actually, I can use DepartmentService.getFullDepartmentName to populate it!
                        vo.setCollege(item.getDeptName());
                        vo.setDeptName(item.getDeptName());
                        vo.setScore(item.getScoreValue());
                        vo.setRemark(item.getRemark());
                        return vo;
                    }).toList();
                    writer.write(sheetRows, sheet);
                }
            } finally {
                writer.finish();
            }
        } catch (Exception e) {
            throw new BusinessException("导出成绩失败");
        }
    }

    @Override
    public void exportRegistrations(Long eventId, HttpServletResponse response) {
        List<RegistrationDTO> source = registrationMapper.selectRegistrationList(eventId);
        List<RegistrationListExportVO> list = new ArrayList<>();
        int index = 1;
        for (RegistrationDTO dto : source) {
            RegistrationListExportVO vo = new RegistrationListExportVO();
            vo.setNo(index++);
            vo.setName(dto.getAthleteName());
            vo.setCollege(dto.getDeptName());
            vo.setDeptName(dto.getDeptName());
            vo.setItem(dto.getItemName());
            vo.setRegistrationTime(dto.getRegistrationTime());
            vo.setStatus(dto.getRegistrationStatus());
            list.add(vo);
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            Event event = eventMapper.selectById(eventId);
            String eventName = event == null ? "赛事" : event.getEventName();
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = URLEncoder.encode(eventName + "_报名名单_" + time, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), RegistrationListExportVO.class).sheet("报名名单").doWrite(list);
        } catch (Exception e) {
            throw new BusinessException("导出报名名单失败");
        }
    }

    @Override
    public List<ScoreVO> myScores(Long eventId) {
        Long userId = BaseContext.getCurrentId();
        List<Score> scores = baseMapper.selectMyPublished(userId, eventId);
        return toScoreVO(scores);
    }

    @Override
    public Map<String, List<ScoreVO>> publicRanking(Long eventId) {
        List<ScoreVO> rows = baseMapper.selectPublicByEvent(eventId);
        return rows.stream().collect(Collectors.groupingBy(ScoreVO::getItemName, LinkedHashMap::new, Collectors.toList()));
    }

    private void validateRegistrationForWrite(Registration registration) {
        if (registration == null) {
            throw new BusinessException("报名记录不存在", 400);
        }
        if (registration.getRegistrationStatus() != RegistrationStatus.CONFIRMED && registration.getRegistrationStatus() != RegistrationStatus.APPROVED) {
            throw new BusinessException("仅审核通过的报名可录入成绩");
        }
    }

    private void validateImportRow(ScoreImportVO row, Long eventId) {
        if (row.getRegistrationId() == null) {
            throw new BusinessException("报名ID不能为空");
        }
        if (row.getScoreValue() == null || row.getScoreValue().trim().isEmpty()) {
            throw new BusinessException("成绩不能为空");
        }
        Registration registration = registrationMapper.selectById(row.getRegistrationId());
        if (registration == null) {
            throw new BusinessException("报名ID不存在");
        }
        if (!eventId.equals(registration.getEventId())) {
            throw new BusinessException("报名不属于该赛事");
        }
        if (registration.getRegistrationStatus() != RegistrationStatus.CONFIRMED && registration.getRegistrationStatus() != RegistrationStatus.APPROVED) {
            throw new BusinessException("报名状态未通过审核");
        }
        Score existing = getOne(new LambdaQueryWrapper<Score>().eq(Score::getRegistrationId, row.getRegistrationId()));
        if (existing != null && Boolean.TRUE.equals(existing.getIsPublished())) {
            throw new BusinessException("已发布成绩不可覆盖");
        }
    }

    private void writeTemplateResponse(Long eventId, HttpServletResponse response, List<RegistrationDTO> registrations, String filePrefix) {
        Event event = eventMapper.selectById(eventId);
        String eventName = event == null ? "赛事" : event.getEventName();
        Map<String, List<ScoreImportVO>> grouped = new LinkedHashMap<>();
        for (RegistrationDTO dto : registrations) {
            ScoreImportVO row = new ScoreImportVO();
            row.setRegistrationId(dto.getId());
            row.setEventName(dto.getEventName());
            row.setItemName(dto.getItemName());
            row.setAthleteName(dto.getAthleteName());
            grouped.computeIfAbsent(dto.getItemName(), key -> new ArrayList<>()).add(row);
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = URLEncoder.encode(eventName + "_" + filePrefix + "_" + time, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ExcelWriter writer = EasyExcel.write(response.getOutputStream(), ScoreImportVO.class).build();
            try {
                int index = 0;
                for (Map.Entry<String, List<ScoreImportVO>> entry : grouped.entrySet()) {
                    WriteSheet sheet = EasyExcel.writerSheet(index++, entry.getKey()).build();
                    writer.write(entry.getValue(), sheet);
                }
            } finally {
                writer.finish();
            }
        } catch (Exception e) {
            throw new BusinessException("导出模板失败");
        }
    }

    private List<ScoreVO> toScoreVO(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Event> eventMap = eventMapper.selectBatchIds(scores.stream().map(Score::getEventId).distinct().toList())
                .stream().collect(Collectors.toMap(Event::getId, item -> item));
        Map<Long, Project> itemMap = projectMapper.selectBatchIds(scores.stream().map(Score::getItemId).distinct().toList())
                .stream().collect(Collectors.toMap(Project::getId, item -> item));
        return scores.stream().map(score -> {
            ScoreVO vo = new ScoreVO();
            vo.setId(score.getId());
            vo.setEventId(score.getEventId());
            vo.setItemId(score.getItemId());
            vo.setRegistrationId(score.getRegistrationId());
            vo.setEventName(eventMap.containsKey(score.getEventId()) ? eventMap.get(score.getEventId()).getEventName() : null);
            vo.setItemName(itemMap.containsKey(score.getItemId()) ? itemMap.get(score.getItemId()).getItemName() : null);
            vo.setScoreValue(score.getScoreValue());
            vo.setScoreRank(score.getScoreRank());
            vo.setRemark(score.getRemark());
            vo.setIsPublished(score.getIsPublished());
            vo.setPublishedAt(score.getPublishedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
