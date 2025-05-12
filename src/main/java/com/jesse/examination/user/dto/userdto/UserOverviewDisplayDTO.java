package com.jesse.examination.user.dto.userdto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserOverviewDisplayDTO
{
    private Long id;                            // 用户 ID（自动生成）

    private String userName;                    // 用户名

    private String fullName;                    // 用户全名

    private String telephoneNumber;             // 手机号

    private String email;                       // 邮箱

    private String registerDateTime;            // 用户注册日期

    private String roles;                       // 用户的角色
}
