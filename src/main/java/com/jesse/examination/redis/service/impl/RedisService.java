package com.jesse.examination.redis.service.impl;

import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.redis.dto.QuestionPosDTO;
import com.jesse.examination.redis.service.RedisServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static com.jesse.examination.redis.keys.ProjectRedisKey.*;

/**
 * Redis 服务，当前 Redis 需要存储的热点数据不多，
 * 当前只存储该用户的所有问题答对次数。
 */
@Slf4j
@Service
public class RedisService implements RedisServiceInterface
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
    @Override
    public void saveQuestionCorrectTimeList(
            String userName,
            List<QuestionCorrectTimesDTO> correctTimesDTOList
    )
    {
        this.redisTemplate.opsForList().rightPushAll(
                CORRECT_TIMES_LIST_KEY + userName,
                correctTimesDTOList
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
     * @return 查询结果（QuestionCorrectTimesDTO 列表）。
     */
    @Override
    public List<QuestionCorrectTimesDTO> readQuestionCorrectTimeList(String userName)
    {
        // 从Redis中获取指定用户的所有列表元素
        List<Object> redisList
                = redisTemplate.opsForList()
                               .range(
                                       CORRECT_TIMES_LIST_KEY + userName,
                                       0, -1
                               );

        // 检查是否存在或空列表
        if (redisList == null || redisList.isEmpty()) {
            return null;
        }

        // 转换元素类型
        List<QuestionCorrectTimesDTO> result = new ArrayList<>(redisList.size());

        for (Object obj : redisList)
        {
            /*
             * 此处 obj 类型是确定的（即 List<QuestionCorrectTimesDTO>），
             * 所有可以使用 @SuppressWarnings 注解来抑制类型转换警告。
             */
            @SuppressWarnings(value = "unchecked")
            boolean isSuccess
                = result.addAll((List<QuestionCorrectTimesDTO>) obj);
        }

        return result;
    }

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值设为 value
    */
    @Override
    public Integer
    setQuestionCorrectTimesById(QuestionPosDTO questionPos, Integer value)
    {
        List<QuestionCorrectTimesDTO> queryResult;

        try {
             queryResult = this.readQuestionCorrectTimeList(questionPos.getUserName());
        }
        catch (NullPointerException exception)
        {
            log.error(
                    "[NullPointerException] setQuestionCorrectTimesById() User: {} not have data Message: {}.",
                    questionPos.getUserName(), exception.getMessage()
            );

            throw new RuntimeException(
                    format("User: %s not have data!", questionPos.getUserName())
            );
        }

        QuestionCorrectTimesDTO questionCorrectTimesDTO
                = queryResult.get(questionPos.getQuestionId() - 1);

        Integer oldValue = questionCorrectTimesDTO.getCorrectTimes();

        questionCorrectTimesDTO.setCorrectTimes(value);

        queryResult.set(questionPos.getQuestionId() - 1, questionCorrectTimesDTO);

        this.redisTemplate.opsForList().set(questionPos.getUserName(), 0, queryResult);

        log.info(
                "setQuestionCorrectTimesById() User: {} updated correctTimes {} -> {} where question id = {}",
                questionPos.getUserName(), oldValue, value, questionPos.getQuestionId()
        );

        return oldValue;
    }

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值 + 1
     */
    @Override
    public Integer
    questionCorrectTimesPlusOneById(QuestionPosDTO questionPos)
    {
        List<QuestionCorrectTimesDTO> queryResult;

        try
        {
            queryResult = this.readQuestionCorrectTimeList(questionPos.getUserName());
        }
        catch (NullPointerException exception)
        {
            log.error(
                    "[NullPointerException] questionCorrectTimesPlusOneById() user: {} not have data Message: {}.",
                    questionPos.getUserName(), exception.getMessage()
            );

            throw new RuntimeException(
                    format("User: %s not have data!", questionPos.getUserName())
            );
        }

        final int questionIndex = questionPos.getQuestionId() - 1;

        QuestionCorrectTimesDTO questionCorrectTimesDTO
                = queryResult.get(questionIndex);

        Integer oldValue = questionCorrectTimesDTO.getCorrectTimes();

        questionCorrectTimesDTO.setCorrectTimes(oldValue + 1);

        queryResult.set(questionIndex, questionCorrectTimesDTO);

        this.redisTemplate.opsForList()
                          .set(
                                  CORRECT_TIMES_LIST_KEY + questionPos.getUserName(),
                                  0, queryResult
                          );

        log.info(
                "questionCorrectTimesPlusOneById() User: {} updated correctTimes {} -> {} where question id = {}",
                questionPos.getUserName(),
                oldValue, oldValue + 1, questionPos.getQuestionId()
        );

        return oldValue;
    }

    /**
     * 将用户 userName 的问题答对次数列表中所有用户的答对次数设为 0
     */
    @Override
    public void
    cleanAllQuestionCorrectTimesToZero(String userName)
    {
        List<QuestionCorrectTimesDTO> queryResult;

        try {
            queryResult = this.readQuestionCorrectTimeList(userName);
        }
        catch (NullPointerException exception)
        {
            log.error(
                    "[NullPointerException] cleanAllQuestionCorrectTimesToZero() user: {} not have data Message: {}.",
                    userName, exception.getMessage()
            );

            throw new RuntimeException(
                    format("User: %s not have data!", userName)
            );
        }

        queryResult.forEach((n) -> n.setCorrectTimes(0));

        this.redisTemplate.opsForList()
                .set(
                        CORRECT_TIMES_LIST_KEY + userName,
                        0, queryResult
                );

        log.info(
                "cleanAllQuestionCorrectTimesToZero() truncate complete. {} rows affected.",
                queryResult.size()
        );
    }

    /**
     * 将用户 userName 的问题答对次数列表从 Redis 中删除。
     */
    @Override
    public void
    deleteAllQuestionCorrectTimesByUser(String userName)
    {
        if (this.redisTemplate.delete(CORRECT_TIMES_LIST_KEY + userName))
        {
            log.info(
                    "deleteAllQuestionCorrectTimesByUser() delete user: {} data complete!",
                    userName
            );
        }
        else
        {
            log.error(
                    "deleteAllQuestionCorrectTimesByUser() user: {} not exist!",
                    userName
            );
        }
    }

    /**
     * 管理员删除某个用户时，删除这个用户所有角色的登录状态。
     */
    @Override
    public void
    deleteUserAllLoginStatusByUserName(String userName)
    {
        if (this.redisTemplate.hasKey(ADMIN_LOGIN_STATUS_KEY.toString())) {
            this.redisTemplate.delete(ADMIN_LOGIN_STATUS_KEY.toString());
        }

        if (this.redisTemplate.hasKey(USER_VERIFYCODE_KEY.toString())) {
            this.redisTemplate.delete(USER_VERIFYCODE_KEY.toString());
        }
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisTemplate;
    }
}
