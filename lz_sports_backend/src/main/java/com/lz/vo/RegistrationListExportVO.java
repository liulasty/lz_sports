package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.util.Date;

@Data
public class RegistrationListExportVO {
    @ExcelProperty("序号")
    private Integer no;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学院/年级")
    private String college;

    @ExcelProperty("班级")
    private String deptName;

    @ExcelProperty("项目")
    private String item;

    @ExcelProperty("报名时间")
    private Date registrationTime;

    @ExcelProperty("状态")
    private String status;
}
