package com.jesse.examination.repository;

import com.jesse.examination.entity.questionentity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>
{
    /**
     * 查询题目，以及它的所有选项。
     * 由于涉及到多表查询，所以需要使用 @Query 注解显式的编写 SQL 语句，
     * 其中 SQL 查询语句的具体解释如下：
     * <li>
     * SELECT 关键字选择 </br>
     * questions.id, questions.content, questions.answer 和
     * JSON_OBJECTAGG(options.option_key, options.content)  </br>
     * 四个字段，其中 JSON_OBJECTAGG() 是一个 SQL 聚合函数，
     * 它将每个题目所有的选项 (options.option_key) 和 选项的内容 (options.content)
     * 一一对应，组合成一个 JSON 对象，该函数仅限 MySQL 5.7+ 版本使用。
     * </li>
     *
     * <li>
     * FROM + INNER JOIN 关键字进行多表关联 </br>
     * INNER JOIN 关键字将 questions 表和 options 表通过外键 question_id 进行关联。 </br>
     * 并且确保只有有选项的题目才会出现在结果之中。
     * </li>
     *
     * <li>
     * GROUP BY 关键字进行分组，本处是依照 questions.id 进行分组。
     * </li>
     * </br>
     * 因此，这个 SQL 语句的功能就是：
     * 查询每个选择题的题目、正确答案，答对次数及其所有的选项，
     * 选项和内容需要整合到一个 JSON 之中。
     * </br>
     * <p>
     * Tips: @Query 注解的 nativeQuery 属性表明是否为本机查询，默认为 false。
     */
    @Query(
            value = """
                    SELECT
                        questions.id            AS QuestionID,
                        questions.content       AS QuestionContent,
                        questions.answer        AS CorrectAnswer,
                        questions.correct_times  AS CorrectTimes,
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
                  SELECT
                       questions.id        AS QuestionID,
                       questions.content   AS QuestionContent,
                       questions.answer    AS CorrectAnswer,
                       options.content     AS CorrectOptionContent
                  FROM questions
                  INNER JOIN options ON options.question_id = questions.answer
                  GROUP BY questions.id
                  """,
            nativeQuery = true
    )
    List<QuestionProjectionWithCorrectOption> findQuestionWithCorrectOption();

    /**
     * 将 question 表中的 correct_times 数据列的值全部设为 0。
     *
     * @return 被更新的记录数
     */
    @Modifying
    @Query(
            value = "UPDATE questions SET correct_times = 0",
            nativeQuery = true
    )
    Integer cleanCorrectTimesToZero();

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
        Integer getCorrectTimes();
    }
}
