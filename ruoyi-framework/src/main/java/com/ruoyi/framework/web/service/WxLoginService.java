package com.ruoyi.framework.web.service;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.model.WxBindBody;
import com.ruoyi.common.core.domain.model.WxPollVO;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.enums.UserType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class WxLoginService {

    private static final String WX_LOGIN_STATE_KEY = "wx:login:state:";
    private static final String WX_LOGIN_UNION_KEY = "wx:login:union:";
    private static final String WX_LOGIN_STATUS_INIT = "INIT";
    private static final String WX_LOGIN_STATUS_SUCCESS = "SUCCESS";
    private static final String WX_LOGIN_STATUS_UNBOUND = "UNBOUND";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TokenService tokenService;

    public String createState() {
        String state = IdUtils.fastUUID();
        redisCache.setCacheObject(WX_LOGIN_STATE_KEY + state, WX_LOGIN_STATUS_INIT, 5, TimeUnit.MINUTES);
        return state;
    }

    public WxPollVO poll(String state) {
        ensureStateExists(state);
        String status = redisCache.getCacheObject(WX_LOGIN_STATE_KEY + state);
        String unionId = redisCache.getCacheObject(WX_LOGIN_UNION_KEY + state);

        WxPollVO vo = new WxPollVO();
        if (StringUtils.isEmpty(status)) {
            vo.setStatus("EXPIRED");
            vo.setError("二维码已过期");
            return vo;
        }
        vo.setStatus(status);
        if (WX_LOGIN_STATUS_UNBOUND.equals(status)) {
            vo.setUnionId(unionId);
            return vo;
        }
        if (WX_LOGIN_STATUS_SUCCESS.equals(status)) {
            SysUser user = userService.selectUserByWechatUnionId(unionId);
            if (user == null) {
                vo.setStatus(WX_LOGIN_STATUS_UNBOUND);
                vo.setUnionId(unionId);
                return vo;
            }
            validateUser(user);
            LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
            vo.setToken(tokenService.createToken(loginUser));
        }
        return vo;
    }

    public WxPollVO confirm(String state, String unionId) {
        ensureStateExists(state);
        if (StringUtils.isEmpty(unionId)) {
            throw new ServiceException("unionId不能为空");
        }

        redisCache.setCacheObject(WX_LOGIN_UNION_KEY + state, unionId, 5, TimeUnit.MINUTES);
        SysUser user = userService.selectUserByWechatUnionId(unionId);
        String status = user == null ? WX_LOGIN_STATUS_UNBOUND : WX_LOGIN_STATUS_SUCCESS;
        redisCache.setCacheObject(WX_LOGIN_STATE_KEY + state, status, 5, TimeUnit.MINUTES);

        WxPollVO vo = new WxPollVO();
        vo.setStatus(status);
        if (WX_LOGIN_STATUS_UNBOUND.equals(status)) {
            vo.setUnionId(unionId);
        }
        if (WX_LOGIN_STATUS_SUCCESS.equals(status)) {
            validateUser(user);
            LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
            vo.setToken(tokenService.createToken(loginUser));
        }
        return vo;
    }

    public WxPollVO bindAndLogin(WxBindBody body) {
        ensureStateExists(body.getState());
        String unionId = redisCache.getCacheObject(WX_LOGIN_UNION_KEY + body.getState());
        if (StringUtils.isEmpty(unionId)) {
            throw new ServiceException("请先完成微信扫码确认");
        }

        smsCodeService.verifyLoginCode(body.getPhone(), body.getSmsCode());
        SysUser user = userService.selectUserByPhoneNumber(body.getPhone());
        if (user == null) {
            throw new ServiceException("该手机号未注册");
        }
        validateUser(user);

        SysUser boundUser = userService.selectUserByWechatUnionId(unionId);
        if (boundUser != null && !boundUser.getUserId().equals(user.getUserId())) {
            throw new ServiceException("该微信已绑定其他账号");
        }

        userService.updateWechatUnionId(user.getUserId(), unionId);
        redisCache.setCacheObject(WX_LOGIN_STATE_KEY + body.getState(), WX_LOGIN_STATUS_SUCCESS, 5, TimeUnit.MINUTES);

        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(userService.selectUserById(user.getUserId()));
        WxPollVO vo = new WxPollVO();
        vo.setStatus(WX_LOGIN_STATUS_SUCCESS);
        vo.setToken(tokenService.createToken(loginUser));
        return vo;
    }

    private void ensureStateExists(String state) {
        if (StringUtils.isEmpty(state)) {
            throw new ServiceException("state不能为空");
        }
        if (!Boolean.TRUE.equals(redisCache.hasKey(WX_LOGIN_STATE_KEY + state))) {
            throw new ServiceException("二维码已过期，请刷新后重试");
        }
    }

    private void validateUser(SysUser user) {
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("用户已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("用户已被停用");
        }
    }

    public String mockUnionId() {
        return "wx-dev-" + LocalDateTime.now().toString().replace(':', '-');
    }
}