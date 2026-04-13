package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.EventStatus;
import com.lz.common.enums.NotificationType;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.*;
import com.lz.mapper.*;
import com.lz.service.EventService;
import com.lz.service.NotificationService;
import com.lz.service.SportsImgService;
import com.lz.vo.EventVO;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;
import com.lz.util.ImageUtils;
import com.lz.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Event Service Implementation
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventMapper eventMapper;
    private final SportsImgService sportsImgService;
    private final EventAdminMappingMapper eventAdminMappingMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final RegistrationMapper registrationMapper;
    private final ImageUtils imageUtils;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addEvent(EventDTO eventDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BusinessException("未登录");
        }
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Event::getEventName, eventDTO.getName());
        if (eventMapper.selectCount(lqw) > 0) {
            throw new BusinessException("名字重复");
        }
        Date regStart = stringToDate(eventDTO.getRegistrationStartTime());
        Date regEnd = stringToDate(eventDTO.getRegistrationEndTime());
        Date eventStart = stringToDate(eventDTO.getEventStartTime());
        Date eventEnd = stringToDate(eventDTO.getEventEndTime());
        if (regStart == null || regEnd == null || eventStart == null || eventEnd == null) {
            throw new BusinessException("赛事时间参数不完整");
        }
        if (!(regStart.before(regEnd) && regEnd.before(eventStart) && eventStart.before(eventEnd))) {
            throw new BusinessException("时间必须满足 regStart < regEnd < eventStart < eventEnd");
        }
        if (eventDTO.getMaxItemsPerAthlete() == null
                || eventDTO.getMaxItemsPerAthlete() < 1
                || eventDTO.getMaxItemsPerAthlete() > 20) {
            throw new BusinessException("maxItemsPerAthlete 必须在1-20之间");
        }

        String coverImage = imageUtils.getRandomFallbackUrl();
        if (eventDTO.getAddImage() != null && eventDTO.getAddImage().length > 0) {
            coverImage = eventDTO.getAddImage()[0];
        }

        Event event = Event.builder()
                .eventName(eventDTO.getName())
                .eventDescription(eventDTO.getType())
                .registrationStartTime(regStart)
                .registrationEndTime(regEnd)
                .eventStartTime(eventStart)
                .eventEndTime(eventEnd)
                .maxItemsPerAthlete(eventDTO.getMaxItemsPerAthlete())
                .eventStatus(EventStatus.DRAFT)
                .imageUrls(coverImage)
                .build();
        save(event);
        bindEventAdmins(event.getId(), currentUserId, eventDTO.getAdminIds());

        if (eventDTO.getAddImage() != null && eventDTO.getAddImage().length > 0) {
            for (String url : eventDTO.getAddImage()) {
                SportsImg sportsImg = new SportsImg();
                sportsImg.setImgType("event");
                sportsImg.setTypeId(event.getId());
                sportsImg.setImgSrc(url);
                sportsImgService.addSrc(sportsImg);
            }
        } else {
            SportsImg sportsImg = new SportsImg();
            sportsImg.setImgType("event");
            sportsImg.setTypeId(event.getId());
            sportsImg.setImgSrc(coverImage);
            sportsImgService.addSrc(sportsImg);
        }

        // 插入包含的比赛项目
        if (eventDTO.getProjects() != null && !eventDTO.getProjects().isEmpty()) {
            for (com.lz.dto.ProjectDTO pDto : eventDTO.getProjects()) {
                Date pStart = stringToDate(pDto.getStartTime());
                Date pEnd = stringToDate(pDto.getEndTime());
                
                if (pStart == null || pEnd == null) {
                    throw new BusinessException("项目时间不能为空");
                }
                if (!pStart.before(pEnd)) {
                    throw new BusinessException("项目开始时间必须早于结束时间");
                }
                if (pStart.before(eventStart)) {
                    throw new BusinessException("项目开始时间需在赛事时间范围内");
                }
                if (pEnd.after(eventEnd)) {
                    throw new BusinessException("项目结束时间需在赛事时间范围内");
                }

                Project project = new Project();
                project.setEventId(event.getId());
                project.setItemName(pDto.getName());
                try {
                    if (pDto.getLimitDeptIds() != null && !pDto.getLimitDeptIds().isEmpty()) {
                        project.setLimitDeptIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(pDto.getLimitDeptIds()));
                    } else {
                        project.setLimitDeptIds(null);
                    }
                } catch (Exception e) {
                    project.setLimitDeptIds(null);
                }
                project.setMaxAttendance(pDto.getMaxAttendance() == null ? 0 : pDto.getMaxAttendance());
                project.setAttendance(0);
                project.setStartTime(pStart);
                project.setEndTime(pEnd);
                
                if (pDto.getCategory() == null || pDto.getCategory().isEmpty()) {
                    project.setCategory(com.lz.common.enums.ProjectCategory.CUSTOM);
                } else {
                    project.setCategory(com.lz.common.enums.ProjectCategory.valueOf(pDto.getCategory()));
                }
                
                if (pDto.getLimitation() == null || pDto.getLimitation().isEmpty()) {
                    project.setLimitation(com.lz.common.enums.GenderLimit.ALL);
                } else {
                    project.setLimitation(com.lz.common.enums.GenderLimit.valueOf(pDto.getLimitation()));
                }
                
                project.initTime();
                projectMapper.insert(project);
            }
        }

        return String.valueOf(event.getId());
    }

    @Override
    public void changeStatus(Long eventId, String status) {
        Event event = getById(eventId);
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        
        EventStatus targetStatus;
        try {
            targetStatus = EventStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的状态: " + status);
        }
        if (targetStatus == EventStatus.OPEN) {
            if (event.getEventStatus() != EventStatus.DRAFT) {
                throw new BusinessException("仅草稿赛事可发布");
            }
            long itemCount = projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getEventId, eventId));
            if (itemCount == 0) {
                throw new BusinessException("发布失败：至少需要一个赛事项目");
            }
            long adminCount = eventAdminMappingMapper.selectCount(new LambdaQueryWrapper<EventAdminMapping>().eq(EventAdminMapping::getEventId, eventId));
            if (adminCount == 0) {
                throw new BusinessException("发布失败：至少需要一个赛事管理员");
            }
            event.setEventStatus(EventStatus.OPEN);
            updateById(event);
            publishEventNotification(event);
            return;
        }
        if (targetStatus == EventStatus.DRAFT) {
            if (event.getEventStatus() != EventStatus.OPEN) {
                throw new BusinessException("仅OPEN状态支持撤回");
            }
            if (event.getRegistrationStartTime() != null && new Date().after(event.getRegistrationStartTime())) {
                throw new BusinessException("仅报名开始前可撤回");
            }
            event.setEventStatus(EventStatus.DRAFT);
            updateById(event);
            return;
        }
        event.setEventStatus(targetStatus);
        updateById(event);
    }

    @Override
    public PageResult list(EventListDTO dto) {
        IPage<Event> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        
        lqw.like(StringUtils.isNotBlank(dto.getName()), Event::getEventName, dto.getName());
        // lqw.eq(StringUtils.isNotBlank(dto.getType()), Event::getDescription, dto.getType());

        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                if (user.getUserType() == UserRole.SUPER_ADMIN || user.getUserType() == UserRole.SCHOOL_ADMIN) {
                } else if (user.getUserType() == UserRole.EVENT_ADMIN) {
                    List<Long> eventIds = eventAdminMappingMapper.selectList(new LambdaQueryWrapper<EventAdminMapping>()
                            .eq(EventAdminMapping::getUserId, userId))
                            .stream().map(EventAdminMapping::getEventId).collect(Collectors.toList());
                    if (eventIds.isEmpty()) {
                        return new PageResult(0, List.of());
                    }
                    lqw.in(Event::getId, eventIds);
                } else {
                    lqw.ne(Event::getEventStatus, EventStatus.DRAFT);
                }
            }
        } else {
            lqw.ne(Event::getEventStatus, EventStatus.DRAFT);
        }
        eventMapper.selectPage(page, lqw);

        List<EventVO> eventVOS = page.getRecords().stream().map(event -> {
            List<String> imageUrls = sportsImgService.selectImgs(event.getId(), "event");
            EventStatus eventStatus = event.getEventStatus() != null ? event.getEventStatus() : EventStatus.DRAFT;
            return EventVO.builder()
                    .id(event.getId())
                    .name(event.getEventName())
                    .fee("0") // .fee(String.valueOf(event.getRegistrationFee()))
                    .type(event.getEventDescription())
                    .date(event.getRegistrationStartTime() != null ? event.getRegistrationStartTime().toString() : "")
                    .end(event.getRegistrationEndTime() != null ? event.getRegistrationEndTime().toString() : "")
                    .status(eventStatus.name())
                    .imageUrls(imageUtils.get3ImageUrlsOrFallback(imageUrls))
                    .regStartTime(event.getRegistrationStartTime() != null ? event.getRegistrationStartTime().toString() : "")
                    .regEndTime(event.getRegistrationEndTime() != null ? event.getRegistrationEndTime().toString() : "")
                    .eventStartTime(event.getEventStartTime() != null ? event.getEventStartTime().toString() : "")
                    .eventEndTime(event.getEventEndTime() != null ? event.getEventEndTime().toString() : "")
                    .maxItemsPerAthlete(event.getMaxItemsPerAthlete())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), eventVOS);
    }

    @Override
    public List<TableData> getNewTen() {
        IPage<Event> page = new Page<>(1, 10);
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(Event::getRegistrationStartTime);
        eventMapper.selectPage(page, lqw);

        return page.getRecords().stream().map(event -> {
            TableData data = new TableData();
            data.setDate(event.getRegistrationStartTime());
            data.setName(event.getEventName());
            data.setType(event.getEventDescription());
            data.setFee(0); // event.getRegistrationFee() removed
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public Event getEventId(Long eventId) {
        Event event = getById(eventId);
        if (event == null) {
            return null;
        }
        if (event.getEventStatus() == EventStatus.DRAFT) {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                throw new BusinessException("资源不存在", 404);
            }
            User user = userMapper.selectById(userId);
            if (user == null || (user.getUserType() != UserRole.SUPER_ADMIN && user.getUserType() != UserRole.SCHOOL_ADMIN)) {
                long mappingCount = eventAdminMappingMapper.selectCount(new LambdaQueryWrapper<EventAdminMapping>()
                        .eq(EventAdminMapping::getEventId, eventId)
                        .eq(EventAdminMapping::getUserId, userId));
                if (mappingCount == 0) {
                    throw new BusinessException("资源不存在", 404);
                }
            }
        }
        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteEvent(String eventId) {
        long id = Long.parseLong(eventId);
        checkEventPermission(id);
        Event event = getById(id);
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        if (event.getEventStatus() != EventStatus.DRAFT) {
            throw new BusinessException("仅DRAFT状态赛事允许删除");
        }

        LambdaQueryWrapper<Project> projectLqw = new LambdaQueryWrapper<>();
        projectLqw.eq(Project::getEventId, id);
        List<Project> projects = projectMapper.selectList(projectLqw);

        if (!projects.isEmpty()) {
            List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
            LambdaQueryWrapper<Registration> regLqw = new LambdaQueryWrapper<>();
            regLqw.in(Registration::getItemId, projectIds);
            Long count = registrationMapper.selectCount(regLqw);
            if (count > 0) {
                throw new BusinessException("该赛事已有报名记录，无法删除");
            }
            projectMapper.deleteBatchIds(projectIds);
        }

        // Delete Admin Mappings
        LambdaQueryWrapper<EventAdminMapping> mappingLqw = new LambdaQueryWrapper<>();
        mappingLqw.eq(EventAdminMapping::getEventId, id);
        eventAdminMappingMapper.delete(mappingLqw);

        // Delete Images
        LambdaQueryWrapper<SportsImg> imgLqw = new LambdaQueryWrapper<>();
        imgLqw.eq(SportsImg::getTypeId, id);
        imgLqw.eq(SportsImg::getImgType, "event");
        sportsImgService.remove(imgLqw);

        removeById(id);
        return "删除成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String eventId, EventDTO eventDTO) {
        long id = Long.parseLong(eventId);
        checkEventPermission(id);
        Event event = getById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }
        if (eventDTO.getName() != null) event.setEventName(eventDTO.getName());
        if (eventDTO.getType() != null) event.setEventDescription(eventDTO.getType());
        if (eventDTO.getMaxItemsPerAthlete() != null) {
            if (eventDTO.getMaxItemsPerAthlete() < 1 || eventDTO.getMaxItemsPerAthlete() > 20) {
                throw new BusinessException("maxItemsPerAthlete 必须在1-20之间");
            }
            event.setMaxItemsPerAthlete(eventDTO.getMaxItemsPerAthlete());
        }
        Date regStart = stringToDateNullable(eventDTO.getRegistrationStartTime());
        Date regEnd = stringToDateNullable(eventDTO.getRegistrationEndTime());
        Date eventStart = stringToDateNullable(eventDTO.getEventStartTime());
        Date eventEnd = stringToDateNullable(eventDTO.getEventEndTime());
        if (regStart != null) event.setRegistrationStartTime(regStart);
        if (regEnd != null) event.setRegistrationEndTime(regEnd);
        if (eventStart != null) event.setEventStartTime(eventStart);
        if (eventEnd != null) event.setEventEndTime(eventEnd);
        if (event.getRegistrationStartTime() != null
                && event.getRegistrationEndTime() != null
                && event.getEventStartTime() != null
                && event.getEventEndTime() != null) {
            if (!(event.getRegistrationStartTime().before(event.getRegistrationEndTime())
                    && event.getRegistrationEndTime().before(event.getEventStartTime())
                    && event.getEventStartTime().before(event.getEventEndTime()))) {
                throw new BusinessException("时间必须满足 regStart < regEnd < eventStart < eventEnd");
            }
        }
        updateById(event);
        if (eventDTO.getAdminIds() != null) {
            eventAdminMappingMapper.delete(new LambdaQueryWrapper<EventAdminMapping>()
                    .eq(EventAdminMapping::getEventId, id));
            bindEventAdmins(id, BaseContext.getCurrentId(), eventDTO.getAdminIds());
        }

        if (eventDTO.getAddImage() != null) {
            for (String url : eventDTO.getAddImage()) {
                SportsImg img = new SportsImg();
                img.setImgType("event");
                img.setTypeId(id);
                img.setImgSrc(url);
                sportsImgService.addSrc(img);
            }
        }
        if (eventDTO.getDeleteImage() != null) {
            for (String url : eventDTO.getDeleteImage()) {
                LambdaQueryWrapper<SportsImg> imgLqw = new LambdaQueryWrapper<>();
                imgLqw.eq(SportsImg::getImgSrc, url);
                imgLqw.eq(SportsImg::getTypeId, id);
                imgLqw.eq(SportsImg::getImgType, "event");
                sportsImgService.remove(imgLqw);
            }
        }
    }
    
    private void checkEventPermission(Long eventId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // School Admin has full access
        if (user.getUserType() == UserRole.SCHOOL_ADMIN || user.getUserType() == UserRole.SUPER_ADMIN) {
            return;
        }

        // Event Admin must check mapping
        if (user.getUserType() == UserRole.EVENT_ADMIN) {
            LambdaQueryWrapper<EventAdminMapping> lqw = new LambdaQueryWrapper<>();
            lqw.eq(EventAdminMapping::getEventId, eventId);
            lqw.eq(EventAdminMapping::getUserId, userId);
            if (eventAdminMappingMapper.selectCount(lqw) > 0) {
                return;
            }
        }

        throw new BusinessException("无权操作此赛事");
    }

    @Override
    public List<Map<Long, String>> getEventType() {
        return eventMapper.selectEventName();
    }

    @Override
    public TypeData getDataByDate(String date) {
        return eventMapper.selectNumsByDate(date);
    }

    @Override
    public int getTotal() {
        return eventMapper.getEventTotal();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEventAdmins(Long eventId, List<Long> userIds) {
        Event event = getById(eventId);
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("请选择管理员");
        }

        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在: " + userId);
            }
            if (user.getUserType() != UserRole.SUPER_ADMIN && user.getUserType() != UserRole.EVENT_ADMIN && user.getUserType() != UserRole.SCHOOL_ADMIN) {
                throw new BusinessException("用户 " + user.getUsername() + " 角色不符合要求");
            }
            long count = eventAdminMappingMapper.selectCount(new LambdaQueryWrapper<EventAdminMapping>()
                    .eq(EventAdminMapping::getEventId, eventId)
                    .eq(EventAdminMapping::getUserId, userId));
            if (count == 0) {
                EventAdminMapping mapping = new EventAdminMapping();
                mapping.setEventId(eventId);
                mapping.setUserId(userId);
                mapping.initTime();
                eventAdminMappingMapper.insert(mapping);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeEventAdmin(Long eventId, Long userId) {
        long count = eventAdminMappingMapper.selectCount(new LambdaQueryWrapper<EventAdminMapping>()
                .eq(EventAdminMapping::getEventId, eventId));
        if (count <= 1) {
            throw new BusinessException("至少保留一名赛事管理员", 400);
        }
        eventAdminMappingMapper.delete(new LambdaQueryWrapper<EventAdminMapping>()
                .eq(EventAdminMapping::getEventId, eventId)
                .eq(EventAdminMapping::getUserId, userId));
    }

    @Override
    public List<User> getEventAdmins(Long eventId) {
        List<EventAdminMapping> mappings = eventAdminMappingMapper.selectList(new LambdaQueryWrapper<EventAdminMapping>()
                .eq(EventAdminMapping::getEventId, eventId));
        if (mappings.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<Long> userIds = mappings.stream().map(EventAdminMapping::getUserId).collect(Collectors.toList());
        return userMapper.selectBatchIds(userIds);
    }

    private Date stringToDate(String s) {
        if (s == null || s.isEmpty()) return null;
        Date d = parseWithPattern(s, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if (d != null) return d;
        d = parseWithPattern(s, "yyyy-MM-dd HH:mm:ss");
        if (d != null) return d;
        d = parseWithPattern(s, "yyyy-MM-dd'T'HH:mm:ss");
        if (d != null) return d;
        throw new BusinessException("日期格式错误");
    }

    private Date stringToDateNullable(String s) {
        if (s == null || s.isEmpty()) return null;
        return stringToDate(s);
    }

    private Date parseWithPattern(String value, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private void publishEventNotification(Event event) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, com.lz.common.enums.UserStatus.ACTIVE));
        for (User user : users) {
            notificationService.create(
                    user.getId(),
                    "新赛事已发布",
                    "赛事《" + event.getEventName() + "》已开放报名",
                    NotificationType.EVENT_PUBLISHED
            );
        }
    }

    private void bindEventAdmins(Long eventId, Long currentUserId, List<Long> adminIds) {
        User operator = userMapper.selectById(currentUserId);
        if (operator == null) {
            throw new BusinessException("当前操作用户不存在", 401);
        }
        boolean operatorCanManageAdmins = operator.getUserType() == UserRole.SCHOOL_ADMIN
                || operator.getUserType() == UserRole.SUPER_ADMIN;
        if (!operatorCanManageAdmins && adminIds != null && !adminIds.isEmpty()) {
            throw new BusinessException("仅学校管理员可分配赛事管理员", 403);
        }

        Set<Long> adminUserIds = new HashSet<>();
        adminUserIds.add(currentUserId);
        if (adminIds != null) {
            adminUserIds.addAll(adminIds);
        }

        for (Long adminUserId : adminUserIds) {
            if (adminUserId == null) {
                continue;
            }
            User adminUser = userMapper.selectById(adminUserId);
            if (adminUser == null) {
                throw new BusinessException("赛事管理员不存在: " + adminUserId);
            }
            if (adminUser.getUserType() != UserRole.SCHOOL_ADMIN
                    && adminUser.getUserType() != UserRole.SUPER_ADMIN
                    && adminUser.getUserType() != UserRole.EVENT_ADMIN) {
                if (!operatorCanManageAdmins) {
                    throw new BusinessException("仅学校管理员可授予赛事管理员角色", 403);
                }
                User updateRoleUser = new User();
                updateRoleUser.setId(adminUserId);
                updateRoleUser.setUserType(UserRole.EVENT_ADMIN);
                updateRoleUser.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(updateRoleUser);
            }

            long mappingCount = eventAdminMappingMapper.selectCount(new LambdaQueryWrapper<EventAdminMapping>()
                    .eq(EventAdminMapping::getEventId, eventId)
                    .eq(EventAdminMapping::getUserId, adminUserId));
            if (mappingCount > 0) {
                continue;
            }

            EventAdminMapping mapping = new EventAdminMapping();
            mapping.setEventId(eventId);
            mapping.setUserId(adminUserId);
            mapping.initTime();
            eventAdminMappingMapper.insert(mapping);
        }
    }
}
