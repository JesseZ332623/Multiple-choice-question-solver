package com.jesse.examination.user.dto.admindto;

import com.jesse.examination.user.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class AdminAddNewUserDTO
{
    private String userName;            // 用户名

    private String password;            // 用户密码

    private String fullName;            // 用户全名

    private String telephoneNumber;     // 手机号

    private String email;               // 邮箱

    private Set<RoleEntity> roles;
}
