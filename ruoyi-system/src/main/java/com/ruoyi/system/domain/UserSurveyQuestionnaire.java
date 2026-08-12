package com.ruoyi.system.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户问卷调查对象 user_survey_questionnaire
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@Data
public class UserSurveyQuestionnaire implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 公司名称（选填） */
    private String companyName;

    /** 公司规模（1-20人/20-50人/50-100人/100人以上） */
    private String companyScale;

    /** 每月上新产品数量（1-20/20-50/50-100/100+） */
    private String monthlyProducts;

    /** 每月制作图片数量（100张以内/100-500张/500-2000张/2000+） */
    private String monthlyImages;

    /** 最希望新增什么功能（多选，JSON格式存储） */
    private String desiredFeatures;

    /** 联系方式（选填） */
    private String contactInfo;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}