package com.jesse.examination.user.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserModifyDTO
{
    private String newUserName;            // 用户名

    private String newPassword;            // 用户密码

    private String newFullName;            // 用户全名

    private String newTelephoneNumber;     // 手机号

    private String newEmail;               // 邮箱
}
