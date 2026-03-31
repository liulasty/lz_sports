package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Athlete Creation DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AthleteDTO implements Serializable {
    @NotNull(message = "赛事ID不能为空")
    @Schema(description = "赛事ID", example = "1")
    private Long eventId;
    private Long userId; // Set by Controller from token
}
