-- =============================================
-- 用户问卷调查功能SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. 在sys_user表中添加字段
-- ----------------------------
ALTER TABLE sys_user 
ADD COLUMN login_count INT DEFAULT 0 COMMENT '登录次数',
ADD COLUMN image_generate_count INT DEFAULT 0 COMMENT '图片生成次数',
ADD COLUMN survey_completed TINYINT DEFAULT 0 COMMENT '是否已完成问卷调查（0:未完成 1:已完成）';

-- ----------------------------
-- 2. 创建问卷调查表
-- ----------------------------
CREATE TABLE IF NOT EXISTS user_survey_questionnaire (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    company_name VARCHAR(200) DEFAULT '' COMMENT '公司名称（选填）',
    company_scale VARCHAR(50) DEFAULT '' COMMENT '公司规模（1-20人/20-50人/50-100人/100人以上）',
    monthly_products VARCHAR(50) DEFAULT '' COMMENT '每月上新产品数量（1-20/20-50/50-100/100+）',
    monthly_images VARCHAR(50) DEFAULT '' COMMENT '每月制作图片数量（100张以内/100-500张/500-2000张/2000+）',
    desired_features TEXT DEFAULT '' COMMENT '最希望新增什么功能（多选，JSON格式存储）',
    contact_info VARCHAR(100) DEFAULT '' COMMENT '联系方式（选填）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户问卷调查表';

-- ----------------------------
-- 3. 创建问卷调查统计视图
-- ----------------------------
CREATE OR REPLACE VIEW v_survey_statistics AS
SELECT 
    company_scale,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM user_survey_questionnaire), 2) as percentage
FROM user_survey_questionnaire 
WHERE company_scale != ''
GROUP BY company_scale;

-- ----------------------------
-- 4. 为现有用户初始化默认值
-- ----------------------------
UPDATE sys_user SET 
    login_count = 0,
    image_generate_count = 0,
    survey_completed = 0
WHERE login_count IS NULL OR image_generate_count IS NULL OR survey_completed IS NULL;