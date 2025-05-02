package com.jesse.examination.user.controller;

import com.jesse.examination.file.FileTransferService;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import com.jesse.examination.question.service.QuestionService;
import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.service.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping(path = "/api/user_info/", produces = "application/json")
public class UserInfoController
{
    private final UserServiceInterface          userServiceInterface;
    private final QuestionService               questionService;
    private final ScoreRecordService            scoreRecordService;
    private final RedisTemplate<String, String> redisTemplate;
    private final FileTransferService           fileTransferService;

    @Autowired
    public UserInfoController(
            UserServiceInterface            userServiceInterface,
            QuestionService                 questionService,
            ScoreRecordService              scoreRecordService,
            RedisTemplate<String, String>   redisTemplate,
            FileTransferService             fileTransferService
    )
    {
        this.userServiceInterface = userServiceInterface;
        this.questionService      = questionService;
        this.scoreRecordService   = scoreRecordService;
        this.redisTemplate        = redisTemplate;
        this.fileTransferService  = fileTransferService;
    }

    @PostMapping(path = "register")
    public ResponseEntity<?> doRegister(
            @RequestBody
            UserRegistrationDTO userRegistrationDTO)
    {
        try
        {
            this.userServiceInterface.userRegister(userRegistrationDTO);

            return ResponseEntity.ok(
                    format(
                            "Register complete. Welcome User: [%s]!",
                            userRegistrationDTO.getUserName()
                    )
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    @PostMapping(path = "login")
    public ResponseEntity<?> doLogin(
            @RequestBody
            UserLoginDTO userLoginDTO)
    {
        try
        {
            this.userServiceInterface.userLogin(userLoginDTO);
            this.redisTemplate.opsForValue().set(
                    "user:UserInfoController:login_username",
                    userLoginDTO.getUserName()
            );

            List<QuestionCorrectTimesDTO> questionCorrectTimesDTOS
                    = this.fileTransferService.readUserCorrectTimesDataFile(
                                this.redisTemplate.opsForValue()
                                        .get("user:UserInfoController:login_username")
                        );

//            for (var n : questionCorrectTimesDTOS) {
//                System.out.println(n);
//            }

            this.questionService.setAllCorrectTimesByIds(questionCorrectTimesDTOS);


            return ResponseEntity.ok(
                    format(
                            "Login complete. Welcome User: [%s]!",
                            userLoginDTO.getUserName()
                    )
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    @PostMapping(path = "logout")
    public ResponseEntity<String> doLogout()
    {
        try
        {
            String logoutUserName
                    = Objects.requireNonNull(
                    this.redisTemplate.opsForValue()
                            .get("user:UserInfoController:login_username")
            );

            String responseText
                    = format("User [%s] logout, see you later~", logoutUserName);

            log.info(responseText);

            // 存档用户数据
            List<QuestionCorrectTimesDTO> questionCorrectTimesList
                    = this.questionService.getAllQuestionCorrectTimes();

            log.info("Call getAllQuestionCorrectTimes() complete.");

            List<ScoreRecordEntity> allScoreList
                    = this.scoreRecordService.findAllScoreRecord();

            log.info("Call findAllScoreRecord complete.");

            this.questionService.clearCorrectTimesToZero();
            this.scoreRecordService.truncateScoreRecordTable();

            log.info("truncate user data complete.");

            this.fileTransferService.saveUserCorrectTimesDataFile(
                    logoutUserName, questionCorrectTimesList
            );

            this.fileTransferService.saveUserScoreDataFile(
                    logoutUserName, allScoreList
            );

            log.info("save date to file complete.");

            this.redisTemplate.delete("user:UserInfoController:login_username");

            log.info("delete user redis data complete.");

            return ResponseEntity.ok().body(responseText);
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(exception.getMessage());
        }
    }

    @PutMapping(path = "modify")
    public ResponseEntity<?> doModify(
            @RequestBody ModifyOperatorDTO modifyOperatorDTO)
    {
        try
        {
            this.userServiceInterface.modifyUserInfo(modifyOperatorDTO);

            return ResponseEntity.ok(
                    format(
                            "Modify complete! User: [%s].",
                            modifyOperatorDTO.getUserMidifyInfoDTO().getNewUserName()
                    )
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }

    @DeleteMapping(path = "delete")
    public ResponseEntity<?> doDelete(@RequestBody UserLoginDTO userLoginDTO)
    {
        try
        {
            this.userServiceInterface.deleteUser(userLoginDTO);

            return ResponseEntity.ok(
                    format(
                            "Delete complete! See you again [%s]!",
                            userLoginDTO.getUserName()
                    )
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}
