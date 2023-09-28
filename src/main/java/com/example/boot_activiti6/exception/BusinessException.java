package com.example.boot_activiti6.exception;

/**
 * @author zxb 2023/9/26 10:20
 */
public class BusinessException extends RuntimeException{
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
