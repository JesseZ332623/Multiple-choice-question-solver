package com.jesse.examination.user.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegistrationDTO
{
    private String userName;            // 用户名

    private String password;            // 用户密码

    private String fullName;            // 用户全名

    private String telephoneNumber;     // 手机号

    private String email;               // 邮箱
}
