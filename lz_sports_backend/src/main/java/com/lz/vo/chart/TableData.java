package com.lz.vo.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Table Data for Dashboard
 */
@Data
public class TableData implements Serializable {
    private Date date;
    private String name;
    private String type;
    private int fee;
}
