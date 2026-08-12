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
 * 官方素材管理 gh_official_material
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_official_material")
public class GhOfficialMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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

    /** 宽度（像素） */
    private Integer width;

    /** 高度（像素） */
    private Integer height;

    /** 时长（秒，视频专用） */
    private Integer duration;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 状态（0-已发布 1-已下架） */
    private String status;

    /** 下载次数 */
    private Integer downloadCount;

    /** 收藏次数 */
    private Integer favoriteCount;
}
