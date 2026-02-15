package com.lz.Solution;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/07/19:37
 * @Description:
 */

/**
 * @author lz
 */
public class FibonacciSequence {
    public static void main(String[] args) {
        long a1 = 0;
        long a2 = 0;
        for (int i = 1; i < 1000; i++) {
            if (i == 1) {
                a1 = 1;
                System.out.println(a1);
                continue;
            }
            if (i == 2) {
                a2 = 1;
                System.out.println(a2);
                continue;
            }

            if (i % 2 == 1) {
                a1 = a1 + a2;
                System.out.println(a1);
            } else {
                a2 = a1 + a2;
                System.out.println(a2);
            }
        }
    }
}