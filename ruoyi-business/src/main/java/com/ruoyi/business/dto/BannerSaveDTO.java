package com.ruoyi.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Banner保存DTO
 *
 * @author guanghe
 */
@Data
public class BannerSaveDTO {

    /** 主键ID（更新时必传） */
    private Long id;

    /** 标题 */
    private String title;

    /** 图片地址 */
    private String imageUrl;

    /** 链接地址 */
    private String linkUrl;

    /** 展示位置 */
    private String position;

    /** 排序 */
    private Integer sort;

    /** 状态（0上架 1下架） */
    private String status;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 备注 */
    private String remark;
}
