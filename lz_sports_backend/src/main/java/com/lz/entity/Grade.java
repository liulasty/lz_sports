package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 年级实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("grade")
public class Grade extends SchoolRelatedEntity<Long> {

    /**
     * 年级名称
     */
    private String name;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}