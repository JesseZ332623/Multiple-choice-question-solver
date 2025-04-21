package com.jesse.examination.scorerecord.service;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ScoreRecordService
{
    /**
     * 根据指定 ID 查询成绩记录（单条查询）。
     */
    ScoreRecordEntity findScoreRecordById(Integer id);

    /**
     * 增加新成绩记录。
     */
    Integer addNewScoreRecord(ScoreRecordEntity scoreRecord);

    /**
     * 根据指定 ID 删除成绩记录。
     */
    Integer deleteScoreRecordByDate(Integer id);

    /**
     * 获取当前所有的成绩记录，以列表的形式返回。
     */
    List<ScoreRecordEntity> findAllScoreRecord();

    /**
     * 清空 score_record 表。
     */
    Long truncateScoreRecordTable();
}
