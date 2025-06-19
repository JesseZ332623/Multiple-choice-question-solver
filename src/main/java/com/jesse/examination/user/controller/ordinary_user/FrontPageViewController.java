package com.jesse.examination.user.controller.ordinary_user;

import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.user.controller.utils.CookieRoles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user_info")
@Controller
public class FrontPageViewController
{
    /**
     * 跳转至普通用户操作首页。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8081/user_info/front_page">
     *         (GET Method) 跳转至普通用户操作首页。
     *     </a>
     * </p>
     */
    @GetMapping(path = "/user_front_page")
    public String frontPageView(
            Model model,
            HttpServletRequest request
    )
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute(CookieRoles.USER.toString()) != null)
            {
                model.addAttribute(
                        "UserName",
                        session.getAttribute(CookieRoles.USER.toString())
                );

                return "UserOperatorPage/UserFrontPage";
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
                    "frontPageView",
                    exception.getMessage()
            );

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }
}
