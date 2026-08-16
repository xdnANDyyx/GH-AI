package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.domain.GhPromptLibrary;
import com.ruoyi.business.dto.PromptLibraryQueryDTO;

import java.util.List;

/**
 * 提示词选项库 服务层接口
 *
 * @author guanghe
 */
public interface IGhPromptLibraryService extends IService<GhPromptLibrary> {

    /**
     * 查询提示词选项库列表（分页）
     *
     * @param query 查询条件
     * @return 分页选项列表
     */
    Page<GhPromptLibrary> listLibrary(PromptLibraryQueryDTO query);

    /**
     * 查询启用中的选项列表（C端按分类拉取）
     *
     * @param category 提示词库分类
     * @param scope    适用功能（可选，过滤 scope 包含该值）
     * @return 选项列表
     */
    List<GhPromptLibrary> listEnabled(String category, String scope);

    /**
     * 查询选项详情
     *
     * @param id 选项ID
     * @return 选项信息
     */
    GhPromptLibrary getLibraryById(Long id);

    /**
     * 新增选项
     *
     * @param library 选项信息
     * @param username 操作人
     * @return 影响行数
     */
    int addLibrary(GhPromptLibrary library, String username);

    /**
     * 修改选项
     *
     * @param library 选项信息
     * @param username 操作人
     * @return 影响行数
     */
    int updateLibrary(GhPromptLibrary library, String username);

    /**
     * 批量删除选项
     *
     * @param ids 选项ID数组
     * @return 影响行数
     */
    int removeLibraryByIds(Long[] ids);
}