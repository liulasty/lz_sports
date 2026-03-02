package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RegistrationExportVO implements Serializable {
    @ExcelProperty("报名ID")
    private Long registrationId;

    @ExcelProperty("赛事名称")
    private String eventName;

    @ExcelProperty("项目名称")
    private String itemName;

    @ExcelProperty("运动员姓名")
    private String athleteName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("联系方式")
    private String contact;

    @ExcelProperty("报名时间")
    private Date registrationTime;

    @ExcelProperty("状态")
    private String status;
}
