package com.jesse.examination.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithCorrectOptionDTO
{
    private Integer questionId;
    private String  questionContent;
    private String  correctAnswer;
    private String  correctOptionContent;
}
