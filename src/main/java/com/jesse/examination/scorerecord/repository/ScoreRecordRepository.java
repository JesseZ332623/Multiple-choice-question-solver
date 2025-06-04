package com.jesse.examination.scorerecord.repository;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, Integer>
{
    /**
     * 查找指定 userName 的所有成绩记录，存于一个列表中。
     */
    @Query(
            value = "SELECT * FROM score_record WHERE(user_name = :userName)",
            nativeQuery = true
    )
    List<ScoreRecordEntity> findAllScoreRecordByUserName(
            @Param(value = "userName") String userName
    );

    /**
     * 删除指定 userName 的所有成绩记录，返回删除的行数。
     */
    @Modifying
    @Query(
            value = "DELETE FROM score_record WHERE(user_name = :userName)",
            nativeQuery = true
    )
    Integer deleteAllScoreRecordByUserName(
            @Param(value = "userName") String userName
    );

    /**
     * 找出指定用户最新的一条成绩记录。
     */
    @Query(value = """
                    SELECT * FROM score_record
                    WHERE user_name = :userName
                          AND
                          submit_date = (SELECT MAX(submit_date) FROM score_record)
                   """,
           nativeQuery = true)
    Optional<ScoreRecordEntity>
    findLatestScoreRecordByName(
        @Param(value = "userName") String userName
    );

    /**
     * 清空 score_record 表。
     */
    @Modifying
    @Query(value = "TRUNCATE score_record", nativeQuery = true)
    void truncateScoreRecordTable();
}