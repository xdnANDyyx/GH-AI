package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/customer")
public class CustomerController extends BaseController {
    
    @Autowired
    private ISysUserService userService;

    /**
     * 获取当前登录客户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult<SysUser> getCurrentCustomerInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return error("用户未登录，请先登录");
        }
        SysUser user = loginUser.getUser();
        if (!"2".equals(user.getUserType())) {
            return error("非客户用户");
        }
        return success(user);
    }

    /**
     * 查询客户列表
     */
    @PreAuthorize("@ss.hasPermi('system:customer:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser user) {
        startPage();
        user.setUserType("2");
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 获取客户详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:customer:query') or @ss.hasRole('admin')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return error("客户不存在");
        }
        if (!"2".equals(user.getUserType())) {
            return error("非客户用户");
        }
        return success(user);
    }

    /**
     * 修改客户状态
     */
    @PreAuthorize("@ss.hasPermi('system:customer:edit') or @ss.hasRole('admin')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        SysUser sysUser = userService.selectUserById(user.getUserId());
        if (sysUser == null) {
            return error("客户不存在");
        }
        if (!"2".equals(sysUser.getUserType())) {
            return error("非客户用户");
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }
}
