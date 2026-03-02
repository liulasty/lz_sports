package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ENDED("已结束");

    @EnumValue
    @JsonValue
    private final String status;

    EventStatus(String status) {
        this.status = status;
    }
}
