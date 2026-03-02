package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("已激活"),
    DISABLED("未启用"),
    PENDING("待审核"),
    REJECTED("已拒绝");

    @EnumValue
    @JsonValue
    private final String status;

    UserStatus(String status) {
        this.status = status;
    }
}
