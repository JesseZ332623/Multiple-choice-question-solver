package com.jesse.examination.user.controller.admin;

import com.jesse.examination.user.controller.utils.CookieRoles;
import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.dto.userdto.UserLoginDTO;
import com.jesse.examination.user.entity.RoleEntity;
import com.jesse.examination.user.service.AdminServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping(path = "/api/admin/", produces = "application/json")
public class AdminController
{
    private final AdminServiceInterface adminServiceInterface;

    @Autowired
    public AdminController(AdminServiceInterface adminServiceInterface) {
        this.adminServiceInterface = adminServiceInterface;
    }

    /**
     * 从集合中获取用户角色并合成一个字符串。
     * 假设集合
     *
     * <pre>
     *     roles = {{1, "ROLE_ADMIN"}, {2, "ROLE_USER}}
     * </pre>
     *
     * 则函数处理后返回：
     *
     * <pre> rolesString = "[ROLE_ADMIN, ROLE_USER]" </pre>
     */
    static public String getRolesString(
            @NotNull Set<RoleEntity> roles)
    {
        StringBuilder newUserRoles = new StringBuilder("[");

        for (RoleEntity role : roles) {
            newUserRoles.append(role.getRoleName()).append(", ");
        }

        newUserRoles.delete(newUserRoles.length() - 2, newUserRoles.length());
        newUserRoles.append("]");

        return newUserRoles.toString();
    }

    @PostMapping(path = "login")
    public ResponseEntity<String>
    adminUserLogin(
            @NotNull @RequestBody
            UserLoginDTO userLoginDTO,
            HttpServletRequest request, HttpServletResponse response
    )
    {
        try
        {
            this.adminServiceInterface.adminUserLogin(userLoginDTO);

            // 添加 Cookie
            UserInfoProcessUtils.addNewCookie(
                    request, response,
                    userLoginDTO.getUserName(), CookieRoles.ADMIN
            );

            return ResponseEntity.ok(
                    format(
                            "Log in complete! Welcome admin: [%s]!",
                            userLoginDTO.getUserName()
                    )
            );
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(exception.getMessage());
        }
    }

