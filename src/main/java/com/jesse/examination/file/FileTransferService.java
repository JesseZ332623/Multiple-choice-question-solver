package com.jesse.examination.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesse.examination.config.jacksonconfig.JacksonConfig;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FileTransferService
{
    @Value(value = "${file.upload-dir}")
    private String storagePath;     // 文件存储目录（从配置文件中读取）

    private @NotNull QuestionCorrectTimesDTO
    processUserCorrectJsonLine(String line)
    {

        QuestionCorrectTimesDTO questionCorrectTimesDTO = null;

        ObjectMapper mapper = JacksonConfig.createObjectMapper();

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

    private @NotNull ScoreRecordEntity
    processUserScoreRecordJsonLine(String line)
    {
        ScoreRecordEntity termpScoreRecordEntity = null;

        /*
        * 要处理的字符串示例：
        * {
        *       "userName" : "Perter",
        *       "submitDate": "2025-04-30T09:41:01",
        *       "correctCount": 0,
        *       "errorCount": 0,
        *       "noAnswerCount": 321,
        *       "mistakeRate": 100.000000
        * },
        *
        * 需要注意的是，在文件中它们是一整行，这里为了可读性才写成这样。
        */

        // 错了哥，以后我不搓轮子了（哭）
        ObjectMapper mapper = JacksonConfig.createObjectMapper();

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

    private void saveFile(String filePath, String fileName, String fileData) throws IOException
    {
        Path dir = Paths.get(filePath);

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        Path completeFilePath = dir.resolve(fileName);

        Files.writeString(completeFilePath, fileData);
    }

    public void saveUserScoreDataFile(
            String userName,
            @NotNull List<ScoreRecordEntity> allScoreList
    )
    {
        try
        {
            final String  scoreFileName = "score_settlement.json";
            StringBuilder scoreDataJsonBuilder = new StringBuilder();

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
        }
    }

    public void saveUserCorrectTimesDataFile(
            String userName,
            @NotNull List<QuestionCorrectTimesDTO> userData
    )
    {
        try
        {
            // 文件名示例：correct_times.json
            final String  correctTimesFileName    = "correct_times.json";
            StringBuilder correctTimesJsonBuilder = new StringBuilder();

            correctTimesJsonBuilder.append("[\n");

            for (var n : userData)
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
        }
    }

    public List<QuestionCorrectTimesDTO>
    readUserCorrectTimesDataFile (String userName)
    {
        // 文件路径示例：D:/Spring-In-Action/Multiple-choice-question-solver/UserData/Perter/correct_times.json
        String filePath = this.storagePath + "/" +
                          userName + "/correct_times.json";

        List<QuestionCorrectTimesDTO> resultList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.contains("[") || line.contains("]")) { continue; }

                resultList.add(this.processUserCorrectJsonLine(line));
            }
        }
        catch (IOException exception) {
            log.error(exception.getMessage());
        }

        return resultList;
    }

    public List<ScoreRecordEntity>
    readUserScoreDataFile(String userName)
    {
        String filePath
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
        catch (IOException exception) {
            log.error(exception.getMessage());
        }

        // System.out.println("Size of resultList = " + resultList.size());

        return resultList;
    }
}
