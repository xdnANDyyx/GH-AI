package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "客户手机号修改请求体")
public class CustomerPhoneUpdateBody {

    @NotBlank(message = "原手机号验证码不能为空")
    @Schema(description = "原手机号验证码", required = true)
    private String oldPhoneCode;

    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式不正确")
    @Schema(description = "新手机号", required = true)
    private String newPhone;

    @NotBlank(message = "新手机号验证码不能为空")
    @Schema(description = "新手机号验证码", required = true)
    private String newPhoneCode;
}