    @PostMapping(path = "logout")
    public ResponseEntity<String>
    adminUserLogout(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            HttpSession session = request.getSession(false);
            String      logoutAdminName;

            if (session != null && session.getAttribute(CookieRoles.ADMIN.toString()) != null)
            {
                logoutAdminName = (String) session.getAttribute(CookieRoles.ADMIN.toString());

                // 得查得到用户名才能执行登出操作不是吗。
                this.adminServiceInterface.adminUserLogout(logoutAdminName);

                session.invalidate();                        // 登出完成后，立刻使该会话无效
                UserInfoProcessUtils.deleteCookie(response); // 删除 Cookie

                log.info("Admin [{}] logout, see you later~", logoutAdminName);

                return ResponseEntity.ok()
                        .body(
                                format(
                                        "Admin [%s] logout, see you later~",
                                        logoutAdminName
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

    /**
     * 管理员查询单个用户信息，服务器以 JSON 作为响应。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/search/Jesse">
     *         (GET Method) 管理员查询用户名为 Jesse 的用户信息
     *     </a>
     * </p>
     */
    @GetMapping(path = "search/{userName}")
    public ResponseEntity<?>
    searchOneUserByName(@PathVariable String userName)
    {
        try
        {
            return ResponseEntity.ok()
                    .body(this.adminServiceInterface.findUserByUserName(userName));
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(exception.getMessage());
        }
    }

    /**
     * 管理员通过 JSON 文本添加单个用户，服务器以 JSON 作为响应，
     * JSON 示例文本见：/resources/api-admin/addNewUser.json。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/add_new_user">
     *         (POST Method) 管理员通过 JSON 添加新用户
     *     </a>
     * </p>
     */
    @PostMapping(path = "add_new_user")
    public ResponseEntity<?>
    addNewUser(
            @RequestBody
            AdminAddNewUserDTO adminAddNewUserDTO
    )
    {
        try
        {
            Long newUserId        = this.adminServiceInterface.addNewUser(adminAddNewUserDTO);
            String newRolesString = UserInfoProcessUtils.getRolesString(adminAddNewUserDTO.getRoles());

            log.info(newRolesString);

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "Add new user ID = [%s], Name = [%s], Roles = %s.",
                                    newUserId, adminAddNewUserDTO.getUserName(),
                                    newRolesString
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

    /**
     * 管理员通过 JSON 文本修改单个用户，服务器以 JSON 作为响应，
     * JSON 示例文本见：/resources/api-admin/modifyUser.json。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/modify_user">
     *         (PUT Method) 管理员通过 JSON 修改以有的用户信息
     *     </a>
     * </p>
     */
    @PutMapping(path = "modify_user")
    public ResponseEntity<?>
    modifyUserByUserName(
            @RequestBody
            AdminModifyUserDTO adminModifyUserDTO)
    {
        try
        {
            Long modifiedUserId
                    = this.adminServiceInterface.modifyUserByUserName(adminModifyUserDTO);

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "Modify  new user ID = [%s], Name = [%s], Roles = %s.",
                                    modifiedUserId, adminModifyUserDTO.getNewUserName(),
                                    UserInfoProcessUtils.getRolesString(adminModifyUserDTO.getNewRoles())
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

    /**
     * 管理员通过用户名删除单个用户，服务器以 JSON 作为响应。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/delete/Peter">
     *         (DELETE Method) 管理员删除用户名为 Peter 的用户信息
     *     </a>
     * </p>
     */
    @DeleteMapping(path = "/delete/{userName}")
    public ResponseEntity<?>
    deleteUserByUserName(@PathVariable String userName)
    {
        try
        {
            Long modifiedUserId
                    = this.adminServiceInterface.deleteUserByUserName(userName);

            return ResponseEntity.ok()
                    .body(
                            format(
                                "Delete user ID = [%s] User Name:[%s].",
                                modifiedUserId, userName
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

    /**
     * 管理员通过指定 id 范围批量删除用户，服务器以 JSON 作为响应。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/delete/range/1_10">
     *         (DELETE Method) 管理员尝试删除 id 范围在 1 到 10 的用户信息
     *     </a>
     * </p>
     */
    @DeleteMapping(path = "/delete/range/{begin}_{end}")
    public ResponseEntity<?>
    deleteUsersByIdRange(@PathVariable Long begin, @PathVariable Long end)
    {
        try
        {
            Long deletedUserIdAmount
                    = this.adminServiceInterface.deleteUsersByIdRange(begin, end);

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "Delete user ID range = (%s, %s), " +
                                    "But only [%s] data row effective.",
                                    begin, end, deletedUserIdAmount
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

    /**
     * 管理员删除全部用户，服务器响应一个字符串报告结果。
     *
     * <p>
     *     链接：
     *     <a href="https://localhost:8080/api/admin/delete/all">
     *         (DELETE Method) 管理员删除所有的用户信息
     *     </a>
     * </p>
     */
    @DeleteMapping(path = "/delete/all")
    public ResponseEntity<?> truncateAllUsers()
    {
        try
        {
            Long deletedUserIdAmount =
                    this.adminServiceInterface.truncateAllUsers();

            return ResponseEntity.ok()
                .body(format("Delete user [%s] rows.", deletedUserIdAmount));
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }

    @GetMapping(path = "/get_default_avatar")
    public ResponseEntity<?> getDefaultAvatar()
    {
        byte[] imageData
                = this.adminServiceInterface.getDefaultAvatar();

        var headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_PNG);

        return  new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping(path = "/set_user_overview_avatar/{userName}")
    public ResponseEntity<?>
    modifyUserAvatar(
            @PathVariable String userName,
            @RequestBody  byte[] imageDataBytes
    )
    {
        try
        {
            this.adminServiceInterface.modifyUserAvatar(
                    userName, imageDataBytes
            );

            return ResponseEntity.ok(
                    format(
                            "Modify user: %s avatar complete!", userName
                    )
            );
        }
        catch (RuntimeException exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(exception.getMessage());
        }
    }
}
