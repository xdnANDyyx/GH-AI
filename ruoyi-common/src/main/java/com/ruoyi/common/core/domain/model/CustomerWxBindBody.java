package com.ruoyi.common.core.domain.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CustomerWxBindBody {
    
    @NotBlank(message = "state不能为空")
    private String state;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    /** 邀请码 */
    private String inviteCode;
}
