package com.jesse.examination.user.exceptions;

public class AlreadyLoginException extends RuntimeException {
    public AlreadyLoginException(String message) {
        super(message);
    }
}
