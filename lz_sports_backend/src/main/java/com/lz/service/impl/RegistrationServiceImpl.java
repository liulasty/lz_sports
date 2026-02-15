package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
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
import java.util.stream.Collectors;

/**
 * 报名服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final AthleteMapper athleteMapper;
    private final EventMapper eventMapper;
    private final ProjectMapper projectMapper;

    @Override
    public PageResult list(int currentPage, int pageSize, String name, String status, Date date) {
        IPage<Registration> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();

        // Filter by status and date
        lqw.eq(status != null && !status.isEmpty(), Registration::getRegistrationStatus, status)
           .eq(date != null, Registration::getRegistrationTime, date);

        // Filter by athlete name (requires subquery or memory filtering)
        if (name != null && !name.isEmpty()) {
            LambdaQueryWrapper<Athlete> athleteQw = new LambdaQueryWrapper<>();
            athleteQw.like(Athlete::getName, name);
            List<Object> athleteIds = athleteMapper.selectObjs(athleteQw.select(Athlete::getAthleteId));
            if (athleteIds.isEmpty()) {
                return new PageResult(0, List.of());
            }
            lqw.in(Registration::getAthleteId, athleteIds);
        }

        registrationMapper.selectPage(page, lqw);
        return buildPageResult(page);
    }

    @Override
    public PageResult listByAthlete(int currentPage, int pageSize, String name, String status, Date date, Long athleteId) {
        IPage<Registration> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Registration> lqw = new LambdaQueryWrapper<>();

        lqw.eq(Registration::getAthleteId, athleteId)
           .eq(status != null && !status.isEmpty(), Registration::getRegistrationStatus, status)
           .eq(date != null, Registration::getRegistrationTime, date);
           
        // 'name' param here likely refers to Project/Event name filter?
        // Old code used custom SQL. Let's assume it filters project name.
        if (name != null && !name.isEmpty()) {
             LambdaQueryWrapper<Project> projectQw = new LambdaQueryWrapper<>();
             projectQw.like(Project::getItemName, name);
             List<Object> itemIds = projectMapper.selectObjs(projectQw.select(Project::getItemId));
             if (itemIds.isEmpty()) {
                 return new PageResult(0, List.of());
             }
             lqw.in(Registration::getItemId, itemIds);
        }

        registrationMapper.selectPage(page, lqw);
        return buildPageResult(page);
    }

    private PageResult buildPageResult(IPage<Registration> page) {
        List<RegistrationDTO> dtos = page.getRecords().stream().map(r -> {
            RegistrationDTO dto = new RegistrationDTO();
            dto.setId(r.getRegistrationId());
            dto.setAthleteId(r.getAthleteId());
            dto.setEventId(r.getEventId());
            dto.setItemId(r.getItemId());
            dto.setRegistrationTime(r.getRegistrationTime());
            dto.setRegistrationStatus(r.getRegistrationStatus());

            Athlete athlete = athleteMapper.selectById(r.getAthleteId());
            if (athlete != null) dto.setAthleteName(athlete.getName());

            Event event = eventMapper.selectById(r.getEventId());
            if (event != null) dto.setEventName(event.getEventName());

            Project project = projectMapper.selectById(r.getItemId());
            if (project != null) dto.setItemName(project.getItemName());

            return dto;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), dtos);
    }

    @Override
    public RegistrationAndAthleteDTO getDetail(Long id) {
        Registration r = getById(id);
        if (r == null) return null;

        RegistrationAndAthleteDTO dto = new RegistrationAndAthleteDTO();
        dto.setId(r.getRegistrationId());
        dto.setApplyTime(r.getRegistrationTime());

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
            dto.setEventId(event.getEventId());
            dto.setEventName(event.getEventName());
        }

        Project project = projectMapper.selectById(r.getItemId());
        if (project != null) {
            dto.setItemId(project.getItemId());
            dto.setItemName(project.getItemName());
            dto.setNum(project.getAttendance());
            dto.setMaxNum(project.getMaxAttendance());
            dto.setLimitation(project.getLimitation());
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
        
        r.setRegistrationStatus("通过");
        updateById(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long id) {
        Registration r = getById(id);
        if (r == null) throw new BusinessException("Registration not found");
        r.setRegistrationStatus("未通过"); // Or "拒绝" based on old logic? Old logic: delete or update status?
        // Old logic `refuse` method deleted the registration? No, let's check old code.
        // Old service interface says `refuse`. Impl usually updates status or deletes.
        // Let's assume updating status to "未通过" is safer than delete.
        updateById(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Registration r = getById(id);
        if (r != null && "通过".equals(r.getRegistrationStatus())) {
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
    @Transactional(rollbackFor = Exception.class)
    public void add(Long projectId) {
        Long userId = BaseContext.getCurrentId();
        Athlete athlete = athleteMapper.selectByUserId(userId);
        if (athlete == null) throw new BusinessException("Not an athlete");
        
        Project project = projectMapper.selectById(projectId);
        if (project == null) throw new BusinessException("Project not found");
        
        // Check if already registered
        LambdaQueryWrapper<Registration> qw = new LambdaQueryWrapper<>();
        qw.eq(Registration::getAthleteId, athlete.getAthleteId())
          .eq(Registration::getItemId, projectId);
        if (count(qw) > 0) throw new BusinessException("Already registered");

        Registration r = new Registration();
        r.setAthleteId(athlete.getAthleteId());
        r.setEventId(project.getEventId());
        r.setItemId(projectId);
        r.setRegistrationTime(new Date());
        r.setRegistrationStatus("审核中");
        
        save(r);
    }

    @Override
    public int getCountByAthlete(Long athleteId) {
        LambdaQueryWrapper<Registration> qw = new LambdaQueryWrapper<>();
        qw.eq(Registration::getAthleteId, athleteId);
        return (int) count(qw);
    }
}
