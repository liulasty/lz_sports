package com.lz.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDateTime;

@Data
public class ScoreVO implements Serializable {
    private Long id;
    private String eventName;
    private String itemName;
    private String athleteName;
    private String grade;
    private String scoreValue;
    private Integer scoreRank;
    private String remark;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private Long eventId;
    private Long itemId;
    private Long registrationId;
    private Date createTime;
}
