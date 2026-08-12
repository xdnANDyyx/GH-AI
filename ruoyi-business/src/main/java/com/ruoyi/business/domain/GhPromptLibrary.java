package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提示词选项库 gh_prompt_library
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_prompt_library")
public class GhPromptLibrary extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提示词库分类（function/platform/product/material/scene/style/selling/size/quality/negative/camera/option） */
    private String category;

    /** 点分式唯一标识 */
    private String promptKey;

    /** 前端显示名 */
    private String label;

    /** 提示词内容 */
    private String promptText;

    /** 适用功能（逗号分隔） */
    private String scope;

    /** 适配模型（gpt/gemini/all） */
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
}