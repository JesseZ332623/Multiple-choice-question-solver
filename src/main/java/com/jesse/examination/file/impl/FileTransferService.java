package com.jesse.examination.file.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesse.examination.config.jacksonconfig.JacksonConfig;
import com.jesse.examination.file.FileTransferServiceInterface;
import com.jesse.examination.file.exceptions.DirectoryRenameException;
import com.jesse.examination.file.exceptions.FileNotExistException;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@Service
public class FileTransferService implements FileTransferServiceInterface
{
    @Value(value = "${file.upload-dir}")
    private String storagePath;     // 文件存储目录（从配置文件中读取）

    // 创建带有时间字符串解析的 ObjectMapper 实例。
    private final ObjectMapper mapper = JacksonConfig.createObjectMapper();

    // 获取默认头像图片的字节数组。
    private static final byte[] DEFAULT_AVATAR_IMAGE_DATA;

    /*
     * 终于把静态块用出来了，一些被 static 修饰的实例在初始化时也可能抛出异常，
     * 此时就需要 静态块 去捕获。
     * 这样在 Spring 应用启动的时候，如果资源加载失败，应用在输出异常信息后也会停止。
     */
    static
    {
        try
        {
            DEFAULT_AVATAR_IMAGE_DATA = Files.readAllBytes(
                    Paths.get(
                            "D:/Spring-In-Action/Multiple-choice-question-solver" +
                                 "/src/main/resources/image/avatar.png"
                    )
            );
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(
                    "[RuntimeException] " + exception.getMessage()
            );
        }
    }

    /**
     * 获取默认头像图片的字节数组。
     */
    public static byte[] getDefaultAvatarImageData()
    {
        return Objects.requireNonNull(DEFAULT_AVATAR_IMAGE_DATA);
    }

    /**
     * 使用 Jackson 库，将 correct_times.json 文件中的一行 JSON 文本映射成一个
     * QuestionCorrectTimesDTO 实例，JSON 文本示例如下：
     *
     * <pre>
     *     {"id" : 21, "correctTimes" : 0},
     * </pre>
     */
    private @NotNull QuestionCorrectTimesDTO
    processUserCorrectJsonLine(String line)
    {

        QuestionCorrectTimesDTO questionCorrectTimesDTO = null;

        try
        {
            questionCorrectTimesDTO
                    = mapper.readValue(line, QuestionCorrectTimesDTO.class);
        }
        catch (JsonProcessingException exception) {
            log.error(exception.getMessage());
        }

        return Objects.requireNonNull(questionCorrectTimesDTO);
    }

    /**
     * 使用 Jackson 库，将 score_settlement.json 文件中的一行 JSON 文本映射成一个 ScoreRecordEntity。
     * JSON 文本示例如下（文件中直接是一整行显示，这里为了可读性做了格式化）：
     *
     * <pre>
     *     {
     *          "userName"      : "Jesse",
     *          "submitDate"    : "2025-05-06T03:50:40",
     *          "correctCount"  : 1,
     *          "errorCount"    : 0,
     *          "noAnswerCount" : 320,
     *          "mistakeRate"   : 99.690000
     *     },
     * </pre>
     */
    private @NotNull ScoreRecordEntity
    processUserScoreRecordJsonLine(String line)
    {
        ScoreRecordEntity termpScoreRecordEntity = null;

        try
        {
            termpScoreRecordEntity = mapper.readValue(line, ScoreRecordEntity.class);
        }
        catch (JsonProcessingException exception)
        {
            log.error(exception.getMessage());
        }

        return Objects.requireNonNull(termpScoreRecordEntity);
    }

