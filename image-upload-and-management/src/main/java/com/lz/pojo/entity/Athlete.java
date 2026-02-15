package com.lz.pojo.entity;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/12:02
 * @Description:
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("athlete")
public class Athlete implements Serializable {

    @TableId(value = "AthleteID", type = IdType.AUTO)
    private Long athleteId;

    @TableField(value = "UserID")
    private Long userId;
    @TableField(value = "Name")
    private String name;
    @TableField(value = "Age")
    private String age;
    @TableField(value = "Gender")
    private String gender;
    @TableField(value = "Contact")
    private String contact;
    @TableField(value = "AthleteState")
    private String AthleteState;
    @TableField(value = "applyTime")
    private Date applyTime;
    @TableField(value = "agreeTime")
    private Date agreeTime;
    @TableField(value = "grade")
    private String grade;
}