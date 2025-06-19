package com.jesse.examination.user.controller.utils;

/**
 * 对于不同的用户角色登录，
 * 应该采用不同的角色键去查询 Cookie。
*/
public enum CookieRoles
{
    USER("user"), ADMIN("admin");

    final String roleName;

    CookieRoles(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return this.roleName;
    }
}