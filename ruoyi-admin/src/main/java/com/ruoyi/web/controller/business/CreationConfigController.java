package com.ruoyi.web.controller.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.domain.GhCreationConfig;
import com.ruoyi.business.dto.CreationConfigQueryDTO;
import com.ruoyi.business.dto.CreationConfigSaveDTO;
import com.ruoyi.business.service.IGhCreationConfigService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 创作配置 Controller
 *
 * @author xdn
 */
@Tag(name = "创作配置", description = "创作配置管理接口")
@RestController
@RequestMapping("/business/creationConfig")
public class CreationConfigController extends BaseController {

    @Autowired
    private IGhCreationConfigService creationConfigService;

    /**
     * 查询配置列表
     */
    @Operation(summary = "查询配置列表", description = "分页查询创作配置列表")
    @PreAuthorize("@ss.hasPermi('gh:config:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhCreationConfig> list(CreationConfigQueryDTO query) {
        Page<GhCreationConfig> page = creationConfigService.listConfig(query);
        TableDataInfo<GhCreationConfig> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * 获取配置详细信息
     */
    @Operation(summary = "获取配置详情", description = "根据ID获取创作配置详细信息")
    @PreAuthorize("@ss.hasPermi('gh:config:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhCreationConfig> getInfo(@Parameter(description = "配置ID") @PathVariable Long id) {
        return success(creationConfigService.getById(id));
    }

    /**
     * 根据分组获取所有配置
     */
    @Operation(summary = "按分组获取配置", description = "根据配置分组获取所有启用的配置")
    @PreAuthorize("@ss.hasPermi('gh:config:query') or @ss.hasRole('admin')")
    @GetMapping("/group/{group}")
    public AjaxResult<List<GhCreationConfig>> getByGroup(@Parameter(description = "配置分组") @PathVariable String group) {
        return success(creationConfigService.listByGroup(group));
    }

    /**
     * 根据分组和键获取配置
     */
    @Operation(summary = "按分组和键获取配置", description = "根据配置分组和键获取配置")
    @PreAuthorize("@ss.hasPermi('gh:config:query') or @ss.hasRole('admin')")
    @GetMapping("/groupKey")
    public AjaxResult<GhCreationConfig> getByGroupKey(
            @Parameter(description = "配置分组") String configGroup,
            @Parameter(description = "配置键") String configKey) {
        return success(creationConfigService.getByGroupKey(configGroup, configKey));
    }

    /**
     * 公开接口：根据分组获取所有启用的配置（供前台调用）
     */
    @Operation(summary = "公开-按分组获取配置", description = "前台公开接口，根据分组获取启用的配置")
    @GetMapping("/public/group/{group}")
    public AjaxResult<List<GhCreationConfig>> getPublicByGroup(@Parameter(description = "配置分组") @PathVariable String group) {
        return success(creationConfigService.listByGroup(group));
    }

    /**
     * 新增配置
     */
    @Operation(summary = "新增配置", description = "新增创作配置")
    @PreAuthorize("@ss.hasPermi('gh:config:add') or @ss.hasRole('admin')")
    @Log(title = "创作配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreationConfigSaveDTO dto) {
        GhCreationConfig config = new GhCreationConfig();
        BeanUtils.copyProperties(dto, config);
        return toAjax(creationConfigService.addConfig(config, getUsername()));
    }

    /**
     * 修改配置
     */
    @Operation(summary = "修改配置", description = "修改创作配置")
    @PreAuthorize("@ss.hasPermi('gh:config:edit') or @ss.hasRole('admin')")
    @Log(title = "创作配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CreationConfigSaveDTO dto) {
        GhCreationConfig config = new GhCreationConfig();
        BeanUtils.copyProperties(dto, config);
        return toAjax(creationConfigService.updateConfig(config, getUsername()));
    }

    /**
     * 删除配置
     */
    @Operation(summary = "删除配置", description = "批量删除创作配置")
    @PreAuthorize("@ss.hasPermi('gh:config:remove') or @ss.hasRole('admin')")
    @Log(title = "创作配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "配置ID数组") @PathVariable Long[] ids) {
        return toAjax(creationConfigService.removeConfigByIds(ids));
    }

    /**
     * 更新配置状态
     */
    @Operation(summary = "更新配置状态", description = "启用或停用创作配置")
    @PreAuthorize("@ss.hasPermi('gh:config:edit') or @ss.hasRole('admin')")
    @Log(title = "创作配置", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult updateStatus(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Parameter(description = "状态（0启用 1停用）") @PathVariable String status) {
        return toAjax(creationConfigService.updateStatus(id, status, getUsername()));
    }
}
