package com.lz.pojo.dto;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/16:52
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lz
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
    
    private Integer userId;
    
    private Integer id;

}