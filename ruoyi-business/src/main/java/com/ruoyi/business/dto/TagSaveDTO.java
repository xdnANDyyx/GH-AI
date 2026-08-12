package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 标签保存DTO
 *
 * @author guanghe
 */
@Data
public class TagSaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 标签名称 */
    private String tagName;

    /** 标签类型 */
    private String tagType;

    /** 排序 */
    private Integer sort;

    /** 状态（0正常 1停用） */
    private String status;

    /** 备注 */
    private String remark;
}
