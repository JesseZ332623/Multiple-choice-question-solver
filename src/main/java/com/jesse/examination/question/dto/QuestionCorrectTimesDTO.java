package com.jesse.examination.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCorrectTimesDTO
{
    Integer id;
    Integer correctTimes;

    @Override
    public String toString()
    {
        return String.format(
                """
                {"id" : %d, "correctTimes" : %d}
                """,
                id, correctTimes
        );
    }
}
