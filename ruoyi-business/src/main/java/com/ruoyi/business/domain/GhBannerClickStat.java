package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Banner点击统计 gh_banner_click_stat
 *
 * @author guanghe
 */
@Data
@TableName("gh_banner_click_stat")
public class GhBannerClickStat {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Banner ID */
    private Long bannerId;

    /** 点击日期 */
    private Date clickDate;

    /** 点击次数 */
    private Integer clickCount;

    /** 独立访客数 */
    private Integer uniqueVisitor;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
