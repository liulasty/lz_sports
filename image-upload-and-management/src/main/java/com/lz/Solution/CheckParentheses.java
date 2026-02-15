package com.lz.Solution;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/15/21:43
 * @Description:
 */

import java.util.Stack;

/**
 * 有效括号
 *
 * @author lz
 * @date 2023/11/15
 */
public class CheckParentheses {
    public static void main(String[] args) {
        String s= "{[()]}";
        String test1 = "()";
        String test2 = "()[]{}";
        String test3 = "(]";
        String test4 = "([)]";
        String test5 = "{[]}";

        // true
        System.out.println(isValid(test1)); 
        // true
        System.out.println(isValid(test2)); 
        // false
        System.out.println(isValid(test3)); 
        // false
        System.out.println(isValid(test4)); 
        // true
        System.out.println(isValid(test5)); 
        // true
        System.out.println(isValid(s));
    }

    public static boolean isValid1(String s) {
        Stack<Character> stack = new Stack<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else if (c == ')' && !stack.isEmpty() && stack.peek() == '(') {
                stack.pop();
            } else if (c == '}' && !stack.isEmpty() && stack.peek() == '{') {
                stack.pop();
            } else if (c == ']' && !stack.isEmpty() && stack.peek() == '[') {
                stack.pop();
            } else {
                return false;
            }
        }

        return stack.isEmpty();
    }
    
    public static boolean isValid(String s) {
        int n = s.length();
        int i = 0;
        Stack<Character> stack = new Stack<>();
        int count = 0;

        while (i < n) {
            if (s.charAt(i) == '(' || s.charAt(i) == '[' || s.charAt(i) == '{') {
                stack.push(s.charAt(i));
                count++;
                i++;
            } else if (s.charAt(i) == ')' || s.charAt(i) == ']' || s.charAt(i) == '}') {
                if (stack.isEmpty()) {
                    // 栈为空，说明存在未匹配的括号
                    return false; 
                }
                char c = stack.pop();
                if ((c == '{' && count != 0) || (c == '[' && count != 1) || (c == '(' && count != -1)) {
                    // 括号不匹配
                    return false; 
                }
                count--;
            } else {
                i--;
            }
        }
        // 栈为空说明没有未匹配的括号
        return stack.isEmpty(); 
    }

    public static boolean isValid2(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) {
                    return false; // 栈为空，说明存在未匹配的括号
                }
                char top = stack.pop();
                if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) {
                    return false; // 括号不匹配
                }
            }
        }
        return stack.isEmpty(); // 栈为空说明所有括号都匹配
    }
}