package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhPointsPackage;
import com.ruoyi.business.dto.PointsPackageQueryDTO;

import java.util.List;

/**
 * 积分套餐 服务层接口
 *
 * @author guanghe
 */
public interface IGhPointsPackageService extends IService<GhPointsPackage> {

    /**
     * 查询积分套餐列表
     *
     * @param query 查询条件
     * @return 积分套餐列表
     */
    List<GhPointsPackage> listPointsPackage(PointsPackageQueryDTO query);

    /**
     * 查询积分套餐详情
     *
     * @param id 套餐ID
     * @return 积分套餐信息
     */
    GhPointsPackage getPointsPackageById(Long id);

    /**
     * 新增积分套餐
     *
     * @param pointsPackage 套餐信息
     * @param username 操作人
     * @return 影响行数
     */
    int addPointsPackage(GhPointsPackage pointsPackage, String username);

    /**
     * 修改积分套餐
     *
     * @param pointsPackage 套餐信息
     * @param username 操作人
     * @return 影响行数
     */
    int updatePointsPackage(GhPointsPackage pointsPackage, String username);

    /**
     * 批量删除积分套餐
     *
     * @param ids 套餐ID数组
     * @return 影响行数
     */
    int removePointsPackageByIds(Long[] ids);

    /**
     * 更新套餐上下架状态
     *
     * @param id       套餐ID
     * @param status   状态
     * @param username 操作人
     * @return 影响行数
     */
    int updateStatus(Long id, String status, String username);
}
