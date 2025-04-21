package com.jesse.examination.question.service;

import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.jesse.examination.question.dto.QuestionWithCorrectOptionDTO;

import java.util.List;

public interface QuestionService
{
    /**
     * 查询 question 表中的所有问题和正确选项，
     * 以及 options  表中对应的所有选项内容。
     */
    List<QuestionInfoDTO> getAllQuestionInfo();

    /**
     * 查询 question 表中的所有问题和正确选项，
     * 以及 options  表中对应的正确选项内容。
     */
    List<QuestionWithCorrectOptionDTO> getAllQuestionWithCorrectOption();

    /**
     * 将 questions 表中指定 id 对应的数据行的 current_times 的值 + 1，
     * 表明用户答对了这道题一次。
     */
    Integer correctTimesPlusOneById(Integer id);

    Integer clearCorrectTimesToZero();
}
