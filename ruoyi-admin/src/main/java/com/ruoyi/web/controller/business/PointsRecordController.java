package com.ruoyi.web.controller.business;

import com.ruoyi.business.dto.PointsAdjustDTO;
import com.ruoyi.business.dto.PointsRecordQueryDTO;
import com.ruoyi.business.service.IGhPointsRecordService;
import com.ruoyi.business.vo.PointsRecordVO;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分变动明细控制器
 *
 * @author guanghe
 */
@Tag(name = "积分变动明细", description = "积分变动明细管理相关接口")
@RestController
@RequestMapping("/business/points")
public class PointsRecordController extends BaseController {

    @Autowired
    private IGhPointsRecordService pointsRecordService;

    /**
     * 查询积分变动记录列表
     */
    @Operation(summary = "查询积分变动记录列表", description = "根据条件分页查询积分变动明细列表")
    @PreAuthorize("@ss.hasPermi('gh:points:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<PointsRecordVO> list(PointsRecordQueryDTO query) {
        startPage();
        List<PointsRecordVO> list = pointsRecordService.listPointsRecord(query);
        return getDataTable(list);
    }

    /**
     * 导出积分变动记录列表
     */
    @Operation(summary = "导出积分变动记录", description = "导出积分变动明细列表")
    @PreAuthorize("@ss.hasPermi('gh:points:export') or @ss.hasRole('admin')")
    @Log(title = "积分变动", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, PointsRecordQueryDTO query) {
        List<PointsRecordVO> list = pointsRecordService.listPointsRecord(query);
        ExcelUtil<PointsRecordVO> util = new ExcelUtil<>(PointsRecordVO.class);
        util.exportExcel(response, list, "积分变动数据");
    }

    /**
     * 手动调整积分
     */
    @Operation(summary = "手动调整积分", description = "管理员手动调整用户积分")
    @PreAuthorize("@ss.hasPermi('gh:points:adjust') or @ss.hasRole('admin')")
    @Log(title = "积分变动", businessType = BusinessType.UPDATE)
    @PostMapping("/adjust")
    public AjaxResult<Void> adjust(@Validated @RequestBody PointsAdjustDTO adjustDTO) {
        return toAjax(pointsRecordService.adjustPoints(adjustDTO, getUserId(), getUsername()));
    }

    /**
     * 批量手动调整积分
     */
    @Operation(summary = "批量手动调整积分", description = "管理员批量手动调整用户积分")
    @PreAuthorize("@ss.hasPermi('gh:points:adjust') or @ss.hasRole('admin')")
    @Log(title = "积分变动", businessType = BusinessType.UPDATE)
    @PostMapping("/batchAdjust")
    public AjaxResult<Void> batchAdjust(@Validated @RequestBody List<PointsAdjustDTO> adjustDTOList) {
        return toAjax(pointsRecordService.batchAdjustPoints(adjustDTOList, getUserId(), getUsername()));
    }

    /**
     * 删除积分变动记录（删除后自动重算该用户后续余额）
     */
    @Operation(summary = "删除积分变动记录", description = "删除记录后自动重算受影响用户的balanceAfter")
    @PreAuthorize("@ss.hasPermi('gh:points:remove') or @ss.hasRole('admin')")
    @Log(title = "积分变动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult<Void> remove(@PathVariable Long[] ids) {
        return toAjax(pointsRecordService.deletePointsRecords(ids));
    }
}
