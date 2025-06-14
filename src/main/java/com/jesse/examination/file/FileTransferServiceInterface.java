package com.jesse.examination.file;

import com.jesse.examination.file.exceptions.DirectoryRenameException;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * 文件传输服务接口。
 */
public interface FileTransferServiceInterface
{
    /**
     * 读取指定用户存档的头像数据（以字节数组的形式返回）。
     */
    byte[] getUserAvatarImage(String userName) throws IOException;

    /**
     * 将头像数据写入指定用户的存档。
     */
    void writeUserAvatarImage(String userName, byte[] imageBytes) throws IOException;

    /**
     * 重命名用户存档路径（修改用户账户信息时用）。
     */
    void renameUserArchiveDir(String oldUserName, String newUserName) throws DirectoryRenameException;

    /**
     * 存储用户成绩记录文件。
     *
     * @param userName     用户名（作为文件路径的根据）
     * @param allScoreList 成绩数据列表
     */
    void saveUserScoreDataFile(
            String userName,
            @NotNull List<ScoreRecordEntity> allScoreList
    );

    /**
     * 存储用户问题答对次数文件。
     *
     * @param userName                  用户名（作为文件路径的根据）
     * @param questionCorrectTimesList  成绩数据列表
     */
    void saveUserCorrectTimesDataFile(
            String userName,
            @NotNull List<QuestionCorrectTimesDTO>
                    questionCorrectTimesList
    );

    /**
     * 通过指定用户名读取该用户的所有问题答对次数。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
    List<QuestionCorrectTimesDTO>
    readUserCorrectTimesDataFile(String userName);

    /**
     * 通过指定用户名读取该用户的所有答题成绩。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
    List<ScoreRecordEntity>
    readUserScoreDataFile(String userName);

    /**
     * 在用户删除账户的同时删除存档数据。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
     void deleteUserArchive(String userName);
}
