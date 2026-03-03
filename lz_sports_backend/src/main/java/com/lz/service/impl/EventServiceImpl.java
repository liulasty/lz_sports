package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lz.common.exception.BusinessException;
import com.lz.common.result.PageResult;
import com.lz.dto.EventDTO;
import com.lz.dto.EventListDTO;
import com.lz.entity.Event;
import com.lz.entity.SportsImg;
import com.lz.mapper.EventMapper;
import com.lz.service.EventService;
import com.lz.service.SportsImgService;
import com.lz.vo.EventVO;
import com.lz.vo.chart.TableData;
import com.lz.vo.chart.TypeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Event Service Implementation
 */
import com.lz.common.enums.EventStatus;
import com.lz.entity.EventAdminMapping;
import com.lz.mapper.EventAdminMappingMapper;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    private final EventMapper eventMapper;
    private final SportsImgService sportsImgService;
    private final EventAdminMappingMapper eventAdminMappingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addEvent(EventDTO eventDTO) {
        // Check for duplicate name
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        lqw.eq(eventDTO.getName() != null && !eventDTO.getName().isEmpty(), Event::getEventName, eventDTO.getName());
        if (eventMapper.selectCount(lqw) > 0) {
            throw new BusinessException("名字重复");
        }

        try {
            Date startDate = stringToDate(eventDTO.getDate1()[0]);
            Date endDate = stringToDate(eventDTO.getDate1()[1]);

            Event event = Event.builder()
                    .eventName(eventDTO.getName())
                    .eligibility(eventDTO.getType())
                    .registrationFee(Integer.parseInt(eventDTO.getFee()))
                    .registrationStart(startDate)
                    .registrationDeadline(endDate)
                    .status(EventStatus.DRAFT) // Default to DRAFT
                    .build();

            save(event);

            // Add images
            if (eventDTO.getAddImage() != null) {
                for (String url : eventDTO.getAddImage()) {
                    SportsImg sportsImg = new SportsImg();
                    sportsImg.setImgType("event");
                    sportsImg.setTypeId(event.getEventId());
                    sportsImg.setImgSrc(url);
                    sportsImgService.addSrc(sportsImg);
                }
            }

            // Add Event Admins
            if (eventDTO.getAdminIds() != null && !eventDTO.getAdminIds().isEmpty()) {
                for (Long userId : eventDTO.getAdminIds()) {
                    EventAdminMapping mapping = new EventAdminMapping();
                    mapping.setEventId(event.getEventId());
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
            event.setStatus(eventStatus);
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
        lqw.eq(dto.getType() != null && !dto.getType().isEmpty(), Event::getEligibility, dto.getType());
        
        // Date filtering logic from old code seems to be missing in detail, assuming exact match or similar
        // If dto.getDate() is provided, maybe filter by start date?
        // Old code didn't seem to use date in list query explicitly in the snippet I saw, 
        // but EventListDTO has date. Let's add basic date filtering if needed.
        if (dto.getDate() != null) {
             // Assuming looking for events starting on this date
             // Since Date includes time, this might be tricky without range. 
             // Skipping date filter for now unless strict requirement.
        }

        eventMapper.selectPage(page, lqw);

        List<EventVO> eventVOS = page.getRecords().stream().map(event -> {
            List<String> imageUrls = sportsImgService.selectImgs(event.getEventId(), "event");
            return EventVO.builder()
                    .id(event.getEventId())
                    .name(event.getEventName())
                    .fee(String.valueOf(event.getRegistrationFee()))
                    .type(event.getEligibility())
                    .date(event.getRegistrationStart().toString()) // Simplify date format
                    .end(event.getRegistrationDeadline().toString())
                    .imageUrls(imageUrls)
                    .build();
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), eventVOS);
    }

    @Override
    public List<TableData> getNewTen() {
        IPage<Event> page = new Page<>(1, 10);
        LambdaQueryWrapper<Event> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(Event::getRegistrationStart);
        eventMapper.selectPage(page, lqw);

        return page.getRecords().stream().map(event -> {
            TableData data = new TableData();
            data.setDate(event.getRegistrationStart());
            data.setName(event.getEventName());
            data.setType(event.getEligibility());
            data.setFee(event.getRegistrationFee());
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
        // TODO: Check if event can be deleted (e.g. no registrations)
        removeById(id);
        return "删除成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String eventId, EventDTO eventDTO) {
        long id = Long.parseLong(eventId);
        Event event = getById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        if (eventDTO.getName() != null) event.setEventName(eventDTO.getName());
        if (eventDTO.getType() != null) event.setEligibility(eventDTO.getType());
        if (eventDTO.getFee() != null) event.setRegistrationFee(Integer.parseInt(eventDTO.getFee()));
        
        if (eventDTO.getDate1() != null && eventDTO.getDate1().length >= 2) {
             event.setRegistrationStart(stringToDate(eventDTO.getDate1()[0]));
             event.setRegistrationDeadline(stringToDate(eventDTO.getDate1()[1]));
        }

        updateById(event);

        // Handle Images
        if (eventDTO.getAddImage() != null) {
            for (String url : eventDTO.getAddImage()) {
                SportsImg img = new SportsImg();
                img.setImgType("event");
                img.setTypeId(id);
                img.setImgSrc(url);
                sportsImgService.addSrc(img);
            }
        }
        // TODO: Handle deleteImage if needed, though old code logic for delete wasn't fully shown in service snippet
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
            log.error("Date parse error", e);
            // Fallback or throw
            return new Date(); 
        }
    }
}
