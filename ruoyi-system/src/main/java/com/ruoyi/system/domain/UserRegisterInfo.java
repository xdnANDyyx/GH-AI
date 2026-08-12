package com.ruoyi.system.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户注册信息对象 user_register_info
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@Data
public class UserRegisterInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 岗位（老板/负责人/运营/设计师/美工/摄影/采购/其他） */
    private String position;

    /** 岗位其他 */
    private String positionOther;

    /** 主营平台（JSON数组） */
    private String platforms;

    /** 平台其他 */
    private String platformOther;

    /** 了解渠道（JSON数组） */
    private String channels;

    /** 渠道其他 */
    private String channelOther;

    /** 使用目的（JSON数组） */
    private String purposes;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}