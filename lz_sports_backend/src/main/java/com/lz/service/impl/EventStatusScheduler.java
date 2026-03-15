package com.lz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.common.enums.EventStatus;
import com.lz.entity.Event;
import com.lz.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventMapper eventMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void refreshEventStatus() {
        try {
            Date now = new Date();
            List<Event> events = eventMapper.selectList(new LambdaQueryWrapper<>());
            for (Event event : events) {
                EventStatus target = nextStatus(event, now);
                if (target != null && target != event.getEventStatus()) {
                    event.setEventStatus(target);
                    eventMapper.updateById(event);
                }
            }
        } catch (Exception e) {
            log.error("EventStatusScheduler执行失败", e);
        }
    }

    private EventStatus nextStatus(Event event, Date now) {
        if (event.getEventStatus() == EventStatus.OPEN
                && event.getRegistrationEndTime() != null
                && now.after(event.getRegistrationEndTime())) {
            return EventStatus.CLOSED;
        }
        if (event.getEventStatus() == EventStatus.CLOSED
                && event.getEventStartTime() != null
                && now.after(event.getEventStartTime())) {
            return EventStatus.ONGOING;
        }
        if (event.getEventStatus() == EventStatus.ONGOING
                && event.getEventEndTime() != null
                && now.after(event.getEventEndTime())) {
            return EventStatus.FINISHED;
        }
        return null;
    }
}
