package com.ruoyi.web.controller.customer;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.UserSurveyQuestionnaire;
import com.ruoyi.system.service.IUserSurveyQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户问卷调查Controller
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@RestController
@RequestMapping("/customer/survey")
@RequiredArgsConstructor
public class CustomerSurveyController {

    private final IUserSurveyQuestionnaireService userSurveyQuestionnaireService;

    /**
     * 获取用户问卷调查状态
     */
    @GetMapping("/status")
    public AjaxResult<Map<String, Object>> getSurveyStatus() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录");
        }

        SysUser user = loginUser.getUser();
        UserSurveyQuestionnaire questionnaire = userSurveyQuestionnaireService.selectUserSurveyQuestionnaireByUserId(user.getUserId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("surveyCompleted", questionnaire != null);
        data.put("loginCount", user.getLoginCount() != null ? user.getLoginCount() : 0);
        data.put("imageGenerateCount", user.getImageGenerateCount() != null ? user.getImageGenerateCount() : 0);
        data.put("needsSurvey", !(user.getSurveyCompleted() != null && user.getSurveyCompleted() == 1));
        
        return AjaxResult.success(data);
    }

    /**
     * 提交问卷调查
     */
    @PostMapping("/submit")
    public AjaxResult<Void> submitSurvey(@RequestBody UserSurveyQuestionnaire questionnaire) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录");
        }

        SysUser user = loginUser.getUser();
        questionnaire.setUserId(user.getUserId());
        
        // 保存或更新问卷
        userSurveyQuestionnaireService.saveOrUpdateUserSurveyQuestionnaire(questionnaire);
        
        // 更新用户问卷完成状态
        user.setSurveyCompleted(1);
        // 这里需要调用用户Service更新用户状态，暂时先返回成功
        // TODO: 添加更新用户问卷完成状态的方法
        
        return AjaxResult.success("提交成功");
    }

    /**
     * 获取用户问卷信息
     */
    @GetMapping("/info")
    public AjaxResult<UserSurveyQuestionnaire> getSurveyInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return AjaxResult.error("用户未登录");
        }

        SysUser user = loginUser.getUser();
        UserSurveyQuestionnaire questionnaire = userSurveyQuestionnaireService.selectUserSurveyQuestionnaireByUserId(user.getUserId());
        
        if (questionnaire == null) {
            questionnaire = new UserSurveyQuestionnaire();
            questionnaire.setUserId(user.getUserId());
        }
        
        return AjaxResult.success(questionnaire);
    }
}