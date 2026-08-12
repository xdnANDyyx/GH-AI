-- =============================================
-- 用户注册信息表
-- 执行时间：按需执行
-- =============================================

-- 创建用户注册信息表
CREATE TABLE IF NOT EXISTS user_register_info (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    
    -- 基本信息
    position VARCHAR(50) DEFAULT '' COMMENT '岗位（老板/负责人/运营/设计师/美工/摄影/采购/其他）',
    position_other VARCHAR(100) DEFAULT '' COMMENT '岗位其他',
    
    -- 主营平台（多选，JSON格式）
    platforms TEXT DEFAULT '' COMMENT '主营平台（JSON数组）',
    platform_other VARCHAR(100) DEFAULT '' COMMENT '平台其他',
    
    -- 了解渠道（多选，JSON格式）
    channels TEXT DEFAULT '' COMMENT '了解渠道（JSON数组）',
    channel_other VARCHAR(100) DEFAULT '' COMMENT '渠道其他',
    
    -- 使用目的（多选，JSON格式）
    purposes TEXT DEFAULT '' COMMENT '使用目的（JSON数组）',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户注册信息表';