package com.jesse.examination.user.controller.frontpage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user_info")
@Controller
public class FrontPageViewController
{
    /**
     * 跳转至首页。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8081/user_info/front_page">
     *         (GET Method) 跳转至首页。
     *     </a>
     * </p>
     */
    @GetMapping(path = "/user_front_page")
    public String FrontPageView() {
        return "UserAccountPage/UserFrontPage";
    }
}
