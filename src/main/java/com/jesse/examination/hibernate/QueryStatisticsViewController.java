package com.jesse.examination.hibernate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/hibernate/")
public class QueryStatisticsViewController
{
    @GetMapping(path = "query_statistics")
    public String showQueryStatisticsView() {
        return "Statistics/HibernateStatistics";
    }
}
