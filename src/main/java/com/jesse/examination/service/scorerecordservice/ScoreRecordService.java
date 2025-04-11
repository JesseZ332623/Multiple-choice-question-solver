package com.jesse.examination.service.scorerecordservice;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ScoreRecordService
{
    /**
     * 根据指定日期查询成绩记录（单条查询）。
     */
    ScoreRecordEntity findScoreRecordByDateTime(LocalDateTime dateTime);

    /**
     * 增加新成绩记录。
     */
    LocalDateTime addNewScoreRecord(ScoreRecordEntity scoreRecord);

    /**
     * 根据指定日期删除成绩记录。
     */
    LocalDateTime deleteScoreRecordByDate(LocalDateTime date);

    /**
     * 获取当前所有的成绩记录，以列表的形式返回。
     */
    List<ScoreRecordEntity> findAllScoreRecord();

    /**
     * 清空 score_record 表。
     */
    void truncateScoreRecordTable();
}