    /**
     * 重命名用户存档路径（修改用户账户信息时用）
     *
     * @param oldPathName 旧路径名，需要确保其存在
     * @param newPathName 新路径名，需要确保其不存在（√）
     *
     * @throws DirectoryRenameException 处理掉内部的异常后，
     *                                  会 re-throw 一个 DirectoryRenameException 向上传递，
     *                                  告知操作失败。
     */
    private void
    renameDirectory(String oldPathName, String newPathName) throws DirectoryRenameException
    {
        Path oldPath = Paths.get(oldPathName);
        Path newPath = Paths.get(newPathName);

        if (!Files.exists(oldPath))
        {
            throw new IllegalArgumentException(
                    format("source path: %s not exist!", oldPathName)
            );
        }

        try
        {
            /*
             * 检查新路径的父级路径是否存在，
             * 否则就递归的创建所有缺失的父目录。
             */
            Files.createDirectories(newPath.getParent());

            /*
             * 新旧路径校验完后，进行文件的移动操作，Files.move() 方法在不同情况下的行为也不同：
             *
             *  case 1：若同盘移动则仅修改文件的元数据，速度极快。
             *  case 2: 若跨盘移动则触发文件的复制，在目录层级很深的时候有性能问题。
             *
             * 这里还需要解释下面两个参数：
             *
             * 1. StandardCopyOption.REPLACE_EXISTING
             *      在操作路径时，若目标路径已经存在，则直接抛出 FileAlreadyExistsException
             *
             * 2. StandardCopyOption.ATOMIC_MOVE
             *      将该移动操作视为一个整体（原子化），如果期间出现任何失败，则都会回滚操作。
             *
             * 但当前项目的对该方法的使用不涉及跨盘的操作，所以不需要 REPLACE_EXISTING 参数。
             */
            Files.move(
                    oldPath, newPath,
                    // StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

            log.info("Rename directory [{}] -> [{}].", oldPathName, newPathName);
        }
        catch (FileAlreadyExistsException exception)
        {
            String errorMessage = format(
                    "New directory already exist, cause: %s.",
                    exception.getMessage()
            );

            log.error(errorMessage);

            throw new DirectoryRenameException(errorMessage);
        }
        catch (IOException exception)
        {
            String errorMessage = format(
                    "[%s] Failed operator cause: %s.",
                    exception.getClass().getSimpleName(),
                    exception.getMessage()
            );

            log.error(errorMessage);

            throw new DirectoryRenameException(errorMessage);
        }
    }

    /**
     * 文本文件存储通用方法。
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param fileData 文件数据（通常是 JSON 字符串）
     */
    private void saveFile(String filePath, String fileName, String fileData) throws IOException
    {
        Path dir = Paths.get(filePath);

        // 检查路径是否存在，没有则创建。
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // 创建文件
        Path completeFilePath = dir.resolve(fileName);

        // 写入数据
        Files.writeString(
                completeFilePath, fileData,
                StandardOpenOption.CREATE,              // 不存在则创建
                StandardOpenOption.TRUNCATE_EXISTING,   // 存在则清空
                StandardOpenOption.WRITE                // 写入模式
        );
    }

    /**
     * 非文本文件存储通用方法。
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @param fileByteData 文件数据（这里是字节数组）
     */
    private void
    saveFile(String filePath, String fileName, byte[] fileByteData) throws IOException
    {
        Path dir = Paths.get(filePath);

        // 检查路径是否存在，没有则创建。
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // 创建文件
        Path completeFilePath = dir.resolve(fileName);

        // 将数据写入文件
        Files.write(
                completeFilePath, fileByteData,
                StandardOpenOption.CREATE,              // 不存在则创建
                StandardOpenOption.TRUNCATE_EXISTING,   // 存在则清空
                StandardOpenOption.WRITE                // 写入模式
        );
    }

    @Override
    public byte[] getUserAvatarImage(String userName) throws IOException
    {
        // 头像存储路径
        String avatarPath = this.storagePath + "/" + userName + "/" + "avatar.png";

        Path image = Paths.get(avatarPath);

        if (!Files.exists(image))
        {
            throw new FileNotExistException(
                    "[FileNotExistException] Path: " + avatarPath + " NOT EXIST!"
            );
        }

        return Files.readAllBytes(image);
    }

    @Override
    public void writeUserAvatarImage(String userName, byte[] imageBytes) throws IOException
    {
        String avatarDirStr  = this.storagePath + "/" + userName + "/";
        String avatarName    = "avatar.png";

        Path avatarDir = Paths.get(avatarDirStr);

        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }

        this.saveFile(avatarDirStr, avatarName, imageBytes);
    }

    @Override
    public void
    renameUserArchiveDir(
            String oldUserName, String newUserName
    ) throws DirectoryRenameException
    {
        this.renameDirectory(
                this.storagePath  + "/" + oldUserName,
                this.storagePath + "/" + newUserName
        );
    }

