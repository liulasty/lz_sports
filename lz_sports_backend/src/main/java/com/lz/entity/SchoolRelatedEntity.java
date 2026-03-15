package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolRelatedEntity<T> extends BaseEntity<T> {
    @TableField("school_id")
    private Long schoolId;
}
