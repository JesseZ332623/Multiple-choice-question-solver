package com.jesse.examination.redis.controller;

import com.jesse.examination.redis.dto.QuestionPosDTO;
import com.jesse.examination.redis.service.RedisServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.lang.String.format;

@RestController
@RequestMapping(path = "/api/redis", produces = "application/json")
public class RedisController
{
    private final RedisServiceInterface redisService;

    public RedisController(RedisServiceInterface redisService) {
        this.redisService = redisService;
    }

    /**
     * 将用户 userName 的问题答对次数列表中
     * ID 为 questionId 的 值设为 value。
     */
    @PutMapping(path = "/set_correct_times/{value}")
    public ResponseEntity<?>
    setQuestionCorrectTimesById(
            @RequestBody  QuestionPosDTO questionPos,
            @PathVariable Integer        value
    )
    {
        try
        {
            Integer oldValue
                    = this.redisService.setQuestionCorrectTimesById(
                            questionPos, value
                    );

            return ResponseEntity.ok(
                    format(
                            "User: %s updated correctTimes [%d] -> [%d] where question id = [%d].",
                            questionPos.getUserName(), oldValue, value,
                            questionPos.getQuestionId()
                    )
            );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    @PutMapping(path = "/correct_times_plus_one")
    public ResponseEntity<?>
    questionCorrectTimesPlusOneById(
            @RequestBody
            QuestionPosDTO questionPos
    )
    {
        try
        {
            Integer oldValue
                = this.redisService
                      .questionCorrectTimesPlusOneById(questionPos);

            return ResponseEntity.ok(
                    format(
                            "User: %s updated correctTimes [%d] -> [%d] where question id = [%d].",
                            questionPos.getUserName(), oldValue, oldValue + 1,
                            questionPos.getQuestionId()
                    )
            );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }

    @PutMapping(path = "/clean_correct_times/{userName}")
    public ResponseEntity<?>
    cleanAllQuestionCorrectTimesToZero(
            @PathVariable String userName
    )
    {
        try
        {
            this.redisService.cleanAllQuestionCorrectTimesToZero(userName);

            return ResponseEntity.ok(
                    format("Clean correct times from user: %s complete!", userName)
            );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}
