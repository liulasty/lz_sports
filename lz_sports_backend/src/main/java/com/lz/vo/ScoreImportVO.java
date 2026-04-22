package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Score Import VO for Dashboard
 */
@Data
public class ScoreImportVO {
    @ExcelProperty(value = "成绩", index = 0)
    private String scoreValue;

    @ExcelProperty(value = "名次", index = 1)
    private Integer scoreRank;

    @ExcelProperty(value = "备注", index = 2)
    private String remark;

    @ExcelProperty(value = "赛事ID", index = 3)
    private Long eventId;

    @ExcelProperty(value = "报名ID", index = 4)
    private Long registrationId;

    @ExcelProperty(value = "赛事名称", index = 5)
    private String eventName;

    @ExcelProperty(value = "项目ID", index = 6)
    private Long itemId;

    @ExcelProperty(value = "项目名称", index = 7)
    private String itemName;

    @ExcelProperty(value = "运动员姓名", index = 8)
    private String athleteName;

    @ExcelProperty(value = "性别", index = 9)
    private String gender;

    @ExcelProperty(value = "部门/班级", index = 10)
    private String deptName;

    @ExcelProperty(value = "联系方式", index = 11)
    private String contact;

    @ExcelProperty(value = "报名状态", index = 12)
    private String registrationStatus;

    @ExcelProperty(value = "报名时间", index = 13)
    private Date registrationTime;
}