    @Override
    public void saveUserScoreDataFile(
            String userName,
            @NotNull List<ScoreRecordEntity> allScoreList
    )
    {
        try
        {
            final String  scoreFileName = "score_settlement.json";
            StringBuilder scoreDataJsonBuilder = new StringBuilder();

            // 手动将数据拼合成 JSON 文件（笑）
            scoreDataJsonBuilder.append("[\n");

            for (var n : allScoreList) {
                scoreDataJsonBuilder.append("\t")
                        .append(n.toString(), 0, n.toString().length() - 1)
                        .append(",\n");
            }

            int lastComma;

            if ((lastComma = scoreDataJsonBuilder.lastIndexOf(",")) != -1) {
                scoreDataJsonBuilder.deleteCharAt(lastComma);
            }

            scoreDataJsonBuilder.append("]\n");

            this.saveFile(
                    this.storagePath + "/" + userName,
                    scoreFileName,
                    scoreDataJsonBuilder.toString()
            );
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * 存储用户问题答对次数文件。
     *
     * @param userName                  用户名（作为文件路径的根据）
     * @param questionCorrectTimesList  成绩数据列表
     */
    @Override
    public void saveUserCorrectTimesDataFile(
            String userName,
            @NotNull List<QuestionCorrectTimesDTO>
            questionCorrectTimesList
    )
    {
        try
        {
            // 文件名示例：correct_times.json
            final String  correctTimesFileName    = "correct_times.json";
            StringBuilder correctTimesJsonBuilder = new StringBuilder();

            correctTimesJsonBuilder.append("[\n");

            for (var n : questionCorrectTimesList)
            {
                correctTimesJsonBuilder.append("\t")
                       .append(n.toString(), 0, n.toString().length() - 1)
                       .append(",\n");
            }

            int lastComma;

            if ((lastComma = correctTimesJsonBuilder.lastIndexOf(",")) != -1) {
                correctTimesJsonBuilder.deleteCharAt(lastComma);
            }

            correctTimesJsonBuilder.append("]\n");

            this.saveFile(
                    storagePath + "/" + userName,
                    correctTimesFileName,
                    correctTimesJsonBuilder.toString()
            );
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * 通过指定用户名读取该用户的所有问题答对次数。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
    @Override
    public List<QuestionCorrectTimesDTO>
    readUserCorrectTimesDataFile (String userName)
    {
        /*
        * 文件路径示例：
        * D:/Spring-In-Action/Multiple-choice-question-solver/UserData/Perter/correct_times.json
        */
        final String filePath = this.storagePath +
                                "/"              +
                                userName         +
                                "/correct_times.json";

        List<QuestionCorrectTimesDTO> resultList = new ArrayList<>();

        // 使用 try-with-resource 如果存在用户不存在的情况也能处理
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.contains("[") || line.contains("]")) { continue; }

                resultList.add(this.processUserCorrectJsonLine(line));
            }
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            // 需要重新抛出异常（向上传递给控制器）
            throw new RuntimeException(exception.getMessage());
        }

        return resultList;
    }

    /**
     * 通过指定用户名读取该用户的所有答题成绩。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
    @Override
    public List<ScoreRecordEntity>
    readUserScoreDataFile(String userName)
    {
        final String filePath
                = this.storagePath + "/" + userName + "/score_settlement.json";

        List<ScoreRecordEntity> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.contains("[") || line.contains("]")) { continue; }

                resultList.add(this.processUserScoreRecordJsonLine(line));
            }
        }
        catch (IOException exception)
        {
            log.error(exception.getMessage());

            throw new RuntimeException(exception.getMessage());
        }

        return resultList;
    }

    /**
     * 在用户删除账户的同时删除存档数据。
     *
     * @param userName 用户名（作为文件路径的根据）
     */
    @Override
    public void deleteUserArchive(String userName)
    {
        // 指定文件路径（示例：${file.upload-dir}/Perter/）
        final String filePath
                = this.storagePath + "/" + userName + "/";

        boolean isSuccess
                = FileSystemUtils.deleteRecursively(
                        Paths.get(filePath).toFile()
                );

        if (!isSuccess)
        {
            String errorMessage
                    = format("Failed to delete directory: [%s]!", filePath);

            log.error(errorMessage);

            throw new RuntimeException(errorMessage);
        }

        log.info("Delete directory {} complete!", filePath);
    }
}
