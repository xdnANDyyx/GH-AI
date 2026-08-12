package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.GhFeatureToggle;

import java.util.List;

/**
 * 功能开关Mapper接口
 *
 * @author guanghe
 * @date 2026-08-04
 */
public interface GhFeatureToggleMapper {

    /**
     * 查询功能开关列表
     */
    List<GhFeatureToggle> selectGhFeatureToggleList(GhFeatureToggle toggle);

    /**
     * 查询所有启用的功能开关
     */
    List<GhFeatureToggle> selectEnabledToggles();

    /**
     * 根据ID查询
     */
    GhFeatureToggle selectGhFeatureToggleById(Long id);

    /**
     * 根据toggleKey查询
     */
    GhFeatureToggle selectGhFeatureToggleByKey(String toggleKey);

    /**
     * 新增
     */
    int insertGhFeatureToggle(GhFeatureToggle toggle);

    /**
     * 修改
     */
    int updateGhFeatureToggle(GhFeatureToggle toggle);

    /**
     * 批量更新开关状态
     */
    int updateToggleStatusBatch(List<GhFeatureToggle> list);

    /**
     * 删除
     */
    int deleteGhFeatureToggleById(Long id);

    /**
     * 批量删除
     */
    int deleteGhFeatureToggleByIds(Long[] ids);
}