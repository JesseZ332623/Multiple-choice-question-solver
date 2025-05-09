package com.jesse.examination.user.service.impl;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.exceptions.DuplicateUserException;
import com.jesse.examination.user.repository.AdminUserEntityRepository;
import com.jesse.examination.user.service.AdminServiceInterface;
import com.jesse.examination.user.service.utils.impl.UserArchiveManager;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

/**
 * 管理员操作用户数据接口。
 */
@Service
@Transactional
public class AdminUserService implements AdminServiceInterface
{
    private final AdminUserEntityRepository       adminUserEntityRepository;
    private final BCryptPasswordEncoder           passwordEncoder;
    private final UserArchiveManager              userArchiveManager ;

    @Autowired
    public AdminUserService(
            AdminUserEntityRepository       adminUserEntityRepository,
            BCryptPasswordEncoder           bCryptPasswordEncoder,
            UserArchiveManager              userArchiveManager
    )
    {
        this.adminUserEntityRepository = adminUserEntityRepository;
        this.passwordEncoder           = bCryptPasswordEncoder;
        this.userArchiveManager        = userArchiveManager;
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
        if (this.adminUserEntityRepository.existsByUsername(userName))
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

    /**
     * 通过用户名查找用户实体。
     */
    @Override
    public UserEntity findUserByUserName(String userName)
    {
        return this.adminUserEntityRepository.findUserByUsername(userName)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format(
                                        "User name: [%s] not exist!", userName
                                )
                        )
                );
    }

    /**
     * 查找表中所有的用户，存于一列表中。
     */
    @Override
    public List<UserEntity> findAllUsers()
    {
        if (this.adminUserEntityRepository.count() == 0) {
            return List.of();
        }

        return this.adminUserEntityRepository.findAll();
    }

    /**
     * 创建新的用户。
     *
     * @param adminAddNewUserDTO
     *        从前端提交的 JSON 中映射而来的新用户数据。
     *
     * @return 返回新用户的 ID
     */
    @Override
    public Long addNewUser(
            @NotNull
            AdminAddNewUserDTO adminAddNewUserDTO)
    {
        this.userNameCheckOut(
                adminAddNewUserDTO.getUserName(),
                adminAddNewUserDTO.getFullName()
        );

        UserEntity newUser = new UserEntity();
        newUser.setUsername(adminAddNewUserDTO.getUserName());
        newUser.setFullName(adminAddNewUserDTO.getFullName());

        // 密码务必要加密后再存入
        newUser.setPassword(this.passwordEncoder.encode(adminAddNewUserDTO.getPassword()));
        newUser.setEmail(adminAddNewUserDTO.getEmail());
        newUser.setTelephoneNumber(adminAddNewUserDTO.getTelephoneNumber());
        newUser.setRoles(adminAddNewUserDTO.getRoles());
        newUser.setRegisterDateTime(LocalDateTime.now());

        this.userArchiveManager
            .createNewArchiveForUser(adminAddNewUserDTO.getUserName());

        // 管理员的修改需要立即生效，所以应该 flush。
        return this.adminUserEntityRepository.saveAndFlush(newUser).getId();
    }

    /**
     * 修改已经存在的用户。
     *
     * @param adminModifyUserDTO
     *        从前端提交的 JSON 中映射而来的修改用户数据。
     */
    @Override
    public Long modifyUserByUserName(
            @NotNull
            AdminModifyUserDTO adminModifyUserDTO)
    {
        UserEntity userQueryResult
                = this.adminUserEntityRepository
                .findUserByUsername(adminModifyUserDTO.getOldUserName())
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
            if (this.adminUserEntityRepository.existsByUsername(adminModifyUserDTO.getNewUserName()))
            {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!",
                                adminModifyUserDTO.getNewUserName()
                        )
                );
            }
        }

        if (!Objects.equals(userQueryResult.getFullName(), adminModifyUserDTO.getNewFullName())) {
            if (this.adminUserEntityRepository.existsByFullName(adminModifyUserDTO.getNewFullName())) {
                throw new DuplicateUserException(
                        format(
                                "New user name: [%s] already exist!",
                                adminModifyUserDTO.getNewFullName()
                        )
                );
            }
        }

        // 修改成新的用户信息
        userQueryResult.setUsername(adminModifyUserDTO.getNewUserName());
        userQueryResult.setPassword(
                this.passwordEncoder.encode(
                        adminModifyUserDTO.getNewPassword()
                )
        );
        userQueryResult.setFullName(adminModifyUserDTO.getNewFullName());
        userQueryResult.setTelephoneNumber(adminModifyUserDTO.getNewTelephoneNumber());
        userQueryResult.setEmail(adminModifyUserDTO.getNewEmail());
        userQueryResult.setRoles(adminModifyUserDTO.getNewRoles());


        return this.adminUserEntityRepository.saveAndFlush(userQueryResult).getId();
    }

    /**
     * 通过用户名删除一条存在的用户数据。
     *
     * @return 删除的用户 ID。
     */
    @Override
    public Long deleteUserByUserName(String userName)
    {
        UserEntity userQueryResult
                = this.adminUserEntityRepository.findUserByUsername(userName)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format(
                                        "User name: [%s] not exist!", userName
                                )
                        )
                );

        this.userArchiveManager.deleteUserArchive(userName);
        this.adminUserEntityRepository.deleteUserByUsername(userName);
        this.adminUserEntityRepository.flush();

        return userQueryResult.getId();
    }

    /**
     * 通过指定 id 范围，批量地删除范围在 [begin, end] 内的用户，
     * 如果范围内有不存在的 id 则跳过。
     *
     * @return 返回实际删除的数据条数
     */
    @Override
    public Long deleteUsersByIdRange(Long begin, Long end)
    {
        List<Long> existsIds
                = this.adminUserEntityRepository.findIdByIdBetween(begin, end);

        if (!existsIds.isEmpty())
        {
            // 逐个删除（由于经过了 findIdByIdBetween() 的查询，id 基本上不会落空）
            for (Long id : existsIds)
            {
                this.userArchiveManager
                        .deleteUserArchive(
                                this.adminUserEntityRepository
                                        .findById(id)
                                        .orElseThrow()
                                        .getUsername()
                        );
            }

            this.adminUserEntityRepository.deleteUserRoleRelationsByIds(existsIds);
            this.adminUserEntityRepository.deleteUserByIds(existsIds);

            this.adminUserEntityRepository.flush();
        }

        return (long) existsIds.size();
    }

    /**
     * 删除所有用户。
     */
    @Override
    public Long truncateAllUsers()
    {
        List<Long> existsIds
            = this.adminUserEntityRepository.findAllUserId();

        // 逐个删除用户存档
        for (Long id : existsIds)
        {
            this.userArchiveManager
                    .deleteUserArchive(
                            this.adminUserEntityRepository
                                .findById(id)
                                .orElseThrow()
                                .getUsername()
                    );
        }

        this.adminUserEntityRepository.deleteAll(); // 删除所有用户

        // ID 计数重置为 1
        this.adminUserEntityRepository.alterAutoIncrementToOne();

        // 刷新（管理员的操作要立即生效）
        this.adminUserEntityRepository.flush();

        return (long) existsIds.size();
    }
}
