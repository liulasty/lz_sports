package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {
    SCHOOL_ADMIN("学校管理员"),
    EVENT_ADMIN("赛事管理员"),
    ATHLETE("运动员");

    @EnumValue
    @JsonValue
    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}
