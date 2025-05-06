package com.jesse.examination.user.service.impl;

import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.exceptions.DuplicateUserException;
import com.jesse.examination.user.exceptions.PasswordMismatchException;
import com.jesse.examination.user.repository.RoleEntityRepository;
import com.jesse.examination.user.repository.UserEntityRepository;
import com.jesse.examination.user.service.UserServiceInterface;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static java.lang.String.format;

@Service
@Transactional
public class UserService implements UserServiceInterface
{
    private final UserEntityRepository  userEntityRepository;
    private final RoleEntityRepository  roleEntityRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(
            UserEntityRepository userEntityRepository,
            RoleEntityRepository roleEntityRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userEntityRepository  = userEntityRepository;
        this.roleEntityRepository  = roleEntityRepository;
        this.passwordEncoder       = passwordEncoder;
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
        if (this.userEntityRepository.existsByUserName(userName))
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

    /**
     * 新用户进行注册服务。
     *
     * @param userRegistrationDTO 从前端注册表单上收集而来的新用户数据
     *
     * @throws DuplicateUserException
     *         当用户名和用户全名冲突的时候所抛的异常
     */
    @Override
    public void userRegister(
            @NotNull
            UserRegistrationDTO userRegistrationDTO
    )
    {
        // 验证用户名和全名
        this.userNameCheckOut(
                userRegistrationDTO.getUserName(),
                userRegistrationDTO.getFullName()
        );

        // 构建新用户实体
        UserEntity newUser = new UserEntity();
        newUser.setUserName(userRegistrationDTO.getUserName());
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
    }

    /**
     * 用户登录服务。
     *
     * @param userLoginDTO 从前端页面收集上来的登录表单信息。
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     * @throws PasswordMismatchException 密码不匹配时抛出
     */
    @Override
    public void userLogin(@NotNull UserLoginDTO userLoginDTO)
    {
        UserEntity userQueryRes = this.userEntityRepository
                .findUserByUserName(userLoginDTO.getUserName())
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
            ModifyOperatorDTO modifyOperatorDTO)
    {
        // 先进行一次登录验证
        this.userLogin(modifyOperatorDTO.getUserLoginDTO());

        // 获取旧的用户信息
        UserEntity userQueryResult
                = this.userEntityRepository
                .findUserByUserName(modifyOperatorDTO.getUserLoginDTO().getUserName())
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
            if (this.userEntityRepository.existsByUserName(newUserName))
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
        userQueryResult.setUserName(newUserName);
        userQueryResult.setPassword(
                this.passwordEncoder.encode(
                        modifyOperatorDTO.getUserMidifyInfoDTO().getNewPassword()
                )
        );
        userQueryResult.setFullName(modifyOperatorDTO.getUserMidifyInfoDTO().getNewFullName());
        userQueryResult.setTelephoneNumber(modifyOperatorDTO.getUserMidifyInfoDTO().getNewTelephoneNumber());
        userQueryResult.setEmail(modifyOperatorDTO.getUserMidifyInfoDTO().getNewEmail());

        this.userEntityRepository.save(userQueryResult);
    }

    /**
     * 用户进行登录验证后删除自己。
     *
     * @param userLoginDTO 用户在删除前需要验证一次账户
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     */
    @Override
    public void deleteUser(@NotNull UserLoginDTO userLoginDTO)
    {
        this.userLogin(userLoginDTO);       // 登录验证，不能想删谁就删谁

        this.userEntityRepository
                .deleteUserByUserName(userLoginDTO.getUserName());
    }
}
