package com.lz.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreExportVO {
    @ExcelProperty("排名")
    private Integer rank;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("院系")
    private String college;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("成绩")
    private String score;

    @ExcelProperty("备注")
    private String remark;
}
