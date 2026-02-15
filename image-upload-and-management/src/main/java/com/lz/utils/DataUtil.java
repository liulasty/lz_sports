package com.lz.utils;

/*
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/14/11:52
 * @Description:
 */


import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 数据效用
 *
 * @author lz
 * @date 2023/11/14
 */
public class DataUtil {

    /**
    由于DateTimeFormatter是线程安全的，我们可以定义为静态常量
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(java.time.ZoneId.of("UTC"));

    /**
     * 字符串转日期
     *
     * @param s 待转换的字符串
     * @return 转换后的Date对象，若转换失败则返回null
     */
    public static Date stringToDate(String s) {
        LocalDateTime dateTime = LocalDateTime.parse(s, FORMATTER);
        return Date.from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    /**
     * 日期转字符串
     *
     * @param date 待转换的日期
     * @return 转换后的字符串
     */
    public static String dateToString(Date date) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        return FORMATTER.format(dateTime);
    }
}