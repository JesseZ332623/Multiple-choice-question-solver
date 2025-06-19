package com.jesse.examination.user.service.impl;

import com.jesse.examination.file.exceptions.FileNotExistException;
import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserDeleteDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.exceptions.DuplicateUserException;
import com.jesse.examination.user.exceptions.PasswordMismatchException;
import com.jesse.examination.user.exceptions.VarifyCodeMismatchException;
import com.jesse.examination.user.repository.RoleEntityRepository;
import com.jesse.examination.user.repository.UserEntityRepository;
import com.jesse.examination.user.service.UserServiceInterface;
import com.jesse.examination.user.service.utils.UserArchiveManagerInterface;
import com.jesse.examination.user.service.utils.impl.LoginChecker;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.jesse.examination.redis.keys.ProjectRedisKey.*;
import static java.lang.String.format;

@Slf4j
@Service
@Transactional
public class UserService implements UserServiceInterface, UserDetailsService
{
    private final UserEntityRepository        userEntityRepository;
    private final RoleEntityRepository        roleEntityRepository;
    private final BCryptPasswordEncoder       passwordEncoder;
    private final UserArchiveManagerInterface userArchiveManager;

    /** 从配置文件中获取的 Session 过期时间，单位为秒*/
    @Value(value = "${server.servlet.session.timeout}")
    private int SESSION_TIME_OUT;

    @Autowired
    public UserService(
            UserEntityRepository        userEntityRepository,
            RoleEntityRepository        roleEntityRepository,
            BCryptPasswordEncoder       passwordEncoder,
            UserArchiveManagerInterface userArchiveManager
    )
    {
        this.userEntityRepository  = userEntityRepository;
        this.roleEntityRepository  = roleEntityRepository;
        this.passwordEncoder       = passwordEncoder;
        this.userArchiveManager    = userArchiveManager;
    }

    /**
     * 我希望用户名是唯一的，
     * 因此在正式将数据写入之前，要进行校验。
     *
     * @param userName 用户名
     *
     * @throws DuplicateUserException
     *         当用户名冲突的时候所抛的异常
     */
    private void userNameCheckOut(String userName)
    {
        if (this.userEntityRepository.existsByUsername(userName))
        {
            throw new DuplicateUserException(
                    format("User name: [%s] already exists!", userName)
            );
        }
    }

    /**
     * 我希望用户全名也是唯一的，
     * 因此在正式将数据写入之前，要进行校验。
     *
     * @param fullName 用户全名
     *
     * @throws DuplicateUserException
     *         当用户名冲突的时候所抛的异常
     */
    private void userFullNameCheckOut(String fullName)
    {
        if (this.userEntityRepository.existsByFullName(fullName))
        {
            throw new DuplicateUserException(
                    format("User full name: [%s] already exists!", fullName)
            );
        }
    }

