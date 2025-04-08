package com.jesse.examination.repository;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, LocalDateTime> {}