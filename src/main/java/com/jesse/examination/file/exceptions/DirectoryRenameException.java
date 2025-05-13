package com.jesse.examination.file.exceptions;

/**
 * 当路径重命名操作失败的时候所抛的异常。
 */
public class DirectoryRenameException extends Exception
{
    public DirectoryRenameException(String message) {
        super(message);
    }
}
