package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.UserSurveyQuestionnaire;
import com.ruoyi.system.mapper.UserSurveyQuestionnaireMapper;
import com.ruoyi.system.service.IUserSurveyQuestionnaireService;

/**
 * 用户问卷调查Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@Service
public class UserSurveyQuestionnaireServiceImpl implements IUserSurveyQuestionnaireService {
    @Autowired
    private UserSurveyQuestionnaireMapper userSurveyQuestionnaireMapper;

    /**
     * 查询用户问卷调查
     * 
     * @param id 用户问卷调查主键
     * @return 用户问卷调查
     */
    @Override
    public UserSurveyQuestionnaire selectUserSurveyQuestionnaireById(Long id) {
        return userSurveyQuestionnaireMapper.selectUserSurveyQuestionnaireById(id);
    }

    /**
     * 根据用户ID查询问卷调查
     * 
     * @param userId 用户ID
     * @return 用户问卷调查
     */
    @Override
    public UserSurveyQuestionnaire selectUserSurveyQuestionnaireByUserId(Long userId) {
        return userSurveyQuestionnaireMapper.selectUserSurveyQuestionnaireByUserId(userId);
    }

    /**
     * 查询用户问卷调查列表
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 用户问卷调查
     */
    @Override
    public List<UserSurveyQuestionnaire> selectUserSurveyQuestionnaireList(UserSurveyQuestionnaire userSurveyQuestionnaire) {
        return userSurveyQuestionnaireMapper.selectUserSurveyQuestionnaireList(userSurveyQuestionnaire);
    }

    /**
     * 新增用户问卷调查
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 结果
     */
    @Override
    public int insertUserSurveyQuestionnaire(UserSurveyQuestionnaire userSurveyQuestionnaire) {
        userSurveyQuestionnaire.setCreateTime(LocalDateTime.now());
        userSurveyQuestionnaire.setUpdateTime(LocalDateTime.now());
        return userSurveyQuestionnaireMapper.insertUserSurveyQuestionnaire(userSurveyQuestionnaire);
    }

    /**
     * 修改用户问卷调查
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 结果
     */
    @Override
    public int updateUserSurveyQuestionnaire(UserSurveyQuestionnaire userSurveyQuestionnaire) {
        userSurveyQuestionnaire.setUpdateTime(LocalDateTime.now());
        return userSurveyQuestionnaireMapper.updateUserSurveyQuestionnaire(userSurveyQuestionnaire);
    }

    /**
     * 批量删除用户问卷调查
     * 
     * @param ids 需要删除的用户问卷调查主键集合
     * @return 结果
     */
    @Override
    public int deleteUserSurveyQuestionnaireByIds(Long[] ids) {
        return userSurveyQuestionnaireMapper.deleteUserSurveyQuestionnaireByIds(ids);
    }

    /**
     * 删除用户问卷调查信息
     * 
     * @param id 用户问卷调查主键
     * @return 结果
     */
    @Override
    public int deleteUserSurveyQuestionnaireById(Long id) {
        return userSurveyQuestionnaireMapper.deleteUserSurveyQuestionnaireById(id);
    }

    /**
     * 保存或更新用户问卷调查
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 结果
     */
    @Override
    public int saveOrUpdateUserSurveyQuestionnaire(UserSurveyQuestionnaire userSurveyQuestionnaire) {
        UserSurveyQuestionnaire existing = userSurveyQuestionnaireMapper.selectUserSurveyQuestionnaireByUserId(userSurveyQuestionnaire.getUserId());
        if (existing != null) {
            userSurveyQuestionnaire.setId(existing.getId());
            return userSurveyQuestionnaireMapper.updateUserSurveyQuestionnaire(userSurveyQuestionnaire);
        } else {
            return userSurveyQuestionnaireMapper.insertUserSurveyQuestionnaire(userSurveyQuestionnaire);
        }
    }
}