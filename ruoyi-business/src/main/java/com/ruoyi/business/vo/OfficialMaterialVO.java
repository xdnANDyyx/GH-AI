package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 官方素材 VO
 *
 * @author guanghe
 */
@Data
public class OfficialMaterialVO {

    /** 主键ID */
    private Long id;

    /** 素材名称 */
    private String materialName;

    /** 素材地址 */
    private String materialUrl;

    /** 缩略图地址 */
    private String thumbnailUrl;

    /** 素材类型 */
    private String materialType;

    /** 文件大小 */
    private Long fileSize;

    /** 宽度 */
    private Integer width;

    /** 高度 */
    private Integer height;

    /** 时长 */
    private Integer duration;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private String status;

    /** 下载次数 */
    private Integer downloadCount;

    /** 收藏次数 */
    private Integer favoriteCount;

    /** 备注 */
    private String remark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 关联标签列表 */
    private List<TagVO> tags;

    @Data
    public static class TagVO {
        private Long id;
        private String tagName;
    }
}
