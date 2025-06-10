package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.redis.service.RedisServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/question")
public class AllQuestionViewController
{
    private final QuestionService       questionService;
    private final RedisServiceInterface redisService;

    @Autowired
    public AllQuestionViewController(
            QuestionService         questionService,
            RedisServiceInterface   redisService
    )
    {
        this.questionService = questionService;
        this.redisService    = redisService;
    }

    /**
     * GET 方法请求，获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容，
     * 以视图的方式作为响应，渲染在 QuestionPractise.html 页面中，
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/question/all_questions">
     *          (GET Method) 获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容。
     *      </a>
     * </p>
     */
    @GetMapping(path = "all_questions")
    public String getAllQuestionView(
            Model model,
            HttpServletRequest request
    )
    {
        try
        {
            HttpSession session       = request.getSession(false);
            String      loginUserName = (String) session.getAttribute("user");

            List<QuestionInfoDTO> allQuestionQueryResult
                    = this.questionService.getAllQuestionInfo();

            List<QuestionCorrectTimesDTO> allQuestionCorrectTimes
                        = this.redisService.readQuestionCorrectTimeList(loginUserName);

            model.addAttribute("UserName", loginUserName);
            model.addAttribute("AllQuestions", allQuestionQueryResult);
            model.addAttribute("QuestionCorrectTimes", allQuestionCorrectTimes);

            return "UserOperatorPage/AllQuestions";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                            this.getClass().getSimpleName(),
                    "getAllQuestionView",
                            exception.getMessage()
                    );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }

    /**
     * GET 方法请求，获取所有问题的内容，正确答案以及对应正确选项的内容。
     * 以视图作为响应，渲染在 AllQuestionsWithCorrectResult.html 页面中
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/question/all_questions_with_correct_option">
     *          (GET Method) 获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容。
     *      </a>
     * </p>
     */
    @GetMapping(path = "all_questions_with_correct_option")
    public String
    getAllQuestionWithCorrectOptionView(
            Model model,
            HttpServletRequest request
    )
    {
        try
        {
            HttpSession session       = request.getSession(false);
            String      loginUserName = (String) session.getAttribute("user");

            var allQuestionWithCorrectOptionQueryResult =
                this.questionService.getAllQuestionWithCorrectOption();

            model.addAttribute("UserName", loginUserName);
            model.addAttribute(
                    "AllQuestionWithCorrectOption",
                    allQuestionWithCorrectOptionQueryResult
            );

            return "UserOperatorPage/AllQuestionsWithCorrectResult";
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

            return "ErrorPage/Controller_ErrorPage";
        }
    }
}