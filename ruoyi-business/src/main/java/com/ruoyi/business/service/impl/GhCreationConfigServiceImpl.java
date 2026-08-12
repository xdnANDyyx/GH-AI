package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhCreationConfig;
import com.ruoyi.business.dto.CreationConfigQueryDTO;
import com.ruoyi.business.mapper.GhCreationConfigMapper;
import com.ruoyi.business.service.IGhCreationConfigService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 创作配置 服务层实现
 *
 * @author xdn
 */
@Service
public class GhCreationConfigServiceImpl extends ServiceImpl<GhCreationConfigMapper, GhCreationConfig> implements IGhCreationConfigService {

    @Override
    public List<GhCreationConfig> listConfig(CreationConfigQueryDTO query) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.eq(StringUtils.isNotEmpty(query.getConfigGroup()), GhCreationConfig::getConfigGroup, query.getConfigGroup());
            wrapper.like(StringUtils.isNotEmpty(query.getConfigKey()), GhCreationConfig::getConfigKey, query.getConfigKey());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhCreationConfig::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhCreationConfig::getConfigGroup).orderByAsc(GhCreationConfig::getSort);
        return list(wrapper);
    }

    @Override
    public GhCreationConfig getByGroupKey(String configGroup, String configKey) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhCreationConfig::getConfigGroup, configGroup);
        wrapper.eq(GhCreationConfig::getConfigKey, configKey);
        return getOne(wrapper);
    }

    @Override
    public List<GhCreationConfig> listByGroup(String configGroup) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhCreationConfig::getConfigGroup, configGroup);
        wrapper.eq(GhCreationConfig::getStatus, "0");
        wrapper.orderByAsc(GhCreationConfig::getSort);
        return list(wrapper);
    }

    @Override
    public int addConfig(GhCreationConfig config, String username) {
        config.setCreateBy(username);
        return save(config) ? 1 : 0;
    }

    @Override
    public int updateConfig(GhCreationConfig config, String username) {
        config.setUpdateBy(username);
        return updateById(config) ? 1 : 0;
    }

    @Override
    public int removeConfigByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhCreationConfig config = new GhCreationConfig();
        config.setId(id);
        config.setStatus(status);
        config.setUpdateBy(username);
        return updateById(config) ? 1 : 0;
    }
}
