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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserServiceInterface          userService;
    private final QuestionService               questionService;
    private final ScoreRecordService            scoreRecordService;
    private final FileTransferService           fileTransferService;

    @Autowired
    public UserInfoController(
            UserServiceInterface            userService,
            QuestionService                 questionService,
            ScoreRecordService              scoreRecordService,
            FileTransferService             fileTransferService
    )
    {
        this.userService          = userService;
        this.questionService      = questionService;
        this.scoreRecordService   = scoreRecordService;
        this.fileTransferService  = fileTransferService;
    }

    @PostMapping(path = "register")
    public ResponseEntity<?> doRegister(
            @RequestBody
            UserRegistrationDTO userRegistrationDTO)
    {
        try
        {
            this.userService.userRegister(userRegistrationDTO);

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
            @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest        request,
            HttpServletResponse       response
    )
    {
        try
        {
            this.userService.userLogin(userLoginDTO);

            /*
             * 为了防止固定会话攻击，每次登录都会强制创建新的 Session。
             */
            HttpSession session = request.getSession(false);
            if (!Objects.equals(session, null)) {
                session.invalidate();
            }

            // 将用户名和这个 session 相关联。
            session = request.getSession(true);
            session.setAttribute("user", userLoginDTO.getUserName());

            // 设置 Cookie 确保浏览器使用新会话 ID
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);         // 如果使用 HTTPS
            response.addCookie(cookie);

            List<QuestionCorrectTimesDTO> questionCorrectTimesDTOS
                    = this.fileTransferService.readUserCorrectTimesDataFile(
                        userLoginDTO.getUserName()
                    );

            List<ScoreRecordEntity> scoreRecordEntityList
                    = this.fileTransferService.readUserScoreDataFile(
                            userLoginDTO.getUserName()
                    );

//            for (var n : questionCorrectTimesDTOS) {
//                System.out.println(n);
//            }

//            for (ScoreRecordEntity scoreRecordEntity : scoreRecordEntityList) {
//                System.out.println(scoreRecordEntity);
//            }

            if (!questionCorrectTimesDTOS.isEmpty()) {
                this.questionService.setAllCorrectTimesByIds(questionCorrectTimesDTOS);
            }

            if (!scoreRecordEntityList.isEmpty()) {
                this.scoreRecordService.saveScoreRecordFromList(scoreRecordEntityList);
            }

            log.info(
                    "User [{}] logged in with session ID: {}",
                    userLoginDTO.getUserName(), session.getId()
            );

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
    public ResponseEntity<String> doLogout(
            HttpServletRequest  request,
            HttpServletResponse response
    )
    {
        try
        {
            HttpSession session = request.getSession(false);
            String logoutUserName = null;

            if (!Objects.equals(session, null))
            {
                logoutUserName = (String) session.getAttribute("user");
                session.invalidate();
            }

            // 创建一个名为 "JSESSIONID" 的新 Cookie，值设为 null
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");        // 路径必须与登录时设置的路径一致
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);        // 立即过期
            response.addCookie(cookie); // 将新的 Cookie 添加到响应中

            log.info("User [{}] logout, see you later~", logoutUserName);

            // 存档用户数据
            List<QuestionCorrectTimesDTO> questionCorrectTimesList
                    = this.questionService.getAllQuestionCorrectTimes();

            log.info("Call getAllQuestionCorrectTimes() complete.");

            List<ScoreRecordEntity> allScoreList
                    = this.scoreRecordService.findAllScoreRecordByUserName(logoutUserName);

            log.info("Call findAllScoreRecord complete.");

            // 清空指定的数据
            this.questionService.clearCorrectTimesToZero();
            // this.scoreRecordService.truncateScoreRecordTable();

            log.info("truncate user data complete.");

            this.fileTransferService.saveUserCorrectTimesDataFile(
                    logoutUserName, questionCorrectTimesList
            );

            this.fileTransferService.saveUserScoreDataFile(
                    logoutUserName, allScoreList
            );

            log.info("save date to file complete.");

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "User [%s] logout, see you later~",
                                    logoutUserName
                            )
                    );
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
            this.userService.modifyUserInfo(modifyOperatorDTO);

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
            this.userService.deleteUser(userLoginDTO);

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
