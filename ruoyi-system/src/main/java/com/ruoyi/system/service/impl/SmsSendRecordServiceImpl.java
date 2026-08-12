package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SmsSendRecord;
import com.ruoyi.system.mapper.SmsSendRecordMapper;
import com.ruoyi.system.service.ISmsSendRecordService;

@Service
public class SmsSendRecordServiceImpl implements ISmsSendRecordService {
    @Autowired
    private SmsSendRecordMapper smsSendRecordMapper;

    @Override
    public int insertSmsSendRecord(SmsSendRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        return smsSendRecordMapper.insertSmsSendRecord(record);
    }

    @Override
    public List<SmsSendRecord> selectSmsSendRecordList(SmsSendRecord record) {
        return smsSendRecordMapper.selectSmsSendRecordList(record);
    }
}

