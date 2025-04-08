package com.jesse.examination.service.scorerecordservice;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;
import com.jesse.examination.repository.ScoreRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * 根据指定日期查询成绩记录（单条查询）。
     */
    @Override
    public ScoreRecordEntity findScoreRecordByDateTime(LocalDateTime datetime)
    {
        return this.scoreRecordRepository.findById(datetime)
                                         .orElseThrow();
    }

    /**
     * 增加新成绩记录。
     */
    @Override
    @Transactional
    public LocalDateTime addNewScoreRecord(@NotNull ScoreRecordEntity scoreRecord)
    {
        Optional<ScoreRecordEntity> scoreRecordQueryResultOptional
                = this.scoreRecordRepository.findById(scoreRecord.getSubmitDate());

        if (scoreRecordQueryResultOptional.isPresent())
        {
            throw new IllegalStateException(
                        String.format(
                                "[IllegalStateException] Submit Date: [%s] has been exist in this table.",
                                scoreRecord.getSubmitDate().toString()
                        )
                );
        }

        return this.scoreRecordRepository.save(scoreRecord).getSubmitDate();
    }

    /**
     * 根据指定日期删除成绩记录。
     */
    @Override
    public LocalDateTime deleteScoreRecordByDate(LocalDateTime dateTime)
    {
        ScoreRecordEntity scoreRecordQueryResult
                = this.scoreRecordRepository.findById(dateTime)
                .orElseThrow();

        if (!Objects.equals(scoreRecordQueryResult.getSubmitDate(), dateTime))
        {
            throw new IllegalStateException(
                    String.format(
                            "[IllegalStateException] Submit Date: [%s] has been exist in this table.", dateTime
                    )
            );
        }

        this.scoreRecordRepository.deleteById(dateTime);

        return dateTime;
    }

    /**
     * 获取当前所有的成绩记录，以列表的形式返回。
     */
    @Override
    public List<ScoreRecordEntity> findAllScoreRecord() {
        return this.scoreRecordRepository.findAll();
    }
}
