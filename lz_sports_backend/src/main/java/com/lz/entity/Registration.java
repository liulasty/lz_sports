package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lz.common.enums.RegistrationStatus;
import lombok.*;

import java.util.Date;

/**
 * 报名实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("registration")
public class Registration extends SchoolRelatedEntity<Long> {

    /**
     * 运动员ID（关联用户ID）
     */
    @TableField("user_id")
    private Long athleteId;

    /**
     * 赛事ID
     */
    @TableField("event_id")
    private Long eventId;

    /**
     * 项目ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 报名时间
     */
    @TableField("registration_time")
    private Date registrationTime;

    /**
     * 报名状态
     */
    @TableField("status")
    private RegistrationStatus registrationStatus;

    /**
     * 驳回原因
     */
    @TableField("reject_reason")
    private String rejectReason;
}