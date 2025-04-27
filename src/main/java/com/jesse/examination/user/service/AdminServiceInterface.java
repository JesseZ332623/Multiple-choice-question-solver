package com.jesse.examination.user.service;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.entity.UserEntity;

import java.util.List;

public interface AdminServiceInterface
{
    UserEntity findUserByUserName(String userName);

    List<UserEntity> findAllUsers();

    Long addNewUser(AdminAddNewUserDTO newUser);

    Long modifyUserByUserName(AdminModifyUserDTO adminModifyUserDTO);

    Long deleteUserByUserName(String userName);

    Long deleteUsersByIdRange(Long begin, Long end);

    Long truncateAllUsers();
}