package com.jesse.examination.scorerecord.service.impl;

import com.jesse.examination.scorerecord.dto.ScoreRecordInsertDTO;
import com.jesse.examination.scorerecord.dto.ScoreRecordQueryDTO;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.scorerecord.repository.ScoreRecordRepository;
import com.jesse.examination.scorerecord.service.ScoreRecordService;

import com.jesse.examination.user.repository.UserEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ScoreRecordServiceImplement implements ScoreRecordService
{
    private final UserEntityRepository  userEntityRepository;
    private final ScoreRecordRepository scoreRecordRepository;

    @Autowired
    public ScoreRecordServiceImplement(
            ScoreRecordRepository scoreRecordRepository,
            UserEntityRepository  userEntityRepository
    ) {
        this.scoreRecordRepository = scoreRecordRepository;
        this.userEntityRepository  = userEntityRepository;
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
     * @param scoreRecordInsertDTO 新的成绩记录
     *
     * @return 返回新插入的数据行 ID
     */
    @Override
    public Integer addNewScoreRecord(ScoreRecordInsertDTO scoreRecordInsertDTO)
    {
        Objects.requireNonNull(scoreRecordInsertDTO);

        ScoreRecordEntity newScoreRecord = new ScoreRecordEntity();

        /*
         * 这里调用用户仓库的 getReferenceById() 方法，
         * 创建一个用户实体代理（将用户 ID 与之关联，而非完整的查询这个实体）。
         */
        newScoreRecord.setUserEntity(
                this.userEntityRepository.getReferenceById(
                        scoreRecordInsertDTO.getUserId()
                )
        );

        newScoreRecord.setSubmitDate(
                scoreRecordInsertDTO.getSubmitDate()
        );

        newScoreRecord.setCorrectCount(
                scoreRecordInsertDTO.getCorrectCount()
        );

        newScoreRecord.setErrorCount(
                scoreRecordInsertDTO.getErrorCount()
        );

        newScoreRecord.setNoAnswerCount(
                scoreRecordInsertDTO.getNoAnswerCount()
        );

        this.scoreRecordRepository.save(newScoreRecord);

        return newScoreRecord.getScoreId();
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
    public List<ScoreRecordEntity> findAllScoreRecord() {
        return this.scoreRecordRepository.findAll();
    }

    /**
     * 查找指定 userName 的所有成绩记录，存于一个列表中。
     */
    @Override
    public List<ScoreRecordQueryDTO>
    findAllScoreRecordByUserName(String userName)
    {
        return this.scoreRecordRepository
                   .findAllScoreRecordByUserName(userName);
    }

    @Override
    public Page<ScoreRecordQueryDTO>
    findPaginatedScoreRecordByUserName(
            String userName, Pageable pageable
    )
    {
        return this.scoreRecordRepository
                   .findPaginatedScoreRecordByUserName(
                           userName, pageable
                   );
    }

    /**
     * 删除指定 userName 的所有成绩记录，返回删除的行数。
     */
    @Override
    @Transactional
    public Integer
    deleteAllScoreRecordByUserName(String userName)
    {
        return this.scoreRecordRepository
                   .deleteAllScoreRecordByUserName(userName);
    }

    /**
     * 将 scoreRecordEntities 列表中的所有数据全部存入表中。
     */
    @Override
    @Transactional
    public void
    saveScoreRecordFromList(
            @NotNull
            List<ScoreRecordEntity> scoreRecordEntities
    )
    {
        this.scoreRecordRepository.saveAllAndFlush(scoreRecordEntities);
    }

    /**
     * 清空 score_record 表。
     */
    @Override
    @Transactional
    public Long
    truncateScoreRecordTable()
    {
        Long currentRows
                = this.scoreRecordRepository.count();

        this.scoreRecordRepository.truncateScoreRecordTable();

        this.scoreRecordRepository.flush();

        return currentRows;
    }

    @Override
    public Optional<ScoreRecordQueryDTO>
    findLatestScoreRecordByName(String userName)
    {
        Pageable firstResultOnly
                = PageRequest.of(0, 1);

        Page<ScoreRecordQueryDTO> pageResult
                = scoreRecordRepository.findLatestScoreRecordByName(
                        userName, firstResultOnly
        );

        return pageResult.stream().findFirst();
    }
}
