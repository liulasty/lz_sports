package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2024/01/05/15:42
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运动员更新 DTO
 *
 * @author lz
 * @date 2024/01/05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AthleteUpdateDTO implements Serializable {
    private Integer athleteId;
    private Integer userId;
    private String name;
    private Integer age;
    private String gender;
    private String contact;
    private String grade;
}