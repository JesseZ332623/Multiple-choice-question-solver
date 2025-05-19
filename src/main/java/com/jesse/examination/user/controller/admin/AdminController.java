package com.jesse.examination.user.controller.admin;

import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.service.AdminServiceInterface;
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
@RequestMapping(path = "/api/admin/", produces = "application/json")
public class AdminController
{
    private final AdminServiceInterface adminServiceInterface;

    @Autowired
    public AdminController(AdminServiceInterface adminServiceInterface) {
        this.adminServiceInterface = adminServiceInterface;
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

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
            AdminAddNewUserDTO adminAddNewUserDTO)
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
    public ResponseEntity<?> deleteUserByUserName(@PathVariable String userName)
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
    deleteUsersByIdRange(
            @PathVariable Long begin,
            @PathVariable Long end)
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
     * 管理员删除全部用户，服务器以 JSON 作为响应。
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
