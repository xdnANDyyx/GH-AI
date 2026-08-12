package com.ruoyi.common.core.domain.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单树VO")
public class MenuTreeVO {

    @Schema(description = "已选中的菜单ID列表")
    private List<Long> checkedKeys;

    @Schema(description = "菜单树列表")
    private Object menus;
}
