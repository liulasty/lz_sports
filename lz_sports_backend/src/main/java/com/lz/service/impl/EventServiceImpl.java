package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.context.BaseContext;
import com.lz.common.enums.EventStatus;
import com.lz.common.enums.UserRole;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.*;
import com.lz.mapper.*;
import com.lz.service.EventService;
import com.lz.service.SportsImgService;
import com.lz.vo.EventVO;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;
import com.lz.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addEvent(EventDTO eventDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // Check for duplicate name
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        lqw.eq(eventDTO.getName() != null && !eventDTO.getName().isEmpty(), Event::getEventName, eventDTO.getName());
        if (eventMapper.selectCount(lqw) > 0) {
            throw new BusinessException("名字重复");
        }

        try {
            Date startDate = stringToDate(eventDTO.getDate1()[0]);
            Date endDate = stringToDate(eventDTO.getDate1()[1]);

            // Apply fallback logic for event image if needed (though Event entity stores it in imageUrls, 
            // usually images are stored in SportsImg table separately, but Event has a field too.
            // Let's use the first image from addImage as main image, or fallback if none.)
            
            // Note: In original code, Event entity has imageUrls field but it wasn't being set from eventDTO.getAddImage().
            // And sportsImgService was used to save images.
            // If we want a cover image on the Event entity itself, we should set it.
            // If eventDTO.getAddImage() is empty, we generate a fallback one.
            
            String coverImage = null;
            if (eventDTO.getAddImage() != null && eventDTO.getAddImage().length < 1) {
                coverImage = eventDTO.getAddImage()[0];
            } else {
                coverImage = imageUtils.getRandomFallbackUrl();
                // If we generated a fallback, should we add it to the SportsImg table too? 
                // Usually yes, so it appears in the gallery.
                // But let's first set it on the Event entity if that's what's displayed in lists.
            }

            Event event = Event.builder()
                    .eventName(eventDTO.getName())
                    .eventDescription(eventDTO.getType()) 
                    .registrationStartTime(startDate)
                    .registrationEndTime(endDate)
                    .eventStatus(EventStatus.DRAFT)
                    .imageUrls(coverImage) // Set the cover image (fallback or uploaded)
                    .build();

            save(event);
            
            // Assign creator as admin automatically
            EventAdminMapping selfMapping = new EventAdminMapping();
            selfMapping.setEventId(event.getId());
            selfMapping.setUserId(currentUserId);
            selfMapping.setCreateTime(LocalDateTime.now());
            eventAdminMappingMapper.insert(selfMapping);

            // Add images to SportsImg table
            if (eventDTO.getAddImage() != null && eventDTO.getAddImage().length < 1) {
                for (String url : eventDTO.getAddImage()) {
                    SportsImg sportsImg = new SportsImg();
                    sportsImg.setImgType("event");
                    sportsImg.setTypeId(event.getId());
                    sportsImg.setImgSrc(url);
                    sportsImgService.addSrc(sportsImg);
                }
            } else {
                // If no images uploaded, save the fallback as a SportsImg too
                if (coverImage != null) {
                    SportsImg sportsImg = new SportsImg();
                    sportsImg.setImgType("event");
                    sportsImg.setTypeId(event.getId());
                    sportsImg.setImgSrc(coverImage);
                    sportsImgService.addSrc(sportsImg);
                }
            }

            // Add Event Admins
            if (eventDTO.getAdminIds() != null && !eventDTO.getAdminIds().isEmpty()) {
                for (Long userId : eventDTO.getAdminIds()) {
                    // Skip if already added (self)
                    if (userId.equals(currentUserId)) continue;
                    
                    EventAdminMapping mapping = new EventAdminMapping();
                    mapping.setEventId(event.getId());
                    mapping.setUserId(userId);
                    mapping.setCreateTime(LocalDateTime.now());
                    eventAdminMappingMapper.insert(mapping);
                }
            }

            return "添加成功";
        } catch (NumberFormatException e) {
            throw new BusinessException("费用格式错误");
        } catch (Exception e) {
            log.error("添加事件失败", e);
            throw new BusinessException("添加事件失败: " + e.getMessage());
        }
    }

    @Override
    public void changeStatus(Long eventId, String status) {
        Event event = getById(eventId);
        if (event == null) {
            throw new BusinessException("赛事不存在");
        }
        
        try {
            EventStatus eventStatus = EventStatus.valueOf(status);
            event.setEventStatus(eventStatus);
            updateById(event);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的状态: " + status);
        }
    }

    @Override
    public PageResult list(EventListDTO dto) {
        IPage<Event> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        
        lqw.like(dto.getName() != null && !dto.getName().isEmpty(), Event::getEventName, dto.getName());
        // lqw.eq(dto.getType() != null && !dto.getType().isEmpty(), Event::getDescription, dto.getType());

        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                if (user.getUserType() == UserRole.SUPER_ADMIN) {
                    // School Admin sees all events
                } else if (user.getUserType() == UserRole.EVENT_ADMIN) {
                    // Event Admin sees only assigned events
                    List<Long> eventIds = eventAdminMappingMapper.selectList(new LambdaQueryWrapper<EventAdminMapping>()
                            .eq(EventAdminMapping::getUserId, userId))
                            .stream().map(EventAdminMapping::getEventId).collect(Collectors.toList());
                    
                    if (eventIds.isEmpty()) {
                        return new PageResult(0, List.of());
                    }
                    lqw.in(Event::getId, eventIds);
                } else {
                    // Athletes/Others see only PUBLISHED events
                    // Or maybe check if public endpoint allows seeing DRAFT? Assuming no.
                    // For now, let's assume they see PUBLISHED.
                    // lqw.eq(Event::getStatus, EventStatus.PUBLISHED);
                    // However, current requirement seems to focus on admin backend list.
                    // If this is used by frontend, we should be careful.
                    // Given the context of "fixing bugs", sticking to existing logic + permissions is key.
                    // If no role logic was present, adding it now makes it safer.
                }
            }
        }
        
        eventMapper.selectPage(page, lqw);

        List<EventVO> eventVOS = page.getRecords().stream().map(event -> {
            List<String> imageUrls = sportsImgService.selectImgs(event.getId(), "event");
            return EventVO.builder()
                    .id(event.getId())
                    .name(event.getEventName())
                    .fee("0") // .fee(String.valueOf(event.getRegistrationFee()))
                    .type(event.getEventDescription())
                    .date(event.getRegistrationStartTime() != null ? event.getRegistrationStartTime().toString() : "")
                    .end(event.getRegistrationEndTime() != null ? event.getRegistrationEndTime().toString() : "")
                    .status(event.getEventStatus().name()) // Convert Enum to String for VO if needed, or update VO
                    .imageUrls(imageUrls)
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
        return getById(eventId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteEvent(String eventId) {
        long id = Long.parseLong(eventId);
        
        // Check permission
        checkEventPermission(id);

        // Check for existing projects and registrations
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
            // Delete projects if no registrations
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
        
        // Check permission
        checkEventPermission(id);
        
        Event event = getById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        if (eventDTO.getName() != null) event.setEventName(eventDTO.getName());
        if (eventDTO.getType() != null) event.setEventDescription(eventDTO.getType());
        // if (eventDTO.getFee() != null) event.setRegistrationFee(Integer.parseInt(eventDTO.getFee()));
        
        if (eventDTO.getDate1() != null && eventDTO.getDate1().length >= 2) {
             event.setRegistrationStartTime(stringToDate(eventDTO.getDate1()[0]));
             event.setRegistrationEndTime(stringToDate(eventDTO.getDate1()[1]));
        }

        updateById(event);

        // Handle Add Images
        if (eventDTO.getAddImage() != null) {
            for (String url : eventDTO.getAddImage()) {
                SportsImg img = new SportsImg();
                img.setImgType("event");
                img.setTypeId(id);
                img.setImgSrc(url);
                sportsImgService.addSrc(img);
            }
        }

        // Handle Delete Images
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
        if (user.getUserType() == UserRole.SCHOOL_ADMIN) {
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

    private Date stringToDate(String s) {
        if (s == null) return null;
        // Adjust pattern to match frontend input
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            log.error("Date parse error: {}", s, e);
            throw new BusinessException("日期格式错误");
        }
    }
}
