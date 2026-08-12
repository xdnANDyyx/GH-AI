package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhCreationConfig;
import com.ruoyi.business.dto.CreationConfigQueryDTO;

import java.util.List;

/**
 * 创作配置 服务层接口
 *
 * @author guanghe
 */
public interface IGhCreationConfigService extends IService<GhCreationConfig> {

    /**
     * 查询配置列表
     *
     * @param query 查询条件
     * @return 配置列表
     */
    List<GhCreationConfig> listConfig(CreationConfigQueryDTO query);

    /**
     * 根据分组和键查询配置
     *
     * @param configGroup 配置分组
     * @param configKey   配置键
     * @return 配置信息
     */
    GhCreationConfig getByGroupKey(String configGroup, String configKey);

    /**
     * 根据分组查询所有配置
     *
     * @param configGroup 配置分组
     * @return 配置列表
     */
    List<GhCreationConfig> listByGroup(String configGroup);

    /**
     * 新增配置
     *
     * @param config   配置信息
     * @param username 操作人
     * @return 影响行数
     */
    int addConfig(GhCreationConfig config, String username);

    /**
     * 修改配置
     *
     * @param config   配置信息
     * @param username 操作人
     * @return 影响行数
     */
    int updateConfig(GhCreationConfig config, String username);

    /**
     * 批量删除配置
     *
     * @param ids 配置ID数组
     * @return 影响行数
     */
    int removeConfigByIds(Long[] ids);

    /**
     * 更新配置状态
     *
     * @param id       配置ID
     * @param status   状态
     * @param username 操作人
     * @return 影响行数
     */
    int updateStatus(Long id, String status, String username);
}
