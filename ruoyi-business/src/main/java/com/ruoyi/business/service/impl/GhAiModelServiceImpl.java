package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhAiModel;
import com.ruoyi.business.mapper.GhAiModelMapper;
import com.ruoyi.business.service.IGhAiModelService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * AI模特 Service 实现
 *
 * @author guanghe
 */
@Service
public class GhAiModelServiceImpl extends ServiceImpl<GhAiModelMapper, GhAiModel> implements IGhAiModelService {

    @Override
    public GhAiModel selectModelById(Long id) {
        return getById(id);
    }

    @Override
    public int insertModel(GhAiModel model, String username) {
        model.setCreateBy(username);
        model.setCreateTime(DateUtils.getNowDate());
        return baseMapper.insert(model);
    }

    @Override
    public int updateModel(GhAiModel model, String username) {
        model.setUpdateBy(username);
        model.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(model);
    }

    @Override
    public int deleteModelsByIds(Long[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhAiModel model = new GhAiModel();
        model.setId(id);
        model.setStatus(status);
        model.setUpdateBy(username);
        model.setUpdateTime(DateUtils.getNowDate());
        return baseMapper.updateById(model);
    }
}