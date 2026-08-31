package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 提示词选项库查询DTO
 *
 * @author guanghe
 */
@Data
public class PromptLibraryQueryDTO {

    /** 提示词库分类 */
    private String category;

    /** 点分式key（模糊） */
    private String promptKey;

    /** 前端显示名（模糊） */
    private String label;

    /** 适用功能 */
    private String scope;

    /** 适配模型 */
    private String model;

    /** 是否默认 */
    private String isDefault;

    /** 状态（0启用 1停用） */
    private String status;

    /** 引用状态筛选：1=已引用，0=未引用 */
    private String referenced;

    /** 被引用的 promptKey 列表（逗号分隔），与 referenced=1 配合使用 */
    private String referencedKeys;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}