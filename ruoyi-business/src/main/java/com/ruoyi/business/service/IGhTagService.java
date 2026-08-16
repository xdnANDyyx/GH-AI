package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.domain.GhTag;
import com.ruoyi.business.dto.TagQueryDTO;

import java.util.List;

/**
 * 标签管理 服务层接口
 *
 * @author guanghe
 */
public interface IGhTagService extends IService<GhTag> {

    /**
     * 查询标签列表（分页）
     *
     * @param query 查询条件
     * @return 分页标签列表
     */
    Page<GhTag> listTag(TagQueryDTO query);

    /**
     * 根据标签类型查询标签列表
     *
     * @param tagType 标签类型
     * @return 标签列表
     */
    List<GhTag> listByType(String tagType);

    /**
     * 新增标签
     *
     * @param tag      标签信息
     * @param username 操作人
     * @return 影响行数
     */
    int addTag(GhTag tag, String username);

    /**
     * 修改标签
     *
     * @param tag      标签信息
     * @param username 操作人
     * @return 影响行数
     */
    int updateTag(GhTag tag, String username);

    /**
     * 批量删除标签
     *
     * @param ids 标签ID数组
     * @return 影响行数
     */
    int removeTagByIds(Long[] ids);
}
