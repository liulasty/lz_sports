package com.lz.Exception;


/**
 * 另一个自定义异常
 *
 * @author lz
 * @date 2024/03/11
 */
public class AnotherCustomException extends Exception {
    private String message;
    
    public AnotherCustomException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 返回此可抛出对象的详细信息消息字符串。
     *
     * @return此 {@code Throwable} 实例的详细消息字符串
     *（可能是 {@code null}）。
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public void setMessage(String message) {
        this.message = message;
    }
}