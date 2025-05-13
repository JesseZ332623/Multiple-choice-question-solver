package com.jesse.examination.user.service.utils.impl;

import com.jesse.examination.file.FileTransferServiceInterface;
import com.jesse.examination.file.exceptions.DirectoryRenameException;
import com.jesse.examination.file.exceptions.FileNotExistException;
import com.jesse.examination.file.impl.FileTransferService;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.redis.service.RedisServiceInterface;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import com.jesse.examination.user.service.utils.UserArchiveManagerInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

    /**
     * 获取指定用户头像数据。
     */
    @Override
    public byte[]
    getUserAvatarImage(String userName) throws IOException
    {
        return this.fileTransferService.getUserAvatarImage(userName);
    }

    /**
     * 设置指定用户头像数据。
     */
    @Override
    public void
    setUserAvatarImage(String userName, byte[] imageDataBytes) throws IOException
    {
        this.fileTransferService.writeUserAvatarImage(userName, imageDataBytes);
    }

    @Override
    public void
    renameUserArchiveDir(
            String oldUserName, String newUserName
    ) throws DirectoryRenameException
    {
        this.fileTransferService.renameUserArchiveDir(oldUserName, newUserName);
    }

    /**
     * 为新用户创建存档数据，数据描述如下所示：
     *
     * <ol>
     *      <li>为该用户创建默认的头像</li>
     *      <li>创建用户所有问题答对次数记录文件</li>
     *      <li>将默认的用户所有问题答对次数列表写入 Redis 数据库</li>
     * </ol>
     *
     * @param userName 指定用户名
     */
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

        this.redisService.saveQuestionCorrectTimeList(
                userName,
                RedisServiceInterface.createDefaultQuestionCorrectTimesList(
                        this.questionService.getQuestionCount()
                )
        );


    }

    /**
     * 用户登录时，读取用户的存档信息，具体操作如下所示。
     *
     * <ol>
     *     <li>读取用户所有问题答对次数记录文件</li>
     *     <li>
     *         将该列表整体存入 Redis 数据库中，</br>
     *         键值对是这样的：
     *         <pre>
     *         [key = userName, value = questionCorrectTimesDTOS]
     *         </pre>
     *          在 Redis 服务器中使用 LRANGE userName 0 -1 命令可以查看这个列表。</br>
     *         （这大概不是最好的写法，但是目前能用就先予以保留吧。）
     *     </li>
     * </ol>
     *
     * @param userName 指定用户名
     */
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
        }

        log.info("Read user: {} archive complete.", userName);
    }

    /**
     * 用户登出时，保存用户的存档信息，具体操作如下所示：
     *
     * <ol>
     *     <li>将用户所有问题答对次数列表从 Redis 中读出，写入指定文件中</li>
     *     <li>删除 Redis 数据库中 userName 键对应的整个列表</li>
     * </ol>
     */
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

    /**
     * 删除用户时，对应的存档、数据库记录也应该一并删除，
     * 具体要删除这几类数据：
     *
     * <ol>
     *     <li>删除用户存档数据</li>
     *     <li>删除 Redis 数据库中 userName 键对应的整个列表</li>
     *     <li>删除 score_record 表中所有用户名为 userName 的数据行</li>
     * </ol>
     */
    @Override
    public void deleteUserArchive(String userName)
    {
        this.fileTransferService.deleteUserArchive(userName);
        this.redisService.deleteAllQuestionCorrectTimesByUser(userName);
        this.scoreRecordService.deleteAllScoreRecordByUserName(userName);
    }

    /**
     * 获取内部的 RedisTemplate 进行一些独立的操作，
     * 有点破坏封装性但是可控且值得。
     */
    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisService.getRedisTemplate();
    }
}
