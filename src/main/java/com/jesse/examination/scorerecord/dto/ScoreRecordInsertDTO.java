package com.jesse.examination.scorerecord.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRecordInsertDTO
{
    private Long           userId;

    private LocalDateTime  submitDate;         // 成绩提交日期

    private Integer        correctCount;       // 正确数

    private Integer        errorCount;         // 错误数

    private Integer        noAnswerCount;      // 未答数
}
