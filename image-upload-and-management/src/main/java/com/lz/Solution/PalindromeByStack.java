package com.lz.Solution;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/10/19/9:17
 * @Description:
 */

import org.yaml.snakeyaml.util.ArrayStack;

/**
 * @author lz
 */
public class PalindromeByStack {
    public static void main(String[] args) {
        System.out.println(isPalindromic("12211221"));
        System.out.println(isPalindromic("231223123"));
    }
    
    public static boolean isPalindromic(String val){
        ArrayStack stack = new ArrayStack(1024);

        for (int i = 0; i < val.length(); i++) {
            final char c = val.charAt(i);
            stack.push(c);
            System.out.println("good");
        }
        
        for(int i = 0; i<val.length();i++){
            final char c = val.charAt(i);
            final char i1 = (char)stack.pop();
            if(c != i1){
                return false;
            }
        }
        return true;
    }
}