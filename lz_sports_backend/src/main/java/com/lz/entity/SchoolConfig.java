package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("school_config")
public class SchoolConfig implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String schoolName;
    private String logoUrl;
    private String themeColor;
    private String contactEmail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
