package com.ruoyi.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分变动明细 gh_points_record
 *
 * @author guanghe
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gh_points_record")
public class GhPointsRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 变动类型（1充值 2消费 3退款 4赠送 5过期 6手动调整） */
    private Integer changeType;

    /** 变动积分 */
    private Integer changePoints;

    /** 变动后余额 */
    private Integer balanceAfter;

    /** 关联订单号 */
    private String relatedOrderNo;

    /** 变动原因 */
    private String reason;

    /** 操作人ID */
    private Long operatorId;
}
