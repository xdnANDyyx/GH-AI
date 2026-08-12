package com.ruoyi.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.business.domain.GhOfficialMaterial;
import com.ruoyi.business.dto.OfficialMaterialQueryDTO;
import com.ruoyi.business.vo.OfficialMaterialVO;

import java.util.List;

/**
 * 官方素材管理 服务层接口
 *
 * @author guanghe
 */
public interface IGhOfficialMaterialService extends IService<GhOfficialMaterial> {

    /**
     * 查询官方素材列表
     *
     * @param query 查询条件
     * @return 素材列表
     */
    List<OfficialMaterialVO> listMaterial(OfficialMaterialQueryDTO query);

    /**
     * 查询官方素材详情
     *
     * @param id 素材ID
     * @return 素材详情
     */
    OfficialMaterialVO getMaterialById(Long id);

    /**
     * 新增官方素材
     *
     * @param material 素材信息
     * @param tagIds   标签ID列表
     * @param username 操作人
     * @return 影响行数
     */
    int addMaterial(GhOfficialMaterial material, List<Long> tagIds, String username);

    /**
     * 修改官方素材
     *
     * @param material 素材信息
     * @param tagIds   标签ID列表
     * @param username 操作人
     * @return 影响行数
     */
    int updateMaterial(GhOfficialMaterial material, List<Long> tagIds, String username);

    /**
     * 批量删除官方素材
     *
     * @param ids 素材ID数组
     * @return 影响行数
     */
    int removeMaterialByIds(Long[] ids);

    /**
     * 更新素材状态
     *
     * @param id       素材ID
     * @param status   状态
     * @param username 操作人
     * @return 影响行数
     */
    int updateStatus(Long id, String status, String username);

    /**
     * 批量上传素材
     *
     * @param materials 素材列表
     * @param username  操作人
     * @return 成功数量
     */
    int batchUpload(List<GhOfficialMaterial> materials, String username);
}
