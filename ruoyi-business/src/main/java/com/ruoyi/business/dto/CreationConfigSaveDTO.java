package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 创作配置保存DTO
 *
 * @author guanghe
 */
@Data
public class CreationConfigSaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 配置分组 */
    private String configGroup;

    /** 配置键 */
    private String configKey;

    /** 配置值（支持JSON文本） */
    private String configValue;

    /** 配置名称 */
    private String configName;

    /** 排序 */
    private Integer sort;

    /** 状态（0启用 1停用） */
    private String status;

    /** 备注 */
    private String remark;
}
