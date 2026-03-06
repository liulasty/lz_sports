package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GenderLimit {
    ALL("ALL"),
    MALE("MALE"),
    FEMALE("FEMALE");

    @EnumValue
    @JsonValue
    private final String limit;

    GenderLimit(String limit) {
        this.limit = limit;
    }
}
