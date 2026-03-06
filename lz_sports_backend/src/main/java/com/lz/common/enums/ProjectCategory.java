package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProjectCategory {
    STANDARD("STANDARD"),
    CUSTOM("CUSTOM");

    @EnumValue
    @JsonValue
    private final String category;

    ProjectCategory(String category) {
        this.category = category;
    }
}
