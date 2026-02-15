package com.lz.context;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/08/19:14
 * @Description:
 */

/**
 * 基本上下文
 *
 * @author lz
 * 一个用于存储和获取当前线程中的Long类型ID的工具类。它使用了Java中的ThreadLocal类来实现。
 * @date 2024/02/08
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 用于将当前线程中的ID设置为指定的Long类型的值。它将传入的ID存储在threadLocal变量中，以供后续的getCurrentId()方法获取。
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
    // 用于获取当前线程中存储的ID。它通过调用threadLocal.get()方法来获取存储的值，并返回结果。
    public static Long getCurrentId() {
        return threadLocal.get();
    }
    // 用于从当前线程中移除存储的ID。它通过调用threadLocal.remove()方法来清除存储的值。
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}