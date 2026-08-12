package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhPointsPackage;
import com.ruoyi.business.dto.PointsPackageQueryDTO;
import com.ruoyi.business.dto.PointsPackageSaveDTO;
import com.ruoyi.business.service.IGhPointsPackageService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分套餐控制器
 *
 * @author guanghe
 */
@Tag(name = "积分套餐", description = "积分套餐管理相关接口")
@RestController
@RequestMapping("/business/package")
public class PointsPackageController extends BaseController {

    @Autowired
    private IGhPointsPackageService pointsPackageService;

    /**
     * 查询积分套餐列表
     */
    @Operation(summary = "查询积分套餐列表", description = "根据条件分页查询积分套餐列表")
    @PreAuthorize("@ss.hasPermi('gh:package:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhPointsPackage> list(PointsPackageQueryDTO query) {
        startPage();
        List<GhPointsPackage> list = pointsPackageService.listPointsPackage(query);
        return getDataTable(list);
    }

    /**
     * 导出积分套餐列表
     */
    @Operation(summary = "导出积分套餐", description = "导出积分套餐列表")
    @PreAuthorize("@ss.hasPermi('gh:package:export') or @ss.hasRole('admin')")
    @Log(title = "积分套餐", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, PointsPackageQueryDTO query) {
        List<GhPointsPackage> list = pointsPackageService.listPointsPackage(query);
        ExcelUtil<GhPointsPackage> util = new ExcelUtil<>(GhPointsPackage.class);
        util.exportExcel(response, list, "积分套餐数据");
    }

    /**
     * 获取积分套餐详细信息
     */
    @Operation(summary = "获取积分套餐详情", description = "根据ID获取积分套餐详细信息")
    @PreAuthorize("@ss.hasPermi('gh:package:query') or @ss.hasRole('admin')")
    @GetMapping(value = "/{id}")
    public AjaxResult<GhPointsPackage> getInfo(@PathVariable("id") Long id) {
        return success(pointsPackageService.getPointsPackageById(id));
    }

    /**
     * 新增积分套餐
     */
    @Operation(summary = "新增积分套餐", description = "新增积分套餐")
    @PreAuthorize("@ss.hasPermi('gh:package:add') or @ss.hasRole('admin')")
    @Log(title = "积分套餐", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult<Void> add(@Validated @RequestBody PointsPackageSaveDTO saveDTO) {
        GhPointsPackage pointsPackage = new GhPointsPackage();
        BeanUtils.copyProperties(saveDTO, pointsPackage);
        pointsPackage.setCreateBy(getUsername());
        return toAjax(pointsPackageService.addPointsPackage(pointsPackage, getUsername()));
    }

    /**
     * 修改积分套餐
     */
    @Operation(summary = "修改积分套餐", description = "修改积分套餐")
    @PreAuthorize("@ss.hasPermi('gh:package:edit') or @ss.hasRole('admin')")
    @Log(title = "积分套餐", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult<Void> edit(@Validated @RequestBody PointsPackageSaveDTO saveDTO) {
        GhPointsPackage pointsPackage = new GhPointsPackage();
        BeanUtils.copyProperties(saveDTO, pointsPackage);
        return toAjax(pointsPackageService.updatePointsPackage(pointsPackage, getUsername()));
    }

    /**
     * 删除积分套餐
     */
    @Operation(summary = "删除积分套餐", description = "批量删除积分套餐")
    @PreAuthorize("@ss.hasPermi('gh:package:remove') or @ss.hasRole('admin')")
    @Log(title = "积分套餐", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult<Void> remove(@PathVariable Long[] ids) {
        return toAjax(pointsPackageService.removePointsPackageByIds(ids));
    }

    /**
     * 更新套餐上下架状态
     */
    @Operation(summary = "更新套餐状态", description = "更新积分套餐上下架状态")
    @PreAuthorize("@ss.hasPermi('gh:package:edit') or @ss.hasRole('admin')")
    @Log(title = "积分套餐", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult<Void> changeStatus(@RequestBody GhPointsPackage pointsPackage) {
        return toAjax(pointsPackageService.updateStatus(pointsPackage.getId(), pointsPackage.getStatus(), getUsername()));
    }
}
