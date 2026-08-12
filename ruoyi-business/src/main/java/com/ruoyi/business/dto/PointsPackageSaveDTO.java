package com.ruoyi.business.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分套餐保存DTO
 *
 * @author guanghe
 */
@Data
public class PointsPackageSaveDTO {

    /** 主键ID（更新时必传） */
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
}
