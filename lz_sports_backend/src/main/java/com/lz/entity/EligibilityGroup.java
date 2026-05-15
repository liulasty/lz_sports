package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资格规则组实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eligibility_group")
public class EligibilityGroup extends BaseEntity<Long> {
    @TableField("config_id")
    private Long configId;

    @TableField("group_logic")
    private String groupLogic;

    @TableField("group_desc")
    private String groupDesc;

    @TableField("sort_order")
    private Integer sortOrder;
}
