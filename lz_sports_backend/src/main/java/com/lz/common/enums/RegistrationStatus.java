package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RegistrationStatus {
    PENDING("审核中"),
    APPROVED("通过"),
    REJECTED("未通过");

    @EnumValue
    @JsonValue
    private final String status;

    RegistrationStatus(String status) {
        this.status = status;
    }
}
