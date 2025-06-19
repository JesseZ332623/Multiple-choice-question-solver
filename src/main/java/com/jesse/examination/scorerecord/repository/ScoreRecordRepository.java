package com.jesse.examination.scorerecord.repository;

import com.jesse.examination.scorerecord.dto.ScoreRecordQueryDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, Integer>
{
    /**
     * 查找指定 userName 的所有成绩记录，存于一个列表中。
     */
    @Query(
            value = """
                    SELECT score_id, user_name, submit_date,
                           correct_count, error_count, no_answer_count
                    FROM score_record
                    INNER JOIN users USING(user_id)
                    WHERE user_name = :userName
                    """,
            nativeQuery = true
    )
    List<ScoreRecordQueryDTO>
    findAllScoreRecordByUserName(
            @Param(value = "userName") String userName
    );

    /**
     * <p>分页的查找指定 userName 的所有成绩记录，存于一个列表中。</p>
     *
     * <p>
     *     这个 @Query 注解比较特殊，
     *     使用了 JPQL 而非原生 SQL，因此不要设置 nativeQuery = true
     * </p>
     */
    @Query(
            value = """
                    SELECT NEW com.jesse.examination.scorerecord.dto.ScoreRecordQueryDTO(
                        s.scoreId,
                        u.username,
                        s.submitDate,
                        s.correctCount,
                        s.errorCount,
                        s.noAnswerCount
                    )
                    FROM ScoreRecordEntity  s
                    JOIN s.userEntity       u
                    WHERE u.username = :userName
                    """,
            countQuery = """
                         SELECT COUNT(s)
                         FROM ScoreRecordEntity  s
                         JOIN s.userEntity       u
                         WHERE u.username = :userName
                         """
    )
    Page<ScoreRecordQueryDTO>
    findPaginatedScoreRecordByUserName(
            @Param(value = "userName") String userName,
            Pageable pageable
    );

    /**
     * 找出指定用户最新的一条成绩记录。
     */
    @Query(value =  """
            SELECT NEW com.jesse.examination.scorerecord.dto.ScoreRecordQueryDTO(
                s.scoreId,
                u.username,
                s.submitDate,
                s.correctCount,
                s.errorCount,
                s.noAnswerCount
            )
            FROM ScoreRecordEntity s
            JOIN s.userEntity u
            WHERE u.username = :userName
            ORDER BY s.submitDate DESC
            """,
    countQuery = """
                 SELECT COUNT(s)
                 FROM ScoreRecordEntity s
                 JOIN s.userEntity u
                 WHERE u.username = :userName
                 """)
    Page<ScoreRecordQueryDTO>
    findLatestScoreRecordByName(
            @Param(value = "userName") String userName,
            Pageable pageable
    );

    /**
     * 插入指定用户 ID 的一条新成绩。
     */
    @Modifying
    @Query(
            value = """
                    INSERT INTO score_record(
                        user_id, submit_date,
                        correct_count, error_count, no_answer_count
                    )
                    VALUE(
                        :userId, :submitDate,
                        :correctCount, :errorCount, :noAnswerCount
                    )
                    """,
            nativeQuery = true
    )
    void insertNewScoreRecord(
            Long userId, LocalDateTime submitDate,
            Integer correctCount, Integer errorCount,
            Integer noAnswerCount
    );

    /**
     * 删除指定 userName 的所有成绩记录，返回删除的行数。
     */
    @Modifying
    @Query(
            value = """
                    DELETE FROM score_record
                    WHERE user_id IN (
                        SELECT user_id FROM users
                        WHERE user_name = :userName
                    )
                    """,
            nativeQuery = true
    )
    Integer deleteAllScoreRecordByUserName(
            @Param(value = "userName") String userName
    );

    /**
     * 清空 score_record 表。
     */
    @Modifying
    @Query(value = "TRUNCATE score_record", nativeQuery = true)
    void truncateScoreRecordTable();
}