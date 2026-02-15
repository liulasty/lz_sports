package com.lz.pojo.dto;

/*
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2024/01/05/0:56
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 注册和运动员 DTO
 *
 * @author lz
 * @date 2024/01/05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationAndAthleteDTO {
    private Long id;
    private String name;
    private String age;
    private String gender;
    private String contact;
    private String athleteGrade;
    
    private Long eventId;
    private String eventName;
    
    private Long itemId;
    private String  itemName;
    private Integer num;
    private Integer maxNum;
    private String limitation;
    private String grade;
    
    private Date applyTime;
}