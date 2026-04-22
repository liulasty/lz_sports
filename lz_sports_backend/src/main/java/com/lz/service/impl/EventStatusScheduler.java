package com.lz.service.impl;

import com.lz.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventService eventService;

    @Scheduled(cron = "0 * * * * ?")
    public void refreshEventStatus() {
        try {
            eventService.refreshEventStatusesAutomatically();
        } catch (Exception e) {
            log.error("EventStatusScheduler执行失败", e);
        }
    }
}
