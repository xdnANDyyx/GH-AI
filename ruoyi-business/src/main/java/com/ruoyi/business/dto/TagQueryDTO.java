package com.ruoyi.business.dto;

import lombok.Data;

/**
 * 标签查询DTO
 *
 * @author guanghe
 */
@Data
public class TagQueryDTO {

    /** 标签名称 */
    private String tagName;

    /** 标签类型 */
    private String tagType;

    /** 状态（0正常 1停用） */
    private String status;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}
