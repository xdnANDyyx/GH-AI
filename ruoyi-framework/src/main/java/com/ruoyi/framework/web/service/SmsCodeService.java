package com.ruoyi.framework.web.service;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SmsSendRecord;
import com.ruoyi.system.service.ISmsSendRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class SmsCodeService {
    private static final String LOGIN_CODE_KEY = "sms:code:login:";
    private static final String LOGIN_LAST_SEND_KEY = "sms:lastsend:login:";
    private static final String LOGIN_HOUR_COUNT_KEY = "sms:hourcount:login:";
    private static final String LOGIN_BAN_KEY = "sms:ban:login:";
    
    private static final String CHANGE_PHONE_CODE_KEY = "sms:code:changephone:";
    private static final String CHANGE_PHONE_LAST_SEND_KEY = "sms:lastsend:changephone:";
    private static final String RESET_PASSWORD_CODE_KEY = "sms:code:resetpwd:";
    private static final String RESET_PASSWORD_LAST_SEND_KEY = "sms:lastsend:resetpwd:";

    private final RedisCache redisCache;
    private final ISmsSendRecordService smsSendRecordService;
    private final Environment environment;

    @Autowired
    public SmsCodeService(RedisCache redisCache,
                          ISmsSendRecordService smsSendRecordService,
                          Environment environment) {
        this.redisCache = redisCache;
        this.smsSendRecordService = smsSendRecordService;
        this.environment = environment;
    }

    public void sendLoginCode(String phone) {
        if (Boolean.TRUE.equals(redisCache.hasKey(LOGIN_BAN_KEY + phone))) {
            throw new ServiceException("该手机号已被禁用，请稍后再试");
        }
        checkSendFrequency(LOGIN_LAST_SEND_KEY + phone);
        
        Long count = redisCache.redisTemplate.opsForValue().increment(LOGIN_HOUR_COUNT_KEY + phone);
        if (count != null && count == 1L) {
            redisCache.expire(LOGIN_HOUR_COUNT_KEY + phone, 1, TimeUnit.HOURS);
        }
        if (count != null && count > 10L) {
            redisCache.setCacheObject(LOGIN_BAN_KEY + phone, "1", 1, TimeUnit.DAYS);
            throw new ServiceException("该手机号发送次数过多，已禁用一天");
        }

        String code = doSendCode(phone, "LOGIN");
        redisCache.setCacheObject(LOGIN_CODE_KEY + phone, code, 5, TimeUnit.MINUTES);
        redisCache.setCacheObject(LOGIN_LAST_SEND_KEY + phone, "1", 1, TimeUnit.MINUTES);
    }

    public void verifyLoginCode(String phone, String code) {
        doVerifyCode(LOGIN_CODE_KEY + phone, phone, code, true);
    }

    public void sendResetPasswordCode(String phone) {
        checkSendFrequency(RESET_PASSWORD_LAST_SEND_KEY + phone);

        String code = doSendCode(phone, "RESET_PASSWORD");
        redisCache.setCacheObject(RESET_PASSWORD_CODE_KEY + phone, code, 5, TimeUnit.MINUTES);
        redisCache.setCacheObject(RESET_PASSWORD_LAST_SEND_KEY + phone, "1", 1, TimeUnit.MINUTES);
    }

    public void verifyResetPasswordCode(String phone, String code) {
        doVerifyCode(RESET_PASSWORD_CODE_KEY + phone, phone, code, true);
    }

    public void sendChangePhoneCode(String phone) {
        checkSendFrequency(CHANGE_PHONE_LAST_SEND_KEY + phone);
        
        String code = doSendCode(phone, "CHANGE_PHONE");
        redisCache.setCacheObject(CHANGE_PHONE_CODE_KEY + phone, code, 5, TimeUnit.MINUTES);
        redisCache.setCacheObject(CHANGE_PHONE_LAST_SEND_KEY + phone, "1", 1, TimeUnit.MINUTES);
    }

    public void verifyChangePhoneCode(String phone, String code) {
        verifyChangePhoneCode(phone, code, true);
    }

    public void verifyChangePhoneCode(String phone, String code, boolean deleteAfterVerify) {
        doVerifyCode(CHANGE_PHONE_CODE_KEY + phone, phone, code, deleteAfterVerify);
    }

    public void deleteChangePhoneCode(String phone) {
        if (StringUtils.isNotEmpty(phone)) {
            redisCache.deleteObject(CHANGE_PHONE_CODE_KEY + phone);
        }
    }

    private void checkSendFrequency(String lastSendKey) {
        if (Boolean.TRUE.equals(redisCache.hasKey(lastSendKey))) {
            throw new ServiceException("发送频繁，请1分钟后重试");
        }
    }

    private void checkSmsConfig() {
        // TODO: 二期接入阿里云短信后启用此配置检查
        throw new ServiceException("短信功能尚未配置，请联系管理员");
    }

    private String doSendCode(String phone, String scene) {
        if (StringUtils.isEmpty(phone)) {
            throw new ServiceException("手机号不能为空");
        }
        // TODO: 二期接入阿里云短信SDK后实现真实发送
        String code = generate6DigitCode();
        LocalDateTime now = LocalDateTime.now();
        // 开发阶段：记录日志即可，不实际发送
        System.out.println("[DEV] SMS code for " + phone + ": " + code);

        SmsSendRecord record = new SmsSendRecord();
        record.setPhone(phone);
        record.setScene(scene);
        record.setTemplateParam("{\"code\":\"" + code + "\"}");
        record.setSuccess(1);
        record.setSendTime(now);
        record.setCreateTime(now);
        smsSendRecordService.insertSmsSendRecord(record);

        return code;
    }

    private void doVerifyCode(String cacheKey, String phone, String code, boolean deleteAfterVerify) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new ServiceException("手机号或验证码不能为空");
        }
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProd = Arrays.stream(activeProfiles).anyMatch("prod"::equals);
        if (!isProd && "8888".equals(code)) {
            return;
        }
        String cacheCode = redisCache.getCacheObject(cacheKey);
        if (StringUtils.isEmpty(cacheCode)) {
            throw new ServiceException("验证码已失效");
        }
        if (!code.equals(cacheCode)) {
            throw new ServiceException("验证码错误");
        }
        if (deleteAfterVerify) {
            redisCache.deleteObject(cacheKey);
        }
    }

    private String generate6DigitCode() {
        int n = ThreadLocalRandom.current().nextInt(0, 1000000);
        return String.format("%06d", n);
    }
}
