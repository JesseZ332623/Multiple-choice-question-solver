package com.jesse.examination.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCorrectTimesDTO implements Serializable
{
    @JsonProperty(value = "id")
    Integer id;

    @JsonProperty(value = "correctTimes")
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
