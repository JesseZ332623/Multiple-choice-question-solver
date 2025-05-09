package com.jesse.examination.email.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向指定用户发送邮件的数据传输类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailContentDTO
{
    private String to;              // 发给谁（如 PerterGriffen@gmail.com）
    private String subject;         // 邮箱主题
    private String textBody;        // 邮件正文

    @Nullable
    private String attachmentPath;  // 附件路径（可以为 null 表示没有附件）
}
