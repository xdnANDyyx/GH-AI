package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 提示词模板 VO
 *
 * @author guanghe
 */
@Data
public class PromptTemplateVO {

    /** 主键ID */
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
