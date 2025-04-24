package com.jesse.examination.user.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginDTO
{
    private String userName;

    private String password;
}
