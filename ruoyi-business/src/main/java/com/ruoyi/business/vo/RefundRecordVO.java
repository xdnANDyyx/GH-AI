package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款记录 VO
 *
 * @author guanghe
 */
@Data
public class RefundRecordVO {

    /** 主键ID */
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 原订单号 */
    private String originalOrderNo;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

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

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
