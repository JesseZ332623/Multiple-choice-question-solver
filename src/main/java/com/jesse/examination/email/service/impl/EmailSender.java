package com.jesse.examination.email.service.impl;

import com.jesse.examination.config.asyncconfig.AsyncConfig;
import com.jesse.examination.email.dto.EmailContentDTO;
import com.jesse.examination.email.service.EmailSenderInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static com.jesse.examination.redis.keys.ProjectRedisKey.ENTERPRISE_EMAIL_ADDRESS;
import static com.jesse.examination.redis.keys.ProjectRedisKey.SERVICE_AUTH_CODE;
import static java.lang.String.format;

/**
 * <p>封装了 javax.mail 库的邮件发送器。</p>
 *
 * <h3>2025-06-23 重构</h3>
 * <div>
 *     <ol>
 *         <li>令 sendEmail() 方法异步执行。</li>
 *         <li>
 *             为满足条件 1，我已完成线程池的配置。（详见：{@link AsyncConfig}）
 *             此外，在保留生成器模式的同时，
 *             需要额外准备被 @Bean 注解的 createEmailSender() 工厂方法。</br>
 *             这样便可在可在上层控制器中就可以直接进行依赖注入，
 *             避免手动的调用生成器进行实例化，导致不被 Spring AOP 代理管理，
 *             最后导致被 @Async 注解的异步方法没有被异步的执行。
 *         </li>
 *         <li>
 *             重构 generateVarifyCode() 方法，
 *             放弃使用 {@link ThreadLocalRandom} 转而使用安全性更高的 {@link SecureRandom}
 *             进行随机数的生成（可能会有微量的性能差异？）。
 *         </li>
 *     </ol>
 * </div>
 */
@Data
@Slf4j
@Service
public class EmailSender implements EmailSenderInterface
{
    /** 提供邮箱服务的域名 */
    private final static String SMTP_HOST = "smtp.qq.com";

    /** 邮箱服务端口*/
    private final static int    SMTP_PORT = 465;

    /** 最大邮件发送尝试次数。*/
    private static int MAX_ATTEMPT_TIMES  = 3;

    /** 提供 SMTP 服务的运营商主机名 (例：smtp.gmail.com、smtp.qq.com)。*/
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

    /** 邮件配置属性 */
    private final Properties mailProperties;

