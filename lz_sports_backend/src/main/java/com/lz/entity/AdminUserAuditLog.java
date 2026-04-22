package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端用户敏感操作审计日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user_audit_log")
public class AdminUserAuditLog extends BaseEntity<Long> {

    @TableField("operator_id")
    private Long operatorId;

    @TableField("target_user_id")
    private Long targetUserId;

    @TableField("action")
    private String action;

    @TableField("before_role")
    private String beforeRole;

    @TableField("after_role")
    private String afterRole;

    @TableField("before_status")
    private String beforeStatus;

    @TableField("after_status")
    private String afterStatus;

    @TableField("remark")
    private String remark;
}
