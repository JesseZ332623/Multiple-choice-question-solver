package com.jesse.examination.user.controller.ordinary_user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/user_info")
public class UserInfoViewController
{
    @GetMapping(path = "/register")
    String registerView() { return "UserAccountPage/UserRegister"; }

    @GetMapping(path = "/login")
    String loginView() { return "UserAccountPage/UserLogin"; }

    @GetMapping(path = "/modify")
    String modifyView() { return "UserAccountPage/UserModify"; }

    @GetMapping(path = "/delete")
    String deleteView() { return "UserAccountPage/UserDelete"; }
}
