package com.lz.pojo.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author lz
 */
@Data
@Builder
@TableName("Registration")
public class Registration {
    @TableId(value = "RegistrationID",type = IdType.AUTO)
    private Long id;
    @TableField("AthleteID")
    private Long athleteId;
    @TableField("EventID")
    private Long eventId;
    @TableField("ItemID")
    private Long itemId;
    @TableField("RegistrationTime")
    private Date time;
    @TableField("RegistrationStatus")
    private String status;
}