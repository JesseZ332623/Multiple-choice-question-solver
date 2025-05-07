package com.jesse.examination.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionInfoDTO
{
    private Integer questionId;
    private String  questionContent;
    private String  correctAnswer;

    // 解析后的选项（如 {"A": "内容", "B": "内容"}）
    public Map<String, Object> options;
}