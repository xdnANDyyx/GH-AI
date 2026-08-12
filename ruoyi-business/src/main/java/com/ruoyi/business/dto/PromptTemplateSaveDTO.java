package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 提示词模板保存DTO
 *
 * @author guanghe
 */
@Data
public class PromptTemplateSaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 所属模块 */
    private String module;

    /** 模板名称 */
    private String name;

    /** 模板内容 */
    private String content;

    /** 是否默认（0否 1是） */
    private String isDefault;

    /** 排序 */
    private Integer sort;

    /** 状态（0正常 1停用） */
    private String status;

    /** 备注 */
    private String remark;
}
