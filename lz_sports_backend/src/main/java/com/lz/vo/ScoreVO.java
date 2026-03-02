package com.lz.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ScoreVO implements Serializable {
    private Long id;
    private String eventName;
    private String itemName;
    private String athleteName;
    private String scoreValue;
    private Integer scoreRank;
    private Date createTime;
}
