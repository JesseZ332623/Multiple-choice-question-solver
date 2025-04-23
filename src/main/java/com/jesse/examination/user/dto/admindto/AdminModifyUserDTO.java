package com.jesse.examination.user.dto.admindto;

import com.jesse.examination.user.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class AdminModifyUserDTO
{
    private String oldUserName;            // 旧用户名（用户查询）

    private String newUserName;            // 用户名

    private String newPassword;            // 用户密码

    private String newFullName;            // 用户全名

    private String newTelephoneNumber;     // 手机号

    private String newEmail;               // 邮箱

    private Set<RoleEntity> newRoles;      // 用户角色
}
