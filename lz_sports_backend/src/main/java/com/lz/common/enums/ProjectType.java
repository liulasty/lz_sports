package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProjectType {
    INDIVIDUAL("INDIVIDUAL"),
    TEAM("TEAM");

    @EnumValue
    @JsonValue
    private final String type;

    ProjectType(String type) {
        this.type = type;
    }
}
