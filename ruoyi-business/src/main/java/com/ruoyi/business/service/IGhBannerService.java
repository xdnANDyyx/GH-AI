package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhBanner;
import com.ruoyi.business.dto.BannerQueryDTO;
import com.ruoyi.business.vo.BannerVO;

import java.util.List;

/**
 * Banner管理 服务层接口
 *
 * @author guanghe
 */
public interface IGhBannerService extends IService<GhBanner> {

    /**
     * 查询Banner列表
     *
     * @param query 查询条件
     * @return Banner列表
     */
    List<GhBanner> listBanner(BannerQueryDTO query);

    /**
     * 查询Banner详情
     *
     * @param id Banner ID
     * @return Banner信息
     */
    GhBanner getBannerById(Long id);

    /**
     * 新增Banner
     *
     * @param banner Banner信息
     * @param username 操作人
     * @return 影响行数
     */
    int addBanner(GhBanner banner, String username);

    /**
     * 修改Banner
     *
     * @param banner Banner信息
     * @param username 操作人
     * @return 影响行数
     */
    int updateBanner(GhBanner banner, String username);

    /**
     * 批量删除Banner
     *
     * @param ids Banner ID数组
     * @return 影响行数
     */
    int removeBannerByIds(Long[] ids);

    /**
     * 更新Banner状态
     *
     * @param id     Banner ID
     * @param status 状态
     * @param username 操作人
     * @return 影响行数
     */
    int updateStatus(Long id, String status, String username);

    /**
     * 增加点击次数
     *
     * @param id Banner ID
     * @return 影响行数
     */
    int incrementClickCount(Long id);
}
