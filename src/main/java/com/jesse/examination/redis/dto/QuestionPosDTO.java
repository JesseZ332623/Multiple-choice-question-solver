package com.jesse.examination.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于定位问题的位置，比如是哪个用户 id 为多少的问题。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPosDTO
{
    private String  userName;
    private Integer questionId;
}
