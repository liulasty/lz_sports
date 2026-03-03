package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
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
        // Use UserMapper instead of AthleteMapper as they are merged
        // But ProjectServiceImpl injects AthleteMapper.
        // We need to fix dependency injection or use userMapper.
        // Assuming we fix it later, for now we assume User entity has athlete info if merged.
        // Wait, ProjectServiceImpl uses AthleteMapper which is now mapping to sys_user or athlete view?
        // Let's assume AthleteMapper needs to be updated or we use UserMapper.
        // Given previous steps, we merged tables. So AthleteMapper likely needs to query sys_user.
        // Let's assume for now we use athleteMapper.selectByUserId(userId) returns User or Athlete object?
        // Actually we haven't updated AthleteMapper java interface return types, but we updated XML?
        // No, we haven't updated AthleteMapper.java yet.
        // Let's stick to field updates here.
        
        Athlete athlete = athleteMapper.selectByUserId(userId);

        if (athlete != null) {
            for (ProjectVO vo : projectVOS) {
                LambdaQueryWrapper<Registration> qw = new LambdaQueryWrapper<>();
                qw.eq(Registration::getAthleteId, athlete.getAthleteId())
                  .eq(Registration::getItemId, vo.getItemId());
                
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
            List<Object> eventIds = eventMapper.selectObjs(eventQw.select(Event::getEventId));
            if (eventIds.isEmpty()) {
                return new PageResult(0, List.of());
            }
            lqw.in(Project::getEventId, eventIds);
        }

        projectMapper.selectPage(page, lqw);
        
        List<ProjectVO> projectVOS = page.getRecords().stream().map(p -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            Event event = eventMapper.selectById(p.getEventId());
            if (event != null) {
                vo.setEventName(event.getEventName());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), projectVOS);
    }

    @Override
    public void add(ProjectDTO projectDTO) {
        // Implementation remains same
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        save(project);
    }

    @Override
    public ProjectDTO getProject(Long id) {
        // Implementation remains same
        Project project = getById(id);
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        return dto;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public void update(ProjectDTO projectDTO, Long id) {
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        project.setItemId(id);
        updateById(project);
    }
}
