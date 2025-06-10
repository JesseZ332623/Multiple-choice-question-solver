package com.jesse.examination.user.controller.admin;

import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.admindto.AdminDisplayUsersDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.service.AdminServiceInterface;
import com.jesse.examination.user.service.impl.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
     * 对每一个用户实体稍作处理，
     * 密码仅仅显示加密后的前 6 位，以及更好地展示用户角色和登录时间数据。
     */
    private List<AdminDisplayUsersDTO>
    columnValueProcess(@NotNull List<UserEntity> allUsers)
    {
        List<AdminDisplayUsersDTO> allDisplayUsers = new ArrayList<>();

        for (UserEntity tempUser : allUsers)
        {
            AdminDisplayUsersDTO tempDisplayUser
                    = new AdminDisplayUsersDTO();

            tempDisplayUser.setId(tempUser.getId());
            tempDisplayUser.setUserName(tempUser.getUsername());
            tempDisplayUser.setPassword(tempUser.getPassword().substring(0, 16));
            tempDisplayUser.setFullName(tempUser.getFullName());
            tempDisplayUser.setTelephoneNumber(tempUser.getTelephoneNumber());
            tempDisplayUser.setEmail(tempUser.getEmail());
            tempDisplayUser.setRegisterDateTime(
                    tempUser.getRegisterDateTime()
                            .toString().replace('T', ' ')
            );
            tempDisplayUser.setRoles(
                    UserInfoProcessUtils.getRolesString(
                            tempUser.getRoles()
                    )
            );

            allDisplayUsers.add(tempDisplayUser);
        }

        return allDisplayUsers;
    }

    @GetMapping(path = "all_users")
    public String getAllUsersView(@NotNull Model model)
    {
        List<UserEntity> allUserList
                = this.adminUserServiceInterface.findAllUsers();

        List<AdminDisplayUsersDTO> allDisplayUsers
                = this.columnValueProcess(allUserList);

        model.addAttribute("AllUsers", allDisplayUsers);

        return "AdminOperatorPage/AllUsers";
    }
}
