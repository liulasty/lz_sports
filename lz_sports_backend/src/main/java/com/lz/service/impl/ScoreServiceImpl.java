package com.lz.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
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
import com.lz.entity.ScoreAuditLog;
import com.lz.entity.User;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.mapper.ScoreAuditLogMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.NotificationService;
import com.lz.service.ScoreService;
import com.lz.util.StringUtils;
import com.lz.vo.RegistrationListExportVO;
import com.lz.vo.ScoreExportVO;
import com.lz.vo.ScoreImportFailureVO;
import com.lz.vo.ScoreImportProjectStatVO;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {
    private static final String IMPORT_MODE_BEST_EFFORT = "BEST_EFFORT";
    private static final String IMPORT_MODE_STRICT = "STRICT";

    private final RegistrationMapper registrationMapper;
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ScoreAuditLogMapper scoreAuditLogMapper;

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
        if (existing != null && hasScoreChanged(existing, dto)) {
            saveScoreAuditLog(existing, dto);
        }
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
    @Transactional(rollbackFor = Exception.class)
    public ScoreImportResultVO importScores(MultipartFile file, Long eventId, String mode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空", 400);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("上传文件大小不能超过10MB", 400);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx")
                && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException("仅支持导入 xls/xlsx 文件", 400);
        }
        String resolvedMode = resolveImportMode(mode);
        boolean strictMode = IMPORT_MODE_STRICT.equals(resolvedMode);
        ScoreImportResultVO result = new ScoreImportResultVO();
        result.setMode(resolvedMode);
        result.setAllOrNothing(strictMode);
        Map<String, int[]> projectCounter = new LinkedHashMap<>();
        try (var inputStream = file.getInputStream()) {
            List<ScoreImportVO> rows = EasyExcel.read(inputStream).head(ScoreImportVO.class).sheet().doReadSync();
            if (rows == null || rows.isEmpty()) {
                throw new BusinessException("导入文件中没有可用数据", 400);
            }
            if (strictMode) {
                validateStrictImportRows(rows, eventId, result, projectCounter);
                if (result.getFailCount() > 0) {
                    fillProjectStatsAndFailureFile(result, projectCounter);
                    return result;
                }
                for (ScoreImportVO row : rows) {
                    ScoreUpsertDTO dto = new ScoreUpsertDTO();
                    dto.setRegistrationId(row.getRegistrationId());
                    dto.setScoreValue(row.getScoreValue());
                    dto.setScoreRank(row.getScoreRank());
                    dto.setRemark(row.getRemark());
                    upsertScore(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    increaseProjectSuccess(projectCounter, row.getItemName());
                }
                fillProjectStatsAndFailureFile(result, projectCounter);
                return result;
            }
            Set<Long> seenRegistrationIds = new HashSet<>();
            int rowNumber = 1;
            for (ScoreImportVO row : rows) {
                rowNumber++;
                try {
                    if (row.getRegistrationId() != null && !seenRegistrationIds.add(row.getRegistrationId())) {
                        throw new BusinessException("导入文件存在重复的报名ID: " + row.getRegistrationId());
                    }
                    validateImportRow(row, eventId);
                    ScoreUpsertDTO dto = new ScoreUpsertDTO();
                    dto.setRegistrationId(row.getRegistrationId());
                    dto.setScoreValue(row.getScoreValue());
                    dto.setScoreRank(row.getScoreRank());
                    dto.setRemark(row.getRemark());
                    upsertScore(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    increaseProjectSuccess(projectCounter, row.getItemName());
                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getFailures().add(new ScoreImportFailureVO(rowNumber, row.getItemName(), e.getMessage()));
                    increaseProjectFailure(projectCounter, row.getItemName());
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Excel导入失败", 400);
        }
        fillProjectStatsAndFailureFile(result, projectCounter);
        return result;
    }

    private void validateStrictImportRows(List<ScoreImportVO> rows, Long eventId, ScoreImportResultVO result, Map<String, int[]> projectCounter) {
        Set<Long> seenRegistrationIds = new HashSet<>();
        int rowNumber = 1;
        for (ScoreImportVO row : rows) {
            rowNumber++;
            try {
                if (row.getRegistrationId() != null && !seenRegistrationIds.add(row.getRegistrationId())) {
                    throw new BusinessException("导入文件存在重复的报名ID: " + row.getRegistrationId());
                }
                validateImportRow(row, eventId);
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailures().add(new ScoreImportFailureVO(rowNumber, row.getItemName(), e.getMessage()));
                increaseProjectFailure(projectCounter, row.getItemName());
            }
        }
    }

    private String resolveImportMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return IMPORT_MODE_BEST_EFFORT;
        }
        String normalized = mode.trim().toUpperCase();
        if (!IMPORT_MODE_BEST_EFFORT.equals(normalized) && !IMPORT_MODE_STRICT.equals(normalized)) {
            throw new BusinessException("导入模式仅支持 BEST_EFFORT 或 STRICT", 400);
        }
        return normalized;
    }

    private void increaseProjectSuccess(Map<String, int[]> projectCounter, String itemName) {
        String key = StringUtils.isBlank(itemName) ? "未识别项目" : itemName.trim();
        projectCounter.computeIfAbsent(key, ignored -> new int[2])[0]++;
    }

    private void increaseProjectFailure(Map<String, int[]> projectCounter, String itemName) {
        String key = StringUtils.isBlank(itemName) ? "未识别项目" : itemName.trim();
        projectCounter.computeIfAbsent(key, ignored -> new int[2])[1]++;
    }

    private void fillProjectStatsAndFailureFile(ScoreImportResultVO result, Map<String, int[]> projectCounter) {
        result.setProjectStats(projectCounter.entrySet().stream()
                .map(entry -> new ScoreImportProjectStatVO(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .toList());
        if (result.getFailures() == null || result.getFailures().isEmpty()) {
            return;
        }
        String csvHeader = "行号,项目,失败原因\n";
        String csvRows = result.getFailures().stream()
                .map(item -> item.getRowNumber() + ",\"" + escapeCsv(item.getItemName()) + "\",\"" + escapeCsv(item.getReason()) + "\"")
                .collect(Collectors.joining("\n"));
        String csv = csvHeader + csvRows;
        result.setFailureDetailFileName("score-import-failures-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");
        result.setFailureDetailCsvBase64(Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8)));
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }

    @Override
    public void downloadTemplate(Long eventId, HttpServletResponse response) {
        List<RegistrationDTO> registrations = registrationMapper.selectRegistrationList(eventId).stream()
                .filter(item ->
                        RegistrationStatus.CONFIRMED.getStatus().equals(item.getRegistrationStatus())
                                || RegistrationStatus.APPROVED.getStatus().equals(item.getRegistrationStatus()))
                .toList();
        writeTemplateResponse(eventId, response, registrations, "参赛名单导出");
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
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getAthleteId, userId)
                .eq(Score::getIsPublished, true)
                .orderByDesc(Score::getPublishedAt)
                .orderByAsc(Score::getScoreRank);
        if (eventId != null) {
            wrapper.eq(Score::getEventId, eventId);
        }
        List<Score> scores = list(wrapper);
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
        if (registration.getRegistrationStatus() != RegistrationStatus.CONFIRMED
                && registration.getRegistrationStatus() != RegistrationStatus.APPROVED) {
            throw new BusinessException("仅APPROVED/CONFIRMED报名可录入成绩");
        }
    }

    private void validateImportRow(ScoreImportVO row, Long eventId) {
        if (row.getRegistrationId() == null) {
            throw new BusinessException("报名ID不能为空");
        }
        if (row.getEventId() == null) {
            throw new BusinessException("赛事ID不能为空");
        }
        if (row.getItemId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (StringUtils.isBlank(row.getEventName())) {
            throw new BusinessException("赛事名称不能为空");
        }
        if (StringUtils.isBlank(row.getItemName())) {
            throw new BusinessException("项目名称不能为空");
        }
        if (StringUtils.isBlank(row.getAthleteName())) {
            throw new BusinessException("运动员姓名不能为空");
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
        if (row.getEventId() != null && !eventId.equals(row.getEventId())) {
            throw new BusinessException("赛事ID与导入接口不一致");
        }
        Event event = eventMapper.selectById(registration.getEventId());
        if (event != null && StringUtils.isNotBlank(event.getEventName())
                && !event.getEventName().equals(row.getEventName().trim())) {
            throw new BusinessException("赛事名称与报名记录不一致");
        }
        if (!row.getItemId().equals(registration.getItemId())) {
            throw new BusinessException("项目ID与报名记录不一致");
        }
        Project project = projectMapper.selectById(registration.getItemId());
        if (project != null && StringUtils.isNotBlank(project.getItemName())
                && !project.getItemName().equals(row.getItemName().trim())) {
            throw new BusinessException("项目名称与报名记录不一致");
        }
        User athlete = userMapper.selectById(registration.getAthleteId());
        if (athlete != null && StringUtils.isNotBlank(athlete.getName())
                && !athlete.getName().equals(row.getAthleteName().trim())) {
            throw new BusinessException("运动员姓名与报名记录不一致");
        }
        if (registration.getRegistrationStatus() != RegistrationStatus.CONFIRMED
                && registration.getRegistrationStatus() != RegistrationStatus.APPROVED) {
            throw new BusinessException("报名状态不是APPROVED/CONFIRMED");
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
            row.setEventId(dto.getEventId());
            row.setRegistrationId(dto.getId());
            row.setEventName(dto.getEventName());
            row.setItemId(dto.getItemId());
            row.setItemName(dto.getItemName());
            row.setAthleteName(dto.getAthleteName());
            row.setGender(dto.getGender());
            row.setDeptName(dto.getDeptName());
            row.setContact(dto.getContact());
            row.setRegistrationStatus(dto.getRegistrationStatus());
            row.setRegistrationTime(dto.getRegistrationTime());
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
                    WriteSheet sheet = EasyExcel.writerSheet(index++, entry.getKey())
                            .registerWriteHandler(new TemplateReadonlyColumnStyleHandler())
                            .build();
                    writer.write(entry.getValue(), sheet);
                }
            } finally {
                writer.finish();
            }
        } catch (Exception e) {
            throw new BusinessException("导出模板失败");
        }
    }

    private boolean hasScoreChanged(Score existing, ScoreUpsertDTO dto) {
        return !java.util.Objects.equals(existing.getScoreValue(), dto.getScoreValue())
                || !java.util.Objects.equals(existing.getScoreRank(), dto.getScoreRank())
                || !java.util.Objects.equals(existing.getRemark(), dto.getRemark());
    }

    private void saveScoreAuditLog(Score existing, ScoreUpsertDTO dto) {
        ScoreAuditLog log = new ScoreAuditLog();
        log.setScoreId(existing.getId());
        log.setRegistrationId(existing.getRegistrationId());
        log.setEventId(existing.getEventId());
        log.setItemId(existing.getItemId());
        log.setAthleteId(existing.getAthleteId());
        log.setOperatorId(BaseContext.getCurrentId());
        log.setBeforeScoreValue(existing.getScoreValue());
        log.setBeforeScoreRank(existing.getScoreRank());
        log.setBeforeRemark(existing.getRemark());
        log.setAfterScoreValue(dto.getScoreValue());
        log.setAfterScoreRank(dto.getScoreRank());
        log.setAfterRemark(dto.getRemark());
        log.initTime();
        scoreAuditLogMapper.insert(log);
    }

    private List<ScoreVO> toScoreVO(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> scoreIds = scores.stream().map(Score::getId).toList();
        Map<Long, ScoreVO> voMap = baseMapper.selectScoreVOByIds(scoreIds).stream()
                .collect(Collectors.toMap(ScoreVO::getId, vo -> vo));
        return scoreIds.stream()
                .map(voMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static class TemplateReadonlyColumnStyleHandler implements CellWriteHandler {
        private static final int INPUT_COLUMN_END_INDEX = 2;
        private CellStyle readonlyCellStyle;

        @Override
        public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<com.alibaba.excel.metadata.data.WriteCellData<?>> cellDataList, Cell cell, com.alibaba.excel.metadata.Head head, Integer relativeRowIndex, Boolean isHead) {
            if (Boolean.TRUE.equals(isHead) || cell == null || cell.getColumnIndex() <= INPUT_COLUMN_END_INDEX) {
                return;
            }
            if (readonlyCellStyle == null) {
                readonlyCellStyle = buildReadonlyCellStyle(cell);
            }
            cell.setCellStyle(readonlyCellStyle);
        }

        private CellStyle buildReadonlyCellStyle(Cell cell) {
            CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
            style.cloneStyleFrom(cell.getCellStyle());
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = cell.getSheet().getWorkbook().createFont();
            font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setFont(font);
            return style;
        }
    }
}
