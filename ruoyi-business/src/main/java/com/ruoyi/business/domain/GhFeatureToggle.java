package com.ruoyi.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 功能开关对象 gh_feature_toggle
 *
 * @author guanghe
 * @date 2026-08-04
 */
public class GhFeatureToggle extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 开关键（唯一标识） */
    @Excel(name = "开关键")
    private String toggleKey;

    /** 开关名称 */
    @Excel(name = "开关名称")
    private String toggleName;

    /** 功能描述 */
    @Excel(name = "功能描述")
    private String description;

    /** 是否启用（0-关闭, 1-启用） */
    @Excel(name = "是否启用", readConverterExp = "0=关闭,1=启用")
    private String enabled;

    /** 模块分组（work-工作台, resource-资源管理, material-素材广场） */
    @Excel(name = "模块分组")
    private String moduleGroup;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToggleKey() {
        return toggleKey;
    }

    public void setToggleKey(String toggleKey) {
        this.toggleKey = toggleKey;
    }

    public String getToggleName() {
        return toggleName;
    }

    public void setToggleName(String toggleName) {
        this.toggleName = toggleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "GhFeatureToggle{" +
                "id=" + id +
                ", toggleKey='" + toggleKey + '\'' +
                ", toggleName='" + toggleName + '\'' +
                ", enabled='" + enabled + '\'' +
                ", moduleGroup='" + moduleGroup + '\'' +
                '}';
    }
}