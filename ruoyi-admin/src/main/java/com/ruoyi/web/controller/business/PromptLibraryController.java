package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhPromptLibrary;
import com.ruoyi.business.dto.PromptLibraryQueryDTO;
import com.ruoyi.business.dto.PromptLibrarySaveDTO;
import com.ruoyi.business.service.IGhPromptLibraryService;
import com.ruoyi.common.annotation.Log;
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
 * 提示词选项库 Controller（后台管理端）
 *
 * @author guanghe
 */
@Tag(name = "提示词选项库", description = "光合AI提示词选项库管理接口")
@RestController
@RequestMapping("/business/promptLibrary")
public class PromptLibraryController extends BaseController {

    @Autowired
    private IGhPromptLibraryService promptLibraryService;

    /**
     * 查询选项列表
     */
    @Operation(summary = "查询选项列表", description = "分页查询提示词选项库列表")
    @PreAuthorize("@ss.hasPermi('gh:promptLibrary:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhPromptLibrary> list(PromptLibraryQueryDTO query) {
        startPage();
        List<GhPromptLibrary> list = promptLibraryService.listLibrary(query);
        return getDataTable(list);
    }

    /**
     * 获取选项详细信息
     */
    @Operation(summary = "获取选项详情", description = "根据ID获取提示词选项详细信息")
    @PreAuthorize("@ss.hasPermi('gh:promptLibrary:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhPromptLibrary> getInfo(@Parameter(description = "选项ID") @PathVariable Long id) {
        return success(promptLibraryService.getLibraryById(id));
    }

    /**
     * 新增选项
     */
    @Operation(summary = "新增选项", description = "新增提示词选项")
    @PreAuthorize("@ss.hasPermi('gh:promptLibrary:add') or @ss.hasRole('admin')")
    @Log(title = "提示词选项库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PromptLibrarySaveDTO dto) {
        GhPromptLibrary library = new GhPromptLibrary();
        BeanUtils.copyProperties(dto, library);
        return toAjax(promptLibraryService.addLibrary(library, getUsername()));
    }

    /**
     * 修改选项
     */
    @Operation(summary = "修改选项", description = "修改提示词选项")
    @PreAuthorize("@ss.hasPermi('gh:promptLibrary:edit') or @ss.hasRole('admin')")
    @Log(title = "提示词选项库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PromptLibrarySaveDTO dto) {
        GhPromptLibrary library = new GhPromptLibrary();
        BeanUtils.copyProperties(dto, library);
        return toAjax(promptLibraryService.updateLibrary(library, getUsername()));
    }

    /**
     * 删除选项
     */
    @Operation(summary = "删除选项", description = "批量删除提示词选项")
    @PreAuthorize("@ss.hasPermi('gh:promptLibrary:remove') or @ss.hasRole('admin')")
    @Log(title = "提示词选项库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "选项ID数组") @PathVariable Long[] ids) {
        return toAjax(promptLibraryService.removeLibraryByIds(ids));
    }
}