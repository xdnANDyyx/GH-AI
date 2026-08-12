package com.ruoyi.common.core.domain.model;

import java.util.List;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户授权角色VO")
public class UserAuthRoleVO {

    @Schema(description = "用户信息")
    private SysUser user;

    @Schema(description = "角色列表")
    private List<SysRole> roles;
}
