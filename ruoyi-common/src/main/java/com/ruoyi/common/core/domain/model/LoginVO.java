package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录返回VO")
public class LoginVO {

    @Schema(description = "令牌")
    private String token;
}
