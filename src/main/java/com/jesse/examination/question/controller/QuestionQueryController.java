package com.jesse.examination.question.controller;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.question.dto.QuestionWithCorrectOptionDTO;
import com.jesse.examination.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/question")
public class QuestionQueryController
{
    private final QuestionService questionService;

    @Autowired
    public QuestionQueryController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /*
     * GET 方法请求，获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容，
     * 服务器以 JSON 的格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * 可能的 URL 为：https://localhost:8081/api/question/all_questions
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

    /*
     * GET 方法请求，获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容。
     * 以 JSON 的格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * 可能的 URL 为：https://localhost:8081/api/question/all_questions_with_correct_option
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

    /*
     * PUT 方法请求，将 questions 表中指定 id 对应的数据行的 current_times 值加上 1，
     * （如果期间出现错误，会以 400 作为响应码）注意这是一个敏感操作，后续会对外屏蔽。
     *
     * 可能的 URL 为：https://localhost:8081/api/question/correct_times_plus_one/114
     */
    @PutMapping(path = "/correct_times_plus_one/{id}")
    public ResponseEntity<?> correctTimesPlusOneById(@PathVariable Integer id)
    {
        try
        {
            Integer updatedId = this.questionService.correctTimesPlusOneById(id);

            return ResponseEntity.ok()
                                 .body(
                                         String.format(
                                                 "Question id = {%d} correct times + 1",
                                                 updatedId
                                         )
                                 );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    @Transactional
    @PutMapping(path = "/clean_correct_times")
    public ResponseEntity<?> clearCorrectTimesToZero()
    {
        Integer rows = this.questionService.clearCorrectTimesToZero();

        return ResponseEntity.ok(
                String.format(
                        "Clear correct times to zero complete. Changed {%d} rows.", rows
                )
        );
    }
}
