package com.jesse.examination.user.service;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.entity.UserEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface AdminServiceInterface
{
    /* 管理员登录 */
    void adminUserLogin(@NotNull UserLoginDTO userLoginDTO);

    /* 管理员登出 */
    void adminUserLogout(String userName);

    UserEntity findUserByUserName(String userName);

    List<UserEntity> findAllUsers();

    Long addNewUser(AdminAddNewUserDTO newUser) throws IOException;

    Long modifyUserByUserName(AdminModifyUserDTO adminModifyUserDTO);

    Long deleteUserByUserName(String userName);

    Long deleteUsersByIdRange(Long begin, Long end);

    Long truncateAllUsers();

    byte[] getDefaultAvatar();

    void modifyUserAvatar(String userName, byte[] imageDataBytes);
}