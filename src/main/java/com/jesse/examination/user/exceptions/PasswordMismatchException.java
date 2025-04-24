package com.jesse.examination.user.exceptions;

/**
 * 当密码不匹配时所抛的异常。
 */
public class PasswordMismatchException extends RuntimeException
{
    public PasswordMismatchException(String message) { super(message); }
}
