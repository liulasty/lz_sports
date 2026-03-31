package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreExportVO {
    @ExcelProperty("排名")
    private Integer rank;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学院/年级")
    private String college;

    @ExcelProperty("班级")
    private String deptName;

    @ExcelProperty("成绩")
    private String score;

    @ExcelProperty("备注")
    private String remark;
}
