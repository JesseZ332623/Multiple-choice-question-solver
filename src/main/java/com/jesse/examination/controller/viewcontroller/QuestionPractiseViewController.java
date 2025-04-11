package com.jesse.examination.controller.viewcontroller;

import com.jesse.examination.DTO.QuestionInfoDTO;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.service.questionservice.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/")
public class QuestionPractiseViewController
{
    private final QuestionService questionService;

    @Autowired
    public QuestionPractiseViewController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /*
     * GET 方法请求，进行所有选择题的练习，以视图作为响应，
     * 渲染在 QuestionPractise.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * 可能的 URL 为：https://localhost:8081/practise
     */
    @GetMapping(path = "practise")
    public String getQuestionPractise(Model model)
    {
        try
        {
            List<QuestionInfoDTO> allQuestionQueryResult
                    = this.questionService.getAllQuestionInfo();

            model.addAttribute("QuestionPractise", allQuestionQueryResult);

            return "QuestionPractise";
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

            return "Error_Page";
        }
    }
}
