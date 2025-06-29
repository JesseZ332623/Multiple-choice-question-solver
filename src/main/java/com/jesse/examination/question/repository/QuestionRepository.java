package com.jesse.examination.question.repository;

import com.jesse.examination.question.entity.questionentity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>
{
    /**
     * 查询题目，以及它的所有选项。
     * 由于涉及到多表查询，所以需要使用 @Query 注解显式的编写 SQL 语句。
     * <ul>
     *     <li>
     *          其中 <code>JSON_OBJECTAGG()</code> 是一个 SQL 聚合函数，
     *          它将每个题目所有的选项 (options.option_key) 和 选项的内容 (options.content)
     *          一一对应，组合成一个 JSON 对象，该函数仅限 MySQL 5.7+ 版本使用。
     *      </li>
     * </ul>
     *
     * <strong>
     *     Tips: @Query 注解的 nativeQuery 属性表明是否为原生 SQL 查询，
     *     默认为 false ，表示将传入的 SQL 语句按 JPA 的 JPQL 的语法进行解析，
     *     反之则按 SQL 的语法进行解析。
     * </strong>
     */
    @Query(
            value = """
                    SELECT
                        questions.id      AS QuestionID,
                        questions.content AS QuestionContent,
                        questions.answer  AS CorrectAnswer,
                        JSON_OBJECTAGG(options.option_key, options.content) AS OptionsJSON
                    FROM questions
                    INNER JOIN options ON questions.id = options.question_id
                    GROUP BY questions.id
                    """,
            nativeQuery = true
    )
    List<QuestionProjection> findQuestionWithAllOptions();

    /**
     * 查询所有题目的 id、正确选项、答案内容。
     */
    @Query(
            value = """
                    SELECT questions.id      AS questionId,
                           questions.content AS questionContent,
                           questions.answer  AS correctAnswer,
                           options.content   AS correctOptionContent
                    FROM questions
                    INNER JOIN options
                    ON questions.id = options.question_id
                    AND
                    questions.answer = options.option_key
                  """,
            nativeQuery = true
    )
    List<QuestionProjectionWithCorrectOption> findQuestionWithCorrectOption();

    /**
     * 往问题表中插入一个新的问题和正确答案。
     *
     * @param content 问题内容
     * @param answer  问题答案
     *
     * @return 返回受影响的行数
     */
    @Modifying
    @Query(
            value = """
                    INSERT INTO questions(content, answer)
                    VALUES (:questionContent, :questionAnswer)
                    """,
            /* 此处是 SQL 原生查询，
             * 所以必须使用 nativeQuery = true 告知 JPA。
             */
            nativeQuery = true
    )
    Integer addNewQuestion(
            @Param(value = "questionContent") String content,
            @Param(value = "questionAnswer")  String answer
    );

    interface QuestionProjectionWithCorrectOption
    {
        Integer     getQuestionID();
        String      getQuestionContent();
        String      getCorrectAnswer();
        String      getCorrectOptionContent();
    }

    interface QuestionProjection
    {
        Integer getQuestionID();
        String  getQuestionContent();
        String  getCorrectAnswer();
        String  getOptionsJSON();
    }
}
