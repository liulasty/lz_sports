package com.lz.pojo.result.chart;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/01/9:56
 * @Description:
 */

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author lz
 */
@Data
public class UserData {
    private String date;
    private int addUser;
    private int addAthlete;
    
}