package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录 VO
 *
 * @author guanghe
 */
@Data
public class PaymentRecordVO {

    /** 主键ID */
    @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    /** 订单编号 */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 套餐ID */
    @Excel(name = "套餐ID", cellType = Excel.ColumnType.NUMERIC)
    private Long packageId;

    /** 套餐名称 */
    @Excel(name = "套餐名称")
    private String packageName;

    /** 支付金额 */
    @Excel(name = "支付金额", cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal amount;

    /** 获得积分 */
    @Excel(name = "获得积分", cellType = Excel.ColumnType.NUMERIC)
    private Integer points;

    /** 支付方式（wechat/alipay） */
    @Excel(name = "支付方式", readConverterExp = "wechat=微信,alipay=支付宝")
    private String payMethod;

    /** 支付状态（0待支付 1已支付 2已退款） */
    @Excel(name = "支付状态", readConverterExp = "0=待支付,1=已支付,2=已退款")
    private String payStatus;

    /** 支付时间 */
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 第三方交易号 */
    @Excel(name = "第三方交易号")
    private String transactionId;

    /** 创建者 */
    @Excel(name = "创建者")
    private String createBy;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    @Excel(name = "更新者")
    private String updateBy;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
