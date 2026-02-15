package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Athlete Update DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AthleteUpdateDTO implements Serializable {
    private Long athleteId;
    private Long userId;
    private String name;
    private Integer age;
    private String gender;
    private String contact;
    private String grade;
}
