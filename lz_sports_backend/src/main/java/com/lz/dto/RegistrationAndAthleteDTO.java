package com.lz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 报名详情 DTO (包含运动员信息)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationAndAthleteDTO {
    private Long id;
    
    // 运动员信息
    private String name;
    private Integer age; // Backend Project uses Integer for age
    private String gender;
    private String contact;
    private String athleteGrade;
    
    // 赛事信息
    private Long eventId;
    private String eventName;
    
    // 项目信息
    private Long itemId;
    private String itemName;
    private Integer num;
    private Integer maxNum;
    private String limitation;
    private String deptName;
    
    private Date applyTime;
    private String status;
}
