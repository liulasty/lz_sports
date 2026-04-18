package com.lz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("result")
public class Score extends BaseEntity<Long> {

    /**
     * 报名ID
     */
    @TableField("registration_id")
    private Long registrationId;

    /**
     * 赛事ID
     */
    @TableField("event_id")
    private Long eventId;

    /**
     * 项目ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 运动员ID（关联用户ID）
     */
    @TableField("user_id")
    private Long athleteId;

    /**
     * 成绩值
     */
    @TableField("score_value")
    private String scoreValue;

    /**
     * 排名
     */
    @TableField("score_rank")
    private Integer scoreRank;

    /**
     * 是否发布
     */
    @TableField("is_published")
    private Boolean isPublished;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField(exist = false)
    private String remark;
}
