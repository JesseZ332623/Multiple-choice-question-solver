package com.jesse.examination.hibernate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/hibernate/")
public class QueryStatisticsViewController
{
    /**
     * 渲染 Hibernate 的统计信息页面，
     * 该页面仅仅测试时用，因此不设任何权限，在正式部署的时候也不会暴露。
     */
    @GetMapping(path = "query_statistics")
    public String showQueryStatisticsView() {
        return "Statistics/HibernateStatistics";
    }
}
