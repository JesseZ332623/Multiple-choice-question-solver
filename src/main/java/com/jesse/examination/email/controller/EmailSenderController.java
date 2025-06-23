package com.jesse.examination.email.controller;

import com.jesse.examination.email.dto.EmailContentDTO;
import com.jesse.examination.email.entity.EmailAuthTableEntity;
import com.jesse.examination.email.exception.EmailSendFailedException;
import com.jesse.examination.email.service.EmailAuthServiceInterface;
import com.jesse.examination.email.service.EmailSenderInterface;
import com.jesse.examination.email.service.impl.EmailSender;
import com.jesse.examination.user.service.AdminServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static com.jesse.examination.redis.keys.ProjectRedisKey.*;

/**
 * 邮件发送服务 Restful 控制器。
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/email")
public class EmailSenderController
{
    /** 默认的验证码长度是 8 位。 */
    private final static int DEFAULT_CODE_LENGTH = 8;

    /** 验证码的有效期为 3 分钟。 */
    private final static int CODE_VALID_TIME = 3;

    private final EmailAuthServiceInterface emailAuthService;
    private final EmailSenderInterface      emailSender;
    private final AdminServiceInterface     emailQueryService;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据传入的参数，组合成一封验证码邮件的全部内容。
     *
     * @param userName   用户名
     * @param userEmail  用户邮箱
     * @param varifyCode 生成的验证码
     */
    private static @NotNull EmailContentDTO
    getEmailContentDTO(String userName, String userEmail, String varifyCode)
    {
        EmailContentDTO emailContent = new EmailContentDTO();

        emailContent.setTo(userEmail);
        emailContent.setSubject("用户：" + userName + " 请查收您的验证码。");
        emailContent.setTextBody(
                format(
                        "用户：%s 您的验证码是：[%s]，" +
                        "请在 %d 分钟内完成验证，超过 %d 分钟后验证码自动失效！",
                        userName, varifyCode, CODE_VALID_TIME, CODE_VALID_TIME
                )
        );

        // 暂时没有附件
        emailContent.setAttachmentPath(null);

        return emailContent;
    }

    /**
     *  在构造函数完成相应注入操作后，
     *  就直接把邮箱的发送者和授权码从数据库中读出，存入 Redis。
     * （这个只是安全措施，我能保证数据库中一定有对应的数据。）
     */
    private void saveEmailAuthInfoToRedis()
    {
        // 先检查 Redis 中是否存在指定数据
        if (
                !this.redisTemplate.hasKey(ENTERPRISE_EMAIL_ADDRESS.toString()) &&
                !this.redisTemplate.hasKey(SERVICE_AUTH_CODE.toString())
        )
        {
            EmailAuthTableEntity emailAuth
                    = this.emailAuthService.getAuthInfoById(1);

            // 往 Redis 中存入发送者的邮箱
            this.redisTemplate.opsForValue().set(
                    ENTERPRISE_EMAIL_ADDRESS.toString(), emailAuth.getEmail()
            );

            // 往 Redis 中存入发送者的授权码
            this.redisTemplate.opsForValue().set(
                    SERVICE_AUTH_CODE.toString(), emailAuth.getEmailAuthCode()
            );
        }
    }

    @Autowired
    public EmailSenderController(
            EmailAuthServiceInterface      emailAuthService,
            AdminServiceInterface          emailQueryService,
            @Qualifier("emailSenderCreator")
            EmailSenderInterface emailSender,
            RedisTemplate<String, Object>  redisTemplate
    )
    {
        this.emailAuthService   = emailAuthService;
        this.emailQueryService  = emailQueryService;
        this.emailSender        = emailSender;
        this.redisTemplate      = redisTemplate;

        this.saveEmailAuthInfoToRedis();
    }

    /**
     * 在登录页面中，
     * 先查询 userName 对应的邮箱，然后填写信息后再正式发送。
     */
    @PostMapping(path = "/send_verify_code_email/{userName}")
    CompletableFuture<ResponseEntity<String>>
    sendVarifyCodeEmail(@PathVariable String userName)
    {
        try
        {
            String userEmail = this.emailQueryService
                                   .findUserByUserName(userName)
                                   .getEmail();

            String varifyCode = EmailSender.generateVarifyCode(DEFAULT_CODE_LENGTH);

            String entireVarifyKey = USER_VERIFYCODE_KEY + userName;

            /*
             * 若在用户发送验证码前，
             * 上一回的验证码存在，需要删除。
             */
            if (this.redisTemplate.hasKey(entireVarifyKey)) {
                this.redisTemplate.delete(entireVarifyKey);
            }

            // 准备邮件内容
            EmailContentDTO emailContent
                    = getEmailContentDTO(userName, userEmail, varifyCode);

            var future = this.emailSender.sendEmail(emailContent);

            log.info("Email send to: {} complete.\n", userEmail);

            // 正式发送（异步的执行）
            return future.thenApply((sendResponse) -> {
                           if (sendResponse.getKey())
                           {
                               try
                               {
                                   /*
                                    * 邮件成功发送后，再将验证码存入 Redis 数据库，
                                    * 并设置有效期为 CODE_VALID_TIME 分钟，超过该时间 Redis 会自动删除。
                                    *
                                    * 此外，这个操作也要被 try-catch 包裹，
                                    * 倘若发送邮件成功之后，Redis 存储验证码之前
                                    * Redis 服务奔溃，则可能无法完成输入验证的操作，
                                    * 因此这样的错误需要被捕获。
                                    */
                                   this.redisTemplate.opsForValue().set(
                                           entireVarifyKey, varifyCode,
                                           CODE_VALID_TIME, TimeUnit.MINUTES
                                   );

                                   return ResponseEntity.ok(
                                           format("Send email to %s complete!", userEmail)
                                   );
                               }
                               catch (Exception exception)
                               {
                                   log.error(
                                           "Save varify code to redis failed! Cause: {}",
                                           exception.getMessage()
                                   );

                                   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                           .body(exception.getMessage());
                               }
                           }
                           else
                           {
                               throw new EmailSendFailedException(sendResponse.getValue());
                           }
                       }).exceptionally((exception) -> {
                           log.error(
                                   "Email service not available! Cause: {}",
                                   exception.getMessage()
                           );

                           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body("Email service not available!");

                    });
        }
        catch (Exception exception)  // 处理同步操作所抛的异常
        {
            log.error(
                    "Request processing failed! Cause: {}",
                    exception.getMessage()
            );

            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body(exception.getMessage())
            );
        }
    }
}
