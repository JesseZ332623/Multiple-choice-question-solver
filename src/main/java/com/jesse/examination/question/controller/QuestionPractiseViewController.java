package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.user.controller.utils.CookieRoles;
import com.jesse.examination.user.service.UserServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Slf4j
@Controller
@RequestMapping(path = "/question")
public class QuestionPractiseViewController
{
    private final QuestionService      questionService;
    private final UserServiceInterface userService;

    @Autowired
    public QuestionPractiseViewController(
            QuestionService questionService,
            UserServiceInterface userService)
    {
        this.userService     = userService;
        this.questionService = questionService;
    }

    /**
     * GET 方法请求，进行所有选择题的练习，以视图作为响应，
     * 渲染在 QuestionPractise.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Controller_ErrorPage.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/question/practise">
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
            HttpSession session = request.getSession(false);

            // 检查用户是否登录，否则不给浏览页面
            if (session != null && session.getAttribute("user") != null)
            {
                List<QuestionInfoDTO> allQuestionQueryResult
                        = this.questionService.getAllQuestionInfo();

                // shuffle() 算法会打乱原有的顺序，所以应该拷贝一份？
                List<QuestionInfoDTO> shuffleAllQuestions
                        = new ArrayList<>(allQuestionQueryResult);
                Collections.shuffle(shuffleAllQuestions);

                String userName
                        = (String) session.getAttribute(
                        CookieRoles.USER.toString()
                );

                model.addAttribute(
                        "UserID",
                        this.userService.findUserIdByUserName(userName)
                );
                model.addAttribute("UserName", userName);
                model.addAttribute("QuestionPractise", shuffleAllQuestions);

                return "UserOperatorPage/QuestionPractise";
            }
            else
            {
                throw new RuntimeException(
                        "No login! Couldn't preview questions!"
                );
            }
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