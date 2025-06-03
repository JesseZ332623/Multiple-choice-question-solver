package com.jesse.examination.root;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 对根目录发起不同类型的请求，跳转不同的页面。
 */
@Controller
public class RootController
{
    /**
     * 如果用 GET 方法访问根目录，则直接跳转到登录页面。
     */
    @GetMapping(path = "/")
    public String defaultRedirect() { return "UserAccountPage/UserLogin"; }

    /**
     * 如果用 OPTIONS 方法访问根目录，
     * 则跳转至显示服务器支持请求方法的页面。
     */
    @RequestMapping(
            method = RequestMethod.OPTIONS,
            path = "/"
    )
    public String getAllowedMethod() { return "Other/ServerRequestMethods"; }
}
