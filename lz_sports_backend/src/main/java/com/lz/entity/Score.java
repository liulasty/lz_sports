package com.lz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long registrationId;
    private Long eventId;
    private Long itemId;
    private Long athleteId;
    private String scoreValue;
    private Integer scoreRank;
    private Boolean isPublished;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
