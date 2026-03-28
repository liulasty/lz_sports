package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.AthleteStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 运动员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("athlete")
public class Athlete extends BaseEntity<Long> {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 赛事ID
     */
    @TableField("event_id")
    private Long eventId;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 年龄
     */
    @TableField("age")
    private String age;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 联系方式
     */
    @TableField("contact")
    private String contact;

    /**
     * 运动员状态
     */
    @TableField("athlete_state")
    private AthleteStatus athleteState;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 审核通过时间
     */
    @TableField("agree_time")
    private LocalDateTime agreeTime;

    /**
     * 年级
     */
    @TableField("grade")
    private String grade;

    /**
     * 赛事名称 (非表字段)
     */
    @TableField(exist = false)
    private String eventName;
}