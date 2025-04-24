package com.jesse.examination.user.exceptions;

/**
 * 用户重复时所抛的异常。
 */
public class DuplicateUserException extends RuntimeException
{
    public DuplicateUserException(String message) { super(message); }
}
