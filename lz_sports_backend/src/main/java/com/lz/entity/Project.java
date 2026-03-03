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
@TableName("event_item")
public class Project implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long itemId;

    @TableField("event_id")
    private Long eventId;

    @TableField("name")
    private String itemName;

    @TableField("create_time")
    private Date createTime;

    @TableField("grade_limit")
    private String grade;

    @TableField("gender_limit")
    private String limitation;

    @TableField("max_count")
    private Integer maxAttendance;

    @TableField("current_count")
    private Integer attendance;

    @TableField("school_id")
    private Long schoolId;
}
