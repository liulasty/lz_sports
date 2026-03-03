package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Event Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("event")
public class Event implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long eventId;

    @TableField("name")
    private String eventName;

    @TableField("reg_start_time")
    private Date registrationStart;

    @TableField("reg_deadline")
    private Date registrationDeadline;

    @TableField("start_time")
    private Date startTime;

    @TableField("end_time")
    private Date endTime;

    @TableField("description")
    private String description;

    @TableField("img_url")
    private String imgUrl;

    @TableField("status")
    private EventStatus status;

    @TableField("school_id")
    private Long schoolId;
}
