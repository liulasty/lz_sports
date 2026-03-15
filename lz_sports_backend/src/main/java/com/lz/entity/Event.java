package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.EventStatus;
import lombok.*;

import java.util.Date;

/**
 * 赛事实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event")
public class Event extends SchoolRelatedEntity<Long> {

    /**
     * 赛事名称
     */
    @TableField("name")
    private String eventName;

    /**
     * 赛事描述
     */
    @TableField("description")
    private String eventDescription;

    /**
     * 报名开始时间
     */
    @TableField("reg_start_time")
    private Date registrationStartTime;

    /**
     * 报名截止时间
     */
    @TableField("reg_deadline")
    private Date registrationEndTime;

    /**
     * 赛事开始时间
     */
    @TableField("start_time")
    private Date eventStartTime;

    /**
     * 赛事结束时间
     */
    @TableField("end_time")
    private Date eventEndTime;

    /**
     * 赛事状态
     */
    @TableField("status")
    private EventStatus eventStatus;

    /**
     * 图片地址
     */
    @TableField("img_url")
    private String imageUrls;
}