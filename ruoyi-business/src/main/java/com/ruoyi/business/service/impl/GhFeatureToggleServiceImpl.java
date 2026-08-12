package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.GhFeatureToggle;
import com.ruoyi.business.mapper.GhFeatureToggleMapper;
import com.ruoyi.business.service.IGhFeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能开关Service业务层处理
 *
 * @author guanghe
 * @date 2026-08-04
 */
@Service
public class GhFeatureToggleServiceImpl implements IGhFeatureToggleService {

    @Autowired
    private GhFeatureToggleMapper ghFeatureToggleMapper;

    @Override
    public List<GhFeatureToggle> selectGhFeatureToggleList(GhFeatureToggle toggle) {
        return ghFeatureToggleMapper.selectGhFeatureToggleList(toggle);
    }

    @Override
    public List<GhFeatureToggle> selectEnabledToggles() {
        return ghFeatureToggleMapper.selectEnabledToggles();
    }

    @Override
    public GhFeatureToggle selectGhFeatureToggleById(Long id) {
        return ghFeatureToggleMapper.selectGhFeatureToggleById(id);
    }

    @Override
    public GhFeatureToggle selectGhFeatureToggleByKey(String toggleKey) {
        return ghFeatureToggleMapper.selectGhFeatureToggleByKey(toggleKey);
    }

    @Override
    public int insertGhFeatureToggle(GhFeatureToggle toggle) {
        return ghFeatureToggleMapper.insertGhFeatureToggle(toggle);
    }

    @Override
    public int updateGhFeatureToggle(GhFeatureToggle toggle) {
        return ghFeatureToggleMapper.updateGhFeatureToggle(toggle);
    }

    @Override
    @Transactional
    public int updateToggleStatusBatch(List<GhFeatureToggle> list) {
        return ghFeatureToggleMapper.updateToggleStatusBatch(list);
    }

    @Override
    public int deleteGhFeatureToggleById(Long id) {
        return ghFeatureToggleMapper.deleteGhFeatureToggleById(id);
    }

    @Override
    public int deleteGhFeatureToggleByIds(Long[] ids) {
        return ghFeatureToggleMapper.deleteGhFeatureToggleByIds(ids);
    }
}