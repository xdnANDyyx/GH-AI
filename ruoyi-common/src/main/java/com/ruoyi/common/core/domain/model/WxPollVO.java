package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信登录轮询状态VO")
public class WxPollVO {

    @Schema(description = "状态: INIT-初始化, SUCCESS-成功, UNBOUND-未绑定, ERROR-错误, EXPIRED-过期")
    private String status;

    @Schema(description = "令牌(状态为SUCCESS时返回)")
    private String token;

    @Schema(description = "微信unionId(状态为UNBOUND时返回)")
    private String unionId;

    @Schema(description = "错误信息(状态为ERROR时返回)")
    private String error;
}
