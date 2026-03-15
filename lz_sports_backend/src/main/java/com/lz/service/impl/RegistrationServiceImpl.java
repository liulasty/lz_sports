package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.RegistrationAndAthleteDTO;
import com.lz.dto.RegistrationDTO;
import com.lz.entity.Athlete;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;

/**
 * 报名服务实现
 */
import com.lz.common.enums.EventStatus;
import com.alibaba.excel.EasyExcel;
import com.lz.vo.RegistrationExportVO;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationMapper registrationMapper;
    private final AthleteMapper athleteMapper; // Note: athleteMapper now maps to sys_user, need verification
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;
    private final RedissonClient redissonClient; // Requires Redisson dependency

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long projectId) {
        // Use Distributed Lock to prevent overselling
        String lockKey = "lock:registration:project:" + projectId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock for 5 seconds, hold for 10 seconds
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    Long userId = BaseContext.getCurrentId();
                    // ... (rest of logic)
                    // We need to fetch User (Athlete) info. Assuming AthleteMapper is adjusted or we use UserMapper.
                    // For now keeping existing logic but wrapping in lock.
                    
                    Athlete athlete = athleteMapper.selectByUserId(userId);
                    if (athlete == null) {
                        throw new BusinessException("请先完善运动员信息");
                    }
            
                    Project project = projectMapper.selectById(projectId);
                    if (project == null) {
                        throw new BusinessException("项目不存在");
                    }
            
                    Event event = eventMapper.selectById(project.getEventId());
                    if (event == null) {
                        throw new BusinessException("赛事不存在");
                    }
            
                    // 1. Check Event Status
                    if (event.getEventStatus() != EventStatus.OPEN) {
                        throw new BusinessException("赛事未发布或已结束，无法报名");
                    }
            
                    // 2. Check Registration Deadline
                    Date now = new Date();
                    if (event.getRegistrationEndTime() != null && now.after(event.getRegistrationEndTime())) {
                        throw new BusinessException("报名已截止");
                    }
            
                    // 3. Check Duplicate Registration
                    LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(Registration::getAthleteId, athlete.getId());
                    lqw.eq(Registration::getItemId, projectId);
                    if (count(lqw) > 0) {
                        throw new BusinessException("您已报名该项目，请勿重复报名");
                    }
            
                    // 4. Check Gender Limit
                    if (project.getLimitation() != null && !project.getLimitation().equals("无限制")) {
                        if (!project.getLimitation().equals(athlete.getGender())) {
                            throw new BusinessException("性别不符合项目要求");
                        }
                    }
            
                    // 6. Check Max Attendance
                    if (project.getAttendance() >= project.getMaxAttendance()) {
                        throw new BusinessException("项目报名人数已满");
                    }
            
                    // Create Registration
                    Registration registration = new Registration();
                    registration.setAthleteId(athlete.getId());
                    registration.setEventId(event.getId());
                    registration.setItemId(projectId);
                    registration.setRegistrationTime(now);
                    registration.setRegistrationStatus(RegistrationStatus.PENDING);
                    registration.setSchoolId(event.getSchoolId());
                    
                    save(registration);
            
                    // Update Project Attendance
                    int updated = projectMapper.incrementAttendance(projectId, project.getMaxAttendance());
                    if (updated == 0) {
                        throw new BusinessException("报名失败，名额已满");
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BusinessException("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统中断");
        }
    }

    @Override
    public PageResult list(int currentPage, int pageSize, String name, String status, Date date) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, null);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult listByAthlete(int currentPage, int pageSize, String name, String status, Date date, Long athleteId) {
        Page<RegistrationDTO> page = new Page<>(currentPage, pageSize);
        IPage<RegistrationDTO> result = registrationMapper.selectRegistrationPage(page, name, status, date, athleteId);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public RegistrationAndAthleteDTO getDetail(Long id) {
        Registration r = getById(id);
        if (r == null) return null;

        RegistrationAndAthleteDTO dto = new RegistrationAndAthleteDTO();
        dto.setId(r.getId());
        dto.setApplyTime(r.getRegistrationTime());
        if (r.getRegistrationStatus() != null) {
            dto.setStatus(r.getRegistrationStatus().getStatus());
        }

        Athlete athlete = athleteMapper.selectById(r.getAthleteId());
        if (athlete != null) {
            dto.setName(athlete.getName());

            dto.setAge(Integer.parseInt(athlete.getAge()));
            dto.setGender(athlete.getGender());
            dto.setContact(athlete.getContact());
            dto.setAthleteGrade(athlete.getGrade());
        }

        Event event = eventMapper.selectById(r.getEventId());
        if (event != null) {
            dto.setEventId(event.getId());
            dto.setEventName(event.getEventName());
        }

        Project project = projectMapper.selectById(r.getItemId());
        if (project != null) {
            dto.setItemId(project.getId());
            dto.setItemName(project.getItemName());
            dto.setNum(project.getAttendance());
            dto.setMaxNum(project.getMaxAttendance());
            if (project.getLimitation() != null) {
                dto.setLimitation(project.getLimitation().getLimit());
            }
            dto.setGrade(project.getGrade());
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("Registration not found");
        
        // Check capacity
        Project project = projectMapper.selectById(r.getItemId());
        if (project != null) {
            if (project.getAttendance() >= project.getMaxAttendance()) {
                throw new BusinessException("Project is full");
            }
            // Increment attendance
            project.setAttendance(project.getAttendance() + 1);
            projectMapper.updateById(project);
        }
        
        r.setRegistrationStatus(RegistrationStatus.APPROVED);
        updateById(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("Registration not found");
        r.setRegistrationStatus(RegistrationStatus.REJECTED);
        updateById(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Registration r = getById(id);
        if (r != null && RegistrationStatus.APPROVED.equals(r.getRegistrationStatus())) {
             // If approved, decrement attendance
             Project project = projectMapper.selectById(r.getItemId());
             if (project != null && project.getAttendance() > 0) {
                 project.setAttendance(project.getAttendance() - 1);
                 projectMapper.updateById(project);
             }
        }
        removeById(id);
    }



    @Override
    public int getCountByAthlete(Long athleteId) {
        LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Registration::getAthleteId, athleteId);
        return (int) count(lqw);
    }

    @Override
    public void export(Long eventId, jakarta.servlet.http.HttpServletResponse response) {
        // Fetch data
        List<RegistrationDTO> list = registrationMapper.selectRegistrationList(eventId);
        
        List<RegistrationExportVO> exportList = new ArrayList<>();
        for (RegistrationDTO dto : list) {
            RegistrationExportVO vo = new RegistrationExportVO();
            vo.setRegistrationId(dto.getRegistrationId());
            vo.setEventName(dto.getEventName());
            vo.setItemName(dto.getItemName());
            vo.setAthleteName(dto.getAthleteName());
            vo.setGender(dto.getGender());
            vo.setGrade(dto.getGrade());
            vo.setContact(dto.getContact());
            vo.setRegistrationTime(dto.getRegistrationTime());
            vo.setStatus(dto.getRegistrationStatus());
            exportList.add(vo);
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("报名名单", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), RegistrationExportVO.class).sheet("名单").doWrite(exportList);
        } catch (Exception e) {
            log.error("Export failed", e);
            throw new BusinessException("导出失败");
        }
    }
}
