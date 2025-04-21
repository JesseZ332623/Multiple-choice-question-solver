package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.question.service.QuestionService;
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
public class AllQuestionViewController
{
    private final QuestionService questionService;

    @Autowired
    public AllQuestionViewController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /*
     * GET 方法请求，获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容，
     * 以视图的方式作为响应，渲染在 QuestionPractise.html 页面中，
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * 可能的 URL 为：https://localhost:8081/all_questions
     */
    @GetMapping(path = "all_questions")
    public String getAllQuestionView(Model model)
    {
        try
        {
            List<QuestionInfoDTO> allQuestionQueryResult
                    = this.questionService.getAllQuestionInfo();

            model.addAttribute(
                    "AllQuestions", allQuestionQueryResult
            );

            return "AllQuestions";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                            this.getClass().getSimpleName(), "getAllQuestionView",
                            exception.getMessage()
                    );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "Error_Page";
        }
    }

    /*
     * GET 方法请求，获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容。
     * 以视图作为响应，渲染在 AllQuestionsWithCorrectResult.html 页面中
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * 可能的 URL 为：https://localhost:8081/all_questions_with_correct_option
     */
    @GetMapping(path = "all_questions_with_correct_option")
    public String getAllQuestionWithCorrectOptionView(Model model)
    {
        try
        {
            var allQuestionWithCorrectOptionQueryResult =
                this.questionService.getAllQuestionWithCorrectOption();

            model.addAttribute(
                    "AllQuestionWithCorrectOption",
                    allQuestionWithCorrectOptionQueryResult
            );

            return "AllQuestionsWithCorrectResult";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(), "getAllQuestionWithCorrectOptionView",
                    exception.getMessage()
            );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "Error_Page";
        }
    }
}
