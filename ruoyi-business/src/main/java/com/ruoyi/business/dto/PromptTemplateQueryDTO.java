package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 提示词模板查询DTO
 *
 * @author guanghe
 */
@Data
public class PromptTemplateQueryDTO {

    /** 所属模块 */
    private String module;

    /** 模板名称 */
    private String name;

    /** 状态（0正常 1停用） */
    private String status;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}
