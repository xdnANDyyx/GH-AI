package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPaymentRecord;
import com.ruoyi.business.dto.PaymentRecordQueryDTO;
import com.ruoyi.business.mapper.GhPaymentRecordMapper;
import com.ruoyi.business.service.IGhPaymentRecordService;
import com.ruoyi.business.vo.PaymentRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 充值/支付记录 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhPaymentRecordServiceImpl extends ServiceImpl<GhPaymentRecordMapper, GhPaymentRecord> implements IGhPaymentRecordService {

    @Override
    public List<PaymentRecordVO> listPaymentRecord(PaymentRecordQueryDTO query) {
        return baseMapper.selectPaymentRecordList(query);
    }

    @Override
    public List<PaymentRecordVO> exportPaymentRecord(PaymentRecordQueryDTO query) {
        return baseMapper.selectPaymentRecordList(query);
    }
}
