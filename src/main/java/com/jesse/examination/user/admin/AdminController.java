package com.jesse.examination.user.admin;

import com.jesse.examination.user.dto.admindto.AdminAddNewUserDTO;
import com.jesse.examination.user.dto.admindto.AdminModifyUserDTO;
import com.jesse.examination.user.entity.RoleEntity;
import com.jesse.examination.user.service.AdminServiceInterface;
import com.jesse.examination.user.service.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static java.lang.String.format;

@Slf4j
@RestController
@RequestMapping(path = "/api/admin/")
public class AdminController
{
    private final AdminServiceInterface adminServiceInterface;

    @Autowired
    public AdminController(AdminServiceInterface adminServiceInterface) {
        this.adminServiceInterface = adminServiceInterface;
    }

    private @NotNull String
    getRolesString(
            @NotNull Set<RoleEntity> roles)
    {
        StringBuilder newUserRoles = new StringBuilder("[");

        for (RoleEntity role : roles) {
            newUserRoles.append(role.getRoleName()).append(", ");
        }
        newUserRoles.delete(newUserRoles.length() - 1, newUserRoles.length());
        newUserRoles.append("]");

        return newUserRoles.toString();
    }

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

    @PostMapping(path = "add_new_user")
    public ResponseEntity<?>
    addNewUser(
            @RequestBody
            AdminAddNewUserDTO adminAddNewUserDTO)
    {
        try
        {
            Long newUserId     = this.adminServiceInterface.addNewUser(adminAddNewUserDTO);
            String newRolesString = this.getRolesString(adminAddNewUserDTO.getRoles());

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
            String newRolesString
                    = this.getRolesString(adminModifyUserDTO.getNewRoles());

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "Modify  new user ID = [%s], Name = [%s], Roles = %s.",
                                    modifiedUserId, adminModifyUserDTO.getNewUserName(),
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

    @DeleteMapping(path = "/delete/{userName}")
    public ResponseEntity<?> deleteUserByUserName(@PathVariable String userName)
    {
        try
        {
            Long modifiedUserId
                    = this.adminServiceInterface.deleteUserByUserName(userName);

            return ResponseEntity.ok()
                    .body(format("Delete user ID = [%s].", modifiedUserId));
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }

    @DeleteMapping(path = "/delete/range/{min}_{max}")
    public ResponseEntity<?>
    deleteUsersByIdRange(
            @PathVariable Long min,
            @PathVariable Long max)
    {
        try
        {
            Long deletedUserIdAmount
                    = this.adminServiceInterface.deleteUsersByIdRange(min, max);

            return ResponseEntity.ok()
                    .body(
                            format(
                                    "Delete user ID range = (%s, %s), %s rows.",
                                    min, max, deletedUserIdAmount
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

    @DeleteMapping(path = "/delete/all")
    public ResponseEntity<?> truncateAllUsers()
    {
        try
        {
            Long deletedUserIdAmount =
                    this.adminServiceInterface.truncateAllUsers();

            return ResponseEntity.ok()
                .body(format("Delete user [%s] rows", deletedUserIdAmount));
        }
        catch (Exception exception)
        {
            log.error(exception.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exception.getMessage());
        }
    }
}
