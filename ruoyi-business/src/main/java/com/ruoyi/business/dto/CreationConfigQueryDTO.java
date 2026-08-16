package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 创作配置查询DTO
 *
 * @author guanghe
 */
@Data
public class CreationConfigQueryDTO {

    /** 配置分组 */
    private String configGroup;

    /** 配置键 */
    private String configKey;

    /** 状态（0启用 1停用） */
    private String status;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}
