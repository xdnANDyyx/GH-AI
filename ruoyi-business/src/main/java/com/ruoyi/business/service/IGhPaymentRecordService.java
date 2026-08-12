package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhPaymentRecord;
import com.ruoyi.business.dto.PaymentRecordQueryDTO;
import com.ruoyi.business.vo.PaymentRecordVO;

import java.util.List;

/**
 * 充值/支付记录 服务层接口
 *
 * @author guanghe
 */
public interface IGhPaymentRecordService extends IService<GhPaymentRecord> {

    /**
     * 查询支付记录列表（关联套餐名称）
     *
     * @param query 查询条件
     * @return 支付记录列表
     */
    List<PaymentRecordVO> listPaymentRecord(PaymentRecordQueryDTO query);

    /**
     * 导出支付记录
     *
     * @param query 查询条件
     * @return 支付记录列表
     */
    List<PaymentRecordVO> exportPaymentRecord(PaymentRecordQueryDTO query);
}
