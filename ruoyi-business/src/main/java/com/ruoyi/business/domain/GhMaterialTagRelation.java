package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 素材标签关联 gh_material_tag_relation
 *
 * @author guanghe
 */
@Data
@TableName("gh_material_tag_relation")
public class GhMaterialTagRelation {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 素材ID */
    private Long materialId;

    /** 标签ID */
    private Long tagId;

    /** 创建时间 */
    private Date createTime;
}
