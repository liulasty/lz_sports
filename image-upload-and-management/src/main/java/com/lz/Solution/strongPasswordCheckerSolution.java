package com.lz.Solution;


import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/*
  Created with IntelliJ IDEA.
 
  @Author: lz
 * @Date: 2024/03/14/16:08
 * @Description:
 */

/**
 * 满足以下条件的密码被认为是强密码：
 * 
 * 由至少 6 个，至多 20 个字符组成。
 * 包含至少 一个小写 字母，至少一个大写 字母，和至少一个数字 。
 * 不包含连续三个重复字符 (比如 "Baaabb0" 是弱密码, 但是"Baaba0" 是强密码)。
 * 
 * 给你一个字符串 password ，返回将 password 修改到满足强密码条件需要的最少修改步数。如果 password 已经是强密码，则返回 0
 *
 * 在一步修改操作中，你可以：
 * 插入一个字符到 password ，
 * 从 password 中删除一个字符，或
 * 用另一个字符来替换 password 中的某个字符。
 *
 * @author lz
 * @date 2024/03/14
 */
public class strongPasswordCheckerSolution {
    static int strongPasswordChecker(String password) {
        int n = password.length();

        // 计算是否有小写字母、大写字母和数字
        int hasLower = 0, hasUpper = 0, hasDigit = 0;
        for (int i = 0; i < n; ++i) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch)) {
                hasLower = 1;
            } else if (Character.isUpperCase(ch)) {
                hasUpper = 1;
            } else if (Character.isDigit(ch)) {
                hasDigit = 1;
            }
        }
        int categories = hasLower + hasUpper + hasDigit;

        if (n < 6) {
            // 计算需要增加的字符数，即6减去密码的长度。
            int addChars = 6 - n;
            // 计算缺少的字符类别数。如果密码中没有小写字母、大写字母或数字中的任一个，就需要增加一个字符类别。
            int missingCategories = 3 - categories;
            // 返回增加字符数和缺少字符类别数中的较大值。
            return Math.max(addChars, missingCategories);
        } else if (n <= 20) {
            // 初始化替换操作数为0，计数器为0，当前字符为一个特殊字符（例如'#'）。
            int replace = 0;
            int cnt = 0;
            char cur = '#';

            // 遍历密码的每个字符。
            for (int i = 0; i < n; ++i) {
                char ch = password.charAt(i);
                // 如果当前字符与上一个字符相同，增加计数器的值。
                if (ch == cur) {
                    ++cnt;
                } else {
                    // 如果当前字符与上一个字符不同，进行以下操作：
                    // 将计数器除以3并将其加到替换操作数中。
                    replace += cnt / 3;
                    // 重置计数器为1。
                    cnt = 1;
                    // 更新当前字符为当前字符。
                    cur = ch;
                }
            }
            // 将计数器除以3并将其加到替换操作数中。
            replace += cnt / 3;

            // 计算缺少的字符类别数，即小写字母、大写字母和数字中没有出现的字符类别数。
            int missingCategories = 3 - categories;
            // 返回替换操作数和缺少字符类别数中的较大值。
            return Math.max(replace, missingCategories);
        } else {
            // 初始化替换操作数和删除操作数为0，计数器为0，当前字符为一个特殊字符（例如'#'）。
            int replace = 0, remove = n - 20;
            int cnt = 0;
            char cur = '#';

            // 遍历密码的每个字符。
            for (int i = 0; i < n; ++i) {
                char ch = password.charAt(i);
                // 如果当前字符与上一个字符相同，增加计数器的值。
                if (ch == cur) {
                    ++cnt;
                } else {
                    // 如果当前字符与上一个字符不同，进行以下操作：
                    // 如果删除操作数大于0且计数器大于等于3：
                    if (remove > 0 && cnt >= 3) {
                        // 如果计数器除以3的余数为0，减少删除操作数和替换操作数的值。
                        if (cnt % 3 == 0) {
                            --remove;
                            --replace;
                        }
                        // 如果计数器除以3的余数为1，减少删除操作数的值。
                        else if (cnt % 3 == 1) {
                            --remove;
                        }
                    }
                    // 将计数器除以3并将其加到替换操作数中。
                    replace += cnt / 3;
                    // 重置计数器为1。
                    cnt = 1;
                    // 更新当前字符为当前字符。
                    cur = ch;
                }
            }
            // 如果删除操作数大于0且计数器大于等于3：
            if (remove > 0 && cnt >= 3) {
                // 如果计数器除以3的余数为0，减少删除操作数和替换操作数的值。
                if (cnt % 3 == 0) {
                    --remove;
                    --replace;
                }
                // 如果计数器除以3的余数为1，减少删除操作数的值。
                else if (cnt % 3 == 1) {
                    --remove;
                }
            }
            // 将计数器除以3并将其加到替换操作数中。
            replace += cnt / 3;

            // 计算需要删除的字符数，即密码长度减去20。
            int deleteChars = n - 20;
            // 计算缺少的字符类别数，即小写字母、大写字母和数字中没有出现的字符类别数。
            int missingCategories = 3 - categories;
            // 返回删除操作数和替换操作数之和与缺少字符类别数中的较大值。
            return deleteChars + Math.max(replace, missingCategories);
        }
    }

    public static void main(String[] args) {
        int passwordChecker = strongPasswordChecker("hoAISJDBVWD09232UHJEPODKNLADU1");

        System.out.println("passwordChecker:" + passwordChecker);
        // 创建一个 Map 对象来存储节日日期数据
        // Map<String, String> festivalDates = new HashMap<>();
        //
        // // 添加公历节日日期
        // festivalDates.put("2024-01-01", "元旦");
        // festivalDates.put("2024-02-14", "情人节");
        // festivalDates.put("2024-03-08", "国际妇女节");
        // festivalDates.put("2024-05-01", "国际劳动节");
        // festivalDates.put("2024-06-01", "儿童节");
        // festivalDates.put("2024-10-01", "国庆节");
        // festivalDates.put("2024-12-25", "圣诞节");
        //
        // // 添加农历节日日期
        // festivalDates.put("2024-01-25", "春节");
        // festivalDates.put("2024-04-04", "清明节");
        // festivalDates.put("2024-06-12", "端午节");
        // festivalDates.put("2024-09-10", "中秋节");
        //
        // // 将 Map 转换为 JSON 对象
        // JSONObject jsonObject = new JSONObject(festivalDates);
        //
        // // 保存 JSON 对象到文件
        // try (FileWriter file = new FileWriter("festivalDates.json")) {
        //     file.write(jsonObject.toString());
        //     System.out.println("节日日期数据已成功保存到 festivalDates.json 文件。");
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }
}