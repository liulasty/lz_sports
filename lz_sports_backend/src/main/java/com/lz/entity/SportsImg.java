package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sports Image Entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sportsimg")
public class SportsImg {
    @TableId(value = "ImgId", type = IdType.AUTO)
    private Long imgId;

    @TableField("ImgType")
    private String imgType;

    @TableField("typeId")
    private Long typeId;

    @TableField("ImgSrc")
    private String imgSrc;
}
