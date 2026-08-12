package com.ruoyi.web.controller.business;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.business.domain.GhAiModel;
import com.ruoyi.business.service.IGhAiModelService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * AI模特管理 Controller
 *
 * @author guanghe
 */
@Tag(name = "AI模特管理", description = "AI模特管理接口")
@RestController
@RequestMapping("/business/aiModel")
public class AiModelController extends BaseController {

    @Autowired
    private IGhAiModelService aiModelService;

    /**
     * 查询模特列表（管理端）
     */
    @Operation(summary = "查询模特列表", description = "分页查询AI模特列表")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhAiModel> list(GhAiModel model) {
        startPage();
        LambdaQueryWrapper<GhAiModel> wrapper = new LambdaQueryWrapper<GhAiModel>()
                .like(model.getName() != null, GhAiModel::getName, model.getName())
                .eq(model.getGender() != null, GhAiModel::getGender, model.getGender())
                .eq(model.getAgeGroup() != null, GhAiModel::getAgeGroup, model.getAgeGroup())
                .eq(model.getEthnicity() != null, GhAiModel::getEthnicity, model.getEthnicity())
                .eq(model.getStatus() != null, GhAiModel::getStatus, model.getStatus())
                .orderByAsc(GhAiModel::getSort)
                .orderByDesc(GhAiModel::getCreateTime);
        List<GhAiModel> list = aiModelService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 获取已发布的模特列表（前台公开接口）
     */
    @Operation(summary = "获取已发布模特", description = "获取所有已发布且已授权的AI模特列表（前台公开）")
    @GetMapping("/published")
    public AjaxResult<List<GhAiModel>> listPublished() {
        LambdaQueryWrapper<GhAiModel> wrapper = new LambdaQueryWrapper<GhAiModel>()
                .eq(GhAiModel::getStatus, "0")
                .eq(GhAiModel::getCommercialAuth, "0")
                .orderByAsc(GhAiModel::getSort)
                .orderByDesc(GhAiModel::getCreateTime);
        return success(aiModelService.list(wrapper));
    }

    /**
     * 公开接口：获取已发布的模特列表（供前台无登录调用）
     */
    @Operation(summary = "公开-获取已发布模特", description = "前台公开接口，获取已发布且已授权的AI模特列表")
    @GetMapping("/public/published")
    public AjaxResult<List<GhAiModel>> publicListPublished() {
        return listPublished();
    }

    /**
     * 获取模特详细信息
     */
    @Operation(summary = "获取模特详情", description = "根据ID获取AI模特详细信息")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhAiModel> getInfo(@Parameter(description = "模特ID") @PathVariable Long id) {
        return success(aiModelService.selectModelById(id));
    }

    /**
     * 新增模特
     */
    @Operation(summary = "新增模特", description = "新增AI模特")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:add') or @ss.hasRole('admin')")
    @Log(title = "AI模特管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GhAiModel model) {
        return toAjax(aiModelService.insertModel(model, getUsername()));
    }

    /**
     * 修改模特
     */
    @Operation(summary = "修改模特", description = "修改AI模特信息")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:edit') or @ss.hasRole('admin')")
    @Log(title = "AI模特管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GhAiModel model) {
        return toAjax(aiModelService.updateModel(model, getUsername()));
    }

    /**
     * 删除模特
     */
    @Operation(summary = "删除模特", description = "批量删除AI模特")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:remove') or @ss.hasRole('admin')")
    @Log(title = "AI模特管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "模特ID数组") @PathVariable Long[] ids) {
        return toAjax(aiModelService.deleteModelsByIds(ids));
    }

    /**
     * 更新模特状态（发布/草稿）
     */
    @Operation(summary = "更新模特状态", description = "更新AI模特状态（0-已发布, 1-草稿）")
    @PreAuthorize("@ss.hasPermi('gh:aiModel:edit') or @ss.hasRole('admin')")
    @Log(title = "AI模特管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult updateStatus(
            @Parameter(description = "模特ID") @PathVariable Long id,
            @Parameter(description = "状态（0已发布 1草稿）") @PathVariable String status) {
        return toAjax(aiModelService.updateStatus(id, status, getUsername()));
    }
}