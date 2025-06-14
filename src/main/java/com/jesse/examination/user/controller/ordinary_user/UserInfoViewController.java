package com.jesse.examination.user.controller.ordinary_user;

import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    String modifyView(HttpServletRequest request, Model model)
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute("user") != null) {
                return "UserAccountPage/UserModify";
            }
            else
            {
                throw new RuntimeException(
                        "Not login! can't preview this page!"
                );
            }
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(),
                    "modifyView",
                    exception.getMessage()
            );

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }

    @GetMapping(path = "/delete")
    String deleteView(HttpServletRequest request, Model model)
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute("user") != null)
            {
                model.addAttribute(
                        "UserName",
                        (String) request.getSession(false)
                                .getAttribute("user")
                );

                return "UserAccountPage/UserDelete";
            }
            else
            {
                throw new RuntimeException(
                        "Not login! can't preview this page!"
                );
            }
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(),
                    "deleteView",
                    exception.getMessage()
            );

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }
}
