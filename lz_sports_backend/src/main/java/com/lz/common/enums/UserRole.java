package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {
    SUPER_ADMIN("SUPER_ADMIN"),
    SCHOOL_ADMIN("SCHOOL_ADMIN"),
    EVENT_ADMIN("EVENT_ADMIN"),
    ATHLETE("ATHLETE"),
    USER("USER");

    @EnumValue
    @JsonValue
    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}
