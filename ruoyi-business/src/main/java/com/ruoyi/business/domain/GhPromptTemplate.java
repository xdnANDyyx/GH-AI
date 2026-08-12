package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提示词模板 gh_prompt_template
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_prompt_template")
public class GhPromptTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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
}
