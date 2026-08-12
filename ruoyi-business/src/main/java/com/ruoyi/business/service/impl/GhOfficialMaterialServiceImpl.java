package com.ruoyi.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.business.domain.GhMaterialTagRelation;
import com.ruoyi.business.domain.GhOfficialMaterial;
import com.ruoyi.business.domain.GhTag;
import com.ruoyi.business.dto.OfficialMaterialQueryDTO;
import com.ruoyi.business.mapper.GhMaterialTagRelationMapper;
import com.ruoyi.business.mapper.GhOfficialMaterialMapper;
import com.ruoyi.business.mapper.GhTagMapper;
import com.ruoyi.business.service.IGhOfficialMaterialService;
import com.ruoyi.business.vo.OfficialMaterialVO;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 官方素材管理 服务层实现
 *
 * @author guanghe
 */
@Service
public class GhOfficialMaterialServiceImpl extends ServiceImpl<GhOfficialMaterialMapper, GhOfficialMaterial>
        implements IGhOfficialMaterialService {

    @Autowired
    private GhMaterialTagRelationMapper materialTagRelationMapper;

    @Autowired
    private GhTagMapper ghTagMapper;

    @Override
    public List<OfficialMaterialVO> listMaterial(OfficialMaterialQueryDTO query) {
        LambdaQueryWrapper<GhOfficialMaterial> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.isNotEmpty(query.getMaterialName()),
                    GhOfficialMaterial::getMaterialName, query.getMaterialName());
            wrapper.eq(StringUtils.isNotEmpty(query.getMaterialType()),
                    GhOfficialMaterial::getMaterialType, query.getMaterialType());
            wrapper.eq(StringUtils.isNotEmpty(query.getStatus()),
                    GhOfficialMaterial::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(GhOfficialMaterial::getCreateTime);

        List<GhOfficialMaterial> materials = list(wrapper);

        if (materials.isEmpty()) {
            return Collections.emptyList();
        }

        // 按标签ID过滤（需要先查关联表）
        if (query != null && query.getTagId() != null) {
            LambdaQueryWrapper<GhMaterialTagRelation> relWrapper = new LambdaQueryWrapper<>();
            relWrapper.eq(GhMaterialTagRelation::getTagId, query.getTagId());
            List<Long> filteredIds = materialTagRelationMapper.selectList(relWrapper)
                    .stream().map(GhMaterialTagRelation::getMaterialId).collect(Collectors.toList());
            materials = materials.stream()
                    .filter(m -> filteredIds.contains(m.getId()))
                    .collect(Collectors.toList());
        }

        // 批量查询标签关联
        List<Long> materialIds = materials.stream().map(GhOfficialMaterial::getId).collect(Collectors.toList());
        LambdaQueryWrapper<GhMaterialTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(GhMaterialTagRelation::getMaterialId, materialIds);
        List<GhMaterialTagRelation> relations = materialTagRelationMapper.selectList(relWrapper);

        // 批量查询标签信息（复用 gh_tag 表）
        Map<Long, List<Long>> materialTagMap = relations.stream()
                .collect(Collectors.groupingBy(GhMaterialTagRelation::getMaterialId,
                        Collectors.mapping(GhMaterialTagRelation::getTagId, Collectors.toList())));

        // 批量查询所有标签名称
        List<Long> allTagIds = relations.stream()
                .map(GhMaterialTagRelation::getTagId).distinct().collect(Collectors.toList());
        Map<Long, String> tagNameMap = Collections.emptyMap();
        if (!allTagIds.isEmpty()) {
            tagNameMap = ghTagMapper.selectBatchIds(allTagIds).stream()
                    .collect(Collectors.toMap(GhTag::getId, GhTag::getTagName));
        }

        Map<Long, String> finalTagNameMap = tagNameMap;
        return materials.stream().map(m -> {
            OfficialMaterialVO vo = new OfficialMaterialVO();
            BeanUtils.copyProperties(m, vo);
            List<Long> tagIds = materialTagMap.getOrDefault(m.getId(), Collections.emptyList());
            List<OfficialMaterialVO.TagVO> tagVOs = tagIds.stream().map(tid -> {
                OfficialMaterialVO.TagVO tagVO = new OfficialMaterialVO.TagVO();
                tagVO.setId(tid);
                tagVO.setTagName(finalTagNameMap.get(tid));
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOs);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OfficialMaterialVO getMaterialById(Long id) {
        GhOfficialMaterial material = getById(id);
        if (material == null) {
            return null;
        }
        OfficialMaterialVO vo = new OfficialMaterialVO();
        BeanUtils.copyProperties(material, vo);

        // 查询标签
        LambdaQueryWrapper<GhMaterialTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(GhMaterialTagRelation::getMaterialId, id);
        List<Long> tagIds = materialTagRelationMapper.selectList(relWrapper)
                .stream().map(GhMaterialTagRelation::getTagId).collect(Collectors.toList());
        Map<Long, String> tagNameMap = Collections.emptyMap();
        if (!tagIds.isEmpty()) {
            tagNameMap = ghTagMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(GhTag::getId, GhTag::getTagName));
        }
        Map<Long, String> finalTagNameMap = tagNameMap;
        List<OfficialMaterialVO.TagVO> tagVOs = tagIds.stream().map(tid -> {
            OfficialMaterialVO.TagVO tagVO = new OfficialMaterialVO.TagVO();
            tagVO.setId(tid);
            tagVO.setTagName(finalTagNameMap.get(tid));
            return tagVO;
        }).collect(Collectors.toList());
        vo.setTags(tagVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addMaterial(GhOfficialMaterial material, List<Long> tagIds, String username) {
        material.setCreateBy(username);
        if (material.getDownloadCount() == null) material.setDownloadCount(0);
        if (material.getFavoriteCount() == null) material.setFavoriteCount(0);
        if (material.getSort() == null) material.setSort(0);
        save(material);
        saveTagRelations(material.getId(), tagIds);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMaterial(GhOfficialMaterial material, List<Long> tagIds, String username) {
        material.setUpdateBy(username);
        updateById(material);
        // 删除旧关联，重建新关联
        LambdaQueryWrapper<GhMaterialTagRelation> del = new LambdaQueryWrapper<>();
        del.eq(GhMaterialTagRelation::getMaterialId, material.getId());
        materialTagRelationMapper.delete(del);
        saveTagRelations(material.getId(), tagIds);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeMaterialByIds(Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        // 删除标签关联
        LambdaQueryWrapper<GhMaterialTagRelation> del = new LambdaQueryWrapper<>();
        del.in(GhMaterialTagRelation::getMaterialId, idList);
        materialTagRelationMapper.delete(del);
        return removeByIds(idList) ? ids.length : 0;
    }

    @Override
    public int updateStatus(Long id, String status, String username) {
        GhOfficialMaterial material = new GhOfficialMaterial();
        material.setId(id);
        material.setStatus(status);
        material.setUpdateBy(username);
        return updateById(material) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpload(List<GhOfficialMaterial> materials, String username) {
        int count = 0;
        for (GhOfficialMaterial material : materials) {
            material.setCreateBy(username);
            if (material.getDownloadCount() == null) material.setDownloadCount(0);
            if (material.getFavoriteCount() == null) material.setFavoriteCount(0);
            if (material.getSort() == null) material.setSort(0);
            if (save(material)) count++;
        }
        return count;
    }

    private void saveTagRelations(Long materialId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            GhMaterialTagRelation rel = new GhMaterialTagRelation();
            rel.setMaterialId(materialId);
            rel.setTagId(tagId);
            materialTagRelationMapper.insert(rel);
        }
    }
}
