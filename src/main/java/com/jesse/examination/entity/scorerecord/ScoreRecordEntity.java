package com.jesse.examination.entity.scorerecord;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * score_record 数据表实体类。
 */
@Data
@Entity
@Table(name = "score_record")
public class ScoreRecordEntity
{
    @Id
    @Column(name = "submit_date")
    private LocalDateTime submitDate;        // 成绩提交日期

    @Column(name = "correct_count")
    private Integer correctCount;   // 正确数

    @Column(name = "error_count")
    private Integer errorCount;     // 错误数

    @Column(name = "no_answer_count")
    private Integer noAnswerCount;  // 未答数

    @Column(name = "mistake_rate")
    private Double mistakeRate;     // 错误率（这个也要存？）
}
