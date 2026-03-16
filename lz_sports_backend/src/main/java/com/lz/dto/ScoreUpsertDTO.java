package com.lz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreUpsertDTO {
    @NotNull(message = "报名ID不能为空")
    private Long registrationId;

    @NotBlank(message = "成绩不能为空")
    private String scoreValue;

    @NotNull(message = "排名不能为空")
    private Integer scoreRank;

    private String remark;
}
