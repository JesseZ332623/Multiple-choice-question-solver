package com.jesse.examination.email.service;

import com.jesse.examination.email.dto.EmailContentDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface EmailSenderInterface
{
    /**
     * 向指定用户发送邮件。
     *
     * @param emailContent 向指定用户发送邮件的数据传输类
     *
     * @return 一个期值，内部存储了方法执行是否成功的消息
     */
    CompletableFuture<Map.Entry<Boolean, String>>
    sendEmail(EmailContentDTO emailContent);
}
