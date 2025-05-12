package com.jesse.examination.file.exceptions;

public class FileNotExistException extends RuntimeException
{
    public FileNotExistException(String message) {
        super(message);
    }
}
