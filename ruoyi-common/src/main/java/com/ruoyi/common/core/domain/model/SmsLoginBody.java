package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "短信验证码登录请求")
public class SmsLoginBody {
    
    @Schema(description = "手机号", required = true, example = "13800138000")
    private String phone;
    
    @Schema(description = "短信验证码", required = true, example = "123456")
    private String code;
}
