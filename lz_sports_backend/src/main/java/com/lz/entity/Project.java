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
 * Project Entity (Event Item)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("eventitem")
public class Project implements Serializable {

    @TableId(value = "ItemID", type = IdType.AUTO)
    private Long itemId;

    @TableField("EventID")
    private Long eventId;

    @TableField("ItemName")
    private String itemName;

    @TableField("createTime")
    private Date createTime;

    @TableField("grade")
    private String grade;

    @TableField("limitation")
    private String limitation;

    @TableField("start")
    private Date projectStart;

    @TableField("end")
    private Date projectEnd;

    @TableField("maxAttendance")
    private Integer maxAttendance;

    @TableField("attendance")
    private Integer attendance;
}
