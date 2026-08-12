package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分套餐 gh_points_package
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_points_package")
public class GhPointsPackage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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
}
