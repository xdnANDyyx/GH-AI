package com.ruoyi.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分手动调整DTO
 *
 * @author guanghe
 */
@Data
public class PointsAdjustDTO {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 变动积分（正数增加，负数扣减） */
    @NotNull(message = "变动积分不能为空")
    private Integer changePoints;

    /** 变动原因 */
    @NotBlank(message = "变动原因不能为空")
    private String reason;
}
