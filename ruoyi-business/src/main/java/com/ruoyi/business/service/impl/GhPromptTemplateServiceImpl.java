package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPromptTemplate;
import com.ruoyi.business.dto.PromptTemplateQueryDTO;
import com.ruoyi.business.mapper.GhPromptTemplateMapper;
import com.ruoyi.business.service.IGhPromptTemplateService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 提示词模板 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhPromptTemplateServiceImpl extends ServiceImpl<GhPromptTemplateMapper, GhPromptTemplate> implements IGhPromptTemplateService {

    @Override
    public Page<GhPromptTemplate> listTemplate(PromptTemplateQueryDTO query) {
        LambdaQueryWrapper<GhPromptTemplate> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.eq(StringUtils.isNotEmpty(query.getModule()), GhPromptTemplate::getModule, query.getModule());
            wrapper.like(StringUtils.isNotEmpty(query.getName()), GhPromptTemplate::getName, query.getName());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhPromptTemplate::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhPromptTemplate::getSort);
        int pageNum = query != null && query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 20;
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public GhPromptTemplate getTemplateById(Long id) {
        return getById(id);
    }

    @Override
    public int addTemplate(GhPromptTemplate template, String username) {
        template.setCreateBy(username);
        if (template.getIsDefault() == null) {
            template.setIsDefault("0");
        }
        return save(template) ? 1 : 0;
    }

    @Override
    public int updateTemplate(GhPromptTemplate template, String username) {
        template.setUpdateBy(username);
        return updateById(template) ? 1 : 0;
    }

    @Override
    public int removeTemplateByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefault(Long id, String username) {
        // 1. 查询目标模板获取所属模块
        GhPromptTemplate target = getById(id);
        if (target == null) {
            return 0;
        }

        // 2. 清除同模块下所有其他模板的默认标记
        LambdaUpdateWrapper<GhPromptTemplate> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(GhPromptTemplate::getModule, target.getModule())
                .ne(GhPromptTemplate::getId, id)
                .set(GhPromptTemplate::getIsDefault, "0")
                .set(GhPromptTemplate::getUpdateBy, username);
        update(clearWrapper);

        // 3. 将目标模板设为默认
        GhPromptTemplate update = new GhPromptTemplate();
        update.setId(id);
        update.setIsDefault("1");
        update.setUpdateBy(username);
        return updateById(update) ? 1 : 0;
    }
}
