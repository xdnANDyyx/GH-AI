package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhAiModel;

/**
 * AI模特 Service 接口
 *
 * @author guanghe
 */
public interface IGhAiModelService extends IService<GhAiModel> {

    /**
     * 根据ID获取模特（含逻辑校验）
     */
    GhAiModel selectModelById(Long id);

    /**
     * 新增模特
     */
    int insertModel(GhAiModel model, String username);

    /**
     * 修改模特
     */
    int updateModel(GhAiModel model, String username);

    /**
     * 批量删除模特
     */
    int deleteModelsByIds(Long[] ids);

    /**
     * 更新模特状态（发布/草稿）
     */
    int updateStatus(Long id, String status, String username);
}