package com.ruoyi.common.core.domain.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色部门树VO")
public class RoleDeptTreeVO {

    @Schema(description = "已选中的部门ID列表")
    private List<Long> checkedKeys;

    @Schema(description = "部门树列表")
    private Object depts;
}
