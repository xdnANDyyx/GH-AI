package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SmsSendRecord;

public interface ISmsSendRecordService {
    int insertSmsSendRecord(SmsSendRecord record);

    List<SmsSendRecord> selectSmsSendRecordList(SmsSendRecord record);
}

