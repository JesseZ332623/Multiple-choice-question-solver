package com.jesse.examination.email.service;

import com.jesse.examination.email.dto.EmailContentDTO;

public interface EmailSenderInterface
{
    /**
     * 向指定用户发送邮件。
     *
     * @param emailContent 向指定用户发送邮件的数据传输类
     */
    void sendEmail(EmailContentDTO emailContent);
}
