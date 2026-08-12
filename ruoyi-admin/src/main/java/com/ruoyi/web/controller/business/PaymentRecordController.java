package com.ruoyi.web.controller.business;

import com.ruoyi.business.dto.PaymentRecordQueryDTO;
import com.ruoyi.business.service.IGhPaymentRecordService;
import com.ruoyi.business.vo.PaymentRecordVO;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 充值/支付记录控制器
 *
 * @author guanghe
 */
@Tag(name = "充值/支付记录", description = "充值/支付记录管理相关接口")
@RestController
@RequestMapping("/business/payment")
public class PaymentRecordController extends BaseController {

    @Autowired
    private IGhPaymentRecordService paymentRecordService;

    /**
     * 查询支付记录列表
     */
    @Operation(summary = "查询支付记录列表", description = "根据条件分页查询充值/支付记录列表")
    @PreAuthorize("@ss.hasPermi('gh:payment:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo<PaymentRecordVO> list(PaymentRecordQueryDTO query) {
        startPage();
        List<PaymentRecordVO> list = paymentRecordService.listPaymentRecord(query);
        return getDataTable(list);
    }

    /**
     * 导出支付记录列表
     */
    @Operation(summary = "导出支付记录", description = "导出充值/支付记录列表")
    @PreAuthorize("@ss.hasPermi('gh:payment:export') or @ss.hasRole('admin')")
    @Log(title = "充值/支付记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, PaymentRecordQueryDTO query) {
        List<PaymentRecordVO> list = paymentRecordService.exportPaymentRecord(query);
        ExcelUtil<PaymentRecordVO> util = new ExcelUtil<>(PaymentRecordVO.class);
        util.exportExcel(response, list, "充值/支付记录数据");
    }
}
