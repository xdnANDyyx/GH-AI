package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPointsPackage;
import com.ruoyi.business.dto.PointsPackageQueryDTO;
import com.ruoyi.business.mapper.GhPointsPackageMapper;
import com.ruoyi.business.service.IGhPointsPackageService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 积分套餐 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhPointsPackageServiceImpl extends ServiceImpl<GhPointsPackageMapper, GhPointsPackage> implements IGhPointsPackageService {

    @Override
    public List<GhPointsPackage> listPointsPackage(PointsPackageQueryDTO query) {
        LambdaQueryWrapper<GhPointsPackage> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.isNotEmpty(query.getName()), GhPointsPackage::getName, query.getName());
            wrapper.eq(query.getPackageType() != null, GhPointsPackage::getPackageType, query.getPackageType());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhPointsPackage::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhPointsPackage::getSort);
        return list(wrapper);
    }

    @Override
    public GhPointsPackage getPointsPackageById(Long id) {
        return getById(id);
    }

    @Override
    public int addPointsPackage(GhPointsPackage pointsPackage, String username) {
        pointsPackage.setCreateBy(username);
        return save(pointsPackage) ? 1 : 0;
    }

    @Override
    public int updatePointsPackage(GhPointsPackage pointsPackage, String username) {
        pointsPackage.setUpdateBy(username);
        return updateById(pointsPackage) ? 1 : 0;
    }

    @Override
    public int removePointsPackageByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhPointsPackage pointsPackage = new GhPointsPackage();
        pointsPackage.setId(id);
        pointsPackage.setStatus(status);
        pointsPackage.setUpdateBy(username);
        return updateById(pointsPackage) ? 1 : 0;
    }
}
