package com.jesse.examination.scorerecord.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRecordQueryDTO
{
    private Integer         scoreId;            // 成绩记录 ID

    private String          userName;           // 用户名

    private LocalDateTime   submitDate;         // 成绩提交日期

    private Integer         correctCount;       // 正确数

    private Integer         errorCount;         // 错误数

    private Integer         noAnswerCount;      // 未答数

    public String getSubmitDate()
    {
        /*
         * 这条语句返回的格式化字符串为：
         *      2025年6月18日星期三 HKT 02:14:13
         */
        return this.submitDate.toInstant(ZoneOffset.UTC)
                         .atZone(ZoneId.systemDefault())
                         .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
    }

    @Override
    public String toString()
    {
        return String.format(
                """
                ScoreId = %d, User Name = %s, Submit Date = %s,
                Correct Count = %d, Error Count = %d, No Answer Count = %d
                """,
                scoreId, userName, submitDate,
                correctCount, errorCount, noAnswerCount
        );
    }
}
