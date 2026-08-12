package com.ruoyi.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 支付记录查询DTO
 *
 * @author guanghe
 */
@Data
public class PaymentRecordQueryDTO {

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付状态（0待支付 1已支付 2已退款） */
    private String payStatus;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
