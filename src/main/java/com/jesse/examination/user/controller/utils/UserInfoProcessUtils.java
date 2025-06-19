package com.jesse.examination.user.controller.utils;

import com.jesse.examination.user.entity.RoleEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * 管理员和用户通用的用户数据处理操作被放在这个类。
 */
@Slf4j
public class UserInfoProcessUtils
{
    /**
     * 在用户登录时，添加新的 Cookie。
     *
     * @param request  HTTP servlet 请求
     * @param response HTTP servlet 响应
     * @param userName Cookie 所关联的用户名
     */
    public static void
    addNewCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String userName, CookieRoles role
    )
    {
        /*
         * 为了防止固定会话攻击，每次登录都会强制创建新的 Session。
         */
        HttpSession session = request.getSession(false);

        if (!Objects.equals(session, null)) {
            session.invalidate();
        }

        // 将用户名和这个 session 相关联。
        session = request.getSession(true);
        session.setAttribute(role.toString(), userName);

        // 设置 Cookie 确保浏览器使用新会话 ID
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);         // 如果使用 HTTPS
        response.addCookie(cookie);

        log.info(
                "{} [{}] logged in with session ID: {}",
                role.toString().toUpperCase(), userName, session.getId()
        );
    }

    /**
     * 在用户登出或者确定干掉它自己时，删除 Cookie。
     *
     * @param response HTTP servlet 响应
     */
    public static void
    deleteCookie(HttpServletResponse response)
    {
        // 创建一个名为 "JSESSIONID" 的新 Cookie，值设为 null
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");        // 路径必须与登录时设置的路径一致
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);        // 立即过期
        response.addCookie(cookie); // 将新的 Cookie 添加到响应中
    }

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
