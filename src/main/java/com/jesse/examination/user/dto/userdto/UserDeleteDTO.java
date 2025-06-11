package com.jesse.examination.user.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * 用户删除操作 DTO，
 * 由于一个用户只能删除它自己，所以用户名自然是已知的。
*/
@Data
@AllArgsConstructor
public class UserDeleteDTO
{
    String password;
    String varifyCode;
}
