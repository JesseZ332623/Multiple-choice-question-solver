package com.jesse.examination.scorerecord.service;

import com.jesse.examination.scorerecord.dto.ScoreRecordInsertDTO;
import com.jesse.examination.scorerecord.dto.ScoreRecordQueryDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ScoreRecordService
{
    /**
     * 根据指定 ID 查询成绩记录（单条查询）。
     */
    ScoreRecordEntity findScoreRecordById(Integer id);

    /**
     * 增加新成绩记录。
     */
    Integer addNewScoreRecord(ScoreRecordInsertDTO scoreRecordInsertDTO);

    /**
     * 根据指定 ID 删除成绩记录。
     */
    Integer deleteScoreRecordByDate(Integer id);

    /**
     * 查找指定 userName 的所有成绩记录，存于一个列表中。
     */
    List<ScoreRecordQueryDTO>
    findAllScoreRecordByUserName(String userName);

    /**
     * 分页的查找查找指定 userName 的所有成绩记录，存于一个列表中。
     */
    Page<ScoreRecordQueryDTO>
    findPaginatedScoreRecordByUserName(
            String userName, Pageable pageable
    );

    /**
     * 删除指定 userName 的所有成绩记录，返回删除的行数。
     */
    Integer deleteAllScoreRecordByUserName(String userName);

    /**
     * 获取当前所有的成绩记录，以列表的形式返回。
     */
    List<ScoreRecordEntity> findAllScoreRecord();

    /**
     * 将 scoreRecordEntities 列表中的所有数据全部存入表中。
     */
    void saveScoreRecordFromList(
            @NotNull
            List<ScoreRecordEntity> scoreRecordEntities
    );

    /**
     * 找出指定用户最新的一条成绩记录。
     */
    Optional<ScoreRecordQueryDTO>
    findLatestScoreRecordByName(String userName);

    /**
     * 清空 score_record 表。
     */
    Long truncateScoreRecordTable();
}
