package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * 积分变动记录 VO
 *
 * @author guanghe
 */
@Data
public class PointsRecordVO {

    /** 主键ID */
    @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 变动类型（1充值 2消费 3退款 4赠送 5过期 6手动调整） */
    @Excel(name = "变动类型", readConverterExp = "1=充值,2=消费,3=退款,4=赠送,5=过期,6=手动调整")
    private Integer changeType;

    /** 变动积分 */
    @Excel(name = "变动积分", cellType = Excel.ColumnType.NUMERIC)
    private Integer changePoints;

    /** 变动后余额 */
    @Excel(name = "变动后余额", cellType = Excel.ColumnType.NUMERIC)
    private Integer balanceAfter;

    /** 关联订单号 */
    @Excel(name = "关联订单号")
    private String relatedOrderNo;

    /** 变动原因 */
    @Excel(name = "变动原因")
    private String reason;

    /** 操作人ID */
    @Excel(name = "操作人ID", cellType = Excel.ColumnType.NUMERIC)
    private Long operatorId;

    /** 操作人名称 */
    @Excel(name = "操作人")
    private String operatorName;

    /** 创建者 */
    @Excel(name = "创建者")
    private String createBy;

    /** 创建时间 */
    @Excel(name = "时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
