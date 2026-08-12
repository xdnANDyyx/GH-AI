package com.ruoyi.business.dto;

import lombok.Data;

import java.util.List;

/**
 * 官方素材保存DTO
 *
 * @author guanghe
 */
@Data
public class OfficialMaterialSaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 素材名称 */
    private String materialName;

    /** 素材地址 */
    private String materialUrl;

    /** 缩略图地址 */
    private String thumbnailUrl;

    /** 素材类型（image-图片/video-视频/model-模型） */
    private String materialType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 宽度 */
    private Integer width;

    /** 高度 */
    private Integer height;

    /** 时长（秒） */
    private Integer duration;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private String status;

    /** 备注 */
    private String remark;

    /** 标签ID列表 */
    private List<Long> tagIds;
}
