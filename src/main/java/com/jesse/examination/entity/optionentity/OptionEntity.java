package com.jesse.examination.entity.optionentity;

import com.jesse.examination.entity.questionentity.QuestionEntity;
import jakarta.persistence.*;
import lombok.Data;

/**
 * options 数据表实体类。
 */
@Data
@Entity
@Table(name = "options")
public class OptionEntity
{
    // EmbeddedId 注解类表明该表有复合主键。
    @EmbeddedId
    private OptionID id;

    // 选项内容
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 确立选项与题目之间的 “多对一” 关系并映射复合主键中的 question_id 列。
    @ManyToOne
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private QuestionEntity question;
}
