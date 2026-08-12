package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhBanner;
import com.ruoyi.business.domain.GhBannerClickStat;
import com.ruoyi.business.dto.BannerQueryDTO;
import com.ruoyi.business.dto.BannerSaveDTO;
import com.ruoyi.business.service.IGhBannerClickStatService;
import com.ruoyi.business.service.IGhBannerService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Banner管理 Controller
 *
 * @author guanghe
 */
@Tag(name = "Banner管理", description = "Banner管理接口")
@RestController
@RequestMapping("/business/banner")
public class BannerController extends BaseController {

    @Autowired
    private IGhBannerService bannerService;
    
    @Autowired
    private IGhBannerClickStatService bannerClickStatService;

    /**
     * 查询Banner列表
     */
    @Operation(summary = "查询Banner列表", description = "分页查询Banner列表")
    @PreAuthorize("@ss.hasPermi('gh:banner:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<GhBanner> list(BannerQueryDTO query) {
        startPage();
        List<GhBanner> list = bannerService.listBanner(query);
        return getDataTable(list);
    }

    /**
     * 前台公开接口：获取已发布的Banner列表（无需权限）
     */
    @Operation(summary = "前台获取已发布Banner", description = "公开接口，仅返回已上架的Banner")
    @GetMapping("/public/list")
    public TableDataInfo<GhBanner> publicList(BannerQueryDTO query) {
        startPage();
        query.setStatus("0");
        List<GhBanner> list = bannerService.listBanner(query);
        return getDataTable(list);
    }

    /**
     * 获取Banner详细信息
     */
    @Operation(summary = "获取Banner详情", description = "根据ID获取Banner详细信息")
    @PreAuthorize("@ss.hasPermi('gh:banner:query') or @ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult<GhBanner> getInfo(@Parameter(description = "Banner ID") @PathVariable Long id) {
        return success(bannerService.getBannerById(id));
    }

    /**
     * 新增Banner
     */
    @Operation(summary = "新增Banner", description = "新增Banner信息")
    @PreAuthorize("@ss.hasPermi('gh:banner:add') or @ss.hasRole('admin')")
    @Log(title = "Banner管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BannerSaveDTO dto) {
        GhBanner banner = new GhBanner();
        BeanUtils.copyProperties(dto, banner);
        return toAjax(bannerService.addBanner(banner, getUsername()));
    }

    /**
     * 修改Banner
     */
    @Operation(summary = "修改Banner", description = "修改Banner信息")
    @PreAuthorize("@ss.hasPermi('gh:banner:edit') or @ss.hasRole('admin')")
    @Log(title = "Banner管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BannerSaveDTO dto) {
        GhBanner banner = new GhBanner();
        BeanUtils.copyProperties(dto, banner);
        return toAjax(bannerService.updateBanner(banner, getUsername()));
    }

    /**
     * 删除Banner
     */
    @Operation(summary = "删除Banner", description = "批量删除Banner")
    @PreAuthorize("@ss.hasPermi('gh:banner:remove') or @ss.hasRole('admin')")
    @Log(title = "Banner管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@Parameter(description = "Banner ID数组") @PathVariable Long[] ids) {
        return toAjax(bannerService.removeBannerByIds(ids));
    }

    /**
     * 更新Banner状态
     */
    @Operation(summary = "更新Banner状态", description = "上架或下架Banner")
    @PreAuthorize("@ss.hasPermi('gh:banner:edit') or @ss.hasRole('admin')")
    @Log(title = "Banner管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult updateStatus(
            @Parameter(description = "Banner ID") @PathVariable Long id,
            @Parameter(description = "状态（0上架 1下架）") @PathVariable String status) {
        return toAjax(bannerService.updateStatus(id, status, getUsername()));
    }

    /**
     * 增加Banner点击数
     */
    @Operation(summary = "增加点击数", description = "增加Banner点击次数")
    @PostMapping("/click/{id}")
    public AjaxResult incrementClick(@Parameter(description = "Banner ID") @PathVariable Long id) {
        return toAjax(bannerService.incrementClickCount(id));
    }

    /**
     * 更新Banner排序
     */
    @Operation(summary = "更新排序", description = "更新Banner排序")
    @PreAuthorize("@ss.hasPermi('gh:banner:edit') or @ss.hasRole('admin')")
    @Log(title = "Banner管理", businessType = BusinessType.UPDATE)
    @PutMapping("/sort/{id}/{sort}")
    public AjaxResult updateSort(
            @Parameter(description = "Banner ID") @PathVariable Long id,
            @Parameter(description = "排序值") @PathVariable Integer sort) {
        GhBanner banner = new GhBanner();
        banner.setId(id);
        banner.setSort(sort);
        banner.setUpdateBy(getUsername());
        return toAjax(bannerService.updateBanner(banner, getUsername()));
    }

    /**
     * 获取Banner点击趋势
     */
    @Operation(summary = "获取点击趋势", description = "查询Banner点击趋势数据")
    @PreAuthorize("@ss.hasPermi('gh:banner:query') or @ss.hasRole('admin')")
    @GetMapping("/clickTrend/{id}")
    public AjaxResult<List<GhBannerClickStat>> getClickTrend(
            @Parameter(description = "Banner ID") @PathVariable Long id,
            @Parameter(description = "开始日期") @RequestParam(required = false) Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) Date endDate) {
        List<GhBannerClickStat> trend = bannerClickStatService.getClickTrend(id, startDate, endDate);
        return success(trend);
    }
}
