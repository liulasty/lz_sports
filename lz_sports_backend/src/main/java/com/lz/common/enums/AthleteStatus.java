package com.lz.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AthleteStatus {
    AUDITING("在审核"),
    SUCCESS("成功"),
    REJECTED("不同意");

    @EnumValue
    @JsonValue
    private final String status;

    AthleteStatus(String status) {
        this.status = status;
    }
}
