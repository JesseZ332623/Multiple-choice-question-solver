package com.jesse.examination.scorerecord.entity;

import com.jesse.examination.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * score_record 数据表实体类。
 */
@Data
@Entity
@Table(name = "score_record")
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRecordEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id", unique = true, nullable = false)
    private Integer scoreId;            // 成绩记录 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;      // 这些成绩属于用户表中的哪个用户（多对一）

    @Column(name = "submit_date", nullable = false)
    private LocalDateTime submitDate;   // 成绩提交日期

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;       // 正确数

    @Column(name = "error_count", nullable = false)
    private Integer errorCount;         // 错误数

    @Column(name = "no_answer_count", nullable = false)
    private Integer noAnswerCount;      // 未答数

    @Override
    public String toString()
    {
        return String.format(
                """
                Score ID = %d, User ID = %d, Submit Date = %s,
                Correct Count = %d, Error Count = %d, No Answer Count = %d
                """,
                scoreId, userEntity.getId(), submitDate.toString(),
                correctCount, errorCount, noAnswerCount
        );
    }
}