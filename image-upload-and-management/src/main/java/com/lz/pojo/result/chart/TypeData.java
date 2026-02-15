package com.lz.pojo.result.chart;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/12/20/23:26
 * @Description:
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lz
 */
@Data

public class TypeData {
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