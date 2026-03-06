package com.lz.vo.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Table Data for Dashboard
 */
public class TableData implements Serializable {
    private Date date;
    private String name;
    private String type;
    private int fee;

    public TableData() {}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }
}
