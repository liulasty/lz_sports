package com.lz.common.exception;

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
        this.code = 0;
    }

    public BusinessException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
