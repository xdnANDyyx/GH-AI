package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "客户信息修改请求体")
public class CustomerProfileUpdateBody {

    @Size(max = 30, message = "客户昵称长度不能超过30个字符")
    @Schema(description = "客户昵称")
    private String nickName;

    @Schema(description = "客户头像地址")
    private String avatar;
}
