package com.jesse.examination.user.controller.utils;

import com.jesse.examination.user.entity.RoleEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 管理员和用户通用的用户数据处理操作被放在这个类。
 */
public class UserInfoProcessUtils
{
    /**
     * 从集合中获取用户角色并合成一个字符串。
     * 假设集合
     *
     * <pre>
     *     roles = {{1, "ROLE_ADMIN"}, {2, "ROLE_USER}}
     * </pre>
     *
     * 则函数处理后返回：
     *
     * <pre> rolesString = "[ROLE_ADMIN, ROLE_USER]" </pre>
     */
    static public String getRolesString(
            @NotNull Set<RoleEntity> roles)
    {
        StringBuilder newUserRoles = new StringBuilder("[");

        for (RoleEntity role : roles) {
            newUserRoles.append(role.getRoleName()).append(", ");
        }
        newUserRoles.delete(newUserRoles.length() - 2, newUserRoles.length());
        newUserRoles.append("]");

        return newUserRoles.toString();
    }
}
