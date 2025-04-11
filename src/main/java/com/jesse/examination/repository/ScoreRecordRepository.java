package com.jesse.examination.repository;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, LocalDateTime>
{
    /**
     * 清空 score_record 表。
     */
    @Modifying
    @Query(value = "TRUNCATE score_record", nativeQuery = true)
    void truncateScoreRecordTable();
}