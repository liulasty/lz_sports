package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛事状态流转操作日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_status_operation_log")
public class EventStatusOperationLog extends BaseEntity<Long> {

    @TableField("event_id")
    private Long eventId;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operation_type")
    private String operationType;

    @TableField("trigger_source")
    private String triggerSource;

    @TableField("reason")
    private String reason;
}
