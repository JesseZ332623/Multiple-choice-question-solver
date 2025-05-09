package com.jesse.examination.user.exceptions;

/**
 * 验证码不匹配异常类。
 */
public class VarifyCodeMismatchException extends RuntimeException
{
    public VarifyCodeMismatchException(String message) {
        super(message);
    }
}
