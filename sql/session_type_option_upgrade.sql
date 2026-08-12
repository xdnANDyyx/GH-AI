-- 会话类型选项配置关联表
CREATE TABLE IF NOT EXISTS `ai_session_type_option` (
  `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `session_type_id` BIGINT(20)    NOT NULL                COMMENT '会话类型ID',
  `model_name`      VARCHAR(100)  DEFAULT NULL             COMMENT '模型名称（如：kling-v3-omni, jimeng）',
  `option_key`      VARCHAR(100)  DEFAULT NULL             COMMENT '选项配置键（如：model_sex, img_quality）',
  `sort_order`      INT(11)       DEFAULT 0                COMMENT '排序',
  `create_by`       VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_type_id` (`session_type_id`),
  KEY `idx_model_name` (`model_name`),
  UNIQUE KEY `uk_session_type_model_option` (`session_type_id`, `model_name`, `option_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='会话类型选项配置关联表';

-- AI图片生成记录表新增扩展提示词字段
ALTER TABLE ai_image_generate_record ADD COLUMN extra_prompt TEXT COMMENT '扩展提示词（由选项配置转换生成的提示词）' AFTER prompt;
