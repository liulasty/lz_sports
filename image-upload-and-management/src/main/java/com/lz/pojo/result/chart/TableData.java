package com.lz.pojo.result.chart;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/01/9:57
 * @Description:
 */

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 表数据
 *
 * @author lz
 * @date 2023/11/01
 */
@Data
public class TableData implements Serializable {
    private Date date;
    private String name;
    private String type;
    private int fee;
}