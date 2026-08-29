package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 创作配置 gh_creation_config
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_creation_config")
public class GhCreationConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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

    /** 逻辑删除时间（NULL表示未删除） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteAt;
}
