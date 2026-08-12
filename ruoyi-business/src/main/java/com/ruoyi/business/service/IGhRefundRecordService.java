package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhRefundRecord;
import com.ruoyi.business.dto.RefundProcessDTO;
import com.ruoyi.business.dto.RefundRecordQueryDTO;
import com.ruoyi.business.vo.RefundRecordVO;

import java.util.List;

/**
 * 退款记录 服务层接口
 *
 * @author guanghe
 */
public interface IGhRefundRecordService extends IService<GhRefundRecord> {

    /**
     * 查询退款记录列表（关联用户名）
     *
     * @param query 查询条件
     * @return 退款记录列表
     */
    List<RefundRecordVO> listRefundRecord(RefundRecordQueryDTO query);

    /**
     * 处理退款申请
     *
     * @param processDTO 处理信息
     * @param username 操作人
     * @return 影响行数
     */
    int processRefund(RefundProcessDTO processDTO, String username);
}
