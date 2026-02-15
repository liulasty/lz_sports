package com.lz.pojo.result.chart;
import java.time.LocalDate;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/01/9:57
 * @Description:
 */

import lombok.Data;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author lz
 */
@Data
public class OrderData<T> implements Serializable{
    private List<T> data = new ArrayList<>();
    private String[] date;
    
    public OrderData(){
        

        String[] date = {"20191001", "20191002", "20191003", "20191004",
                "20191005", "20191006", "20191007"};
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 创建日期格式化器，指定格式为yyyyMM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        // 获取前六个月的日期
        for (int i = 6; i >= 0; i--) {
            // 通过当前日期减去i个月，得到前六个月的日期
            LocalDate sixMonthsAgo = currentDate.minusMonths(i);

            // 将日期转换为指定格式的字符串
            String formattedSixMonthsAgo = sixMonthsAgo.format(formatter);

            // 将格式化后的日期存入数组中
            date[6-i] = formattedSixMonthsAgo;
        }



        this.date = date;
    }
}