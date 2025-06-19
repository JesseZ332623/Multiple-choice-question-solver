package com.jesse.examination.user.controller.ordinary_user;

import com.jesse.examination.user.controller.utils.CookieRoles;
import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.userdto.ModifyOperatorDTO;
import com.jesse.examination.user.dto.userdto.UserDeleteDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.dto.userdto.UserRegistrationDTO;
import com.jesse.examination.user.service.UserServiceInterface;
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

            if (session != null && session.getAttribute(CookieRoles.USER.toString()) != null)
            {
                operatorUserName
                        = (String) session.getAttribute(CookieRoles.USER.toString());

                this.userService.setUserAvatarImage(operatorUserName, imageByteData);

                return ResponseEntity.ok(
                        format(
                                "Set overview avatar with user: %s complete!",
                                operatorUserName
                        )
                );
            }
            else {
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
            UserInfoProcessUtils.addNewCookie(
                    request, response,
                    userLoginDTO.getUserName(), CookieRoles.USER
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
            String      logoutUserName;

            // 必须确认会话存在和登录用户存在才可执行登出操作。
            if (session != null && session.getAttribute(CookieRoles.USER.toString()) != null)
            {
                logoutUserName = (String) session.getAttribute(CookieRoles.USER.toString());

                this.userService.userLogout(logoutUserName);
                session.invalidate();   // 登出完成后，立刻使该会话无效
                UserInfoProcessUtils.deleteCookie(response); // 删除 Cookie

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
                        "[RuntimeException] Session not exist, log out failed!"
                );
            }
        }
        catch (Exception exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }

    @PutMapping(path = "modify")
    public ResponseEntity<?> doModify(
            @RequestBody
            ModifyOperatorDTO modifyOperatorDTO,
            HttpServletRequest  request,
            HttpServletResponse response
    )
    {
        try
        {
            HttpSession session = request.getSession(false);
            String      modifyUserName;

            // 必须确认会话存在和登录用户存在才可执行登出操作。
            if (session != null && session.getAttribute(CookieRoles.USER.toString()) != null)
            {
                // 查询当前登录的用户名
                modifyUserName = (String) session.getAttribute(CookieRoles.USER.toString());

                // 当前登录的用户名必须和表单输入的用户名相同，才执行修改操作
                if (modifyUserName.equals(
                        modifyOperatorDTO.getUserLoginDTO().getUserName()))
                {
                    this.userService.modifyUserInfo(modifyOperatorDTO);

                    session.invalidate();   // 修改完毕用户登出，立刻使该会话无效
                    UserInfoProcessUtils.deleteCookie(response); // 删除 Cookie
                }
                else // 反之抛出异常，告知用户不得修改不属于自己的账号
                {
                    throw new IllegalArgumentException(
                            format("User %s: you can't modify account which it is not yours.", modifyUserName)
                    );
                }
            }
            else // 倘若连 Session 都查不到，那自然不可能完成登出操作
            {
                throw new RuntimeException(
                        "[RuntimeException] Session not exist, modify account failed!"
                );
            }

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

            if (session != null && session.getAttribute(CookieRoles.USER.toString()) != null)
            {
                /*
                 * 这里当执行 session.invalidate() 后，
                 * 用户名的字符串引用就失效了，所以这里要拿一份拷贝而非引用。
                 */
                String deletedUserName =
                        new String((String) session.getAttribute(CookieRoles.USER.toString()));

                this.userService.deleteUser(userDeleteDTO, deletedUserName);

                /*
                    用户账户完成删除后，
                    其对应的会话和 Cookie 也应该失效。
                */
                session.invalidate();
                UserInfoProcessUtils.deleteCookie(response);

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
