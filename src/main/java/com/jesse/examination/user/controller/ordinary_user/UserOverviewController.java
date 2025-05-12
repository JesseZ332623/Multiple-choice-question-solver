package com.jesse.examination.user.controller.ordinary_user;

import com.jesse.examination.user.controller.utils.UserInfoProcessUtils;
import com.jesse.examination.user.dto.userdto.UserOverviewDisplayDTO;
import com.jesse.examination.user.entity.UserEntity;
import com.jesse.examination.user.service.AdminServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户个人主页视图控制器。
 */
@Controller
@RequestMapping(path = "/user_info/overview")
public class UserOverviewController
{
    // 这里要动用一下管理员的权限去查询用户的完整信息（可能会造成权限污染？）
    private final AdminServiceInterface userInfoQueryService;

    @Autowired
    public UserOverviewController(AdminServiceInterface userInfoQueryService) {
        this.userInfoQueryService = userInfoQueryService;
    }

    @GetMapping(path = "/{userName}")
    public String userOverview(
            @PathVariable String userName,
            Model model
    )
    {
        UserEntity user = this.userInfoQueryService.findUserByUserName(userName);

        var userDisplay = new UserOverviewDisplayDTO();

        userDisplay.setId(user.getId());
        userDisplay.setUserName(user.getUsername());
        userDisplay.setFullName(user.getFullName());
        userDisplay.setTelephoneNumber(user.getTelephoneNumber());
        userDisplay.setEmail(user.getEmail());
        userDisplay.setRegisterDateTime(
                user.getRegisterDateTime()
                    .toString().replace('T', ' ')
        );
        userDisplay.setRoles(
                UserInfoProcessUtils.getRolesString(user.getRoles())
        );

        model.addAttribute("UserOverview", userDisplay);

        return "UserAccountPage/UserOverview";
    }
}
