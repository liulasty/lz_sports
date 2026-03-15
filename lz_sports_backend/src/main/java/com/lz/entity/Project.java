package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.GenderLimit;
import lombok.*;

import java.util.Date;

/**
 * 赛事项目实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_item")
public class Project extends BaseEntity<Long> {

    /**
     * 赛事ID
     */
    @TableField("event_id")
    private Long eventId;

    /**
     * 项目名称
     */
    @TableField("name")
    private String itemName;

    /**
     * 年级限制
     */
    @TableField("grade_limit")
    private String grade;

    /**
     * 性别限制
     */
    @TableField("gender_limit")
    private GenderLimit limitation;

    /**
     * 最大报名人数
     */
    @TableField("max_count")
    private Integer maxAttendance;

    /**
     * 当前报名人数
     */
    @TableField("current_count")
    private Integer attendance;
}