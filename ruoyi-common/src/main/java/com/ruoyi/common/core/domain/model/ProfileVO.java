package com.ruoyi.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "个人信息VO")
public class ProfileVO {

    @Schema(description = "用户信息")
    private Object user;

    @Schema(description = "角色组")
    private String roleGroup;

    @Schema(description = "岗位组")
    private String postGroup;
}
