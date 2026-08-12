package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 官方素材查询DTO
 *
 * @author guanghe
 */
@Data
public class OfficialMaterialQueryDTO {

    /** 素材名称 */
    private String materialName;

    /** 素材类型 */
    private String materialType;

    /** 状态 */
    private String status;

    /** 标签ID */
    private Long tagId;
}
