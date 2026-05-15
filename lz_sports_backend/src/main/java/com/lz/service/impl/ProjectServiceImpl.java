package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.ProjectCategory;
import com.lz.common.enums.GenderLimit;
import com.lz.common.enums.EventStatus;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventListDTO;
import com.lz.dto.ProjectDTO;
import com.lz.entity.*;
import com.lz.mapper.AthleteMapper;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.ScoreMapper;
import com.lz.mapper.UserMapper;
import com.lz.mapper.DepartmentMapper;
import com.lz.service.EligibilityService;
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
    private final RegistrationMapper registrationMapper;
    private final ScoreMapper scoreMapper;
    private final UserMapper userMapper;
    private final AthleteMapper athleteMapper;
    private final DepartmentMapper departmentMapper;
    private final EligibilityService eligibilityService;

    @SuppressWarnings("unchecked")
    @Override
    public PageResult listByAthlete(EventListDTO listDto) {
        PageResult pageResult = list(listDto);
        List<ProjectVO> projectVOS = (List<ProjectVO>) pageResult.getRecords();

        Long userId = BaseContext.getCurrentId();

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
    public Long add(ProjectDTO projectDTO) {
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
        try {
            if (projectDTO.getLimitDeptIds() != null && !projectDTO.getLimitDeptIds().isEmpty()) {
                project.setLimitDeptIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(projectDTO.getLimitDeptIds()));
            } else {
                project.setLimitDeptIds(null);
            }
        } catch (Exception e) {
            project.setLimitDeptIds(null);
        }
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
        syncEligibilityFromOldFields(project.getId(), projectDTO, project);
        return project.getId();
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
            if (project.getLimitDeptIds() != null && !project.getLimitDeptIds().isEmpty()) {
                try {
                    List<Long> ids = new com.fasterxml.jackson.databind.ObjectMapper().readValue(project.getLimitDeptIds(), new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
                    dto.setLimitDeptIds(ids);
                } catch (Exception e) {
                    dto.setLimitDeptIds(List.of());
                }
            } else {
                dto.setLimitDeptIds(List.of());
            }
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
        long scoreCount = scoreMapper.selectCount(new LambdaQueryWrapper<Score>().eq(Score::getItemId, id));
        if (scoreCount > 0) {
            throw new BusinessException("该项目已有成绩记录，无法删除");
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
        validateUpdateByEventStatus(event, projectDTO);
        validateMaxAttendanceUpdate(project, projectDTO);
        validateRestrictionUpdate(project, projectDTO, event.getId());

        if (projectDTO.getName() != null) {
            project.setItemName(projectDTO.getName());
        }
        if (projectDTO.getLimitation() != null && !projectDTO.getLimitation().isEmpty()) {
            project.setLimitation(GenderLimit.valueOf(projectDTO.getLimitation()));
        }
        try {
            if (projectDTO.getLimitDeptIds() != null && !projectDTO.getLimitDeptIds().isEmpty()) {
                project.setLimitDeptIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(projectDTO.getLimitDeptIds()));
            } else {
                project.setLimitDeptIds(null);
            }
        } catch (Exception e) {
            project.setLimitDeptIds(null);
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
        syncEligibilityFromOldFields(id, projectDTO, project);
    }

    /**
     * 旧字段双写：当通过 limitation/limitDeptIds 更新时，自动同步到新资格规则表。
     */
    private void syncEligibilityFromOldFields(Long itemId, ProjectDTO dto, Project project) {
        try {
            com.lz.dto.EligibilityConfigDTO config = new com.lz.dto.EligibilityConfigDTO();
            java.util.List<com.lz.dto.EligibilityConfigDTO.RuleDTO> rules = new java.util.ArrayList<>();

            String limitStr = dto.getLimitation() != null && !dto.getLimitation().isEmpty()
                    ? dto.getLimitation() : (project.getLimitation() != null ? project.getLimitation().name() : "ALL");
            if (!"ALL".equals(limitStr)) {
                com.lz.dto.EligibilityConfigDTO.RuleDTO r = new com.lz.dto.EligibilityConfigDTO.RuleDTO();
                r.setDimension("GENDER");
                r.setOperator("EQ");
                r.setValue("MALE".equals(limitStr) ? "男" : "女");
                rules.add(r);
            }

            java.util.List<Long> deptIds = dto.getLimitDeptIds() != null
                    ? dto.getLimitDeptIds() : parseDeptIds(project.getLimitDeptIds());
            if (deptIds != null && !deptIds.isEmpty()) {
                com.lz.dto.EligibilityConfigDTO.RuleDTO r = new com.lz.dto.EligibilityConfigDTO.RuleDTO();
                r.setDimension("DEPT");
                r.setOperator("IN");
                r.setValue(deptIds);
                rules.add(r);
            }

            if (!rules.isEmpty()) {
                com.lz.dto.EligibilityConfigDTO.GroupDTO group = new com.lz.dto.EligibilityConfigDTO.GroupDTO();
                group.setGroupLogic("AND");
                group.setRules(rules);
                config.setGroupCombination("AND");
                config.setEnabled(true);
                config.setGroups(java.util.List.of(group));
            } else {
                config.setGroupCombination("AND");
                config.setEnabled(false);
                config.setGroups(java.util.List.of());
            }
            eligibilityService.saveConfig(itemId, config);
        } catch (Exception e) {
            log.warn("syncEligibilityFromOldFields failed for itemId={}: {}", itemId, e.getMessage());
        }
    }

    private void validateUpdateByEventStatus(Event event, ProjectDTO projectDTO) {
        EventStatus status = event.getEventStatus() == null ? EventStatus.DRAFT : event.getEventStatus();
        if (status == EventStatus.DRAFT) {
            return;
        }

        boolean hasKeyFieldUpdate = projectDTO.getMaxAttendance() != null
                || (projectDTO.getLimitation() != null && !projectDTO.getLimitation().isEmpty())
                || projectDTO.getLimitDeptIds() != null
                || (projectDTO.getStartTime() != null && !projectDTO.getStartTime().isEmpty())
                || (projectDTO.getEndTime() != null && !projectDTO.getEndTime().isEmpty());
        if (hasKeyFieldUpdate) {
            throw new BusinessException("非DRAFT状态禁止修改项目关键字段");
        }
    }

    private void validateMaxAttendanceUpdate(Project project, ProjectDTO projectDTO) {
        if (projectDTO.getMaxAttendance() == null) {
            return;
        }
        if (projectDTO.getMaxAttendance() < project.getAttendance()) {
            throw new BusinessException("最大名额不能小于当前已报名人数");
        }
    }

    /**
     * 使用资格规则引擎校验：改严限制时，现有活跃报名运动员是否仍满足新条件。
     * 构建内存 GroupEntry 避免未保存的规则无法查询。
     */
    private void validateRestrictionUpdate(Project project, ProjectDTO projectDTO, Long eventId) {
        if (!hasRestrictionFieldUpdate(projectDTO)) {
            return;
        }
        // 从 DTO 构建内存规则组
        com.lz.eligibility.engine.GroupEntry proposedRules = buildProposedRestrictionGroup(project, projectDTO);
        if (proposedRules == null) return;

        List<Registration> activeRegistrations = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getItemId, project.getId())
                .notIn(Registration::getRegistrationStatus, RegistrationStatus.CANCELLED, RegistrationStatus.REJECTED));

        com.lz.eligibility.engine.RuleEvaluator evaluator = new com.lz.eligibility.engine.RuleEvaluator();

        for (Registration registration : activeRegistrations) {
            Athlete athlete = athleteMapper.selectOne(new LambdaQueryWrapper<Athlete>()
                    .eq(Athlete::getEventId, eventId)
                    .eq(Athlete::getUserId, registration.getAthleteId()));
            if (athlete == null) continue;

            // 获取祖先部门 ID
            List<Long> ancestorDeptIds = resolveAncestorDeptIds(athlete.getDeptId());

            com.lz.eligibility.engine.AthleteContext ctx = com.lz.eligibility.engine.AthleteContext.builder()
                    .gender(athlete.getGender())
                    .deptId(athlete.getDeptId())
                    .ancestorDeptIds(ancestorDeptIds)
                    .build();

            com.lz.eligibility.engine.EligibilityResult result = evaluator.evaluate(
                    ctx, List.of(proposedRules), com.lz.eligibility.engine.GroupLogic.AND);
            if (!result.isPassed()) {
                throw new BusinessException("存在不符条件的报名数据，禁止修改");
            }
        }
    }

    private boolean hasRestrictionFieldUpdate(ProjectDTO projectDTO) {
        return (projectDTO.getLimitation() != null && !projectDTO.getLimitation().isEmpty())
                || projectDTO.getLimitDeptIds() != null;
    }

    /**
     * 将旧格式的 gender_limit + limit_dept_ids 转为引擎 GroupEntry。
     */
    private com.lz.eligibility.engine.GroupEntry buildProposedRestrictionGroup(Project project, ProjectDTO projectDTO) {
        List<com.lz.eligibility.engine.RuleEntry> rules = new java.util.ArrayList<>();

        // gender
        String limitStr = projectDTO.getLimitation() != null && !projectDTO.getLimitation().isEmpty()
                ? projectDTO.getLimitation() : (project.getLimitation() != null ? project.getLimitation().name() : "ALL");
        if (!"ALL".equals(limitStr)) {
            String genderValue = "MALE".equals(limitStr) ? "男" : "女";
            rules.add(new com.lz.eligibility.engine.RuleEntry(
                    com.lz.eligibility.engine.Dimension.GENDER,
                    com.lz.eligibility.engine.Operator.EQ,
                    genderValue));
        }

        // dept
        List<Long> deptIds = projectDTO.getLimitDeptIds() != null
                ? projectDTO.getLimitDeptIds()
                : parseDeptIds(project.getLimitDeptIds());
        if (deptIds != null && !deptIds.isEmpty()) {
            rules.add(new com.lz.eligibility.engine.RuleEntry(
                    com.lz.eligibility.engine.Dimension.DEPT,
                    com.lz.eligibility.engine.Operator.IN,
                    deptIds));
        }

        if (rules.isEmpty()) return null;
        return new com.lz.eligibility.engine.GroupEntry(
                com.lz.eligibility.engine.GroupLogic.AND, null, rules);
    }

    private List<Long> parseDeptIds(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 解析部门祖先节点 ID（复用 AthleteContextResolver 的逻辑）。
     * 由于 ProjectService 中不方便注入 Resolver，内联实现。
     */
    private List<Long> resolveAncestorDeptIds(Long deptId) {
        if (deptId == null) return List.of();
        com.lz.entity.Department dept = departmentMapper.selectById(deptId);
        if (dept == null) return List.of();
        List<Long> ancestors = new java.util.ArrayList<>();
        boolean isK12 = "K12".equalsIgnoreCase(dept.getOrgMode()) || "HIGH_SCHOOL".equalsIgnoreCase(dept.getOrgMode());

        if (isK12) {
            if (dept.getGrade() != null) {
                List<com.lz.entity.Department> grades = departmentMapper.selectList(
                        new LambdaQueryWrapper<com.lz.entity.Department>()
                                .eq(com.lz.entity.Department::getGrade, dept.getGrade())
                                .eq(com.lz.entity.Department::getOrgMode, dept.getOrgMode())
                                .and(w -> w.isNull(com.lz.entity.Department::getClassName)
                                        .or(w2 -> w2.eq(com.lz.entity.Department::getClassName, ""))));
                for (com.lz.entity.Department d : grades) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) ancestors.add(d.getId());
                }
            }
        } else {
            if (dept.getMajor() != null && !dept.getMajor().isEmpty()) {
                List<com.lz.entity.Department> majors = departmentMapper.selectList(
                        new LambdaQueryWrapper<com.lz.entity.Department>()
                                .eq(com.lz.entity.Department::getCollege, dept.getCollege())
                                .eq(com.lz.entity.Department::getMajor, dept.getMajor())
                                .and(w -> w.isNull(com.lz.entity.Department::getClassName)
                                        .or(w2 -> w2.eq(com.lz.entity.Department::getClassName, ""))));
                for (com.lz.entity.Department d : majors) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) ancestors.add(d.getId());
                }
            }
            if (dept.getCollege() != null) {
                List<com.lz.entity.Department> colleges = departmentMapper.selectList(
                        new LambdaQueryWrapper<com.lz.entity.Department>()
                                .eq(com.lz.entity.Department::getCollege, dept.getCollege())
                                .and(w -> w.isNull(com.lz.entity.Department::getMajor)
                                        .or(w2 -> w2.eq(com.lz.entity.Department::getMajor, "")))
                                .and(w -> w.isNull(com.lz.entity.Department::getClassName)
                                        .or(w2 -> w2.eq(com.lz.entity.Department::getClassName, ""))));
                for (com.lz.entity.Department d : colleges) {
                    if (d.getId() != null && !d.getId().equals(dept.getId())) ancestors.add(d.getId());
                }
            }
        }
        return ancestors;
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
