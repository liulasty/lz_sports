package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Score Import VO for Dashboard
 */
@Data
public class ScoreImportVO {
    @ExcelProperty("赛事ID")
    private Long eventId;

    @ExcelProperty("报名ID")
    private Long registrationId;

    @ExcelProperty("赛事名称")
    private String eventName;

    @ExcelProperty("项目ID")
    private Long itemId;

    @ExcelProperty("项目名称")
    private String itemName;

    @ExcelProperty("运动员姓名")
    private String athleteName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("部门/班级")
    private String deptName;

    @ExcelProperty("联系方式")
    private String contact;

    @ExcelProperty("报名状态")
    private String registrationStatus;

    @ExcelProperty("报名时间")
    private Date registrationTime;

    @ExcelProperty("成绩")
    private String scoreValue;

    @ExcelProperty("名次")
    private Integer scoreRank;

    @ExcelProperty("备注")
    private String remark;
}
