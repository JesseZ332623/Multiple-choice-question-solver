package com.jesse.examination.user.service.impl;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.exceptions.DuplicateUserException;
import com.jesse.examination.user.repository.AdminUserEntityRepository;
import com.jesse.examination.user.service.AdminServiceInterface;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static java.lang.String.format;

@Service
@Transactional
public class AdminUserService implements AdminServiceInterface
{
    AdminUserEntityRepository adminUserEntityRepository;
    BCryptPasswordEncoder     passwordEncoder;

    @Autowired
    public AdminUserService(
            AdminUserEntityRepository adminUserEntityRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder)
    {
        this.adminUserEntityRepository = adminUserEntityRepository;
        this.passwordEncoder           = bCryptPasswordEncoder;
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
        if (this.adminUserEntityRepository.existsByUserName(userName))
        {
            throw new DuplicateUserException(
                    format("User name: [%s] already exists!", userName)
            );
        }

        if (this.adminUserEntityRepository.existsByFullName(fullName))
        {
            throw new DuplicateUserException(
                    format("User full name: [%s] already exists!", fullName)
            );
        }
    }

    @Override
    public UserEntity findUserByUserName(String userName)
    {
        return this.adminUserEntityRepository.findUserByUserName(userName)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format(
                                        "User name: [%s] not exist!", userName
                                )
                        )
                );
    }

    @Override
    public Long addNewUser(
            @NotNull
            AdminAddNewUserDTO newUserRegisterDTO)
    {
        this.userNameCheckOut(
                newUserRegisterDTO.getUserName(),
                newUserRegisterDTO.getFullName()
        );

        UserEntity newUser = new UserEntity();
        newUser.setUserName(newUserRegisterDTO.getUserName());
        newUser.setFullName(newUserRegisterDTO.getFullName());
        newUser.setPassword(this.passwordEncoder.encode(newUserRegisterDTO.getPassword()));
        newUser.setEmail(newUserRegisterDTO.getEmail());
        newUser.setTelephoneNumber(newUserRegisterDTO.getTelephoneNumber());
        newUser.setRoles(newUserRegisterDTO.getRoles());

        return this.adminUserEntityRepository.save(newUser).getId();
    }

    @Override
    public Long modifyUserByUserName(
            @NotNull
            AdminModifyUserDTO adminModifyUserDTO)
    {
        UserEntity userQueryResult
                = this.adminUserEntityRepository
                .findUserByUserName(adminModifyUserDTO.getOldUserName())
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format(
                                        "User name: [%s] not exist!",
                                        adminModifyUserDTO.getOldUserName()
                                )
                        )
                );

        if (!Objects.equals(userQueryResult.getUsername(), adminModifyUserDTO.getNewUserName()))
        {
            if (this.adminUserEntityRepository.existsByUserName(adminModifyUserDTO.getNewUserName()))
            {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!",
                                adminModifyUserDTO.getNewUserName()
                        )
                );
            }
        }

        if (!Objects.equals(userQueryResult.getFullName(), adminModifyUserDTO.getNewFullName()))
        {
            if (this.adminUserEntityRepository.existsByFullName(adminModifyUserDTO.getNewFullName()))
            {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!",
                                adminModifyUserDTO.getNewFullName()
                        )
                );
            }
        }

        // 修改成新的用户信息
        userQueryResult.setUserName(adminModifyUserDTO.getNewUserName());
        userQueryResult.setPassword(
                this.passwordEncoder.encode(
                        adminModifyUserDTO.getNewPassword()
                )
        );
        userQueryResult.setFullName(adminModifyUserDTO.getNewFullName());
        userQueryResult.setTelephoneNumber(adminModifyUserDTO.getNewTelephoneNumber());
        userQueryResult.setEmail(adminModifyUserDTO.getNewEmail());

        return this.adminUserEntityRepository.save(userQueryResult).getId();
    }

    @Override
    public Long deleteUserByUserName(String userName)
    {
        UserEntity userQueryResult
                = this.adminUserEntityRepository.findUserByUserName(userName)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format(
                                        "User name: [%s] not exist!", userName
                                )
                        )
                );

        this.adminUserEntityRepository.deleteUserByUserName(userName);

        return userQueryResult.getId();
    }

    @Override
    public Long deleteUsersByIdRange(Long min, Long max)
    {
        long totalCount = this.adminUserEntityRepository.count();

        if (totalCount == 0) { return 0L; }

        if (totalCount < max)
        {
            throw new IndexOutOfBoundsException(
                    format(
                            "Repository only has %s data, [%s, %s] is invalid range!",
                            totalCount, min, max
                    )
            );
        }

        for (long index = 0L; index < max; ++index) {
            this.adminUserEntityRepository.deleteById(index);
        }

        return max - min;
    }

    @Override
    public Long truncateAllUsers()
    {
        Long totalDataAmount = this.adminUserEntityRepository.count();

        this.adminUserEntityRepository.deleteAll();

        return totalDataAmount;
    }
}
