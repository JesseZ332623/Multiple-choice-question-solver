package com.jesse.examination.errorhandle;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpringErrorViewController implements ErrorController
{
    @RequestMapping(path = "/error")
    public String pageNotFoundView(@NotNull Model model)
    {
        model.addAttribute(
                "ErrorMessage",
                "Page Not Found!"
        );

        return "error/GenericErrorPage";
    }
}
