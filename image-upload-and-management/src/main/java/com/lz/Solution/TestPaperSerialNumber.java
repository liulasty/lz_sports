package com.lz.Solution;

/*
  Created with IntelliJ IDEA.
 
  @Author: lz
 * @Date: 2023/12/26/18:55
 * @Description:
 */

/*
  @author lz
 */
import java.util.Arrays;

/**
 * 试纸序列号
 *
 * @author lz
 * @date 2023/12/26
 */
public class TestPaperSerialNumber {
    /**
     *计算二进制序列号中1的个数
     */
    static int countOnes(int n) {
        int count = 0;
        while (n > 0) {
            count += n & 1;
            n = n >> 1;
        }
        return count;
    }

    /**
     * 将序列号由大到小变为小到大排序
      */
    static int[] sortSerialNumbers(int[] serialNumbers) {
        for (int i = 0; i < serialNumbers.length; i++) {
            for (int j = i + 1; j < serialNumbers.length; j++) {
                if (countOnes(serialNumbers[i]) > countOnes(serialNumbers[j]) ||
                        (countOnes(serialNumbers[i]) == countOnes(serialNumbers[j]) 
                                && serialNumbers[i] > serialNumbers[j])) {
                    int temp = serialNumbers[i];
                    serialNumbers[i] = serialNumbers[j];
                    serialNumbers[j] = temp;
                }
            }
        }
        return serialNumbers;
    }

    /**
     *快速排序方式对数组进行排序
     */
    static void quickSort(int[] arr, int low, int high) {
        if (arr == null || arr.length == 0) {
            return;
        }
        if (low >= high) {
            return;
        }

        int middle = low + (high - low) / 2;
        int pivot = arr[middle];

        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }
            while (arr[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }

        if (low < j) {
            quickSort(arr, low, j);
        }
        if (high > i) {
            quickSort(arr, i, high);
        }
    }

    public static void main(String[] args) {
        

        /*
          问题2
          举例输入
         */
        int[] serialNumbers = {255, 0, 170, 85}; 
        int[] sortedSerialNumbers = sortSerialNumbers(serialNumbers);
        System.out.println("排序后的序列号： " + Arrays.toString(sortedSerialNumbers));

        /*
          问题3
          举例输入
          */
        
        int[] unsortedArray = {255, 0, 170, 85}; 
        quickSort(unsortedArray, 0, unsortedArray.length - 1);
        System.out.println("快速排序后的序列号： " + Arrays.toString(unsortedArray));
    }
}