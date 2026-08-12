package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPointsRecord;
import com.ruoyi.business.dto.PointsAdjustDTO;
import com.ruoyi.business.dto.PointsRecordQueryDTO;
import com.ruoyi.business.mapper.GhPointsRecordMapper;
import com.ruoyi.business.service.IGhPointsRecordService;
import com.ruoyi.business.vo.PointsRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 积分变动明细 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhPointsRecordServiceImpl extends ServiceImpl<GhPointsRecordMapper, GhPointsRecord> implements IGhPointsRecordService {

    @Override
    public List<PointsRecordVO> listPointsRecord(PointsRecordQueryDTO query) {
        return baseMapper.selectPointsRecordList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int adjustPoints(PointsAdjustDTO adjustDTO, Long operatorId, String username) {
        GhPointsRecord latestRecord = getOne(
            new LambdaQueryWrapper<GhPointsRecord>()
                .eq(GhPointsRecord::getUserId, adjustDTO.getUserId())
                .orderByDesc(GhPointsRecord::getCreateTime)
                .last("limit 1"),
            false
        );
        int currentBalance = latestRecord != null && latestRecord.getBalanceAfter() != null
            ? latestRecord.getBalanceAfter() : 0;
        int newBalance = currentBalance + adjustDTO.getChangePoints();

        GhPointsRecord record = new GhPointsRecord();
        record.setUserId(adjustDTO.getUserId());
        record.setChangeType(6); // 手动调整
        record.setChangePoints(adjustDTO.getChangePoints());
        record.setReason(adjustDTO.getReason());
        record.setOperatorId(operatorId);
        record.setCreateBy(username);
        record.setCreateTime(new Date());
        record.setBalanceAfter(newBalance);
        return save(record) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAdjustPoints(List<PointsAdjustDTO> adjustDTOList, Long operatorId, String username) {
        int count = 0;
        for (PointsAdjustDTO adjustDTO : adjustDTOList) {
            count += adjustPoints(adjustDTO, operatorId, username);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePointsRecords(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        List<Long> idList = Arrays.asList(ids);
        List<GhPointsRecord> recordsToDelete = list(
            new LambdaQueryWrapper<GhPointsRecord>().in(GhPointsRecord::getId, idList)
        );
        if (recordsToDelete.isEmpty()) {
            return 0;
        }
        Set<Long> affectedUserIds = recordsToDelete.stream()
            .map(GhPointsRecord::getUserId)
            .collect(Collectors.toSet());
        boolean removed = removeByIds(idList);
        if (!removed) {
            return 0;
        }
        for (Long userId : affectedUserIds) {
            recalculateBalanceAfter(userId);
        }
        return recordsToDelete.size();
    }

    private void recalculateBalanceAfter(Long userId) {
        List<GhPointsRecord> userRecords = list(
            new LambdaQueryWrapper<GhPointsRecord>()
                .eq(GhPointsRecord::getUserId, userId)
                .orderByAsc(GhPointsRecord::getCreateTime)
                .orderByAsc(GhPointsRecord::getId)
        );
        int balance = 0;
        for (GhPointsRecord record : userRecords) {
            balance += (record.getChangePoints() != null ? record.getChangePoints() : 0);
            record.setBalanceAfter(balance);
        }
        if (!userRecords.isEmpty()) {
            updateBatchById(userRecords);
        }
    }
}
