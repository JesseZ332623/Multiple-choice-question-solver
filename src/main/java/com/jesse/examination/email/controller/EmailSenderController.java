package com.jesse.examination.email.controller;

import com.jesse.examination.email.dto.EmailContentDTO;
import com.jesse.examination.email.service.EmailSenderInterface;
import com.jesse.examination.email.service.impl.EmailSender;
import com.jesse.examination.user.service.AdminServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * 邮件发送 Restful 控制器。
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/email")
public class EmailSenderController
{
    // 默认的验证码长度是 8 位
    private final static int DEFAULT_CODE_LENGTH = 8;

    private final EmailSenderInterface  emailSender;
    private final AdminServiceInterface emailQueryService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public EmailSenderController(
            AdminServiceInterface          emailQueryService,
            RedisTemplate<String, Object>  redisTemplate
    )
    {
        // 邮箱模块采用生成器模式，所以它的参数需要我自己决定。
        this.emailSender
                = new EmailSender.EmailSenderBuilder()
                .smtpHost("smtp.qq.com")
                .smtpPort(465)
                .userName("3191955858@qq.com")
                .appPassword("ttlzjkjjbldzdffj")
                .defaultSetProperties()
                .defaultSetSession()
                .build();

        this.emailQueryService = emailQueryService;
        this.redisTemplate     = redisTemplate;
    }

    /**
     * 在登录页面中，
     * 先查询 userName 对应的邮箱，然后填写信息后再正式发送。
     */
    @PostMapping(path = "/send_verify_code_email/{userName}")
    ResponseEntity<?>
    sendVarifyCodeEmail(@PathVariable String userName)
    {
        try
        {
            String userEmail = this.emailQueryService
                                   .findUserByUserName(userName)
                                   .getEmail();

            String varifyCode
                    = EmailSender.generateVarifyCode(DEFAULT_CODE_LENGTH);

            /*
             * 将验证码存入 Redis 数据库，
             * 并设置有效期为 60 秒，超过该时间 Redis 会自动删除。
             */
            this.redisTemplate.opsForValue().set(
                    "VerifyCodeFor" + userName, varifyCode,
                    60, TimeUnit.SECONDS
            );

            EmailContentDTO emailContent = new EmailContentDTO();

            emailContent.setTo(userEmail);
            emailContent.setSubject("用户：" + userName + " 请查收您的验证码。");
            emailContent.setTextBody(
                    format(
                            "用户：%s 您的验证码是：[%s]，" +
                            "请在 60 秒内完成验证，超过 60 秒后验证码失效。",
                            userName, varifyCode
                    )
            );
            emailContent.setAttachmentPath(null);

            this.emailSender.sendEmail(emailContent);

            return ResponseEntity.ok(
                    format("Send email to %s complete!", userEmail)
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}
