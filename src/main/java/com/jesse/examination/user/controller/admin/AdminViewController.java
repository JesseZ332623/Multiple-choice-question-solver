package com.jesse.examination.user.controller.admin;

import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.user.controller.utils.CookieRoles;
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

import java.util.List;
import java.util.stream.Collectors;

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
     * 对每一个用户实体稍作处理，
     * 密码仅仅显示加密后的前 6 位，以及更好地展示用户角色和登录时间数据。
     *
     * <p>2025-6-16: 从原来的迭代改成并行流的形式，在用户非常多的时候或许性能更好？</>
     */
    private List<AdminDisplayUsersDTO>
    columnValueProcess(@NotNull List<UserEntity> allUsers)
    {
        // List<AdminDisplayUsersDTO> allDisplayUsers = new ArrayList<>();

        return allUsers.stream().parallel()
        .map(
                (user) -> {
                    var adminDisplay = new AdminDisplayUsersDTO();
                    adminDisplay.setId(user.getId());
                    adminDisplay.setUserName(user.getUsername());
                    adminDisplay.setPassword(user.getPassword().substring(0, 16));
                    adminDisplay.setFullName(user.getFullName());
                    adminDisplay.setTelephoneNumber(user.getTelephoneNumber());
                    adminDisplay.setEmail(user.getEmail());
                    adminDisplay.setRegisterDateTime(
                            user.getRegisterDateTime()
                                    .toString().replace('T', ' ')
                    );
                    adminDisplay.setRoles(
                            AdminController.getRolesString(
                                    user.getRoles()
                            )
                    );

                    return adminDisplay;
                }
        ).collect(Collectors.toList());
    }

    @GetMapping(path = "all_users")
    public String
    getAllUsersView(@NotNull Model model, HttpServletRequest request)
    {
        try
        {
            HttpSession session  = request.getSession(false);

            if (session != null && session.getAttribute(CookieRoles.ADMIN.toString()) != null)
            {
                String loginUserName = (String) session.getAttribute(CookieRoles.ADMIN.toString());

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
