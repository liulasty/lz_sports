package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE"),
    DISABLED("DISABLED"),
    PENDING("PENDING"),
    REJECTED("REJECTED");

    @EnumValue
    @JsonValue
    private final String status;

    UserStatus(String status) {
        this.status = status;
    }
}
