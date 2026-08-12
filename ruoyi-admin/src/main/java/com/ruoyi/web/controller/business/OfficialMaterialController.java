package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhOfficialMaterial;
import com.ruoyi.business.dto.OfficialMaterialQueryDTO;
import com.ruoyi.business.dto.OfficialMaterialSaveDTO;
import com.ruoyi.business.service.IGhOfficialMaterialService;
import com.ruoyi.business.vo.OfficialMaterialVO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 官方素材管理 Controller
 *
 * @author guanghe
 */
@Tag(name = "官方素材管理", description = "官方素材管理接口")
@RestController
@RequestMapping("/business/officialMaterial")
public class OfficialMaterialController extends BaseController {

    @Autowired
    private IGhOfficialMaterialService materialService;

    /**
     * 查询官方素材列表
     */
    @Operation(summary = "查询官方素材列表", description = "分页查询官方素材列表")
    @PreAuthorize("@ss.hasPermi('gh:material:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<OfficialMaterialVO> list(OfficialMaterialQueryDTO query) {
        startPage();
        List<OfficialMaterialVO> list = materialService.listMaterial(query);
        return getDataTable(list);
    }

    /**
     * 前台公开接口：获取已发布的官方素材列表（无需权限）
     */
    @Operation(summary = "前台获取已发布官方素材", description = "公开接口，仅返回已上架的官方素材")
    @GetMapping("/public/list")
    public TableDataInfo<OfficialMaterialVO> publicList(OfficialMaterialQueryDTO query) {
        startPage();
        query.setStatus("0");
        List<OfficialMaterialVO> list = materialService.listMaterial(query);
        return getDataTable(list);
    }

    /**
     * 获取官方素材详细信息
     */
    @Operation(summary = "获取官方素材详情", description = "根据ID获取官方素材详细信息")
    @PreAuthorize("@ss.hasPermi('gh:material:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<OfficialMaterialVO> getInfo(@Parameter(description = "素材ID") @PathVariable Long id) {
        return success(materialService.getMaterialById(id));
    }

    /**
     * 新增官方素材
     */
    @Operation(summary = "新增官方素材", description = "新增官方素材信息")
    @PreAuthorize("@ss.hasPermi('gh:material:add') or @ss.hasRole('admin')")
    @Log(title = "官方素材管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OfficialMaterialSaveDTO dto) {
        GhOfficialMaterial material = new GhOfficialMaterial();
        BeanUtils.copyProperties(dto, material);
        return toAjax(materialService.addMaterial(material, dto.getTagIds(), getUsername()));
    }

    /**
     * 修改官方素材
     */
    @Operation(summary = "修改官方素材", description = "修改官方素材信息")
    @PreAuthorize("@ss.hasPermi('gh:material:edit') or @ss.hasRole('admin')")
    @Log(title = "官方素材管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OfficialMaterialSaveDTO dto) {
        GhOfficialMaterial material = new GhOfficialMaterial();
        BeanUtils.copyProperties(dto, material);
        return toAjax(materialService.updateMaterial(material, dto.getTagIds(), getUsername()));
    }

    /**
     * 删除官方素材
     */
    @Operation(summary = "删除官方素材", description = "批量删除官方素材")
    @PreAuthorize("@ss.hasPermi('gh:material:remove') or @ss.hasRole('admin')")
    @Log(title = "官方素材管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "素材ID数组") @PathVariable Long[] ids) {
        return toAjax(materialService.removeMaterialByIds(ids));
    }

    /**
     * 更新官方素材状态
     */
    @Operation(summary = "更新素材状态", description = "发布或下架素材")
    @PreAuthorize("@ss.hasPermi('gh:material:edit') or @ss.hasRole('admin')")
    @Log(title = "官方素材管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult updateStatus(
            @Parameter(description = "素材ID") @PathVariable Long id,
            @Parameter(description = "状态（0已发布 1已下架）") @PathVariable String status) {
        return toAjax(materialService.updateStatus(id, status, getUsername()));
    }

    /**
     * 批量上传素材
     */
    @Operation(summary = "批量上传素材", description = "批量上传多个素材")
    @PreAuthorize("@ss.hasPermi('gh:material:add') or @ss.hasRole('admin')")
    @Log(title = "官方素材管理", businessType = BusinessType.INSERT)
    @PostMapping("/batchUpload")
    public AjaxResult batchUpload(@RequestBody List<OfficialMaterialSaveDTO> dtos) {
        List<GhOfficialMaterial> materials = dtos.stream().map(dto -> {
            GhOfficialMaterial material = new GhOfficialMaterial();
            BeanUtils.copyProperties(dto, material);
            return material;
        }).toList();
        return toAjax(materialService.batchUpload(materials, getUsername()));
    }
}
