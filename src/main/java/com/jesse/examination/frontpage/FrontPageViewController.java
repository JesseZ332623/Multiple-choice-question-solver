package com.jesse.examination.frontpage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class FrontPageViewController
{
    /**
     * 跳转至首页。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8081/">
     *         (GET Method) 跳转至首页。
     *     </a>
     * </p>
     */
    @GetMapping
    public String FrontPageView() { return "FrontPage"; }
}
