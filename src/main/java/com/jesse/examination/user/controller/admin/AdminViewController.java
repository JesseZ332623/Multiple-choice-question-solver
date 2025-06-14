package com.jesse.examination.user.controller.admin;

import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.admindto.AdminDisplayUsersDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.service.AdminServiceInterface;
import com.jesse.examination.user.service.impl.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/admin")
public class AdminViewController
{
    private final AdminServiceInterface adminUserServiceInterface;

    @Autowired
    public AdminViewController(AdminUserService adminUserServiceInterface) {
        this.adminUserServiceInterface = adminUserServiceInterface;
    }

    /**
     * 对每一个用户实体稍作处理，密码仅仅显示加密后的前 6 位。
     */
    private List<UserEntity>
    columnValueProcess(@NotNull List<UserEntity> allUsers)
    {
        for (UserEntity user : allUsers) {
            user.setPassword(user.getPassword().substring(0, 7));
        }

        return allUsers;
    }

    @GetMapping(path = "all_users")
    public String
    getAllUsersView(@NotNull Model model, HttpServletRequest request)
    {
        try
        {
            HttpSession session  = request.getSession(false);

            if (session != null && session.getAttribute("user") != null)
            {
                String loginUserName = (String) session.getAttribute("user");

                model.addAttribute("UserName", loginUserName);
                model.addAttribute(
                        "AllUsers",
                        this.columnValueProcess(
                                this.adminUserServiceInterface.findAllUsers()
                        )
                );

                return "AdminOperatorPage/AllUsers";
            }
            else
            {
                throw new RuntimeException(
                        "No admin login! Couldn't preview users!"
                );
            }
        }
        catch (RuntimeException exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                    this.getClass().getSimpleName(),
                    "getAllUsersView",
                    exception.getMessage()
            );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "ErrorPage/Controller_ErrorPage";
        }
    }
}
