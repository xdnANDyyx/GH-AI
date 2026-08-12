package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.UserSurveyQuestionnaire;

/**
 * 用户问卷调查Mapper接口
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
public interface UserSurveyQuestionnaireMapper {
    /**
     * 查询用户问卷调查
     * 
     * @param id 用户问卷调查主键
     * @return 用户问卷调查
     */
    public UserSurveyQuestionnaire selectUserSurveyQuestionnaireById(Long id);

    /**
     * 根据用户ID查询问卷调查
     * 
     * @param userId 用户ID
     * @return 用户问卷调查
     */
    public UserSurveyQuestionnaire selectUserSurveyQuestionnaireByUserId(Long userId);

    /**
     * 查询用户问卷调查列表
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 用户问卷调查集合
     */
    public List<UserSurveyQuestionnaire> selectUserSurveyQuestionnaireList(UserSurveyQuestionnaire userSurveyQuestionnaire);

    /**
     * 新增用户问卷调查
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 结果
     */
    public int insertUserSurveyQuestionnaire(UserSurveyQuestionnaire userSurveyQuestionnaire);

    /**
     * 修改用户问卷调查
     * 
     * @param userSurveyQuestionnaire 用户问卷调查
     * @return 结果
     */
    public int updateUserSurveyQuestionnaire(UserSurveyQuestionnaire userSurveyQuestionnaire);

    /**
     * 删除用户问卷调查
     * 
     * @param id 用户问卷调查主键
     * @return 结果
     */
    public int deleteUserSurveyQuestionnaireById(Long id);

    /**
     * 批量删除用户问卷调查
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserSurveyQuestionnaireByIds(Long[] ids);
}