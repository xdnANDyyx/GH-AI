package com.ruoyi.web.controller.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.domain.GhPromptTemplate;
import com.ruoyi.business.dto.PromptTemplateQueryDTO;
import com.ruoyi.business.dto.PromptTemplateSaveDTO;
import com.ruoyi.business.service.IGhPromptTemplateService;
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
 * 提示词模板 Controller
 *
 * @author guanghe
 */
@Tag(name = "提示词模板", description = "提示词模板管理接口")
@RestController
@RequestMapping("/business/promptTemplate")
public class PromptTemplateController extends BaseController {

    @Autowired
    private IGhPromptTemplateService promptTemplateService;

    /**
     * 查询模板列表
     */
    @Operation(summary = "查询模板列表", description = "分页查询提示词模板列表")
    @PreAuthorize("@ss.hasPermi('gh:prompt:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhPromptTemplate> list(PromptTemplateQueryDTO query) {
        Page<GhPromptTemplate> page = promptTemplateService.listTemplate(query);
        TableDataInfo<GhPromptTemplate> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    /**
     * 获取模板详细信息
     */
    @Operation(summary = "获取模板详情", description = "根据ID获取提示词模板详细信息")
    @PreAuthorize("@ss.hasPermi('gh:prompt:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhPromptTemplate> getInfo(@Parameter(description = "模板ID") @PathVariable Long id) {
        return success(promptTemplateService.getTemplateById(id));
    }

    /**
     * 新增模板
     */
    @Operation(summary = "新增模板", description = "新增提示词模板")
    @PreAuthorize("@ss.hasPermi('gh:prompt:add') or @ss.hasRole('admin')")
    @Log(title = "提示词模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PromptTemplateSaveDTO dto) {
        GhPromptTemplate template = new GhPromptTemplate();
        BeanUtils.copyProperties(dto, template);
        return toAjax(promptTemplateService.addTemplate(template, getUsername()));
    }

    /**
     * 修改模板
     */
    @Operation(summary = "修改模板", description = "修改提示词模板")
    @PreAuthorize("@ss.hasPermi('gh:prompt:edit') or @ss.hasRole('admin')")
    @Log(title = "提示词模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PromptTemplateSaveDTO dto) {
        GhPromptTemplate template = new GhPromptTemplate();
        BeanUtils.copyProperties(dto, template);
        return toAjax(promptTemplateService.updateTemplate(template, getUsername()));
    }

    /**
     * 删除模板
     */
    @Operation(summary = "删除模板", description = "批量删除提示词模板")
    @PreAuthorize("@ss.hasPermi('gh:prompt:remove') or @ss.hasRole('admin')")
    @Log(title = "提示词模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "模板ID数组") @PathVariable Long[] ids) {
        return toAjax(promptTemplateService.removeTemplateByIds(ids));
    }

    /**
     * 设为默认模板
     */
    @Operation(summary = "设为默认模板", description = "将指定模板设为同模块下的默认模板")
    @PreAuthorize("@ss.hasPermi('gh:prompt:edit') or @ss.hasRole('admin')")
    @Log(title = "提示词模板", businessType = BusinessType.UPDATE)
    @PutMapping("/setDefault/{id}")
    public AjaxResult setDefault(@Parameter(description = "模板ID") @PathVariable Long id) {
        return toAjax(promptTemplateService.setDefault(id, getUsername()));
    }
}
