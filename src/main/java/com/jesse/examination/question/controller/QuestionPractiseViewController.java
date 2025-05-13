package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.redis.service.impl.RedisService;
import com.jesse.examination.user.service.UserServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping(path = "/question")
public class QuestionPractiseViewController
{
    private final QuestionService questionService;;

    @Autowired
    public QuestionPractiseViewController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET 方法请求，进行所有选择题的练习，以视图作为响应，
     * 渲染在 QuestionPractise.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/practise">
     *          (GET Method) 进行所有选择题的练习，以视图作为响应。
     *      </a>
     * </p>
     */
    @GetMapping(path = "practise")
    public String getQuestionPractise(
            Model model,
            HttpServletRequest request
    )
    {
        try
        {
            List<QuestionInfoDTO> allQuestionQueryResult
                    = this.questionService.getAllQuestionInfo();

            HttpSession session = request.getSession(false);

            if (!Objects.equals(session, null))
            {
                model.addAttribute(
                        "UserName",
                        session.getAttribute("user")
                );
            }

            model.addAttribute("QuestionPractise", allQuestionQueryResult);

            return "UserOperatorPage/QuestionPractise";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage =
                    ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(), "getQuestionPractise",
                    exception.getMessage()
            );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }
}