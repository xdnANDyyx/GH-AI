-- =============================================
-- 为问卷调查功能更新sys_user表
-- 执行时间：在user_survey_questionnaire.sql之后执行
-- =============================================

-- 添加问卷调查相关字段
ALTER TABLE sys_user 
ADD COLUMN login_count INT DEFAULT 0 COMMENT '登录次数' AFTER `update_time`,
ADD COLUMN image_generate_count INT DEFAULT 0 COMMENT '图片生成次数' AFTER `login_count`,
ADD COLUMN survey_completed TINYINT DEFAULT 0 COMMENT '是否已完成问卷调查（0:未完成 1:已完成）' AFTER `image_generate_count`;

-- 为现有用户初始化默认值
UPDATE sys_user SET 
    login_count = 0,
    image_generate_count = 0,
    survey_completed = 0
WHERE login_count IS NULL OR image_generate_count IS NULL OR survey_completed IS NULL;

-- 说明：
-- 1. login_count: 记录用户登录次数，每次登录成功后+1
-- 2. image_generate_count: 记录用户生成图片的次数，每次成功生成图片后+1
-- 3. survey_completed: 标记用户是否已完成问卷调查