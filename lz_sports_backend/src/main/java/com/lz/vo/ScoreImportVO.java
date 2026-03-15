package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Score Import VO for Dashboard
 */
@Data
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
}
