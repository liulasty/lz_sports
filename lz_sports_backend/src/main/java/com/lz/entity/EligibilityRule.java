package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资格规则条目实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eligibility_rule")
public class EligibilityRule extends BaseEntity<Long> {
    @TableField("group_id")
    private Long groupId;

    @TableField("dimension")
    private String dimension;

    @TableField("operator")
    private String operator;

    @TableField("value_json")
    private String valueJson;

    @TableField("sort_order")
    private Integer sortOrder;
}
