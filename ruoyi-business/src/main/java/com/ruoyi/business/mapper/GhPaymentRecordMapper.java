package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.domain.GhPaymentRecord;
import com.ruoyi.business.dto.PaymentRecordQueryDTO;
import com.ruoyi.business.vo.PaymentRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 充值/支付记录 Mapper接口
 *
 * @author guanghe
 */
@Mapper
public interface GhPaymentRecordMapper extends BaseMapper<GhPaymentRecord> {

    /**
     * 查询支付记录列表（关联套餐名称）
     *
     * @param query 查询条件
     * @return 支付记录列表
     */
    List<PaymentRecordVO> selectPaymentRecordList(@Param("query") PaymentRecordQueryDTO query);
}
