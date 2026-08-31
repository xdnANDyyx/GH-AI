package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhPromptLibrary;
import com.ruoyi.business.dto.PromptLibraryQueryDTO;
import com.ruoyi.business.mapper.GhPromptLibraryMapper;
import com.ruoyi.business.service.IGhPromptLibraryService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
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
        wrapper.isNull(GhPromptLibrary::getDeleteAt);
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
            // 引用状态筛选：referencedKeys 为逗号分隔的 promptKey 列表
            if (StringUtils.isNotEmpty(query.getReferencedKeys())) {
                List<String> keyList = Arrays.asList(query.getReferencedKeys().split(","));
                if ("1".equals(query.getReferenced())) {
                    // 已引用：promptKey IN (keys)
                    wrapper.in(GhPromptLibrary::getPromptKey, keyList);
                } else if ("0".equals(query.getReferenced())) {
                    // 未引用：promptKey NOT IN (keys)
                    wrapper.notIn(GhPromptLibrary::getPromptKey, keyList);
                }
            } else if ("0".equals(query.getReferenced())) {
                // 未引用且没有任何被引用的 key，则所有数据都算未引用
                // 不加任何条件即可
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
        wrapper.isNull(GhPromptLibrary::getDeleteAt);
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
        LambdaQueryWrapper<GhPromptLibrary> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(GhPromptLibrary::getDeleteAt);
        wrapper.eq(GhPromptLibrary::getId, id);
        return getOne(wrapper);
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
        library.setUpdateTime(new Date());
        return updateById(library) ? 1 : 0;
    }

    @Override
    public int removeLibraryByIds(Long[] ids) {
        // 逻辑删除：设置 delete_at 为当前时间，而非物理删除
        LambdaUpdateWrapper<GhPromptLibrary> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(GhPromptLibrary::getId, Arrays.asList(ids))
                .isNull(GhPromptLibrary::getDeleteAt)
                .set(GhPromptLibrary::getDeleteAt, new Date())
                .set(GhPromptLibrary::getUpdateTime, new Date());
        return update(wrapper) ? ids.length : 0;
    }
}
