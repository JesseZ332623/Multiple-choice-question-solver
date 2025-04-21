package com.jesse.examination.scorerecord.service.impl;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.scorerecord.repository.ScoreRecordRepository;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
public class ScoreRecordServiceImplement implements ScoreRecordService
{
    private final ScoreRecordRepository scoreRecordRepository;

    @Autowired
    public ScoreRecordServiceImplement(ScoreRecordRepository scoreRecordRepository) {
        this.scoreRecordRepository = scoreRecordRepository;
    }


    /**
     * 根据指定 ID 查询成绩记录（单条查询）。
     *
     * @param id 成绩 ID
     *
     * @return 查询结果或者抛出 NoSuchElementException 异常。
     */
    @Override
    public ScoreRecordEntity findScoreRecordById(Integer id)
    {
        return this.scoreRecordRepository.findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                String.format(
                                        "[NoSuchElementException] Score ID: {%d} no exist in this table.", id
                                )
                        )
                );
    }

    /**
     * 增加新成绩记录。
     *
     * @param scoreRecord 新的成绩记录
     *
     * @return 返回新插入的数据行 ID
     */
    @Override
    public Integer addNewScoreRecord(ScoreRecordEntity scoreRecord)
    {
        Objects.requireNonNull(scoreRecord);

        return this.scoreRecordRepository.save(scoreRecord).getScoreId();
    }

    /**
     * 根据指定 ID 删除成绩记录。
     *
     * @param id 成绩 ID
     */
    @Override
    @Transactional
    public Integer deleteScoreRecordByDate(Integer id)
    {
        ScoreRecordEntity scoreRecord
                = this.scoreRecordRepository.findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                String.format(
                                        "[NoSuchElementException] Score ID: {%d} no exist in this table.", id
                                )
                        )
                );

        this.scoreRecordRepository.delete(scoreRecord);

        return scoreRecord.getScoreId();
    }

    /**
     * 获取当前所有的成绩记录，以列表的形式返回。
     */
    @Override
    @Transactional
    public List<ScoreRecordEntity> findAllScoreRecord() {
        return this.scoreRecordRepository.findAll();
    }

    /**
     * 清空 score_record 表。
     */
    @Override
    @Transactional
    public Long truncateScoreRecordTable()
    {
        Long currentRows
                = this.scoreRecordRepository.count();
        this.scoreRecordRepository.truncateScoreRecordTable();

        this.scoreRecordRepository.flush();

        return currentRows;
    }
}
