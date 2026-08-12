package com.ruoyi.web.controller.business;

import com.ruoyi.business.dto.RefundProcessDTO;
import com.ruoyi.business.dto.RefundRecordQueryDTO;
import com.ruoyi.business.service.IGhRefundRecordService;
import com.ruoyi.business.vo.RefundRecordVO;
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
 * 退款记录控制器
 *
 * @author guanghe
 */
@Tag(name = "退款记录", description = "退款记录管理相关接口")
@RestController
@RequestMapping("/business/refund")
public class RefundRecordController extends BaseController {

    @Autowired
    private IGhRefundRecordService refundRecordService;

    /**
     * 查询退款记录列表
     */
    @Operation(summary = "查询退款记录列表", description = "根据条件分页查询退款记录列表")
    @PreAuthorize("@ss.hasPermi('gh:refund:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<RefundRecordVO> list(RefundRecordQueryDTO query) {
        startPage();
        List<RefundRecordVO> list = refundRecordService.listRefundRecord(query);
        return getDataTable(list);
    }

    /**
     * 导出退款记录列表
     */
    @Operation(summary = "导出退款记录", description = "导出退款记录列表")
    @PreAuthorize("@ss.hasPermi('gh:refund:export') or @ss.hasRole('admin')")
    @Log(title = "退款记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, RefundRecordQueryDTO query) {
        List<RefundRecordVO> list = refundRecordService.listRefundRecord(query);
        ExcelUtil<RefundRecordVO> util = new ExcelUtil<>(RefundRecordVO.class);
        util.exportExcel(response, list, "退款记录数据");
    }

    /**
     * 处理退款申请
     */
    @Operation(summary = "处理退款申请", description = "管理员审核处理退款申请")
    @PreAuthorize("@ss.hasPermi('gh:refund:edit') or @ss.hasRole('admin')")
    @Log(title = "退款记录", businessType = BusinessType.UPDATE)
    @PutMapping("/process")
    public AjaxResult<Void> process(@Validated @RequestBody RefundProcessDTO processDTO) {
        return toAjax(refundRecordService.processRefund(processDTO, getUsername()));
    }
}
