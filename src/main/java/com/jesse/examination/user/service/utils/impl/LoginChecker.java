package com.jesse.examination.user.service.utils.impl;

import com.jesse.examination.user.entity.RoleEntity;
import com.jesse.examination.user.exceptions.AlreadyLoginException;
import com.jesse.examination.user.exceptions.PasswordMismatchException;
import com.jesse.examination.user.exceptions.VarifyCodeMismatchException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static com.jesse.examination.redis.keys.ProjectRedisKey.*;

/**
 * 用户和管理员登录时，要进行的各项验证逻辑会放在此处。
 */
@Slf4j
public class LoginChecker
{
    /**
     * 管理员或普通用户登录时，检查他们的登录状态。
     *
     * @param redisTemplate     redis 模板
     * @param loginStatusKey    某用户登录状态键
     *
     * <br> {@code loginStatusKey} 的示例如下：
     * <br> {@code LOGIN_STATUS_OF_USER_Peter} 或者
     *      {@code LOGIN_STATUS_OF_ADMIN_Jesse}
     *
     * @throws AlreadyLoginException 在检查到用户已经登录时抛出
     */
    public static void
    checkLoginStatus(
            @NotNull
            RedisTemplate<String, Object> redisTemplate,
            String loginStatusKey, Set<RoleEntity> roles
    )
    {
        String userName
                = loginStatusKey.substring(
                loginStatusKey.lastIndexOf('_') + 1
        );

        // 对于无参且无返回值的方法，可以使用 Runnable 函数式接口？
        Runnable loginStausCheckBasic
                = () ->
        {
            // 检查该用户的登录状态键是否存在
            boolean loginStatusKeyExist = redisTemplate.hasKey(loginStatusKey);
            Boolean isLogin;

            // 若存在，检查登录状态
            if (loginStatusKeyExist) {
                isLogin = (Boolean) redisTemplate.opsForValue().get(loginStatusKey);
            }
            else
            {
                /* 反之认为它登录超时或者头一回登录，新建登录状态键值对。*/
                isLogin = false;
                redisTemplate.opsForValue().set(loginStatusKey, false);
            }

            // 若该用户已经登录了，
            // 在别的设备上就不允许登录，直接甩出异常。
            if (isLogin != null && isLogin.equals(true))
            {
                throw new AlreadyLoginException(
                        format(
                                "%s %s already login!",
                                loginStatusKey.contains("ADMIN") ? "Admin" : "User",
                                userName
                        )
                );
            }
        };

        /*
         * 这里需要考虑这么一个情况，一个用户可能既是用户又是管理员，
         * 当他以管理员登录时，他的用户登录就应该被顶掉（反之亦然，已经通过 Session 实现）。
         * 因此，这里的登录状态信息也应该同步，
         * 对于既是 user 又是 admin 的用户，需要确保他只有一个角色的登录状态为真。
         */

        // 倘若该用户既是 user 又是 admin
        if (roles.size() == 2)
        {
            // 若是管理员登录
            if (loginStatusKey.contains(ADMIN_LOGIN_STATUS_KEY))
            {
                loginStausCheckBasic.run();
                
                // 顶掉他用户角色的登录状态
                redisTemplate.opsForValue().set(
                        USER_LOGIN_STATUS_KEY + userName, false
                );
            }
            else
            {
                loginStausCheckBasic.run();

                // 顶掉他管理员角色的登录状态
                redisTemplate.opsForValue().set(
                        ADMIN_LOGIN_STATUS_KEY + userName, false
                );
            }
        }
        else {  // 对于单角色的用户登录状态处理
            loginStausCheckBasic.run();
        }
    }

    /**
     * 登录密码的检查。
     *
     * @param passwordEncoder BCrypt 加密器
     * @param rawPassword     从前端表单收集的密码，会被加密后进行比对
     * @param encodePassword  存储在数据库的，该用户已加密的密码
     *
     * @throws PasswordMismatchException 当密码比对不正确时抛出
     */
    public static void
    passwordCheck(
            @NotNull
            BCryptPasswordEncoder passwordEncoder,
            CharSequence rawPassword, String encodePassword
    )
    {
        /*
         * 将用户前端输入的密码通过同样的方式加密后
         * 与数据库中的密文进行逐字节的比较，如果不相等则抛出异常。
         */
        if (!passwordEncoder.matches(rawPassword, encodePassword))
        {
            throw new PasswordMismatchException("Incorrect password!");
        }
    }

    /**
     * 验证码的检查。
     *
     * @param redisTemplate       redis 模板
     * @param varifyCodeKey       某用户的验证码键
     * @param varifyCodeFromInput 从前端表单收集来的验证码
     *
     * <br> {@code varifyCodeKey} 的示例如下：
     * <br> {@code VERIFY_CODE_FOR_Jesse} 或者
     *      {@code VERIFY_CODE_FOR_Peter}
     *
     * @throws VarifyCodeMismatchException 当验证码不匹配时抛出
     */
    public static void
    varifyCodeCheck(
            @NotNull
            RedisTemplate<String, Object> redisTemplate,
            String varifyCodeKey, String varifyCodeFromInput
    )
    {
        // 从 Redis 数据库中查询该用户的验证码
        String userVerifyCode
                = (String) redisTemplate
                            .opsForValue()
                            .get(varifyCodeKey);

        // 与从页面表单上收集的验证码做比较
        if (!Objects.equals(userVerifyCode, varifyCodeFromInput))
        {
            throw new VarifyCodeMismatchException(
                    "Code value incorrect or has been invalid."
            );
        }
        else
        {
            // 需要注意的是，如果用户登录成功了，对应的验证码也应该删掉。
            redisTemplate.delete(varifyCodeKey);
        }
    }

    /**
     * 查找某个角色的用户表，看看是否存在 roleName 这个角色。
     *
     * @param roles     用户角色表
     * @param roleName  要查询的角色名
     * @param userName  用户名，用于生成异常消息
     *
     * @throws InsufficientAuthenticationException
     *         没有查到指定角色抛出的权限不足异常
     */
    public static void
    checkRoles(
            Set<RoleEntity> roles,
            String roleName, String userName
    )
    {
        // 查找这个用户的角色表，看看是否存在 roleName 这个角色
        boolean isAdmin = false;

        for (RoleEntity role : roles)
        {
            if (role.getRoleName().equals(roleName))
            {
                isAdmin = true;
                break;
            }
        }

        // 倘若不存在管理员角色，需要甩出一个权限不足地异常。
        if (!isAdmin)
        {
            throw new InsufficientAuthenticationException(
                    format(
                            "User %s are not admin! Login request rejected!",
                            userName
                    )
            );
        }
    }
}
