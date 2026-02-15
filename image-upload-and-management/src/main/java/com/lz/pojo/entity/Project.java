package com.lz.pojo.entity;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/29/22:37
 * @Description:
 */

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
@TableName("eventitem")
public class Project {
    /**
     * 项目 ID
     */
    @TableId(value = "ItemID", type = IdType.AUTO)
    private Long itemID;
    /**
     * 事件 ID
     */
    @TableField("EventID")
    private Long eventID;
    /**
     * 项目名称
     */
    @TableField("ItemName")
    private String itemName;
    
    @TableField("createTime")
    private Date createTime;

    /**
     * 项目所属年级
     */
    @TableField("grade")
    private String grade;

    /**
     * 项目限制
     */
    @TableField("limitation")
    private String limitation;

    /**
     * 项目开始
     */
    @TableField("start")
    private Date projectStart;

    /**
     * 项目结束
     */
    @TableField("end")
    private Date projectEnd;

    /**
     * 最大参赛人数
     */
    @TableField("maxAttendance")
    private Integer maxAttendance;

    /**
     * 参赛人数
     */
    @TableField("attendance")
    private Integer attendance;
}