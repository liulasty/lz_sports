package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 赛事管理员关联实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_admin_mapping")
public class EventAdminMapping extends BaseEntity<Long> {

    /**
     * 赛事ID
     */
    @TableField("event_id")
    private Long eventId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
}