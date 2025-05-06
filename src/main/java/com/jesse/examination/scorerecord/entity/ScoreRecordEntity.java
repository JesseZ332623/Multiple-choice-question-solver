package com.jesse.examination.scorerecord.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * score_record 数据表实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "score_record")
public class ScoreRecordEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Integer scoreId;                // 成绩记录 ID

    @Column(name = "user_name")
    @JsonProperty("userName")
    private String userName;                // 该条成绩属于的用户

    @Column(name = "submit_date")
    @JsonProperty("submitDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submitDate;       // 成绩提交日期

    @Column(name = "correct_count")
    @JsonProperty("correctCount")
    private Integer correctCount;           // 正确数

    @Column(name = "error_count")
    @JsonProperty("errorCount")
    private Integer errorCount;             // 错误数

    @Column(name = "no_answer_count")
    @JsonProperty("noAnswerCount")
    private Integer noAnswerCount;          // 未答数

    @Column(name = "mistake_rate")
    @JsonProperty("mistakeRate")
    private Double mistakeRate;             // 错误率

    @Override
    public String toString()
    {
        return String.format(
                """
                {"userName":"%s", "submitDate":"%s", "correctCount":%d, "errorCount":%d, "noAnswerCount":%d, "mistakeRate":%f}
                """,
                getUserName(), submitDate.toString(), getCorrectCount(),
                getErrorCount(), getNoAnswerCount(), getMistakeRate()
        );
    }
}