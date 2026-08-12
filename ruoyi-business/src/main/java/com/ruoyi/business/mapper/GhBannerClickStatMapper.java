package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.domain.GhBannerClickStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Banner点击统计 Mapper接口
 *
 * @author guanghe
 */
@Mapper
public interface GhBannerClickStatMapper extends BaseMapper<GhBannerClickStat> {

    /**
     * 查询Banner点击趋势
     *
     * @param bannerId  Banner ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计列表
     */
    List<GhBannerClickStat> selectClickTrend(@Param("bannerId") Long bannerId,
                                             @Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);
}
