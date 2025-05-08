package com.jesse.examination.user.service.utils;

import com.jesse.examination.file.FileTransferServiceInterface;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.redis.service.RedisServiceInterface;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户存档管理工具类（用户和管理员都会用到）。
 */
@Slf4j
@Service
public class UserArchiveManager
{
    private final FileTransferServiceInterface fileTransferService;
    private final RedisServiceInterface        redisService;
    private final QuestionService              questionService;
    private final ScoreRecordService           scoreRecordService;

    @Autowired
    public UserArchiveManager(
            FileTransferServiceInterface fileTransferService,
            RedisServiceInterface        redisService,
            QuestionService              questionService,
            ScoreRecordService           scoreRecordService
    )
    {
        this.fileTransferService = fileTransferService;
        this.redisService        = redisService;
        this.questionService     = questionService;
        this.scoreRecordService  = scoreRecordService;
    }

    /**
     * 为新用户创建存档数据，redis 数据库中也应该创建对应的数据。
     */
    public void createNewArchiveForUser(String userName)
    {
        this.fileTransferService.saveUserScoreDataFile(
                userName, List.of()
        );

        this.fileTransferService.saveUserCorrectTimesDataFile(
                userName, List.of()
        );

        this.redisService.saveQuestionCorrectTimeList(
                userName,
                RedisServiceInterface.createDefaultQuestionCorrectTimesList(
                        this.questionService.getQuestionCount()
                )
        );
    }

    /**
     * 用户登录时，读取用户的存档信息。
     */
    public void readUserArchive(String userName)
    {
        List<QuestionCorrectTimesDTO> questionCorrectTimesDTOS
                = this.fileTransferService.readUserCorrectTimesDataFile(userName);

        List<ScoreRecordEntity> scoreRecordEntityList
                = this.fileTransferService.readUserScoreDataFile(userName);

        if (!questionCorrectTimesDTOS.isEmpty())
        {
            this.redisService.saveQuestionCorrectTimeList(
                    userName, questionCorrectTimesDTOS
            );
        }
        else
        {
            // 为新用户在 Redis 中写入默认的数据。
            redisService.saveQuestionCorrectTimeList(
                    userName,
                    RedisServiceInterface.createDefaultQuestionCorrectTimesList(
                            this.questionService.getQuestionCount()
                    )
            );
        }

        if (!scoreRecordEntityList.isEmpty()) {
            this.scoreRecordService.saveScoreRecordFromList(scoreRecordEntityList);
        }

        log.info("Read user: {} archive complete.", userName);
    }

    /**
     * 用户登出时，保存用户的存档信息。
     */
    public void saveUserArchive(String userName)
    {
        // 存档用户数据
        List<QuestionCorrectTimesDTO> questionCorrectTimesList
                = this.redisService.readQuestionCorrectTimeList(userName);

        List<ScoreRecordEntity> allScoreList
                = this.scoreRecordService.findAllScoreRecordByUserName(userName);

        // 清空 Redis 中指定的数据
        this.redisService.deleteAllQuestionCorrectTimesByUser(userName);

        this.fileTransferService.saveUserCorrectTimesDataFile(
                userName, questionCorrectTimesList
        );

        this.fileTransferService.saveUserScoreDataFile(
                userName, allScoreList
        );

        log.info("Save user: {} data to file complete.", userName);
    }

    /**
     * 删除用户时，对应的存档也应该一并删除。
     */
    public void deleteUserArchive(String userName)
    {
        this.fileTransferService.deleteUserArchive(userName);
        this.redisService.deleteAllQuestionCorrectTimesByUser(userName);
    }
}
