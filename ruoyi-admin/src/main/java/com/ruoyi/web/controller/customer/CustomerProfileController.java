package com.ruoyi.web.controller.customer;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class CustomerProfileController {

    @GetMapping("/getInfo")
    public AjaxResult<SysUser> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录，请先登录");
        }

        SysUser user = loginUser.getUser();
        if (!"2".equals(user.getUserType())) {
            return AjaxResult.error("非客户用户");
        }

        return AjaxResult.success(user);
    }
}