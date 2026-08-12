package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhBanner;
import com.ruoyi.business.dto.BannerQueryDTO;
import com.ruoyi.business.mapper.GhBannerMapper;
import com.ruoyi.business.service.IGhBannerService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Banner管理 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhBannerServiceImpl extends ServiceImpl<GhBannerMapper, GhBanner> implements IGhBannerService {

    @Override
    public List<GhBanner> listBanner(BannerQueryDTO query) {
        LambdaQueryWrapper<GhBanner> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.isNotEmpty(query.getTitle()), GhBanner::getTitle, query.getTitle());
            wrapper.eq(StringUtils.isNotEmpty(query.getPosition()), GhBanner::getPosition, query.getPosition());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhBanner::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhBanner::getSort);
        return list(wrapper);
    }

    @Override
    public GhBanner getBannerById(Long id) {
        return getById(id);
    }

    @Override
    public int addBanner(GhBanner banner, String username) {
        banner.setCreateBy(username);
        banner.setCreateTime(new Date());
        if (banner.getClickCount() == null) {
            banner.setClickCount(0);
        }
        return save(banner) ? 1 : 0;
    }

    @Override
    public int updateBanner(GhBanner banner, String username) {
        banner.setUpdateBy(username);
        banner.setUpdateTime(new Date());
        return updateById(banner) ? 1 : 0;
    }

    @Override
    public int removeBannerByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhBanner banner = new GhBanner();
        banner.setId(id);
        banner.setStatus(status);
        banner.setUpdateBy(username);
        banner.setUpdateTime(new Date());
        return updateById(banner) ? 1 : 0;
    }

    @Override
    public int incrementClickCount(Long id) {
        GhBanner banner = getById(id);
        if (banner != null) {
            GhBanner update = new GhBanner();
            update.setId(id);
            update.setClickCount(banner.getClickCount() + 1);
            return updateById(update) ? 1 : 0;
        }
        return 0;
    }
}
