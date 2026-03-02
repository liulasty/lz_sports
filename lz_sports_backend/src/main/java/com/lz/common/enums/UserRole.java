package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("工作人员"),
    ATHLETE("运动员"),
    STUDENT("学生"),
    USER("普通用户");

    @EnumValue
    @JsonValue
    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}
