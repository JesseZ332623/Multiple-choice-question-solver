package com.jesse.examination.scorerecord.controller;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.scorerecord.service.ScoreRecordService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping(path = "/api/score_record", produces = "application/json")
public class ScoreRecordController
{
    private final ScoreRecordService scoreRecordService;

    @Autowired
    public ScoreRecordController(ScoreRecordService scoreRecordService) {
        this.scoreRecordService = scoreRecordService;
    }

    /**
     *  GET 方法请求，通过指定的时间信息查询对应的成绩记录行，
     *  前后端时间信息遵循 ISO 标准且精确到秒。</br>
     *  服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/search/1">
     *          (GET Method) 通过指定的时间信息查询对应的成绩记录行，以 JSON 格式作为响应。
     *      </a>
     *</p>
     */
    @GetMapping(path = "/search/{id}")
    public ResponseEntity<?> getOneScoreRecord(
            @PathVariable(name = "id") Integer id
    )
    {
        try
        {
            ScoreRecordEntity scoreRecord
                    = this.scoreRecordService.findScoreRecordById(id);

            return ResponseEntity.ok(scoreRecord);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    /**
     * Get 方法请求，获取数据库中所有的练习成绩记录。
     * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/all_score_record">
     *          (GET Method) 获取数据库中所有的练习成绩记录，以 JSON 格式作为响应。
     *      </a>
     *</p>
    */
    @GetMapping(path = "/all_score_record")
    public ResponseEntity<?> getAllScoreRecord()
    {
        return ResponseEntity.ok(
                this.scoreRecordService.findAllScoreRecord()
        );
    }


    /**
     * Get 方法请求，获取指定用户最新的一条成绩记录（在用户完成最新一次的练习后会调用）。
     * 服务器以 JSON 格式作为响应，如下所示：
     *
     * <pre>
     * {
     *     "userName"  : "Perter",
     *     "submitDate": "2025-04-09T09:57:29",
     *     "correctCount": 0,
     *     "errorCount": 0,
     *     "noAnswerCount": 321,
     *     "mistakeRate": 100.0
     * }
     * </pre>
     *
     * 如果期间出现错误，会以 500 作为响应码。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/score_settlement/Perter">
     *          (GET Method) 获取用户 Perter 最新的一条成绩记录，以 JSON 格式作为响应。
     *      </a>
     *</p>
     *
     * @param userName 指定用户
     */
    @GetMapping(path = "/score_settlement/{userName}")
    public ResponseEntity<?> getLatestScoreSettlement(
            @PathVariable String userName
    )
    {
        try
        {
            ScoreRecordEntity latestScoreRecord
                    = this.scoreRecordService.findLatestScoreRecordByName(userName);

            return ResponseEntity.ok(latestScoreRecord);
        }
        catch (NoSuchElementException exception)
        {
            log.error(exception.getMessage());
			
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    /**
     * Post 方法请求，往数据表中添加一条新地练习记录。
     * 如果需要手动提交进行测试的话，JSON 格式示例如下：
     *
     * <pre>
     * {
     *     "userName"  : "Perter"
     *     "submitDate": "2015-01-12T19:12:37",
     *     "correctCount": 100,
     *     "errorCount": 20,
     *     "noAnswerCount": 30,
     *     "mistakeRate": 13.3333333
     * }
     *</pre>
     *
     * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/add_one_new_score_record">
     *          (POST Method) 往数据表中添加一条新地练习记录，以 JSON 格式作为响应。
     *      </a>
     *</p>
     */
    @PostMapping(path = "/add_one_new_score_record")
    public ResponseEntity<?> addOneNewScoreRecord(
            @RequestBody
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ScoreRecordEntity scoreRecord
    )
    {
        try
        {
            Integer insertFeedBack
                    = this.scoreRecordService.addNewScoreRecord(scoreRecord);

            return ResponseEntity.ok()
                                 .body(
                                         format(
                                                 "Insert New Score Record Complete, Record ID: {%s}.",
                                                 insertFeedBack.toString()
                                         )
                                 );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    @DeleteMapping(path = "/delete_by_username/{userName}")
    public ResponseEntity<String>
    deleteAllScoreRecordByUserName(@PathVariable String userName)
    {
        Integer affectedRows
            = this.scoreRecordService.deleteAllScoreRecordByUserName(userName);

        return ResponseEntity.ok(
                format(
                        "Delete %s's score record complete, truncate rows: %d.",
                        userName, affectedRows
                )
        );
    }

    /**
     * Delete 方法请求，清空所有的练习记录。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/truncate">
     *          (DELETE Method) 将所有数据行的 correct_times 列的值设为 0。
     *      </a>
     *</p>
     *
     * <strong>注意这是一个敏感操作，后续会对外屏蔽。</strong>
     */
    @DeleteMapping(path = "/truncate")
    public ResponseEntity<?> truncateScoreRecord()
    {
        return ResponseEntity.ok(
                format(
                        "Truncate score record complete, truncate rows: {%d}.",
                        this.scoreRecordService.truncateScoreRecordTable()
                )
        );
    }

    /**
     * Delete 方法请求，通过指定的 ID 删除对应的成绩记录行，
     * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     *<p>
     *      链接：
     *      <a href="https://localhost:8081/api/score_record/delete/1">
     *          (DELETE Method) Delete 方法请求，通过指定的 ID 删除对应的成绩记录行，
     *          以 JSON 格式作为响应。
     *      </a>
     *</p>
     */
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<?> deleteOneScoreRecord(
            @PathVariable(name = "id") Integer id
    )
    {
        try
        {
            Integer deleteFeedBack
                    = this.scoreRecordService.deleteScoreRecordByDate(id);

            return ResponseEntity.ok()
                                 .body(
                                         format(
                                                 "Delete Score Record Complete, Record id: {%d}.",
                                                 deleteFeedBack
                                         )
                                 );
        }
        catch (Exception exception)
        {
            // log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}