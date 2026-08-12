package com.ruoyi.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 积分变动记录查询DTO
 *
 * @author guanghe
 */
@Data
public class PointsRecordQueryDTO {

    /** 用户ID */
    private Long userId;

    /** 变动类型（1充值 2消费 3退款 4赠送 5过期 6手动调整） */
    private Integer changeType;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
