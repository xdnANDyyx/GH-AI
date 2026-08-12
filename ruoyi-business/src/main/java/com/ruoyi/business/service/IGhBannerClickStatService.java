package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhBannerClickStat;

import java.util.Date;
import java.util.List;

/**
 * Banner点击统计 服务层接口
 *
 * @author guanghe
 */
public interface IGhBannerClickStatService extends IService<GhBannerClickStat> {

    /**
     * 查询Banner点击趋势
     *
     * @param bannerId  Banner ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计列表
     */
    List<GhBannerClickStat> getClickTrend(Long bannerId, Date startDate, Date endDate);

    /**
     * 记录点击
     *
     * @param bannerId Banner ID
     */
    void recordClick(Long bannerId);
}
