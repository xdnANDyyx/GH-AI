package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhCreationConfig;
import com.ruoyi.business.dto.CreationConfigQueryDTO;
import com.ruoyi.business.mapper.GhCreationConfigMapper;
import com.ruoyi.business.service.IGhCreationConfigService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 创作配置 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhCreationConfigServiceImpl extends ServiceImpl<GhCreationConfigMapper, GhCreationConfig> implements IGhCreationConfigService {

    @Override
    public Page<GhCreationConfig> listConfig(CreationConfigQueryDTO query) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(GhCreationConfig::getDeleteAt);
        if (query != null) {
            wrapper.eq(StringUtils.isNotEmpty(query.getConfigGroup()), GhCreationConfig::getConfigGroup, query.getConfigGroup());
            wrapper.like(StringUtils.isNotEmpty(query.getConfigKey()), GhCreationConfig::getConfigKey, query.getConfigKey());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhCreationConfig::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhCreationConfig::getConfigGroup).orderByAsc(GhCreationConfig::getSort);
        int pageNum = query != null && query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 20;
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public GhCreationConfig getByGroupKey(String configGroup, String configKey) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(GhCreationConfig::getDeleteAt);
        wrapper.eq(GhCreationConfig::getConfigGroup, configGroup);
        wrapper.eq(GhCreationConfig::getConfigKey, configKey);
        return getOne(wrapper);
    }

    @Override
    public List<GhCreationConfig> listByGroup(String configGroup) {
        LambdaQueryWrapper<GhCreationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(GhCreationConfig::getDeleteAt);
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
        config.setUpdateTime(new Date());
        return updateById(config) ? 1 : 0;
    }

    @Override
    public int removeConfigByIds(Long[] ids) {
        // 逻辑删除：设置 delete_at 为当前时间，而非物理删除
        LambdaUpdateWrapper<GhCreationConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(GhCreationConfig::getId, Arrays.asList(ids))
                .isNull(GhCreationConfig::getDeleteAt)
                .set(GhCreationConfig::getDeleteAt, new Date())
                .set(GhCreationConfig::getUpdateTime, new Date());
        return update(wrapper) ? ids.length : 0;
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhCreationConfig config = new GhCreationConfig();
        config.setId(id);
        config.setStatus(status);
        config.setUpdateBy(username);
        config.setUpdateTime(new Date());
        return updateById(config) ? 1 : 0;
    }
}
