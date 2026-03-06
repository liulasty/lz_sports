package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RegistrationStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CONFIRMED("CONFIRMED"),
    CANCELLED("CANCELLED");

    @EnumValue
    @JsonValue
    private final String status;

    RegistrationStatus(String status) {
        this.status = status;
    }
}
