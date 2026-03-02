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

    @TableId(value = "eventId", type = IdType.AUTO)
    private Long eventId;

    @TableField("EventName")
    private String eventName;

    @TableField("RegistrationStart")
    private Date registrationStart;

    @TableField("RegistrationEnd")
    private Date registrationDeadline;

    @TableField("RegistrationFee")
    private Integer registrationFee;

    @TableField("Eligibility")
    private String eligibility;

    @TableField("status")
    private EventStatus status;

    @TableField("school_id")
    private Long schoolId;
}
