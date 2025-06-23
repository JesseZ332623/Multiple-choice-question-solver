package com.jesse.examination.user.service.utils.impl;

import com.jesse.examination.file.FileTransferServiceInterface;
import com.jesse.examination.file.exceptions.DirectoryRenameException;
import com.jesse.examination.file.impl.FileTransferService;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.redis.service.RedisServiceInterface;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import com.jesse.examination.user.service.utils.UserArchiveManagerInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * 用户存档管理工具类（用户和管理员都会用到）。
 */
@Slf4j
@Service
public class UserArchiveManager implements UserArchiveManagerInterface
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

    @Override
    public byte[] getDefaultAvatarImage()
    {
        return FileTransferService.getDefaultAvatarImageData();
    }

    @Override
    public byte[]
    getUserAvatarImage(String userName) throws IOException
    {
        return this.fileTransferService.getUserAvatarImage(userName);
    }

    @Override
    public void
    setUserAvatarImage(String userName, byte[] imageDataBytes) throws IOException
    {
        this.fileTransferService.writeUserAvatarImage(userName, imageDataBytes);
    }

    @Override
    public void
    renameUserArchiveDir(
            @NotNull String oldUserName, String newUserName
    ) throws DirectoryRenameException
    {
        // 倘若新旧用户名的值完全相同，自然也没有更改路径之必要。
        if (oldUserName.equals(newUserName)) {
            return;
        }

        this.fileTransferService.renameUserArchiveDir(oldUserName, newUserName);
    }


    @Override
    public void createNewArchiveForUser(String userName) throws IOException
    {
        this.fileTransferService.writeUserAvatarImage(
                userName,
                FileTransferService.getDefaultAvatarImageData()
        );

        this.fileTransferService.saveUserCorrectTimesDataFile(
                userName,
                RedisServiceInterface.createDefaultQuestionCorrectTimesList(
                        this.questionService.getQuestionCount()
                )
        );
    }

    @Override
    public void readUserArchive(String userName)
    {
        List<QuestionCorrectTimesDTO> questionCorrectTimesDTOS
                = this.fileTransferService.readUserCorrectTimesDataFile(userName);

        if (!questionCorrectTimesDTOS.isEmpty())
        {
            this.redisService.saveQuestionCorrectTimeList(
                    userName, questionCorrectTimesDTOS
            );

            log.info("Read user: {} archive complete.", userName);
        }
        else
        {
            throw new RuntimeException(format("Read %s's archive failed.", userName));
        }
    }

    @Override
    public void saveUserArchive(String userName)
    {
        // 存档用户数据
        this.fileTransferService.saveUserCorrectTimesDataFile(
                userName,
                this.redisService.readQuestionCorrectTimeList(userName)
        );

        // 清空 Redis 中指定的数据
        this.redisService.deleteAllQuestionCorrectTimesByUser(userName);

        log.info("Save user: {} data to file complete.", userName);
    }

    @Override
    public void deleteUserArchive(String userName)
    {
        this.fileTransferService.deleteUserArchive(userName);
        this.redisService.deleteAllQuestionCorrectTimesByUser(userName);
        this.redisService.deleteUserAllLoginStatusByUserName(userName);
        this.scoreRecordService.deleteAllScoreRecordByUserName(userName);
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisService.getRedisTemplate();
    }
}
