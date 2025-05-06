package com.jesse.examination.redis.service;

import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Redis 服务，当前 Redis 需要存储的热点数据不多，
 * 当前只存储该用户的所有问题答对次数。
 */
@Slf4j
@Service
public class RedisService
{
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将指定用户的所有问题的答对次数列表存入 Redis。
     *
     * @param userName              用户名，作为存储键
     * @param correctTimesDTOList   该用户所有问题答对次数的列表
     */
    public void saveQuestionCorrectTimeList(
            String userName,
            List<QuestionCorrectTimesDTO> correctTimesDTOList
    )
    {
        this.redisTemplate.opsForList().rightPushAll(
                userName, correctTimesDTOList
        );

        log.info(
                "User: {} save correct time list complete, size: {}.",
                userName, correctTimesDTOList.size()
        );
    }

    /**
     * 获取指定用户的问题答对次数列表。
     *
     * @param userName 用户名作为查询的键
     *
     * @return 查询结果。
     */
    public List<QuestionCorrectTimesDTO>
    readQuestionCorrectTimeList(String userName)
    {
        /*
         * 由于 RedisTemplate 的泛型参数 V 是 Object，
         * 所有这里需要对查询结果做映射操作，然后再组合成一个 List。
         */
        return Objects.requireNonNull(
                        this.redisTemplate.opsForList()
                            .range(userName, 0, -1))
                            .stream()
                            .map(object -> (QuestionCorrectTimesDTO) object)
                            .collect(Collectors.toList()
                        );
    }

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值设为 value
    */
    public Integer
    setQuestionCorrectTimesById(String userName, Integer questionId, Integer value)
    {
        List<QuestionCorrectTimesDTO> queryResult = List.of();

        try {
             queryResult = this.readQuestionCorrectTimeList(userName);
        }
        catch (NullPointerException exception) {
            log.error("[NullPointerException] {}.", exception.getMessage());
        }

        boolean isFound  = false;
        Integer oldValue = 0;

        for (var n : queryResult)
        {
            if (Objects.equals(n.getId(), questionId))
            {
                oldValue = n.getCorrectTimes();
                n.setCorrectTimes(value);

                isFound = true;
                break;
            }
        }

        if (!isFound) {
            throw new IllegalArgumentException(
                    format("questionId: %d not exist in list.", questionId)
            );
        }

        this.redisTemplate.opsForList().set(userName, 0, queryResult);

        log.info(
                "User: {} updated correctTimes {} -> {} where question id = {}",
                userName, oldValue, value, questionId
        );

        return value;
    }

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值 + 1
     */
    public Integer
    questionCorrectTimesPlusOneById(String userName, Integer questionId)
    {
        return 0;
    }

    /**
     * 将用户 userName 的问题答对次数列表中所有用户的答对次数设为 0
     */
    public Integer
    cleanAllQuestionCorrectTimesToZero(String userName)
    {
        return 0;
    }
}
