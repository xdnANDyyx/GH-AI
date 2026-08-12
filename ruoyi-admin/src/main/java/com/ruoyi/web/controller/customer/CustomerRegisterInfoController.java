package com.ruoyi.web.controller.customer;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.UserRegisterInfo;
import com.ruoyi.system.service.IUserRegisterInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册信息Controller
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@RestController
@RequestMapping("/customer/register/info")
@RequiredArgsConstructor
public class CustomerRegisterInfoController {

    private final IUserRegisterInfoService userRegisterInfoService;

    /**
     * 提交注册信息
     */
    @PostMapping("/submit")
    public AjaxResult<Void> submitRegisterInfo(@RequestBody UserRegisterInfo registerInfo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录");
        }

        registerInfo.setUserId(loginUser.getUser().getUserId());
        userRegisterInfoService.saveOrUpdateUserRegisterInfo(registerInfo);
        
        return AjaxResult.success("提交成功");
    }

    /**
     * 获取用户注册信息
     */
    @GetMapping("/info")
    public AjaxResult<UserRegisterInfo> getRegisterInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录");
        }

        UserRegisterInfo registerInfo = userRegisterInfoService.selectUserRegisterInfoByUserId(loginUser.getUser().getUserId());
        
        if (registerInfo == null) {
            registerInfo = new UserRegisterInfo();
            registerInfo.setUserId(loginUser.getUser().getUserId());
        }
        
        return AjaxResult.success(registerInfo);
    }
}