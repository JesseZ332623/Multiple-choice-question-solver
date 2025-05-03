package com.jesse.examination.question.service.impl;

import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.dto.QuestionInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesse.examination.question.dto.QuestionWithCorrectOptionDTO;
import com.jesse.examination.question.entity.questionentity.QuestionEntity;
import com.jesse.examination.question.repository.QuestionRepository;
import com.jesse.examination.question.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class QuestionServiceImplement implements QuestionService
{
    private final QuestionRepository questionRepository;

    /**
     * 业务场景中出现了平凡更新表数据的情况，如果使用 JPA 去一条条执行，
     * 真的是慢到令人发指，所用需要使用 JdbcTemplate batchUpdate() 方法提提速。
     */
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public QuestionServiceImplement(
            QuestionRepository questionRepository,
            JdbcTemplate jdbcTemplate)
    {
        this.questionRepository = questionRepository;
        this.jdbcTemplate       = jdbcTemplate;
    }

    /**
     * 将 JSON 文本反序列化为一个个键值对，存储于 Map<String, Object> 之中。
     * JSON 文本可能的格式如下：</br>
     * <pre>
     * {
     *   "A": "马克思主义基本原理同中国具体实际相合",
     *   "B": "马克思主义基本原理同中华优秀传统文化相结合",
     *   "C": "马克思主义基本本理同中国综合国力相结合",
     *   "D": "马克思主义基本原理同中华传统文化相结合"
     * }
     * </pre></br>
     *
     * 反序列化后为：</br>
     *      {"A", "马克思主义基本原理同中国具体实际相合"},        </br>
     *      {"B", "马克思主义基本原理同中华优秀传统文化相结合"},  </br>
     *      {"C", "马克思主义基本本理同中国综合国力相结合"},      </br>
     *      {"D", "马克思主义基本原理同中华传统文化相结合"}       </br>
     */
    private Map<String, Object> parseOptionsJson(String json)
    {
        try
        {
            /*
            * ObjectMapper().readValue() 方法用于反序列化 JSON 文本，
            * 而 TypeReference 作为 Jackson 的工具类，
            * 用于 “绕过” Java 编译时对于泛型参数的类型擦除 (Type Erasure)，具体做法如下：
            *
            * 1. 定义泛型类型，比如此处最完整的写法应该是：
            *       TypeReference<Map<String, Object>>
            *    但通常泛型参数可以被省略。
            *
            * 2. 创建匿名子类：
            *       new TypeReference<Map<String, Object>>() {}
            *    表达式创建了一个继承自 TypeReference 的匿名子类，其中泛型的信息被保存其中。
            *
            * 3. Jackson 通过反射读取这个匿名子类的 getType() 方法，
            *    由此 “绕过” 编译时的类型擦除，获取完整的泛型信息。
            * */
            return new ObjectMapper().readValue(json, new TypeReference<>() {});
        }
        catch (JsonProcessingException exception)
        {
            /*
                在反序列化 JSON 文本时出现问题会抛出 JsonProcessingException 异常，
                此处将保留异常信息再次抛出 RuntimeException 异常向上传递。
            */
            throw new RuntimeException(
                    String.format(
                            "[RuntimeException] Processing JSON data failed, ERROR: %s.",
                            exception.getMessage()
                    )
            );
        }
    }

    @Override
    public List<QuestionInfoDTO> getAllQuestionInfo()
    {
        List<QuestionRepository.QuestionProjection> questionInfo
                = this.questionRepository.findQuestionWithAllOptions();

        /*
         * 将查询的结果（一个列表类型）化成字节流 (调用 stream() 方法)，
         * 对于流中的每一个元素，按照提供的 Lamba 表达式映射成 QuestionInfoDTO 记录类的实例 (调用 map() 方法)，
         * 最后调用 toList() 方法转化成列表返回。
         */
        return questionInfo.stream()
                           .map(
                                   projection ->
                                              new QuestionInfoDTO(
                                                      projection.getQuestionID(),
                                                      projection.getQuestionContent(),
                                                      projection.getCorrectAnswer(),
                                                      projection.getCorrectTimes(),
                                                      this.parseOptionsJson(projection.getOptionsJSON())
                                              )
                              ).toList();
    }

    /**
     * 获取所有问题的答对次数，存储在一个不可变列表中。
     */
    @Override
    public List<QuestionCorrectTimesDTO> getAllQuestionCorrectTimes()
    {
        return this.questionRepository.findAllQuestionCorrectTimes();
    }

    @Override
    public List<QuestionWithCorrectOptionDTO> getAllQuestionWithCorrectOption()
    {
        List<QuestionRepository.QuestionProjectionWithCorrectOption> allQuestionWithCorrectOption =
                this.questionRepository.findQuestionWithCorrectOption();

        return allQuestionWithCorrectOption.stream()
                                           .map(projection ->
                                                   new QuestionWithCorrectOptionDTO(
                                                           projection.getQuestionID(),
                                                           projection.getQuestionContent(),
                                                           projection.getCorrectAnswer(),
                                                           projection.getCorrectOptionContent()
                                                   )
                                           ).toList();
    }

    @Override
    @Transactional
    public Integer correctTimesPlusOneById(Integer id)
    {
        Optional<QuestionEntity> questionOptional = this.questionRepository.findById(id);

        if (questionOptional.isPresent())
        {
            QuestionEntity question = questionOptional.get();
            question.setCorrectTimes(question.getCorrectTimes() + 1);

            this.questionRepository.save(question);

            return question.getCorrectTimes();
        }
        else
        {
            throw new RuntimeException(
                    String.format(
                            "[RuntimeException] id = {%d} not exist in questions table.", id
                    )
            );
        }
    }

    /**
     * 将 questions 表中指定 id 对应的数据行的
     * current_times 的值设为 value。
     */
    @Override
    @Transactional
    public void setOneCorrectTimesById(Integer id, Integer value)
    {
        QuestionEntity queryRes
                = this.questionRepository.findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException(format("ID = {%d} not exist!", id))
                );

        if (value < 0)
        {
            throw new IllegalArgumentException(
                    format("value which = %d not less than 0", value)
            );
        }

        queryRes.setCorrectTimes(value);

        this.questionRepository.save(queryRes);
    }


    @Override
    @Transactional
    public int[] setAllCorrectTimesByIds(
            @NotNull
            List<QuestionCorrectTimesDTO> correctTimesDTOList
    )
    {
        String sql = "UPDATE questions SET correct_times = ? WHERE id = ?";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
        {
            @Override
            public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException
            {
                QuestionCorrectTimesDTO update = correctTimesDTOList.get(i);
                ps.setInt(1, update.getCorrectTimes());
                ps.setInt(2, update.getId());
            }

            @Override
            public int getBatchSize() { return correctTimesDTOList.size(); }
        });
    }

    @Override
    @Transactional
    public Integer clearCorrectTimesToZero() {
        return this.questionRepository.cleanCorrectTimesToZero();
    }
}
