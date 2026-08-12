package com.ruoyi.common.core.domain.model;

import java.util.List;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户详情VO")
public class UserDetailVO {

    @Schema(description = "用户信息")
    private SysUser data;

    @Schema(description = "岗位ID列表")
    private List<Long> postIds;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "角色列表")
    private List<SysRole> roles;

    @Schema(description = "岗位列表")
    private Object posts;
}
