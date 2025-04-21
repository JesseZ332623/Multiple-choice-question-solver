package com.jesse.examination.question.entity.optionentity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据表 options 的复合主键类（被 @Embeddable 注解），
 * 实现 Serializable 接口表明这个类可以被序列化。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OptionID implements Serializable
{
    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "option_key")
    @Enumerated(value = EnumType.STRING)
    private OptionKey optionKey;
}
