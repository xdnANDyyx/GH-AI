package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhTag;
import com.ruoyi.business.dto.TagQueryDTO;
import com.ruoyi.business.mapper.GhTagMapper;
import com.ruoyi.business.service.IGhTagService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 标签管理 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhTagServiceImpl extends ServiceImpl<GhTagMapper, GhTag> implements IGhTagService {

    @Override
    public List<GhTag> listTag(TagQueryDTO query) {
        LambdaQueryWrapper<GhTag> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.isNotEmpty(query.getTagName()), GhTag::getTagName, query.getTagName());
            wrapper.eq(StringUtils.isNotEmpty(query.getTagType()), GhTag::getTagType, query.getTagType());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()), GhTag::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(GhTag::getSort);
        return list(wrapper);
    }

    @Override
    public List<GhTag> listByType(String tagType) {
        LambdaQueryWrapper<GhTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GhTag::getTagType, tagType);
        wrapper.eq(GhTag::getStatus, "0");
        wrapper.orderByAsc(GhTag::getSort);
        return list(wrapper);
    }

    @Override
    public int addTag(GhTag tag, String username) {
        tag.setCreateBy(username);
        return save(tag) ? 1 : 0;
    }

    @Override
    public int updateTag(GhTag tag, String username) {
        tag.setUpdateBy(username);
        return updateById(tag) ? 1 : 0;
    }

    @Override
    public int removeTagByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? ids.length : 0;
    }
}
