package com.lz.Solution;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/12/6:35
 * @Description:
 */

import java.util.Arrays;

/**
 * 最长子字符串长度
 *
 * @author lz
 * @date 2023/10/12
 */
public class zuichangStr {
    public static void main(String[] args) {
        String s = "1234567891a122";

        int n = s.length();

        int max = 0;

        int[] lastSeen = new int[256];
        // 将指定的 int 值分配给指定的整数数组的每个元素。
        //
        // 形参:
        // a – 要填充的数组
        // val – 要存储在数组所有元素中的值
        Arrays.fill(lastSeen, -1);

        for (int i = 0, j = 0; j < n; j++) {
            char c = s.charAt(j);
            
            System.out.println("i = " + i);
            System.out.println("lastSeen[c] + 1 = " + (lastSeen[c] + 1));
            // 更新起始位置
            i = Math.max(i, lastSeen[c] + 1);
            System.out.println("max = " + max);
            System.out.println("j - i + 1 = " + (j - i + 1));
            max = Math.max(max, j - i + 1);
            // 记录字符最后出现的位置
            lastSeen[c] = j;
            // soutfun(lastSeen);
        }

        System.out.println("max = " + max);
    }

    

    public static void soutfun(int[] ints){
        for (int i = 49;i<58;i++){
            System.out.print(ints[i]+",");
        }
        System.out.println();
    }
}