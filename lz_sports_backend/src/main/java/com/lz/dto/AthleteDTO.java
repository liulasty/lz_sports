package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Athlete Creation DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AthleteDTO implements Serializable {
    private Integer age;
    private String gender;
    private String name;
    private String phone;
    private String grade;
    private Long userId;
    private Long eventId;
    private Long id; // Optional, for compatibility
}
