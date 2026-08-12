package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 提示词选项库 VO
 *
 * @author guanghe
 */
@Data
public class PromptLibraryVO {

    /** 主键ID */
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

    /** 备注 */
    private String remark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}