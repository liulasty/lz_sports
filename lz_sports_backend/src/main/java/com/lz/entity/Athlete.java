package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.AthleteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Athlete Entity
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
    private AthleteStatus athleteState;

    @TableField(value = "applyTime")
    private LocalDateTime applyTime;

    @TableField(value = "agreeTime")
    private LocalDateTime agreeTime;

    @TableField(value = "grade")
    private String grade;

    @TableField(value = "school_id")
    private Long schoolId;
}
