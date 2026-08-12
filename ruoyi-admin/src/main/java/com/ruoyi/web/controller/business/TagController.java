package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhTag;
import com.ruoyi.business.dto.TagQueryDTO;
import com.ruoyi.business.dto.TagSaveDTO;
import com.ruoyi.business.service.IGhTagService;
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
 * 标签管理 Controller
 *
 * @author guanghe
 */
@Tag(name = "标签管理", description = "标签管理接口")
@RestController
@RequestMapping("/business/tag")
public class TagController extends BaseController {

    @Autowired
    private IGhTagService tagService;

    /**
     * 查询标签列表
     */
    @Operation(summary = "查询标签列表", description = "分页查询标签列表")
    @PreAuthorize("@ss.hasPermi('gh:tag:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhTag> list(TagQueryDTO query) {
        startPage();
        List<GhTag> list = tagService.listTag(query);
        return getDataTable(list);
    }

    /**
     * 根据类型查询标签列表
     */
    @Operation(summary = "按类型查询标签", description = "根据标签类型查询所有启用的标签")
    @PreAuthorize("@ss.hasPermi('gh:tag:list') or @ss.hasRole('admin')")
    @GetMapping("/type/{tagType}")
    public AjaxResult<List<GhTag>> listByType(@Parameter(description = "标签类型") @PathVariable String tagType) {
        return success(tagService.listByType(tagType));
    }

    /**
     * 获取标签详细信息
     */
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详细信息")
    @PreAuthorize("@ss.hasPermi('gh:tag:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhTag> getInfo(@Parameter(description = "标签ID") @PathVariable Long id) {
        return success(tagService.getById(id));
    }

    /**
     * 新增标签
     */
    @Operation(summary = "新增标签", description = "新增标签信息")
    @PreAuthorize("@ss.hasPermi('gh:tag:add') or @ss.hasRole('admin')")
    @Log(title = "标签管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody TagSaveDTO dto) {
        GhTag tag = new GhTag();
        BeanUtils.copyProperties(dto, tag);
        return toAjax(tagService.addTag(tag, getUsername()));
    }

    /**
     * 修改标签
     */
    @Operation(summary = "修改标签", description = "修改标签信息")
    @PreAuthorize("@ss.hasPermi('gh:tag:edit') or @ss.hasRole('admin')")
    @Log(title = "标签管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody TagSaveDTO dto) {
        GhTag tag = new GhTag();
        BeanUtils.copyProperties(dto, tag);
        return toAjax(tagService.updateTag(tag, getUsername()));
    }

    /**
     * 删除标签
     */
    @Operation(summary = "删除标签", description = "批量删除标签")
    @PreAuthorize("@ss.hasPermi('gh:tag:remove') or @ss.hasRole('admin')")
    @Log(title = "标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "标签ID数组") @PathVariable Long[] ids) {
        return toAjax(tagService.removeTagByIds(ids));
    }
}
