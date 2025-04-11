package com.jesse.examination.controller.viewcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class FrontPageViewController
{
    /**
     * 跳转至首页。
     */
    @GetMapping
    public String FrontPageView() { return "FrontPage"; }
}
