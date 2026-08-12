package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分套餐 VO
 *
 * @author guanghe
 */
@Data
public class PointsPackageVO {

    /** 主键ID */
    private Long id;

    /** 套餐名称 */
    private String name;

    /** 套餐类型（0免费 1付费） */
    private Integer packageType;

    /** 价格 */
    private BigDecimal price;

    /** 积分数 */
    private Integer points;

    /** 有效天数 */
    private Integer validityDays;

    /** 套餐描述 */
    private String description;

    /** 权益说明 */
    private String rightsText;

    /** 状态（0上架 1下架） */
    private String status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

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
