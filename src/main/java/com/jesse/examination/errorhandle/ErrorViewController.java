package com.jesse.examination.errorhandle;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorViewController
{
    @GetMapping(path = "/my_error/access_denied")
    public String accessDeniedView() {
        return "ErrorPage/AccessDenied";
    }

    @GetMapping(path = "/my_error/controller_error")
    public String controllerErrorView() {
        return "ErrorPage/Controller_ErrorPage";
    }
}
