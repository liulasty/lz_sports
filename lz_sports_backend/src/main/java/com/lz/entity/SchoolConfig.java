package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学校配置实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("school_config")
public class SchoolConfig extends SchoolRelatedEntity<Long> {

    /**
     * 学校名称
     */
    @TableField("school_name")
    private String schoolName;

    /**
     * logo地址
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 主题色
     */
    @TableField("theme_color")
    private String themeColor;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 是否初始化
     */
    @TableField("is_initialized")
    private boolean isInitialized;

    @TableField("org_mode")
    private String orgMode;
}