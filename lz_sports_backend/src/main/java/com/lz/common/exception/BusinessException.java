package com.lz.common.exception;

import com.lz.common.result.ResultCode;
import lombok.Getter;

/**
 * Custom Business Exception (RuntimeException for Transaction Rollback)
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String message;
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResultCode.CONFLICT.getCode();
    }

    public BusinessException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.message = resultCode.getMsg();
        this.code = resultCode.getCode();
    }
}
