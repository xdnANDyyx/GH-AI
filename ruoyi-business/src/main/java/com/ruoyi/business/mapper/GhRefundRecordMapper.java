package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.domain.GhRefundRecord;
import com.ruoyi.business.dto.RefundRecordQueryDTO;
import com.ruoyi.business.vo.RefundRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退款记录 Mapper接口
 *
 * @author guanghe
 */
@Mapper
public interface GhRefundRecordMapper extends BaseMapper<GhRefundRecord> {

    /**
     * 查询退款记录列表（关联用户名）
     *
     * @param query 查询条件
     * @return 退款记录列表
     */
    List<RefundRecordVO> selectRefundRecordList(@Param("query") RefundRecordQueryDTO query);
}
