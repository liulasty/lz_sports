package com.lz.Solution;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/15/0:04
 * @Description:
 */

/**
 * @author lz
 */
public class SingleNumber {
    public static void main(String[] args) {
        int[] nums = {1,0,12,23,34,56,1,0,12,23,56};
        int result = nums[0];
        if(nums.length>1){
            for (int i = 1; i < nums.length; i++) {
                result = result^nums[i];
            }
        }
        System.out.println("result = " + result);
    }
}