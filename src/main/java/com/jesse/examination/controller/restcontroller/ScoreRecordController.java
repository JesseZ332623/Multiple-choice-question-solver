package com.jesse.examination.controller.restcontroller;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;
import com.jesse.examination.service.scorerecordservice.ScoreRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/score_record")
public class ScoreRecordController
{
    private final ScoreRecordService scoreRecordService;

    @Autowired
    public ScoreRecordController(ScoreRecordService scoreRecordService) {
        this.scoreRecordService = scoreRecordService;
    }

    /*
     *  GET 方法请求，通过指定的时间信息查询对应的成绩记录行，
     *  前后端时间信息遵循 ISO 标准且精确到秒。
     *
     *  示例：2025-04-08T09:12:04
     *
     *  服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     *  可能的 URL 为：http://localhost:8081/api/score_record/2025-04-08T09:12:04
     */
    @GetMapping(path = "/{dateTime}")
    public ResponseEntity<?> getOneScoreRecord(
            @PathVariable(name = "dateTime") LocalDateTime dateTime
    )
    {
        try
        {
            ScoreRecordEntity scoreRecord
                    = this.scoreRecordService.findScoreRecordByDateTime(dateTime);

            return ResponseEntity.ok(scoreRecord);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    /*
    * Get 方法请求，获取数据库中所有的练习成绩记录。
    * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
    *
    * 可能的 URL 为：http://localhost:8081/api/score_record/all_score_record
    */
    @GetMapping(path = "/all_score_record")
    public ResponseEntity<?> getAllScoreRecord() {
        return ResponseEntity.ok(this.scoreRecordService.findAllScoreRecord());
    }


    /*
     * Get 方法请求，获取数据库中最新的一条成绩记录（在用户完成最新一次的练习后会调用）。
     * 服务器以 JSON 格式作为响应，如下所示：
     *
     * {
     *     "submitDate": "2025-04-09T09:57:29",
     *     "correctCount": 0,
     *     "errorCount": 0,
     *     "noAnswerCount": 321,
     *     "mistakeRate": 100.0
     * }
     *
     * 如果期间出现错误，会以 500 作为响应码。
     *
     * 可能的 URL 为：http://localhost:8081/api/score_record/score_settlement
     * */
    @GetMapping(path = "/score_settlement")
    public ResponseEntity<?> scoreSettlement()
    {
        try
        {
            Optional<ScoreRecordEntity> latestScoreRecord
                    = this.scoreRecordService.findAllScoreRecord()
                    .stream().max(Comparator.comparing(ScoreRecordEntity::getSubmitDate));

            ScoreRecordEntity scoreRecord = null;
            if (latestScoreRecord.isPresent()) {
                scoreRecord = latestScoreRecord.get();
            }

            return ResponseEntity.ok(scoreRecord);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    /*
     * Post 方法请求，往数据表中添加一条新的练习记录。
     * 如果需要手动提交进行测试的话，JSON 格式示例如下：
     *
     *
     * {
     *     "submitDate": "2015-01-12T19:12:37",
     *     "correctCount": 100,
     *     "errorCount": 20,
     *     "noAnswerCount": 30,
     *     "mistakeRate": 13.3333333
     * }
     *
     *
     * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * 可能的 URL 为：http://localhost:8081/api/score_record/add_one_new_score_record"
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
            LocalDateTime insertFeedBack
                    = this.scoreRecordService.addNewScoreRecord(scoreRecord);

            return ResponseEntity.ok()
                                 .body(
                                         String.format(
                                                 "Insert New Score Record Complete, Record Submit time: %s",
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

    /*
     * Delete 方法请求，通过指定的时间信息删除对应的成绩记录行。
     * 前后端时间信息遵循 ISO 标准且精确到秒。
     *
     * 示例：2025-04-08T09:12:04
     *
     * 服务器以 JSON 格式作为响应（如果期间出现错误，会以 500 作为响应码）。
     *
     * 可能的 URL 为：http://localhost:8081/api/score_record/2025-04-08T09:12:04"
     */
    @DeleteMapping(path = "/{dateTime}")
    public ResponseEntity<?> deleteOneScoreRecord(
            @PathVariable(name = "dateTime") LocalDateTime dateTime
    )
    {
        try
        {
            LocalDateTime deleteFeedBack
                    = this.scoreRecordService.deleteScoreRecordByDate(dateTime);

            return ResponseEntity.ok()
                                 .body(
                                         String.format(
                                                 "Delete Score Record Complete, Record Submit time: %s",
                                                 deleteFeedBack.toString()
                                         )
                                 );
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}