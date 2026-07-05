package com.quiz.platform.common;

/** 业务异常：Controller层统一捕获，返回 Result.fail(message) */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
