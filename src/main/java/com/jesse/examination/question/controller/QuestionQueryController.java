package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.question.dto.QuestionWithCorrectOptionDTO;
import com.jesse.examination.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/question", produces = "application/json")
public class QuestionQueryController
{
    private final QuestionService questionService;

    @Autowired
    public QuestionQueryController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET 方法请求，获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容，
     * 服务器以 JSON 的格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/api/question/all_questions">
     *          (GET Method) 获取所有问题的内容，正确选项，
     *          答对次数以及它的所有选项的内容，以 JSON 的格式作为响应。
     *      </a>
     * </p>
     */
    @GetMapping(path = "/all_questions")
    public ResponseEntity<?> getAllQuestionInfo()
    {
        try
        {
            List<QuestionInfoDTO>
                    allQuestionInfo = this.questionService.getAllQuestionInfo();

            return ResponseEntity.ok().body(allQuestionInfo);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(exception.getMessage());
        }
    }

    /**
     * GET 方法请求，获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容。
     * 以 JSON 的格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/api/question/all_questions_with_correct_option">
     *          (GET Method) GET 方法请求，获取所有问题的内容，
     *          正确答案，答对的次数以及对应正确选项的内容。
     *      </a>
     * </p>
     */
    @GetMapping(path = "/all_questions_with_correct_option")
    public ResponseEntity<?> getAllQuestionWithCorrectOption()
    {
        try
        {
            List<QuestionWithCorrectOptionDTO> allQuestionWithCorrectOption =
                    this.questionService.getAllQuestionWithCorrectOption();

            return ResponseEntity.ok().body(allQuestionWithCorrectOption);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(exception.getMessage());
        }
    }
}
