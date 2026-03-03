package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 报名实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("registration")
public class Registration implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long registrationId;

    @TableField("user_id")
    private Long athleteId;

    @TableField("event_id")
    private Long eventId;

    @TableField("item_id")
    private Long itemId;

    @TableField("create_time")
    private Date registrationTime;

    @TableField("status")
    private RegistrationStatus registrationStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("school_id")
    private Long schoolId;
}
