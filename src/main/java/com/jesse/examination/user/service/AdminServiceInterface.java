package com.jesse.examination.user.service;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.entity.UserEntity;

public interface AdminServiceInterface
{
    UserEntity findUserByUserName(String userName);

    Long addNewUser(AdminAddNewUserDTO newUser);

    Long modifyUserByUserName(AdminModifyUserDTO adminModifyUserDTO);

    Long deleteUserByUserName(String userName);

    Long deleteUsersByIdRange(Long min, Long max);

    Long truncateAllUsers();
}