package com.jesse.examination.question.entity.optionentity;

import lombok.Getter;

/**
 * 选项枚举类。
 */
@Getter
public enum OptionKey
{
    A("A"), B("B"), C("C"), D("D");

    private final String option;

    OptionKey(String option) {
        this.option = option;
    }
}
