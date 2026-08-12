package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPaymentRecord;
import com.ruoyi.business.domain.GhPointsRecord;
import com.ruoyi.business.domain.GhRefundRecord;
import com.ruoyi.business.dto.RefundProcessDTO;
import com.ruoyi.business.dto.RefundRecordQueryDTO;
import com.ruoyi.business.mapper.GhRefundRecordMapper;
import com.ruoyi.business.service.IGhPaymentRecordService;
import com.ruoyi.business.service.IGhPointsRecordService;
import com.ruoyi.business.service.IGhRefundRecordService;
import com.ruoyi.business.vo.RefundRecordVO;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 退款记录 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhRefundRecordServiceImpl extends ServiceImpl<GhRefundRecordMapper, GhRefundRecord> implements IGhRefundRecordService {

    @Autowired
    private IGhPaymentRecordService paymentRecordService;

    @Autowired
    private IGhPointsRecordService pointsRecordService;

    @Override
    public List<RefundRecordVO> listRefundRecord(RefundRecordQueryDTO query) {
        return baseMapper.selectRefundRecordList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(RefundProcessDTO processDTO, String username) {
        GhRefundRecord refundRecord = getById(processDTO.getId());
        if (refundRecord == null) {
            throw new ServiceException("退款记录不存在");
        }
        if (!"0".equals(refundRecord.getStatus())) {
            throw new ServiceException("该退款记录已处理，不能重复操作");
        }

        String status = processDTO.getStatus();
        GhRefundRecord updateRecord = new GhRefundRecord();
        updateRecord.setId(processDTO.getId());
        updateRecord.setStatus(status);
        updateRecord.setProcessRemark(processDTO.getProcessRemark());
        updateRecord.setProcessTime(new Date());
        updateRecord.setUpdateBy(username);

        boolean success = updateById(updateRecord);
        if (!success) {
            throw new ServiceException("更新退款状态失败");
        }

        if ("1".equals(status)) {
            GhPaymentRecord paymentRecord = paymentRecordService.getOne(
                new LambdaQueryWrapper<GhPaymentRecord>()
                    .eq(GhPaymentRecord::getOrderNo, refundRecord.getOriginalOrderNo())
            );
            if (paymentRecord != null) {
                paymentRecord.setPayStatus("2");
                paymentRecordService.updateById(paymentRecord);
            }

            GhPointsRecord pointsRecord = new GhPointsRecord();
            pointsRecord.setUserId(refundRecord.getUserId());
            pointsRecord.setChangeType(5);
            pointsRecord.setChangePoints(refundRecord.getRefundPoints() != null ? refundRecord.getRefundPoints() : 0);
            pointsRecord.setReason("退款返还积分");
            pointsRecord.setRelatedOrderNo(refundRecord.getOriginalOrderNo());
            pointsRecord.setCreateBy(username);
            pointsRecord.setCreateTime(new Date());
            // 余额需要后台定时或前端取最新记录方式补齐，这里暂不计算
            pointsRecord.setBalanceAfter(pointsRecord.getChangePoints());
            pointsRecordService.save(pointsRecord);
        }

        return 1;
    }
}
