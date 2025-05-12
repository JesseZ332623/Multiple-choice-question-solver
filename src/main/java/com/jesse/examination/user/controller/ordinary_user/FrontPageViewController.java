package com.jesse.examination.user.controller.ordinary_user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Slf4j
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
    public String FrontPageView(
            Model model,
            HttpServletRequest request
    )
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (!Objects.equals(session, null))
            {
                model.addAttribute(
                        "UserName",
                        session.getAttribute("user")
                );
            }
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            model.addAttribute(
                    "UserName", "Unknow"
            );
        }

        return "UserOperatorPage/UserFrontPage";
    }
}
