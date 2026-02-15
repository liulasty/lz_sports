package com.lz.pojo.constant;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2024/02/08/13:14
 * @Description:
 */

/**
 * 工作日
 *
 * @author lz
 * @date 2024/02/08
 */
public enum Weekday {
    /**
     * 星期一
     */
    MONDAY("星期一"),
    /**
     * 星期二
     */
    TUESDAY("星期二"),
    /**
     * 星期三
     */
    WEDNESDAY("星期三"),
    /**
     * 星期四
     */
    THURSDAY("星期四"),
    /**
     * 星期五
     */
    FRIDAY("星期五"),
    /**
     * 星期六
     */
    SATURDAY("星期六"),
    /**
     * 星期日
     */
    SUNDAY("星期天");

    /**
     * 中文名
     */
    private final String chineseName;

    Weekday(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }
}