    @Override
    public byte[] getUserAvatarImage(String userName)
    {
        try
        {
            return this.userArchiveManager.getUserAvatarImage(userName);
        }
        catch (IOException | FileNotExistException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void
    setUserAvatarImage(String userName, byte[] imageDataBytes)
    {
        try
        {
            this.userArchiveManager.setUserAvatarImage(userName, imageDataBytes);
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void
    userRegister(
            @NotNull
            UserRegistrationDTO userRegistrationDTO
    ) throws IOException
    {
        // 验证用户名和全名
        this.userNameCheckOut(userRegistrationDTO.getUserName());
        this.userFullNameCheckOut(userRegistrationDTO.getFullName());

        // 构建新用户实体
        UserEntity newUser = new UserEntity();
        newUser.setUsername(userRegistrationDTO.getUserName());
        newUser.setFullName(userRegistrationDTO.getFullName());
        newUser.setTelephoneNumber(userRegistrationDTO.getTelephoneNumber());
        newUser.setEmail(userRegistrationDTO.getEmail());

        // 密码要加密后存入数据库
        newUser.setPassword(
                this.passwordEncoder.encode(
                        userRegistrationDTO.getPassword()
                )
        );

        // 默认用户角色分配是 ROLE_USER
        newUser.setRoles(
                Set.of(this.roleEntityRepository.findRoleByRoleName("ROLE_USER"))
        );

        // 设置用户的注册时间
        newUser.setRegisterDateTime(LocalDateTime.now());

        // 存入
        this.userEntityRepository.save(newUser);

        // 创建新用户的存档
        this.userArchiveManager.createNewArchiveForUser(
                userRegistrationDTO.getUserName()
        );
    }

    @Override
    public void
    userLogin(@NotNull UserLoginDTO userLoginDTO)
    {
        var redisTemplate = this.userArchiveManager.getRedisTemplate();

        String userLoginStatusKey
                = USER_LOGIN_STATUS_KEY + userLoginDTO.getUserName();
        String varifyCodeKey
                = USER_VERIFYCODE_KEY + userLoginDTO.getUserName();

        UserEntity userQueryRes = this.userEntityRepository
                .findUserByUsername(userLoginDTO.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        format("User name: [%s] not found!", userLoginDTO.getUserName()))
                );

        LoginChecker.checkLoginStatus(
                redisTemplate,
                userLoginStatusKey, userQueryRes.getRoles()
        );

        LoginChecker.passwordCheck(
                this.passwordEncoder,
                userLoginDTO.getPassword(), userQueryRes.getPassword()
        );

        LoginChecker.varifyCodeCheck(
                redisTemplate,
                varifyCodeKey, userLoginDTO.getVerifyCode()
        );

        // 所有验证通过之后，加载用户存档。
        userArchiveManager.readUserArchive(userLoginDTO.getUserName());

        /*
         * 所有检查完毕后，设置登录状态为已登录
         * （有效期为 SESSION_TIME_OUT ，与会话超时时间同步，超时则删除该数据）。
         */
        redisTemplate.opsForValue()
                     .set(
                             userLoginStatusKey, true,
                             SESSION_TIME_OUT, TimeUnit.SECONDS
                     );
    }

    /**
     * 用户登出服务。
     */
    @Override
    public void userLogout(String userName)
    {
        // 设置登录状态为未登录
        this.userArchiveManager
            .getRedisTemplate().opsForValue()
            .set(USER_LOGIN_STATUS_KEY + userName, false);

        // 存储用户的存档。
        this.userArchiveManager.saveUserArchive(userName);
    }

    @Override
    public void modifyUserInfo(
            @NotNull
            ModifyOperatorDTO modifyOperatorDTO
    ) throws Exception
    {
        // 先获取旧的用户信息
        UserEntity userQueryResult
                = this.userEntityRepository
                .findUserByUsername(modifyOperatorDTO.getUserLoginDTO().getUserName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        format(
                                "User name: [%s] not found!",
                                modifyOperatorDTO.getUserLoginDTO().getUserName()))
                );

        String newUserName
                = modifyOperatorDTO.getUserMidifyInfoDTO().getNewUserName();

        String newUserFullName
                = modifyOperatorDTO.getUserMidifyInfoDTO().getNewFullName();

        // 检查用户名是否冲突
        if (!userQueryResult.getUsername().equals(newUserName)) {
            this.userNameCheckOut(newUserName);
        }

        // 检查全名是否冲突
        if (!userQueryResult.getFullName().equals(newUserFullName)) {
            this.userFullNameCheckOut(newUserFullName);
        }

        // 也需要检查表单中输入的旧密码是否和数据库中的密码匹配
        LoginChecker.passwordCheck(
                this.passwordEncoder,
                modifyOperatorDTO.getUserLoginDTO().getPassword(),
                userQueryResult.getPassword()
        );

        // 删除旧用户的登录状态
        this.userArchiveManager
                .saveUserArchive(userQueryResult.getUsername());

        // 存档用户的数据
        this.userArchiveManager
                .getRedisTemplate()
                .delete(USER_LOGIN_STATUS_KEY + userQueryResult.getUsername());

        // 上述检查全部通过后，开始正式修改用户数据。

        userQueryResult.setUsername(newUserName);
        userQueryResult.setPassword(
                this.passwordEncoder.encode(
                        // 新密码不要忘记加密后存入
                        modifyOperatorDTO.getUserMidifyInfoDTO().getNewPassword()
                )
        );
        userQueryResult.setFullName(
                modifyOperatorDTO.getUserMidifyInfoDTO().getNewFullName()
        );
        userQueryResult.setTelephoneNumber(
                modifyOperatorDTO.getUserMidifyInfoDTO().getNewTelephoneNumber()
        );
        userQueryResult.setEmail(
                modifyOperatorDTO.getUserMidifyInfoDTO().getNewEmail()
        );

        // 存入新用户信息
        this.userEntityRepository.save(userQueryResult);

        // 用户名改了，与之对应的存档路径也应该修改。
        this.userArchiveManager.renameUserArchiveDir(
                modifyOperatorDTO.getUserLoginDTO().getUserName(),
                newUserName
        );
    }

    /**
     * 用户删除自己的账户。
     *
     * @param userDeleteDTO 在删除自己前需要密码与验证码
     * @param userName      从前端的会话中可知用户名
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     */
    @Override
    public void deleteUser(
            @NotNull
            UserDeleteDTO userDeleteDTO, String userName
    )
    {
        // 和登录操作一样，按用户名查出该用户的全部账户信息
        UserEntity userQueryRes = this.userEntityRepository
                .findUserByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        format("User name: [%s] not found!", userName))
                );

        /*
         * 将用户前端输入的密码通过同样的方式加密后与数据库中的密文进行逐字节的比较，
         * 如果不相等则抛出异常。
         */
        if (!this.passwordEncoder.matches(
                userDeleteDTO.getPassword(), userQueryRes.getPassword()))
        {
            throw new PasswordMismatchException("Incorrect password!");
        }

        // 从 Redis 数据库中查询该用户的验证码
        String userVerifyCode
                = (String) this.userArchiveManager
                .getRedisTemplate()
                .opsForValue()
                .get(USER_VERIFYCODE_KEY + userName);

        // 与从页面表单上收集的验证码做比较
        if (!Objects.equals(userDeleteDTO.getVarifyCode(), userVerifyCode))
        {
            throw new VarifyCodeMismatchException(
                    "Code value incorrect or has been invalid."
            );
        }
        else
        {
            // 需要注意的是，如果用户删除成功了，对应的验证码也应该删掉。
            this.userArchiveManager
                    .getRedisTemplate()
                    .delete(USER_VERIFYCODE_KEY + userName);
        }

        // 完成上述所有验证操作后，开始执行删除操作。
        // 删除这个用户的登录状态数据
        this.userArchiveManager
            .getRedisTemplate()
            .delete(USER_LOGIN_STATUS_KEY + userName);

        // 删除用户账户信息
        this.userEntityRepository.deleteUserByUsername(userName);

        // 删除用户存档信息
        this.userArchiveManager.deleteUserArchive(userName);
    }

    /**
     * 通过用户名查询指定的用户 ID（SQL 原生查询）。
     */
    public @NotNull Long
    findUserIdByUserName(@NotNull String userName)
    {
        return this.userEntityRepository
                   .findUserIdByUserName(userName);
    }

    /**
     * Locates the user based on the username.
     * In the actual implementation, the search
     * may possibly be case-sensitive, or case-insensitive depending on how the
     * implementation instance is configured.
     * In this case, the <code>UserDetails</code>
     * object that comes back may have a username of a different case than what
     * was actually requested.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public UserDetails
    loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserEntity queryResult
                = this.userEntityRepository
                      .findUserByUsername(username)
                      .orElseThrow(
                             () -> new UsernameNotFoundException(
                                     "User: " + username + " Not Found."
                             )
                      );

        return new User(
                queryResult.getUsername(),
                queryResult.getPassword(),
                queryResult.getAuthorities()
        );
    }
}
