package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.model.LoginVO;
import com.ruoyi.common.core.domain.model.SmsLoginBody;
import com.ruoyi.common.core.domain.model.SmsSendCodeBody;
import com.ruoyi.common.core.domain.model.WxBindBody;
import com.ruoyi.common.core.domain.model.WxPollVO;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SmsCodeService;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.web.service.WxLoginService;
import com.ruoyi.system.domain.vo.RouterVo;
import com.ruoyi.system.domain.vo.UserInfoVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统登录", description = "系统管理员登录相关接口")
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private WxLoginService wxLoginService;

    @Operation(summary = "账号密码登录", description = "管理员使用用户名密码登录系统")
    @PostMapping("/login")
    public AjaxResult<LoginVO> login(@RequestBody LoginBody loginBody) {
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return AjaxResult.success(vo);
    }

    @Operation(summary = "发送短信验证码", description = "发送登录短信验证码")
    @PostMapping("/login/sms/sendCode")
    public AjaxResult<Void> sendSmsCode(@RequestBody SmsSendCodeBody body) {
        smsCodeService.sendLoginCode(body.getPhone());
        return AjaxResult.success();
    }

    @Operation(summary = "短信验证码登录", description = "管理员使用手机号和短信验证码登录")
    @PostMapping("/login/sms")
    public AjaxResult<LoginVO> loginBySms(@RequestBody SmsLoginBody body) {
        String token = loginService.loginBySms(body.getPhone(), body.getCode());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return AjaxResult.success(vo);
    }

    @Operation(summary = "初始化微信登录", description = "生成开发态微信登录状态标识")
    @PostMapping("/login/wechat/state")
    public AjaxResult<String> createWechatLoginState() {
        return AjaxResult.success(wxLoginService.createState());
    }

    @Operation(summary = "轮询微信登录状态", description = "根据 state 查询微信登录状态")
    @GetMapping("/login/wechat/poll")
    public AjaxResult<WxPollVO> pollWechatLogin(@RequestParam String state) {
        return AjaxResult.success(wxLoginService.poll(state));
    }

    @Operation(summary = "开发态模拟微信扫码确认", description = "开发环境下模拟微信扫码成功")
    @PostMapping("/login/wechat/mockConfirm")
    public AjaxResult<WxPollVO> mockConfirmWechatLogin(@RequestParam String state, @RequestParam(required = false) String unionId) {
        String currentUnionId = StringUtils.isNotEmpty(unionId) ? unionId : wxLoginService.mockUnionId();
        return AjaxResult.success(wxLoginService.confirm(state, currentUnionId));
    }

    @Operation(summary = "绑定微信并登录", description = "未绑定账号时通过手机号验证码绑定后完成登录")
    @PostMapping("/login/wechat/bind")
    public AjaxResult<WxPollVO> bindWechatLogin(@RequestBody WxBindBody body) {
        return AjaxResult.success(wxLoginService.bindAndLogin(body));
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息、角色和权限")
    @GetMapping("getInfo")
    public AjaxResult<UserInfoVO> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (!loginUser.getPermissions().equals(permissions)){
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setUser(user);
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        vo.setIsDefaultModifyPwd(initPasswordIsModify(user.getPwdUpdateDate()));
        vo.setIsPasswordExpired(passwordIsExpiration(user.getPwdUpdateDate()));
        return AjaxResult.success(vo);
    }
    
    @Operation(summary = "获取路由信息", description = "获取当前用户的菜单路由信息")
    @GetMapping("getRouters")
    public AjaxResult<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
    
    public boolean initPasswordIsModify(java.util.Date pwdUpdateDate) {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    public boolean passwordIsExpiration(java.util.Date pwdUpdateDate) {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0) {
            if (StringUtils.isNull(pwdUpdateDate)){
                return true;
            }
            java.util.Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}
