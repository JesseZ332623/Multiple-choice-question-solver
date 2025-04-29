package com.jesse.examination.user.controller.ordinary_user_frontpage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@RequestMapping("/user_info")
@Controller
public class FrontPageViewController
{
    public final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public FrontPageViewController(RedisTemplate<String, String> template) {
        this.redisTemplate = template;
    }

    /**
     * 跳转至普通用户操作首页。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8081/user_info/front_page">
     *         (GET Method) 跳转至首页。
     *     </a>
     * </p>
     */
    @GetMapping(path = "/user_front_page")
    public String FrontPageView(Model model)
    {
        Optional<String> value
                = Optional.ofNullable(
                this.redisTemplate.opsForValue().get(
                        "user:UserInfoController:login_username"
                )
        );

        if (value.isPresent()) {
            model.addAttribute("UserName", value.get());
        }
        else {
            model.addAttribute("UserName", "Unknow");
        }

        return "UserOperatorPage/UserFrontPage";
    }
}
