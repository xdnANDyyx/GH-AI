package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.domain.GhPointsRecord;
import com.ruoyi.business.dto.PointsRecordQueryDTO;
import com.ruoyi.business.vo.PointsRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分变动明细 Mapper接口
 *
 * @author guanghe
 */
@Mapper
public interface GhPointsRecordMapper extends BaseMapper<GhPointsRecord> {

    /**
     * 查询积分变动记录列表（关联用户名）
     *
     * @param query 查询条件
     * @return 积分变动记录列表
     */
    List<PointsRecordVO> selectPointsRecordList(@Param("query") PointsRecordQueryDTO query);
}
