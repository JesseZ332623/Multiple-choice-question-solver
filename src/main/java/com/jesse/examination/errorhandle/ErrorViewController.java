package com.jesse.examination.errorhandle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/error")
public class ErrorViewController
{
    @GetMapping(path = "/access_denied")
    String accessDeniedView() {
        return "ErrorPage/AccessDenied";
    }

    @GetMapping(path = "/controller_error")
    String controllerErrorView() {
        return "ErrorPage/Controller_ErrorPage";
    }
}
