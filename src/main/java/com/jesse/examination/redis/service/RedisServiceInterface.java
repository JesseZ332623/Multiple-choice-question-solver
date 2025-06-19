package com.jesse.examination.redis.service;

import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.redis.dto.QuestionPosDTO;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

public interface RedisServiceInterface
{
    /**
     * 工厂方法，为新用户创建一个默认的问题答对次数列表。
     */
    @Contract(pure = true)
    static @NotNull List<QuestionCorrectTimesDTO>
    createDefaultQuestionCorrectTimesList(Long count)
    {
        ArrayList<QuestionCorrectTimesDTO> questionCorrectTimes
                = new ArrayList<>();

        for (int index = 1; index <= count; ++index) {
            questionCorrectTimes.add(new QuestionCorrectTimesDTO(index, 0));
        }

        return questionCorrectTimes;
    }

    /**
     * 将指定用户的所有问题的答对次数列表存入 Redis。
     *
     * @param userName              用户名，作为存储键
     * @param correctTimesDTOList   该用户所有问题答对次数的列表
     */
    void saveQuestionCorrectTimeList(
            String userName,
            List<QuestionCorrectTimesDTO> correctTimesDTOList
    );

    /**
     * 获取指定用户的问题答对次数列表。
     *
     * @param userName 用户名作为查询的键
     *
     * @return 查询结果。
     */
    List<QuestionCorrectTimesDTO>
    readQuestionCorrectTimeList(String userName);

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值设为 value。
     */
    Integer setQuestionCorrectTimesById(
            QuestionPosDTO questionPos, Integer value
    );

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值 + 1。
     */
    Integer questionCorrectTimesPlusOneById(
            QuestionPosDTO questionPos
    );

    /**
     * 将用户 userName 的问题答对次数列表中所有用户的答对次数设为 0。
     */
    void cleanAllQuestionCorrectTimesToZero(String userName);

    /**
     * 将用户 userName 的问题答对次数列表从 Redis 中删除。
     */
    void deleteAllQuestionCorrectTimesByUser(String userName);

    /**
     * 管理员删除某个用户时，删除这个用户所有角色的登录状态。
     */
    void deleteUserAllLoginStatusByUserName(String userName);

    /**
     * 获取内部的 RedisTemplate，执行一些独立的操作。
     */
    RedisTemplate<String, Object> getRedisTemplate();
}
