package com.ruoyi.business.dto;

import lombok.Data;

/**
 * Banner查询DTO
 *
 * @author guanghe
 */
@Data
public class BannerQueryDTO {

    /** 标题 */
    private String title;

    /** 展示位置 */
    private String position;

    /** 状态（0上架 1下架） */
    private String status;
}
