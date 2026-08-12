package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhPointsRecord;
import com.ruoyi.business.dto.PointsAdjustDTO;
import com.ruoyi.business.dto.PointsRecordQueryDTO;
import com.ruoyi.business.vo.PointsRecordVO;

import java.util.List;

/**
 * 积分变动明细 服务层接口
 *
 * @author guanghe
 */
public interface IGhPointsRecordService extends IService<GhPointsRecord> {

    /**
     * 查询积分变动记录列表（关联用户名）
     *
     * @param query 查询条件
     * @return 积分变动记录列表
     */
    List<PointsRecordVO> listPointsRecord(PointsRecordQueryDTO query);

    /**
     * 手动调整积分
     *
     * @param adjustDTO 调整信息
     * @param operatorId 操作人ID
     * @param username 操作人用户名
     * @return 影响行数
     */
    int adjustPoints(PointsAdjustDTO adjustDTO, Long operatorId, String username);

    /**
     * 批量手动调整积分
     *
     * @param adjustDTOList 调整信息列表
     * @param operatorId 操作人ID
     * @param username 操作人用户名
     * @return 影响行数
     */
    int batchAdjustPoints(List<PointsAdjustDTO> adjustDTOList, Long operatorId, String username);

    /**
     * 删除积分变动记录并重算受影响用户的余额
     *
     * @param ids 要删除的记录ID数组
     * @return 删除的记录数
     */
    int deletePointsRecords(Long[] ids);
}
