package com.jesse.examination.user.service.impl;

import com.jesse.examination.email.controller.EmailSenderController;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 某用户登录状态确认 Redis 键，
     * 拼合后为： LOGIN_STATUS_OF_[USER_NAME]
     */
    private static final String LOGIN_STATUS_KEY = "LOGIN_STATUS_OF_";

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
     * 我希望用户名和用户全名也是唯一的，
     * 因此在 userRegister() 方法正式将数据写入之前，要进行校验。
     *
     * @param userName 用户名
     * @param fullName 用户全名
     *
     * @throws DuplicateUserException
     *         当用户名和用户全名冲突的时候所抛的异常
     */
    private void userNameCheckOut(String userName, String fullName)
    {
        if (this.userEntityRepository.existsByUsername(userName))
        {
            throw new DuplicateUserException(
                    format("User name: [%s] already exists!", userName)
            );
        }

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

    /**
     * 设置指定用户头像数据。
     */
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

    /**
     * 新用户进行注册服务。
     *
     * @param userRegistrationDTO 从前端注册表单上收集而来的新用户数据
     *
     * @throws DuplicateUserException
     *         当用户名和用户全名冲突的时候所抛的异常
     */
    @Override
    public void
    userRegister(@NotNull UserRegistrationDTO userRegistrationDTO) throws IOException
    {
        // 验证用户名和全名
        this.userNameCheckOut(
                userRegistrationDTO.getUserName(),
                userRegistrationDTO.getFullName()
        );

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
        this.userArchiveManager.createNewArchiveForUser(userRegistrationDTO.getUserName());
    }

    /**
     * 用户登录服务。
     *
     * @param userLoginDTO 从前端页面收集上来的登录表单信息。
     *
     * @throws UsernameNotFoundException   检查到用户不存在时抛出
     * @throws PasswordMismatchException   密码不匹配时抛出
     * @throws VarifyCodeMismatchException 验证码不匹配时抛出
     */
    @Override
    public void userLogin(@NotNull UserLoginDTO userLoginDTO)
    {
        var redisTemplate         = this.userArchiveManager.getRedisTemplate();
        String userLoginStatusKey = LOGIN_STATUS_KEY + userLoginDTO.getUserName();

        // 检查该用户的登录状态键是否存在
        boolean loginStatusKeyExist = redisTemplate.hasKey(userLoginStatusKey);
        Boolean isLogin;

        // 若存在，检查登录状态
        if (loginStatusKeyExist) {
            isLogin = (Boolean) redisTemplate.opsForValue().get(userLoginStatusKey);
        }
        else // 反之认为它是第一回登录
        {
            isLogin = false;
            redisTemplate.opsForValue().set(userLoginStatusKey, false);
        }

        // 若该用户已经登录了，
        // 在别的设备上就不允许登录，直接甩出异常。
        if (isLogin != null && isLogin.equals(true))
        {
            throw new RuntimeException(
                    format("User %s already login!", userLoginDTO.getUserName())
            );
        }

        UserEntity userQueryRes = this.userEntityRepository
                .findUserByUsername(userLoginDTO.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        format("User name: [%s] not found!", userLoginDTO.getUserName()))
                );

        /*
         * 将用户前端输入的密码通过同样的方式加密后与数据库中的密文进行逐字节的比较，
         * 如果不相等则抛出异常。
        */
        if (!this.passwordEncoder.matches(
                userLoginDTO.getPassword(), userQueryRes.getPassword()))
        {
            throw new PasswordMismatchException("Incorrect password!");
        }

        // 从 Redis 数据库中查询该用户的验证码
        String userVerifyCode
                = (String) this.userArchiveManager
                               .getRedisTemplate()
                               .opsForValue()
                               .get(
                                       EmailSenderController.VERIFYCODE_KEY +
                                       userLoginDTO.getUserName()
                               );

        // 与从页面表单上收集的验证码做比较
        if (!Objects.equals(userLoginDTO.getVerifyCode(), userVerifyCode))
        {
            throw new VarifyCodeMismatchException(
                    "Code value incorrect or has been invalid."
            );
        }
        else
        {
            // 需要注意的是，如果用户登录成功了，对应的验证码也应该删掉。
            this.userArchiveManager
                    .getRedisTemplate()
                    .delete(
                            EmailSenderController.VERIFYCODE_KEY +
                                 userLoginDTO.getUserName()
                    );
        }

        // 加载用户存档
        userArchiveManager.readUserArchive(userLoginDTO.getUserName());

        // 设置登录状态为已登录
        redisTemplate.opsForValue().set(userLoginStatusKey, true);
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
            .set(LOGIN_STATUS_KEY + userName, false);

        // 存储用户的存档。
        this.userArchiveManager.saveUserArchive(userName);
    }

    /**
     * 用户修改账户数据服务（用户在修改前需要验证一次账户）。
     *
     * @param modifyOperatorDTO 用户修改操作的 DTO
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     * @throws DuplicateUserException    检查到用户重复时抛出
     */
    @Override
    public void modifyUserInfo(
            @NotNull
            ModifyOperatorDTO modifyOperatorDTO) throws Exception
    {
        // 先进行一次登录验证
        this.userLogin(modifyOperatorDTO.getUserLoginDTO());

        // 获取旧的用户信息
        UserEntity userQueryResult
                = this.userEntityRepository
                .findUserByUsername(modifyOperatorDTO.getUserLoginDTO().getUserName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        format(
                                "User name: [%s] not found!",
                                modifyOperatorDTO.getUserLoginDTO().getUserName()))
                );

        // 检查用户名和全名是否冲突（排除自身）
        String newUserName
                = modifyOperatorDTO.getUserMidifyInfoDTO().getNewUserName();

        String newUserFullName
                = modifyOperatorDTO.getUserMidifyInfoDTO().getNewFullName();

        if (!userQueryResult.getUsername().equals(newUserName))
        {
            if (this.userEntityRepository.existsByUsername(newUserName))
            {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!", newUserName
                        )
                );
            }
        }

        if (!userQueryResult.getFullName().equals(newUserFullName))
        {
            if (this.userEntityRepository.existsByFullName(newUserFullName))
            {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!", newUserFullName
                        )
                );
            }
        }

        // 修改成新的用户信息
        userQueryResult.setUsername(newUserName);
        userQueryResult.setPassword(
                this.passwordEncoder.encode(
                        modifyOperatorDTO.getUserMidifyInfoDTO().getNewPassword()
                )
        );
        userQueryResult.setFullName(modifyOperatorDTO.getUserMidifyInfoDTO().getNewFullName());
        userQueryResult.setTelephoneNumber(modifyOperatorDTO.getUserMidifyInfoDTO().getNewTelephoneNumber());
        userQueryResult.setEmail(modifyOperatorDTO.getUserMidifyInfoDTO().getNewEmail());

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
                .get(EmailSenderController.VERIFYCODE_KEY + userName);

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
                    .delete(EmailSenderController.VERIFYCODE_KEY + userName);
        }

        // 完成上述所有验证操作后，开始执行删除操作。
        // 删除这个用户的登录状态数据
        this.userArchiveManager
            .getRedisTemplate()
            .delete(LOGIN_STATUS_KEY + userName);

        // 删除用户账户信息
        this.userEntityRepository.deleteUserByUsername(userName);

        // 删除用户存档信息
        this.userArchiveManager.deleteUserArchive(userName);
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
