package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成绩变更审计日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score_audit_log")
public class ScoreAuditLog extends BaseEntity<Long> {

    @TableField("score_id")
    private Long scoreId;

    @TableField("registration_id")
    private Long registrationId;

    @TableField("event_id")
    private Long eventId;

    @TableField("item_id")
    private Long itemId;

    @TableField("athlete_id")
    private Long athleteId;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("before_score_value")
    private String beforeScoreValue;

    @TableField("before_score_rank")
    private Integer beforeScoreRank;

    @TableField("before_remark")
    private String beforeRemark;

    @TableField("after_score_value")
    private String afterScoreValue;

    @TableField("after_score_rank")
    private Integer afterScoreRank;

    @TableField("after_remark")
    private String afterRemark;
}
