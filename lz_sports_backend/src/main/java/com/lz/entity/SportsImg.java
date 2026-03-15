package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 运动图片实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sportsimg")
public class SportsImg extends BaseEntity<Long> {

    /**
     * 图片类型
     */
    @TableField("img_type")
    private String imgType;

    /**
     * 类型关联ID
     */
    @TableField("type_id")
    private Long typeId;

    /**
     * 图片路径
     */
    @TableField("img_src")
    private String imgSrc;
}