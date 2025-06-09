package com.jesse.examination.scorerecord.controller;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@Controller
@RequestMapping(path = "/score_record")
public class PractiseScoreViewController {
    private final ScoreRecordService scoreRecordService;

    @Autowired
    public PractiseScoreViewController(ScoreRecordService scoreRecordService) {
        this.scoreRecordService = scoreRecordService;
    }

    /**
     * GET 方法请求，分页的渲染当前登录用户的所有练习记录，
     * 以视图作为响应，渲染在 AllPractiseScore.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/score_record/paginated_score_record">
     *          (GET Method) 渲染某用户第一页的五条练习记录，以视图作为响应。
     *      </a>
     * </p>
     *
     * @param page    起始页数（默认为 1）
     * @param size    每页的数据条数（默认为 5）
     * @param model   Thymeleaf 框架的模型实例，将后端数据作为属性传递给前端
     * @param request HTTP Servlet 请求（主要是获取 Session 信息）
     */
    @GetMapping(path = "paginated_score_record")
    public String getAllPractiseScoreView(
            @RequestParam(name = "page", defaultValue = "1")   int page,
            @RequestParam(name = "size", defaultValue = "10")  int size,
            Model              model,
            HttpServletRequest request
    )
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (Objects.equals(session, null))
            {
                throw new RuntimeException(
                        "No user log in!"
                );
            }

            page = Math.max(0, page);               // 页数不得为负
            size = Math.min(15, Math.max(1, size)); // 每一页最多不超过 15 条

            // 从 Session 处获取用户名
            String userName = (String) session.getAttribute("user");

            // 构建分页信息
            Pageable pageable
                    = PageRequest.of(
                    page - 1, size,
                    Sort.by("submitDate").descending()
            );

            // 查询分页数据
            Page<ScoreRecordEntity> scoreRecordEntityPage
                    = this.scoreRecordService
                    .findPaginatedScoreRecordByUserName(
                            userName, pageable
                    );

            var pageCount = scoreRecordEntityPage.getTotalPages();

            if (page > pageCount && pageCount != 0)
            {
                throw new IllegalArgumentException(
                        format("Page value couldn't more than %d.", pageCount)
                );
            }

            // 添加用户名属性
            model.addAttribute("UserName", userName);

            // 添加当前页码属性
            model.addAttribute("CurrentPage", page);

            // 添加每页数据条数属性
            model.addAttribute("OnePageAmount", size);

            // 添加总页数属性
            model.addAttribute("PageCount", pageCount);

            // 添加总元素数属性
            model.addAttribute("EntityCount", scoreRecordEntityPage.getTotalElements());

            // 添加某一页的成绩数据属性
            model.addAttribute("PaginatedScoreRecord", scoreRecordEntityPage);

            return "UserOperatorPage/PaginatedPractiseScore";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(),
                    "getAllPractiseScoreView",
                    exception.getMessage()
            );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }

    /**
     * GET 方法请求，在用户交卷后渲染它此时的成绩，以视图作为响应，
     * 渲染在 ScoreSettlement.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     * 链接：
     * <a href="https://localhost:8081/score_record/current_score_settlement">
     * (GET Method) 渲染所有练习记录，以视图作为响应。
     * </a>
     * </p>
     *
     * <strong>
     * 若用户直接访问的话，就会以最新的一次练习记录作为数据进行渲染。
     * </strong>
     */
    @GetMapping(path = "current_score_settlement")
    public String scoreSettlementView(
            Model model,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        if (!Objects.equals(session, null)) {
            model.addAttribute(
                    "UserName",
                    session.getAttribute("user")
            );
        } else {
            throw new RuntimeException(
                    "Opps! No user log in!"
            );
        }

        return "UserOperatorPage/ScoreSettlement";
    }
}
