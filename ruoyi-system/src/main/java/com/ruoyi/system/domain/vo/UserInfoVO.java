package com.ruoyi.system.domain.vo;

import com.ruoyi.common.core.domain.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "用户信息VO")
public class UserInfoVO {

    @Schema(description = "用户信息")
    private SysUser user;

    @Schema(description = "角色标识集合")
    private Set<String> roles;

    @Schema(description = "权限标识集合")
    private Set<String> permissions;

    @Schema(description = "是否初始密码未修改")
    private Boolean isDefaultModifyPwd;

    @Schema(description = "密码是否已过期")
    private Boolean isPasswordExpired;

}
