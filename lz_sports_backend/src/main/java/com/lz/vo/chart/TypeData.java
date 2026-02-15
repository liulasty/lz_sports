package com.lz.vo.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * Type Data for Charts
 */
@Data
public class TypeData implements Serializable {
    private int online;
    private int group;
    private int offline;
    private int other;

    public TypeData() {
        this.online = 0;
        this.group = 0;
        this.offline = 0;
        this.other = 0;
    }
}
