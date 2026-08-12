package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模特管理 gh_ai_model
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_ai_model")
public class GhAiModel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模特名称 */
    private String name;

    /** 性别: male-男, female-女, neutral-中性 */
    private String gender;

    /** 年龄段: young-青年, middle-中年, senior-老年 */
    private String ageGroup;

    /** 种族/肤色 */
    private String ethnicity;

    /** 发型 */
    private String hairStyle;

    /** 姿势 */
    private String pose;

    /** 着装 */
    private String clothing;

    /** 场景 */
    private String scene;

    /** 预览图URL */
    private String previewUrl;

    /** 状态: 0-已发布, 1-草稿 */
    private String status;

    /** 商用授权: 0-已授权, 1-未授权 */
    private String commercialAuth;

    /** 排序 */
    private Integer sort;
}