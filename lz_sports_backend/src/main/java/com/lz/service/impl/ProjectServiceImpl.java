package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.ProjectCategory;
import com.lz.common.enums.GenderLimit;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.entity.*;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.service.ProjectService;
import com.lz.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project Service Implementation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectMapper projectMapper;
    private final EventMapper eventMapper;
    private final AthleteMapper athleteMapper;
    private final RegistrationMapper registrationMapper;

    @SuppressWarnings("unchecked")
    @Override
    public PageResult listByAthlete(EventListDTO listDto) {
        PageResult pageResult = list(listDto);
        List<ProjectVO> projectVOS = (List<ProjectVO>) pageResult.getRecords();

        Long userId = BaseContext.getCurrentId();
        Athlete athlete = athleteMapper.selectByUserId(userId);

        if (athlete != null) {
            for (ProjectVO vo : projectVOS) {
                LambdaQueryWrapper<Registration> qw = new LambdaQueryWrapper<>();
                qw.eq(Registration::getAthleteId, userId)
                  .eq(Registration::getItemId, vo.getId());
                
                Registration registration = registrationMapper.selectOne(qw);
                if (registration == null) {
                    vo.setRegistrationStatus("未报名");
                } else {
                    vo.setRegistrationStatus(registration.getRegistrationStatus().getStatus());
                }
            }
        } else {
             for (ProjectVO vo : projectVOS) {
                 vo.setRegistrationStatus("未报名");
             }
        }
        return pageResult;
    }

    @Override
    public PageResult list(EventListDTO listDto) {
        IPage<Project> page = new Page<>(listDto.getCurrentPage(), listDto.getPageSize());
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        
        // Filter by project name
        lqw.like(listDto.getName() != null && !listDto.getName().isEmpty(), Project::getItemName, listDto.getName());
        
        // Filter by event name
        if (listDto.getType() != null && !listDto.getType().isEmpty()) { 
            LambdaQueryWrapper<Event> eventQw = new LambdaQueryWrapper<>();
            eventQw.like(Event::getEventName, listDto.getType());
            List<Object> eventIds = eventMapper.selectObjs(eventQw.select(Event::getId));
            if (eventIds.isEmpty()) {
                return new PageResult(0, List.of());
            }
            lqw.in(Project::getEventId, eventIds);
        }

        projectMapper.selectPage(page, lqw);
        
        List<Project> records = page.getRecords();
        List<Long> eventIds = records.stream()
                .map(Project::getEventId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, String> eventNameMap = new java.util.HashMap<>();
        if (!eventIds.isEmpty()) {
            List<Event> events = eventMapper.selectBatchIds(eventIds);
            eventNameMap = events.stream().collect(Collectors.toMap(Event::getId, Event::getEventName));
        }

        java.util.Map<Long, String> finalEventNameMap = eventNameMap;
        List<ProjectVO> projectVOS = records.stream().map(p -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            if (finalEventNameMap.containsKey(p.getEventId())) {
                vo.setEventName(finalEventNameMap.get(p.getEventId()));
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), projectVOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ProjectDTO projectDTO) {
        Event event = eventMapper.selectById(projectDTO.getEvent());
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        Date startTime = parseDate(projectDTO.getStartTime());
        Date endTime = parseDate(projectDTO.getEndTime());
        if (startTime == null || endTime == null) {
            throw new BusinessException("项目时间不能为空");
        }
        if (!startTime.before(endTime)) {
            throw new BusinessException("项目开始时间必须早于结束时间");
        }
        if (event.getEventStartTime() != null && startTime.before(event.getEventStartTime())) {
            throw new BusinessException("项目开始时间需在赛事时间范围内");
        }
        if (event.getEventEndTime() != null && endTime.after(event.getEventEndTime())) {
            throw new BusinessException("项目结束时间需在赛事时间范围内");
        }
        Project project = new Project();
        project.setEventId(projectDTO.getEvent());
        project.setItemName(projectDTO.getName());
        project.setGrade(projectDTO.getGrade());
        project.setMaxAttendance(projectDTO.getMaxAttendance() == null ? 0 : projectDTO.getMaxAttendance());
        project.setAttendance(projectDTO.getAttendance() == null ? 0 : projectDTO.getAttendance());
        project.setStartTime(startTime);
        project.setEndTime(endTime);
        if (projectDTO.getCategory() == null || projectDTO.getCategory().isEmpty()) {
            project.setCategory(ProjectCategory.CUSTOM);
        } else {
            project.setCategory(ProjectCategory.valueOf(projectDTO.getCategory()));
        }
        if (projectDTO.getLimitation() == null || projectDTO.getLimitation().isEmpty()) {
            project.setLimitation(GenderLimit.ALL);
        } else {
            project.setLimitation(GenderLimit.valueOf(projectDTO.getLimitation()));
        }
        project.initTime();
        save(project);
    }

    @Override
    public ProjectDTO getProject(Long id) {
        Project project = getById(id);
        ProjectDTO dto = new ProjectDTO();
        if (project != null) {
            dto.setName(project.getItemName());
            dto.setEvent(project.getEventId());
            dto.setLimitation(project.getLimitation() == null ? null : project.getLimitation().name());
            dto.setCategory(project.getCategory() == null ? null : project.getCategory().name());
            dto.setGrade(project.getGrade());
            dto.setAttendance(project.getAttendance());
            dto.setMaxAttendance(project.getMaxAttendance());
            dto.setStartTime(project.getStartTime() == null ? null : formatDate(project.getStartTime()));
            dto.setEndTime(project.getEndTime() == null ? null : formatDate(project.getEndTime()));
        }
        return dto;
    }

    @Override
    public List<ProjectVO> listByEventId(Long eventId) {
        List<Project> records = projectMapper.selectList(new LambdaQueryWrapper<Project>().eq(Project::getEventId, eventId));
        return records.stream().map(p -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        if (project.getCategory() == ProjectCategory.STANDARD) {
            throw new BusinessException("STANDARD 项目不可删除", 403);
        }
        long registrationCount = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>().eq(Registration::getItemId, id));
        if (registrationCount > 0) {
            throw new BusinessException("该项目已有" + registrationCount + "人报名，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectDTO projectDTO, Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        Event event = eventMapper.selectById(project.getEventId());
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        if (projectDTO.getName() != null) {
            project.setItemName(projectDTO.getName());
        }
        if (projectDTO.getLimitation() != null && !projectDTO.getLimitation().isEmpty()) {
            project.setLimitation(GenderLimit.valueOf(projectDTO.getLimitation()));
        }
        if (projectDTO.getGrade() != null) {
            project.setGrade(projectDTO.getGrade());
        }
        if (projectDTO.getMaxAttendance() != null) {
            project.setMaxAttendance(projectDTO.getMaxAttendance());
        }
        if (projectDTO.getCategory() != null && !projectDTO.getCategory().isEmpty()) {
            project.setCategory(ProjectCategory.valueOf(projectDTO.getCategory()));
        }
        Date startTime = parseDateNullable(projectDTO.getStartTime());
        Date endTime = parseDateNullable(projectDTO.getEndTime());
        if (startTime != null) {
            project.setStartTime(startTime);
        }
        if (endTime != null) {
            project.setEndTime(endTime);
        }
        if (project.getStartTime() != null && project.getEndTime() != null) {
            if (!project.getStartTime().before(project.getEndTime())) {
                throw new BusinessException("项目开始时间必须早于结束时间");
            }
            if (event.getEventStartTime() != null && project.getStartTime().before(event.getEventStartTime())) {
                throw new BusinessException("项目开始时间需在赛事时间范围内");
            }
            if (event.getEventEndTime() != null && project.getEndTime().after(event.getEventEndTime())) {
                throw new BusinessException("项目结束时间需在赛事时间范围内");
            }
        }
        project.setUpdateTime(java.time.LocalDateTime.now());
        updateById(project);
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        Date d = parseWithPattern(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if (d != null) return d;
        d = parseWithPattern(value, "yyyy-MM-dd HH:mm:ss");
        if (d != null) return d;
        d = parseWithPattern(value, "yyyy-MM-dd'T'HH:mm:ss");
        if (d != null) return d;
        throw new BusinessException("项目时间格式错误");
    }

    private Date parseDateNullable(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return parseDate(value);
    }

    private Date parseWithPattern(String value, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private String formatDate(Date value) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }
}
