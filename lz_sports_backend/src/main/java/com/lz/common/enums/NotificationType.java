package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum NotificationType {
    ATHLETE_APPROVED("ATHLETE_APPROVED"),
    ATHLETE_REJECTED("ATHLETE_REJECTED"),
    RESULT_PUBLISHED("RESULT_PUBLISHED"),
    EVENT_PUBLISHED("EVENT_PUBLISHED"),
    SYSTEM("SYSTEM");

    @EnumValue
    @JsonValue
    private final String type;

    NotificationType(String type) {
        this.type = type;
    }
}
