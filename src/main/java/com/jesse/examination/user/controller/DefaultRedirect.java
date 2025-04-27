package com.jesse.examination.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultRedirect
{
    /**
     * 如果访问根目录，直接跳转到登录页面。
     */
    @GetMapping(path = "/")
    public String defaultRedirect() { return "UserAccountPage/UserLogin"; }
}
