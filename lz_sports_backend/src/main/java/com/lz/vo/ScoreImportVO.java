package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

public class ScoreImportVO {
    @ExcelProperty("报名ID")
    private Long registrationId;

    @ExcelProperty("赛事名称")
    private String eventName;

    @ExcelProperty("项目名称")
    private String itemName;

    @ExcelProperty("运动员姓名")
    private String athleteName;

    @ExcelProperty("成绩")
    private String scoreValue;

    @ExcelProperty("名次")
    private Integer scoreRank;

    public ScoreImportVO() {}

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public void setAthleteName(String athleteName) {
        this.athleteName = athleteName;
    }

    public String getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(String scoreValue) {
        this.scoreValue = scoreValue;
    }

    public Integer getScoreRank() {
        return scoreRank;
    }

    public void setScoreRank(Integer scoreRank) {
        this.scoreRank = scoreRank;
    }
}
