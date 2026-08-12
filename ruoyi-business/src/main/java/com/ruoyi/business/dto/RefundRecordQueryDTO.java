package com.ruoyi.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 退款记录查询DTO
 *
 * @author guanghe
 */
@Data
public class RefundRecordQueryDTO {

    /** 退款单号 */
    private String refundNo;

    /** 原订单号 */
    private String originalOrderNo;

    /** 用户ID */
    private Long userId;

    /** 状态（0待处理 1已退款 2已拒绝） */
    private String status;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
