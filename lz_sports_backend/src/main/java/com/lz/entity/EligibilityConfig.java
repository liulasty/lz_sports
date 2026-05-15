package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资格规则配置 —— 泛化归属（owner_type + owner_id）。
 * 目前 owner_type='EVENT_ITEM' 挂在项目级，未来可扩展赛事级。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eligibility_config")
public class EligibilityConfig extends BaseEntity<Long> {
    @TableField("owner_type")
    private String ownerType;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("group_combination")
    private String groupCombination;

    @TableField("enabled")
    private Boolean enabled;
}
