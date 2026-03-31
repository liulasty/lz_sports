package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends SchoolRelatedEntity<Long> {

    @TableField("college")
    private String college;

    @TableField("major")
    private String major;

    @TableField("grade")
    private String grade;

    @TableField("class_name")
    private String className;

    @TableField("dept_name")
    private String deptName;

    @TableField("org_mode")
    private String orgMode;

    @TableField("sort_order")
    private Integer sortOrder;
}