package com.ruoyi.web.controller.business;

import com.ruoyi.business.domain.GhFeatureToggle;
import com.ruoyi.business.service.IGhFeatureToggleService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能开关Controller
 *
 * @author guanghe
 * @date 2026-08-04
 */
@RestController
@RequestMapping("/business/featureToggle")
public class FeatureToggleController extends BaseController {

    @Autowired
    private IGhFeatureToggleService ghFeatureToggleService;

    /**
     * 查询功能开关列表
     */
    @PreAuthorize("@ss.hasPermi('gh:featureToggle:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(GhFeatureToggle toggle) {
        startPage();
        List<GhFeatureToggle> list = ghFeatureToggleService.selectGhFeatureToggleList(toggle);
        return getDataTable(list);
    }

    /**
     * 查询所有启用的功能开关（无需分页，供前台调用）
     */
    @GetMapping("/enabled")
    public AjaxResult enabledToggles() {
        List<GhFeatureToggle> list = ghFeatureToggleService.selectEnabledToggles();
        return success(list);
    }

    /**
     * 公开接口：查询所有启用的功能开关（供前台无登录调用）
     */
    @GetMapping("/public/enabled")
    public AjaxResult publicEnabledToggles() {
        List<GhFeatureToggle> list = ghFeatureToggleService.selectEnabledToggles();
        return success(list);
    }

    /**
     * 公开接口：查询所有功能开关（含关闭的，供前台判断功能是否可用）
     */
    @GetMapping("/public/list")
    public AjaxResult publicListAll() {
        List<GhFeatureToggle> list = ghFeatureToggleService.selectGhFeatureToggleList(new GhFeatureToggle());
        return success(list);
    }

    /**
     * 根据ID获取详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(ghFeatureToggleService.selectGhFeatureToggleById(id));
    }

    /**
     * 根据toggleKey获取详情
     */
    @GetMapping("/key/{toggleKey}")
    public AjaxResult getInfoByKey(@PathVariable String toggleKey) {
        return success(ghFeatureToggleService.selectGhFeatureToggleByKey(toggleKey));
    }

    /**
     * 新增功能开关
     */
    @PreAuthorize("@ss.hasPermi('gh:featureToggle:add') or @ss.hasRole('admin')")
    @PostMapping
    public AjaxResult add(@RequestBody GhFeatureToggle toggle) {
        return toAjax(ghFeatureToggleService.insertGhFeatureToggle(toggle));
    }

    /**
     * 修改功能开关
     */
    @PreAuthorize("@ss.hasPermi('gh:featureToggle:edit') or @ss.hasRole('admin')")
    @PutMapping
    public AjaxResult edit(@RequestBody GhFeatureToggle toggle) {
        return toAjax(ghFeatureToggleService.updateGhFeatureToggle(toggle));
    }

    /**
     * 批量更新开关状态
     */
    @PreAuthorize("@ss.hasPermi('gh:featureToggle:edit') or @ss.hasRole('admin')")
    @PutMapping("/batchStatus")
    public AjaxResult batchStatus(@RequestBody List<GhFeatureToggle> list) {
        return toAjax(ghFeatureToggleService.updateToggleStatusBatch(list));
    }

    /**
     * 删除功能开关
     */
    @PreAuthorize("@ss.hasPermi('gh:featureToggle:remove') or @ss.hasRole('admin')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ghFeatureToggleService.deleteGhFeatureToggleByIds(ids));
    }
}