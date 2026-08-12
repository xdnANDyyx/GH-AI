package com.ruoyi.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Banner VO
 *
 * @author guanghe
 */
@Data
public class BannerVO {

    /** 主键ID */
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

    /** 点击次数 */
    private Integer clickCount;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

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
