package com.lz.entity;

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
 * 报名实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Registration")
public class Registration implements Serializable {

    @TableId(value = "RegistrationID", type = IdType.AUTO)
    private Long registrationId;

    @TableField("AthleteID")
    private Long athleteId;

    @TableField("EventID")
    private Long eventId;

    @TableField("ItemID")
    private Long itemId;

    @TableField("RegistrationTime")
    private Date registrationTime;

    @TableField("RegistrationStatus")
    private String registrationStatus;
}