    /** Redis 模板实例 */
    private final RedisTemplate<String, Object> redisTemplate;

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
        this.mailProperties      = builder.getMailProperties();
        this.redisTemplate       = builder.getRedisTemplate();
    }

    @Bean(name = "emailSenderCreator")
    public static EmailSender createEmailSender(
            RedisTemplate<String, Object> redisTemplate
    )
    {
        return new EmailSender.EmailSenderBuilder()
                              .smtpHost(SMTP_HOST)
                              .smtpPort(SMTP_PORT)
                              .redisTemplate(redisTemplate)
                              .defaultSetProperties()
                              .build();
    }

    /** 生成长度为 digits 的验证码并返回。*/
    public static String
    generateVarifyCode(int digits)
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

        char[] buffer             = new char[digits];
        SecureRandom secureRandom = new SecureRandom();

        for (int index = 0; index < digits; ++index) {
            buffer[index]
                    = (char) ('0' + secureRandom.nextInt(10));
        }

        return String.valueOf(buffer);
    }

    /**
     * <p>邮件发送器实例生成器。</p>
     *
     * <span>
     *     在 createEmailSender() 工厂方法中用到了该生成器，
     *     因此需要 @Component 注解标记为一个组件，使得 Spring 能识别到它。
     * </span>
     */
    @Data
    @Component
    public static class EmailSenderBuilder
    {
        /** 提供 SMTP 服务的运营商主机名 */
        private String smtpHost             = null;

        /** SMTP 端口号 */
        private int    smtpPort             = -1;

        /** 邮件配置属性 */
        private Properties  mailProperties  = null;

        /** Redis 模板实例 */
        private RedisTemplate<String, Object> redisTemplate = null;

        /** 设置提供 SMTP 服务的运营商主机名。*/
        public EmailSenderBuilder smtpHost(String host) {
            this.smtpHost = host; return this;
        }

        /** 设置 SMTP 端口号。*/
        public EmailSenderBuilder smtpPort(int port) {
            this.smtpPort = port; return this;
        }

        /** 注入 Redis 模板实例。 */
        public EmailSenderBuilder
        redisTemplate(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate; return this;
        }

        /** 添加邮件服务配置属性（暂时用不到，但考虑扩展性予以保留）。*/
        public EmailSenderBuilder
        addProperty(String key, String value)
        {
            if (this.mailProperties == null) {
                this.mailProperties = new Properties();
            }

            mailProperties.put(key, value);

            return this;
        }

        /** 配置默认的邮件属性。*/
        public EmailSenderBuilder defaultSetProperties()
        {
            if (this.smtpPort != -1 || this.smtpHost != null)
            {
                this.mailProperties = new Properties();

                this.mailProperties.put("mail.smtp.auth", "true");
                this.mailProperties.put("mail.smtp.host", this.smtpHost);
                this.mailProperties.put("mail.smtp.port", this.smtpPort);
                this.mailProperties.put("mail.smtp.connectionpool", "true");
                this.mailProperties.put("mail.smtp.connectionpooltimeout", "5000");
                this.mailProperties.put("mail.smtp.connectionpoolsize", "10");

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

        /** 字段设置完毕，构造出实例并返回。*/
        public EmailSender build() {
            return new EmailSender(this);
        }
    }

    /**
     * 创建邮件发送会话的静态方法。
     *
     * @param props     邮件配置属性
     * @param userName  邮件发送人用户名
     * @param password  邮箱服务授权码
     *
     * @return 构造好的 Session 实例
     */
    private static Session createSession(
            Properties props,
            String userName, String password)
    {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
    }

    @Override
    @Async(value = "emailSendExecutor")
    public CompletableFuture<Map.Entry<Boolean, String>>
    sendEmail(@NotNull EmailContentDTO emailContent)
    {
        int attempts = 1;   // 尝试次数计数

        while (attempts <= MAX_ATTEMPT_TIMES)
        {
            try
            {
                String fromName
                        = (String) this.redisTemplate.opsForValue()
                        .get(ENTERPRISE_EMAIL_ADDRESS.toString());

                String authCode
                        = (String) this.redisTemplate.opsForValue()
                        .get(SERVICE_AUTH_CODE.toString());

                // 每一次发送邮件，都会使用新的 Session
                Message message
                        = new MimeMessage(
                                createSession(this.mailProperties, fromName, authCode)
                        );

                if (fromName != null) {
                    message.setFrom(new InternetAddress(fromName));
                }
                message.setRecipient(
                        Message.RecipientType.TO,
                        new InternetAddress(emailContent.getTo())
                );
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

                // log.info("Email send to: {} complete.\n", emailContent.getTo());

                break;
            }
            catch (MessagingException | IOException exception)
            {
                log.warn(
                        "Send email to {} failed (Try {} | {})! Cause: {}",
                        emailContent.getTo(),
                        attempts, MAX_ATTEMPT_TIMES,
                        exception.getMessage()
                );

                ++attempts;

                if (attempts > MAX_ATTEMPT_TIMES)
                {
                    log.error(
                            "Send email to {} finally failed! MAX_ATTEMPT_TIMES = {}",
                            emailContent.getTo(), MAX_ATTEMPT_TIMES
                    );

                    // 多次尝试皆失败后，返回一个 false
                    return CompletableFuture.completedFuture(
                            new AbstractMap.SimpleEntry<>(
                                    false, exception.getMessage()
                            )
                    );
                }
                else // 倘若尝试机会还未耗尽，阻塞该线程一小段时间后在进行尝试。
                {
                    try {
                        Thread.sleep(1500);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 发送成功返回正确消息的期值
        return CompletableFuture.completedFuture(
                new AbstractMap.SimpleEntry<>(
                        true, "OK!"
                )
        );
    }
}
