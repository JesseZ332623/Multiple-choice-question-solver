package com.jesse.examination.question.entity.questionentity;

import com.jesse.examination.question.entity.optionentity.OptionEntity;
import com.jesse.examination.question.entity.optionentity.OptionKey;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * question 数据表实体类。
 */
@Data
@Entity
@Table(name = "questions")
public class QuestionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;                                     // 问题 ID

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;                                 // 问题内容

    @Column(name = "answer")
    @Enumerated(value = EnumType.STRING)
    private OptionKey answer;                               // 问题答案

    // 问题选项（一个问题有多个选项）
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<OptionEntity> options;
}
