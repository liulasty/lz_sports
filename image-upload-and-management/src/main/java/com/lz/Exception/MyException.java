package com.lz.Exception;


/**
 * 我的异常
 *
 * @author lz
 * @date 2023/12/06
 */
public class MyException extends Exception{
    private String message;

    
    public MyException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public void setMessage(String message) {
        this.message = message;
    }
}