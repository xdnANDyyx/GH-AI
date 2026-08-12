package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "参数配置", description = "系统参数配置管理接口")
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {
    @Autowired
    private ISysConfigService configService;

    @Operation(summary = "获取参数配置列表", description = "分页查询参数配置列表")
    @PreAuthorize("@ss.hasPermi('system:config:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<SysConfig> list(SysConfig config) {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getDataTable(list);
    }

    @Operation(summary = "导出参数配置", description = "导出参数配置到Excel")
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:config:export') or @ss.hasRole('admin')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysConfig config) {
        List<SysConfig> list = configService.selectConfigList(config);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        util.exportExcel(response, list, "参数数据");
    }

    @Operation(summary = "获取参数配置详情", description = "根据参数ID获取参数配置详细信息")
    @PreAuthorize("@ss.hasPermi('system:config:query') or @ss.hasRole('admin')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@Parameter(description = "参数ID") @PathVariable Long configId) {
        return success(configService.selectConfigById(configId));
    }

    @Operation(summary = "根据键名查询参数值", description = "根据参数键名查询参数值")
    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@Parameter(description = "参数键名") @PathVariable String configKey) {
        return success(configService.selectConfigByKey(configKey));
    }

    @Operation(summary = "新增参数配置", description = "新增系统参数配置")
    @PreAuthorize("@ss.hasPermi('system:config:add') or @ss.hasRole('admin')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)){
            return error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(getUsername());
        return toAjax(configService.insertConfig(config));
    }

    @Operation(summary = "修改参数配置", description = "修改参数配置信息")
    @PreAuthorize("@ss.hasPermi('system:config:edit') or @ss.hasRole('admin')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)){
            return error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateConfig(config));
    }

    @Operation(summary = "删除参数配置", description = "批量删除参数配置")
    @PreAuthorize("@ss.hasPermi('system:config:remove') or @ss.hasRole('admin')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@Parameter(description = "参数ID数组") @PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return success();
    }

    @Operation(summary = "刷新参数缓存", description = "刷新系统参数缓存")
    @PreAuthorize("@ss.hasPermi('system:config:remove') or @ss.hasRole('admin')")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public AjaxResult refreshCache() {
        configService.resetConfigCache();
        return success();
    }
}
