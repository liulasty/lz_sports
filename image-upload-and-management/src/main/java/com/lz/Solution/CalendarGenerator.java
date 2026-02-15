package com.lz.Solution;

import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * 日历生成器
 *
 * @author lz
 * @date 2024/03/12
 */
public class CalendarGenerator {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("日 一 二 三 四 五 六");

        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        for (int i = 0; i < weekday; i++) {
            System.out.print("   ");
        }

        while (calendar.get(Calendar.MONTH) == month) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            System.out.printf("%2d ", day);

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                System.out.println();
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            System.out.println();
        }
    }
}