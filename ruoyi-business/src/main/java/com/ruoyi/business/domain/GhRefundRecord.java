package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款记录 gh_refund_record
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_refund_record")
public class GhRefundRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 原订单号 */
    private String originalOrderNo;

    /** 用户ID */
    private Long userId;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退回积分 */
    private Integer refundPoints;

    /** 退款原因 */
    private String refundReason;

    /** 状态（0待处理 1已退款 2已拒绝） */
    private String status;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processTime;

    /** 处理备注 */
    private String processRemark;
}
