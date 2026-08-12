package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SmsSendRecord;

public interface SmsSendRecordMapper {
    int insertSmsSendRecord(SmsSendRecord record);

    List<SmsSendRecord> selectSmsSendRecordList(SmsSendRecord record);
}

