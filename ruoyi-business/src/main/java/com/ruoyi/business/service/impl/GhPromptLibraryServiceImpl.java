package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPromptLibrary;
import com.ruoyi.business.dto.PromptLibraryQueryDTO;
import com.ruoyi.business.mapper.GhPromptLibraryMapper;
import com.ruoyi.business.service.IGhPromptLibraryService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 提示词选项库 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhPromptLibraryServiceImpl extends ServiceImpl<GhPromptLibraryMapper, GhPromptLibrary> implements IGhPromptLibraryService {

    @Override
    public Page<GhPromptLibrary> listLibrary(PromptLibraryQueryDTO query) {
        LambdaQueryWrapper<GhPromptLibrary> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.eq(StringUtils.isNotEmpty(query.getCategory()), GhPromptLibrary::getCategory, query.getCategory());
            wrapper.like(StringUtils.isNotEmpty(query.getPromptKey()), GhPromptLibrary::getPromptKey, query.getPromptKey());
            wrapper.like(StringUtils.isNotEmpty(query.getLabel()), GhPromptLibrary::getLabel, query.getLabel());
            wrapper.eq(StringUtils.isNotEmpty(query.getModel()), GhPromptLibrary::getModel, query.getModel());
            wrapper.eq(StringUtils.isNotEmpty(query.getIsDefault()), GhPromptLibrary::getIsDefault, query.getIsDefault());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhPromptLibrary::getStatus, query.getStatus());
            if (StringUtils.isNotEmpty(query.getScope())) {
                wrapper.like(GhPromptLibrary::getScope, query.getScope());
            }
        }
        wrapper.orderByAsc(GhPromptLibrary::getSort);
        int pageNum = query != null && query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 20;
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<GhPromptLibrary> listEnabled(String category, String scope) {
        LambdaQueryWrapper<GhPromptLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhPromptLibrary::getStatus, "0");
        wrapper.eq(StringUtils.isNotEmpty(category), GhPromptLibrary::getCategory, category);
        if (StringUtils.isNotEmpty(scope)) {
            wrapper.like(GhPromptLibrary::getScope, scope);
        }
        wrapper.orderByAsc(GhPromptLibrary::getSort);
        return list(wrapper);
    }

    @Override
    public GhPromptLibrary getLibraryById(Long id) {
        return getById(id);
    }

    @Override
    public int addLibrary(GhPromptLibrary library, String username) {
        library.setCreateBy(username);
        if (library.getStatus() == null) {
            library.setStatus("0");
        }
        if (library.getIsDefault() == null) {
            library.setIsDefault("0");
        }
        if (library.getModel() == null) {
            library.setModel("all");
        }
        if (library.getPriority() == null) {
            library.setPriority(100);
        }
        if (library.getVersion() == null) {
            library.setVersion("1.0.0");
        }
        return save(library) ? 1 : 0;
    }

    @Override
    public int updateLibrary(GhPromptLibrary library, String username) {
        library.setUpdateBy(username);
        return updateById(library) ? 1 : 0;
    }

    @Override
    public int removeLibraryByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }
}