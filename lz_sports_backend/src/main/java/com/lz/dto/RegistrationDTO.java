package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 报名列表 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO implements Serializable {
    private Long id;
    private Long registrationId; // Alias for id in export
    private Long athleteId;
    private String athleteName;
    private String gender;
    private String grade;
    private String contact;
    private Long eventId;
    private String eventName;
    private Long itemId;
    private String itemName;
    private Date registrationTime;
    private String registrationStatus;
}
