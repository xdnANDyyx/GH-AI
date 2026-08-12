package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhBannerClickStat;
import com.ruoyi.business.mapper.GhBannerClickStatMapper;
import com.ruoyi.business.service.IGhBannerClickStatService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Banner点击统计 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhBannerClickStatServiceImpl extends ServiceImpl<GhBannerClickStatMapper, GhBannerClickStat>
        implements IGhBannerClickStatService {

    @Override
    public List<GhBannerClickStat> getClickTrend(Long bannerId, Date startDate, Date endDate) {
        LambdaQueryWrapper<GhBannerClickStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhBannerClickStat::getBannerId, bannerId);
        wrapper.ge(startDate != null, GhBannerClickStat::getClickDate, startDate);
        wrapper.le(endDate != null, GhBannerClickStat::getClickDate, endDate);
        wrapper.orderByAsc(GhBannerClickStat::getClickDate);
        return list(wrapper);
    }

    @Override
    public void recordClick(Long bannerId) {
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<GhBannerClickStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhBannerClickStat::getBannerId, bannerId);
        wrapper.eq(GhBannerClickStat::getClickDate, todayDate);
        GhBannerClickStat stat = getOne(wrapper);

        if (stat != null) {
            stat.setClickCount(stat.getClickCount() + 1);
            updateById(stat);
        } else {
            stat = new GhBannerClickStat();
            stat.setBannerId(bannerId);
            stat.setClickDate(todayDate);
            stat.setClickCount(1);
            stat.setUniqueVisitor(1);
            save(stat);
        }
    }
}
