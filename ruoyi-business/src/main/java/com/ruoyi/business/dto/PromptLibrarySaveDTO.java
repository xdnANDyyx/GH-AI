package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 提示词选项库保存DTO
 *
 * @author guanghe
 */
@Data
public class PromptLibrarySaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 提示词库分类 */
    private String category;

    /** 点分式唯一标识 */
    private String promptKey;

    /** 前端显示名 */
    private String label;

    /** 提示词内容 */
    private String promptText;

    /** 适用功能（逗号分隔） */
    private String scope;

    /** 适配模型 */
    private String model;

    /** 拼接优先级 */
    private Integer priority;

    /** 是否默认（0否 1是） */
    private String isDefault;

    /** 排序 */
    private Integer sort;

    /** 状态（0启用 1停用） */
    private String status;

    /** 版本号 */
    private String version;

    /** A/B测试组（A/B） */
    private String abGroup;

    /** 效果成功率统计（%） */
    private java.math.BigDecimal successRate;

    /** 备注 */
    private String remark;
}