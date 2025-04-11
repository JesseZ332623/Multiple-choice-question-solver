package com.jesse.examination.service.questionservice;

import com.jesse.examination.DTO.QuestionInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesse.examination.DTO.QuestionWithCorrectOptionDTO;
import com.jesse.examination.entity.questionentity.QuestionEntity;
import com.jesse.examination.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class QuestionServiceImplement implements QuestionService
{
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImplement(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
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

    @Override
    @Transactional
    public Integer clearCorrectTimesToZero() {
        return this.questionRepository.cleanCorrectTimesToZero();
    }
}
