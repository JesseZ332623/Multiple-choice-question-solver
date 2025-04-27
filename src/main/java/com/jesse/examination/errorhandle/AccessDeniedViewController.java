package com.jesse.examination.errorhandle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedViewController
{
    @GetMapping(path = "/error/access_denied")
    String accessDeniedView() {
        return "ErrorPage/AccessDenied";
    }
}
