package com.jesse.examination.user.controller.ordinary_user;

import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserDeleteDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.service.UserServiceInterface;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping(path = "/api/user_info/", produces = "application/json")
public class UserInfoController
{
    private final UserServiceInterface userService;

    @Autowired
    public UserInfoController(UserServiceInterface userService) {
        this.userService = userService;
    }

    /**
     * 在用户登录时，添加新的 Cookie。
     *
     * @param request  HTTP servlet 请求
     * @param response HTTP servlet 响应
     * @param userName Cookie 所关联的用户名
     */
    private static void
    addNewCookie(
            HttpServletRequest request,
            HttpServletResponse response, String userName)
    {
        /*
         * 为了防止固定会话攻击，每次登录都会强制创建新的 Session。
         */
        HttpSession session = request.getSession(false);

        if (!Objects.equals(session, null)) {
            session.invalidate();
        }

        // 将用户名和这个 session 相关联。
        session = request.getSession(true);
        session.setAttribute("user", userName);

        // 设置 Cookie 确保浏览器使用新会话 ID
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);         // 如果使用 HTTPS
        response.addCookie(cookie);

        log.info(
                "User [{}] logged in with session ID: {}",
                userName, session.getId()
        );
    }

    /**
     * 在用户登出以及确定干掉它自己时，删除 Cookie。
     *
     * @param response HTTP servlet 响应
     */
    private static void
    deleteCookie(HttpServletResponse response)
    {
        // 创建一个名为 "JSESSIONID" 的新 Cookie，值设为 null
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");        // 路径必须与登录时设置的路径一致
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);        // 立即过期
        response.addCookie(cookie); // 将新的 Cookie 添加到响应中
    }

    /**
     * 获取指定用户的头像数据。
     */
    @GetMapping(path = "user_overview_avatar/{userName}")
    public ResponseEntity<?>
    getUserAvatar(@PathVariable String userName)
    {
        try
        {
            byte[] imageData = this.userService.getUserAvatarImage(userName);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        }
        catch (RuntimeException exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }

    /**
     * 设置用户的头像数据，这个操作需要更安全的处理，
     * 因此要从 Session 中拿到用户名，而不是在前端通过 URL 参数指定。
     *
     * @param imageByteData 从前端页面传来的图片字节数组。
     */
    @PostMapping(path = "set_user_overview_avatar")
    public ResponseEntity<?>
    setUserOverviewAvatar(
            @RequestBody byte[] imageByteData,
            HttpServletRequest  request,
            HttpServletResponse response
    )
    {
        try
        {
            HttpSession session = request.getSession(false);
            String operatorUserName;

            if (!Objects.equals(session, null))
            {
                operatorUserName
                        = (String) session.getAttribute("user");

                this.userService.setUserAvatarImage(operatorUserName, imageByteData);

                return ResponseEntity.ok(
                        format(
                                "Set overview avatar with user: %s complete!",
                                operatorUserName
                        )
                );
            }
            else
            {
                throw new RuntimeException("Session not found!");
            }
        }
        catch (RuntimeException exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
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
            @RequestBody
            UserLoginDTO userLoginDTO,
            HttpServletRequest  request,
            HttpServletResponse response
    )
    {
        try
        {
            // 正式登录
            this.userService.userLogin(userLoginDTO);

            // 添加 Cookie
            addNewCookie(request, response, userLoginDTO.getUserName());

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
            HttpSession session        = request.getSession(false);
            String      logoutUserName;

            if (!Objects.equals(session, null))
            {
                logoutUserName = (String) session.getAttribute("user");

                // 得查得到用户名才能执行登出操作不是吗。
                this.userService.userLogout(logoutUserName);
                session.invalidate();   // 登出完成后，立刻使该会话无效
                deleteCookie(response); // 删除 Cookie

                log.info("User [{}] logout, see you later~", logoutUserName);

                return ResponseEntity.ok()
                        .body(format(
                                "User [%s] logout, see you later~",
                                logoutUserName
                        )
               );
            }
            else    // 倘若连 Session 都查不到，那自然不可能完成登出操作
            {
                throw new RuntimeException(
                        "Session not exist, log out failed!"
                );
            }
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
    public ResponseEntity<?>
    doDelete(
            @RequestBody
            UserDeleteDTO userDeleteDTO,
            HttpServletRequest request,
            HttpServletResponse response
    )
    {
        try
        {
            HttpSession session = request.getSession(false);

            if (!Objects.equals(session, null))
            {
                /*
                 * 这里当执行 session.invalidate() 后，
                 * 用户名的字符串引用就失效了，所以这里要拿一份拷贝而非引用。
                 */
                String deletedUserName =
                        new String((String) session.getAttribute("user"));

                this.userService.deleteUser(userDeleteDTO, deletedUserName);

                /*
                    用户账户完成删除后，
                    其对应的会话和 Cookie 也应该失效。
                */
                session.invalidate();
                deleteCookie(response);

                return ResponseEntity.ok(
                        format("Delete complete! See you again [%s]!", deletedUserName)
                );
            }
            else
            {
                throw new RuntimeException(
                        "Session not exist, delete account failed!"
                );
            }
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }
}
