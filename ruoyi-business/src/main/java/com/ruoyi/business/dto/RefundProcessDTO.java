package com.ruoyi.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款处理DTO
 *
 * @author guanghe
 */
@Data
public class RefundProcessDTO {

    /** 退款记录ID */
    @NotNull(message = "退款记录ID不能为空")
    private Long id;

    /** 处理状态（1已退款 2已拒绝） */
    @NotBlank(message = "处理状态不能为空")
    private String status;

    /** 处理备注 */
    private String processRemark;
}
