package com.lz.service.impl;

import com.lz.common.enums.EventStatus;
import com.lz.entity.Event;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * 赛事状态机规则定义
 */
public final class EventStateMachine {

    private static final Set<EventStatus> MANUAL_ALLOWED_TARGETS = EnumSet.of(EventStatus.OPEN, EventStatus.DRAFT);

    private EventStateMachine() {
    }

    public static boolean isManualTargetAllowed(EventStatus target) {
        return MANUAL_ALLOWED_TARGETS.contains(target);
    }

    public static boolean canManualTransition(EventStatus from, EventStatus to) {
        return (from == EventStatus.DRAFT && to == EventStatus.OPEN)
                || (from == EventStatus.OPEN && to == EventStatus.DRAFT);
    }

    public static EventStatus nextAutoStatus(Event event, Date now) {
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
