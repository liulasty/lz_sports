package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT("DRAFT"),
    OPEN("OPEN"),
    CLOSED("CLOSED"),
    ONGOING("ONGOING"),
    FINISHED("FINISHED");

    @EnumValue
    @JsonValue
    private final String status;

    EventStatus(String status) {
        this.status = status;
    }
}
