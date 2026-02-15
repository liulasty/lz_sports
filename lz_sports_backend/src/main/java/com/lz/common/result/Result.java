package com.lz.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * Unified API Response
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 1: Success, 0: Failure
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.data = data;
        result.code = 1;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    public static <T> Result<T> error(T data, String msg) {
        Result<T> result = new Result<>();
        result.data = data;
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
