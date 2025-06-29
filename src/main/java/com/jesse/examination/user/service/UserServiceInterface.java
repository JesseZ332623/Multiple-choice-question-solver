package com.jesse.examination.user.service;

import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserDeleteDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.exceptions.DuplicateUserException;
import com.jesse.examination.user.exceptions.PasswordMismatchException;
import com.jesse.examination.user.exceptions.VarifyCodeMismatchException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

public interface UserServiceInterface
{
    /**
     * 通过用户名获取用户头像数据。
     */
    byte[] getUserAvatarImage(String userName);

    /**
     * 设置指定用户的头像数据。
     */
    void setUserAvatarImage(String userName, byte[] imageDataBytes);

    /**
     * 新用户进行注册服务。
     *
     * @param userRegistrationDTO 从前端注册表单上收集而来的新用户数据
     *
     * @throws DuplicateUserException
     *         当用户名或者用户全名冲突的时候所抛的异常
     *
     * @throws IOException 写入用户存档时可能抛出的异常
     */
    void userRegister(
            @NotNull
            UserRegistrationDTO userRegistrationDTO
    ) throws IOException;

    /**
     * 用户登录服务。
     *
     * @param userLoginDTO 从前端页面收集上来的登录表单信息
     *
     * @throws UsernameNotFoundException   检查到用户不存在时抛出
     * @throws PasswordMismatchException   密码不匹配时抛出
     * @throws VarifyCodeMismatchException 验证码不匹配时抛出
     */
    void userLogin(@NotNull UserLoginDTO userLoginDTO);

    /**
     * 用户登出服务。
     */
    void userLogout(String userName);

    /**
     * 用户修改账户数据服务（用户在修改前需要验证一次账户）。
     *
     * @param modifyOperatorDTO 用户修改操作的 DTO
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     * @throws DuplicateUserException    检查到用户重复时抛出
     * @throws PasswordMismatchException 密码不匹配时抛出
     */
    void modifyUserInfo(
            @NotNull
            ModifyOperatorDTO modifyOperatorDTO
    ) throws Exception;

    /**
     * 用户删除自己账户的服务。
     *
     * @param userLoginDTO 用户在删除前需要验证一次账户
     *
     * @throws UsernameNotFoundException 检查到用户不存在时抛出
     */
    void deleteUser(
            @NotNull
            UserDeleteDTO userLoginDTO, String userName
    );

    /**
     * 通过用户名查询指定的用户 ID（SQL 原生查询）。
     */
    Long findUserIdByUserName(String userName);
}
