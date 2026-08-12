package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信登录二维码VO")
public class WxQrVO {

    @Schema(description = "状态标识")
    private String state;

    @Schema(description = "二维码链接")
    private String qrUrl;

    @Schema(description = "过期时间(秒)")
    private Integer expireSeconds;
}
