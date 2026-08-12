package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhPromptTemplate;
import com.ruoyi.business.dto.PromptTemplateQueryDTO;

import java.util.List;

/**
 * 提示词模板 服务层接口
 *
 * @author guanghe
 */
public interface IGhPromptTemplateService extends IService<GhPromptTemplate> {

    /**
     * 查询提示词模板列表
     *
     * @param query 查询条件
     * @return 模板列表
     */
    List<GhPromptTemplate> listTemplate(PromptTemplateQueryDTO query);

    /**
     * 查询模板详情
     *
     * @param id 模板ID
     * @return 模板信息
     */
    GhPromptTemplate getTemplateById(Long id);

    /**
     * 新增模板
     *
     * @param template 模板信息
     * @param username 操作人
     * @return 影响行数
     */
    int addTemplate(GhPromptTemplate template, String username);

    /**
     * 修改模板
     *
     * @param template 模板信息
     * @param username 操作人
     * @return 影响行数
     */
    int updateTemplate(GhPromptTemplate template, String username);

    /**
     * 批量删除模板
     *
     * @param ids 模板ID数组
     * @return 影响行数
     */
    int removeTemplateByIds(Long[] ids);

    /**
     * 设为默认模板（同模块下仅一个默认）
     *
     * @param id       模板ID
     * @param username 操作人
     * @return 影响行数
     */
    int setDefault(Long id, String username);
}
