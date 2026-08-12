package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 积分套餐查询DTO
 *
 * @author guanghe
 */
@Data
public class PointsPackageQueryDTO {

    /** 套餐名称 */
    private String name;

    /** 套餐类型（0免费 1付费） */
    private Integer packageType;

    /** 状态（0上架 1下架） */
    private String status;
}
