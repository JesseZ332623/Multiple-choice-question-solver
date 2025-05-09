package com.jesse.examination.email.service.impl;

import com.jesse.examination.email.dto.EmailContentDTO;
import com.jesse.examination.email.service.EmailSenderInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

/**
 * 封装了 javax.mail 库的邮件发送器。
 */
@Data
@Slf4j
public class EmailSender implements EmailSenderInterface
{
    // 提供 SMTP 服务的运营商主机名 (例：smtp.gmail.com、smtp.qq.com)。
    private final String smtpHost;

    /**
     * SMTP 端口号，不同的服务商对邮件开放的标准服务端口都不同，具体如下所示：
     * <pre>
     *     Gmail   587 STARTTLS
     *     Outlook 587 STARTTLS
     *     Yahoo   465 SSL
     *     QQ-Mail 465 or 587 SSL
     * </pre>
     * 其他服务商的端口号可以查询他们提供的文档。
     */
    private final int smtpPort;

    // 发送人邮箱地址
    private final String userName;

    // 发送人邮箱密码（以 Google 为例，可以申请应用专用密码使用）。
    private final String applicationPassword;

    // 邮件配置属性
    private final Properties mailProperties;

    // 邮件会话控制
    private final Session session;

    /**
     * 邮件发送器构造函数，在调用 EmailSenderBuilder::build() 时调用，
     * 外部不可以直接调用。
     *
     * @param builder 邮件发送器实例生成器
     */
    private EmailSender(
            @NotNull
            EmailSenderBuilder builder)
    {
        this.smtpHost            = builder.getSmtpHost();
        this.smtpPort            = builder.getSmtpPort();
        this.userName            = builder.getUserName();
        this.applicationPassword = builder.getApplicationPassword();
        this.mailProperties      = builder.getMailProperties();
        this.session             = builder.getSession();
    }

    /**
     * 生成长度为 digits 的验证码并返回。
     */
    public static String generateVarifyCode(int digits)
    {
        if (digits <= 0)
        {
            throw new IllegalArgumentException(
                    format(
                            "Number digits could not less than 1, your value = %d.",
                            digits
                    )
            );
        }

        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits);

        return String.valueOf(ThreadLocalRandom.current().nextInt(min, max));
    }

    /**
     * 邮件发送器实例生成器。
     */
    @Data
    public static class EmailSenderBuilder
    {
        private String smtpHost             = null;
        private int    smtpPort             = -1;
        private String userName             = null;
        private String applicationPassword  = null;
        private Properties  mailProperties  = null;
        private Session     session         = null;

        /**
         * 设置提供 SMTP 服务的运营商主机名。
         */
        public EmailSenderBuilder smtpHost(String host) {
            this.smtpHost = host; return this;
        }

        /**
         * 设置 SMTP 端口号。
         */
        public EmailSenderBuilder smtpPort(int port) {
            this.smtpPort = port; return this;
        }

        /**
         * 设置发送人邮箱地址。
         */
        public EmailSenderBuilder userName(String user) {
            this.userName = user; return this;
        }

        /**
         * 发送人邮箱密码。
         */
        public EmailSenderBuilder appPassword(String password) {
            this.applicationPassword = password; return this;
        }

        public EmailSenderBuilder addProperty(String key, String value)
        {
            if (this.mailProperties == null) {
                this.mailProperties = new Properties();
            }

            mailProperties.put(key, value);

            return this;
        }

        /**
         * 配置默认的邮件属性。
         */
        public EmailSenderBuilder defaultSetProperties()
        {
            if (this.smtpPort != -1 || this.smtpHost != null)
            {
                this.mailProperties = new Properties();
                this.mailProperties.put("mail.smtp.auth", "true");
                this.mailProperties.put("mail.smtp.host", this.smtpHost);
                this.mailProperties.put("mail.smtp.port", this.smtpPort);

                switch (this.smtpPort)
                {
                    case 465:
                        this.mailProperties.put("mail.smtp.ssl.enable", "true");
                        break;

                    case 587:
                        this.mailProperties.put("mail.smtp.starttls.enable", "true");
                        break;
                }
            }
            else
            {
                throw new IllegalStateException(
                        "[IllegalStateException] Couldn't set Properties, " +
                                "Cause: Not setting SMTP Port or SMTP Host."
                );
            }

            return this;
        }

        /**
         * 配置默认的 Session 会话。
         */
        public EmailSenderBuilder defaultSetSession()
        {
            if (
                    this.userName != null            &&
                            this.applicationPassword != null &&
                            this.mailProperties != null
            ) {
                this.session = Session.getInstance(this.mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, applicationPassword);
                    }
                });
            }
            else
            {
                throw new IllegalStateException(
                        "[IllegalStateException] Couldn't set session, " +
                        "Cause: Not setting user name, app password or mail properties."
                );
            }

            return this;
        }

        /**
         * 字段设置完毕，构造出实例并返回。
         */
        public EmailSender build() {
            return new EmailSender(this);
        }
    }

    /**
     * 向指定用户发送邮件。
     *
     * @param emailContent 向指定用户发送邮件的数据传输类
     */
    @Override
    public void sendEmail(EmailContentDTO emailContent)
    {
        try
        {
            Message message = new MimeMessage(this.getSession());

            message.setFrom(new InternetAddress(this.getUserName()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailContent.getTo()));
            message.setSubject(emailContent.getSubject());

            if (emailContent.getAttachmentPath() == null) {
                message.setText(emailContent.getTextBody());
            }
            else
            {
                Multipart multipart = new MimeMultipart();

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(emailContent.getTextBody());
                multipart.addBodyPart(textPart);

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(emailContent.getAttachmentPath()));
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);
            }

            Transport.send(message);

            log.info(
                    "Email send to: {} complete.\n",
                    emailContent.getTo()
            );
        }
        catch (MessagingException | IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(
                    "[RuntimeException] Failed to send com.jesse.examination.email: " + exception.getMessage()
            );
        }
    }
}
