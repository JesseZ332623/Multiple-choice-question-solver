package com.jesse.examination.errorhandle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
