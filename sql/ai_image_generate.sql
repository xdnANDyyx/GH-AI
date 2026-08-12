-- =============================================
-- AI图片生成模块SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. AI图片生成会话表
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_session`;
CREATE TABLE `ai_image_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `session_type` varchar(50) DEFAULT NULL COMMENT '会话类型（关联字典ai_session_type）',
  `session_name` varchar(200) DEFAULT NULL COMMENT '会话名称',
  `last_prompt` text DEFAULT NULL COMMENT '最后一条提示词',
  `generate_count` int(11) DEFAULT 0 COMMENT '生成次数',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:已删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_type` (`session_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成会话表';

-- ----------------------------
-- 2. AI图片生成记录表
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_generate_record`;
CREATE TABLE `ai_image_generate_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `session_id` bigint(20) DEFAULT NULL COMMENT '会话id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `type` char(1) NOT NULL DEFAULT '1' COMMENT '记录类型（1:对话,2:画布）',
  `generate_type` char(1) NOT NULL COMMENT '生成类型（1:文本生成, 2:图片生成）',
  `prompt` text NOT NULL COMMENT '文本描述',
  `chat_content` text DEFAULT NULL COMMENT '聊天内容',
  `target_images` text DEFAULT NULL COMMENT '目标图URL列表（JSON数组）',
  `reference_images` text DEFAULT NULL COMMENT '参考图URL列表（JSON数组）',
  `generate_count` int(11) NOT NULL DEFAULT 1 COMMENT '生成数量',
  `width` int(11) DEFAULT NULL COMMENT '图片宽度（像素）',
  `height` int(11) DEFAULT NULL COMMENT '图片高度（像素）',
  `options` text DEFAULT NULL COMMENT '生成选项（JSON对象，支持动态扩展）',
  `ai_provider` varchar(50) DEFAULT NULL COMMENT 'AI服务商',
  `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称',
  `ai_task_id` varchar(100) DEFAULT NULL COMMENT 'AI任务ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:待处理, 1:处理中, 2:成功, 3:失败）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成记录表';

-- ----------------------------
-- 2. 用户资产表
-- ----------------------------
DROP TABLE IF EXISTS `user_asset`;
CREATE TABLE `user_asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `asset_type` char(1) NOT NULL COMMENT '资产类型（1:AI生成图片）',
  `asset_name` varchar(200) DEFAULT NULL COMMENT '资产名称',
  `asset_url` varchar(500) NOT NULL COMMENT '资产URL（原始远程URL）',
  `local_path` varchar(500) DEFAULT NULL COMMENT '本地存储路径（相对路径，如：/profile/upload/2024/03/21/xxx.png）',
  `source_id` bigint(20) DEFAULT NULL COMMENT '来源id（关联ai_image_generate_record）',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';

-- ----------------------------
-- 3. AI图片生成选项配置表（支持动态扩展）
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_option_config`;
CREATE TABLE `ai_image_option_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `option_key` varchar(50) NOT NULL COMMENT '选项键（如：img_quality, img_ratio, back_set, img_style）',
  `option_name` varchar(100) NOT NULL COMMENT '选项名称（如：图片品质, 图片比例, 背景设置, 图片风格）',
  `option_type` varchar(50) DEFAULT 'select' COMMENT '选项类型（select:下拉选择, radio:单选, checkbox:多选）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_option_key` (`option_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成选项配置表';

-- ----------------------------
-- 4. AI图片生成选项值配置表
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_option_value`;
CREATE TABLE `ai_image_option_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `option_id` bigint(20) NOT NULL COMMENT '选项配置id',
  `value` varchar(100) NOT NULL COMMENT '选项值',
  `label` varchar(100) NOT NULL COMMENT '选项标签',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标图片',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `is_default` char(1) DEFAULT '0' COMMENT '是否默认（0:否, 1:是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_option_id` (`option_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成选项值配置表';

-- ----------------------------
-- 5. AI图片品质-比例-像素对应表
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_pixel_config`;
CREATE TABLE `ai_image_pixel_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `quality_value` varchar(50) NOT NULL COMMENT '品质值（如：1k, 2k）',
  `quality_label` varchar(100) NOT NULL COMMENT '品质标签（如：标清1K, 高清2K）',
  `ratio_value` varchar(50) NOT NULL COMMENT '比例值（如：1:1, 4:3, 3:2, 16:9, 21:9）',
  `ratio_label` varchar(100) NOT NULL COMMENT '比例标签（如：1:1正方形, 4:3标准, 3:2横版, 16:9宽屏, 21:9超宽）',
  `width` int(11) NOT NULL COMMENT '宽度（像素）',
  `height` int(11) NOT NULL COMMENT '高度（像素）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_quality_ratio` (`quality_value`, `ratio_value`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片品质-比例-像素对应表';

-- ----------------------------
-- 6. 初始化选项配置数据
-- ----------------------------

-- 图片品质配置
INSERT INTO `ai_image_option_config` (`option_key`, `option_name`, `option_type`, `sort_order`, `status`, `create_by`, `create_time`) 
VALUES ('img_quality', '图片品质', 'select', 1, '0', 'admin', NOW());

SET @quality_id = LAST_INSERT_ID();

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@quality_id, '1k', '标清1K', 1, 'Y', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@quality_id, '2k', '高清2K', 2, 'N', '0', 'admin', NOW());

-- 图片比例配置
INSERT INTO `ai_image_option_config` (`option_key`, `option_name`, `option_type`, `sort_order`, `status`, `create_by`, `create_time`) 
VALUES ('img_ratio', '图片比例', 'select', 2, '0', 'admin', NOW());

SET @ratio_id = LAST_INSERT_ID();

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@ratio_id, '1:1', '1:1 正方形', 1, 'Y', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@ratio_id, '4:3', '4:3 标准', 2, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@ratio_id, '3:2', '3:2 横版', 3, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@ratio_id, '16:9', '16:9 宽屏', 4, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@ratio_id, '21:9', '21:9 超宽', 5, 'N', '0', 'admin', NOW());

-- 背景设置配置
INSERT INTO `ai_image_option_config` (`option_key`, `option_name`, `option_type`, `sort_order`, `status`, `create_by`, `create_time`) 
VALUES ('back_set', '背景设置', 'select', 3, '0', 'admin', NOW());

SET @back_set_id = LAST_INSERT_ID();

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@back_set_id, 'white', '白底图', 1, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@back_set_id, 'original', '原图背景', 2, 'Y', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@back_set_id, 'transparent', '透明背景', 3, 'N', '0', 'admin', NOW());

-- 图片风格配置
INSERT INTO `ai_image_option_config` (`option_key`, `option_name`, `option_type`, `sort_order`, `status`, `create_by`, `create_time`) 
VALUES ('img_style', '图片风格', 'select', 4, '0', 'admin', NOW());

SET @style_id = LAST_INSERT_ID();

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@style_id, 'business', '商务风格', 1, 'Y', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@style_id, 'fashion', '时尚风格', 2, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@style_id, 'retro', '复古风格', 3, 'N', '0', 'admin', NOW());

INSERT INTO `ai_image_option_value` (`option_id`, `value`, `label`, `sort_order`, `is_default`, `status`, `create_by`, `create_time`) 
VALUES (@style_id, 'sports', '运动风格', 4, 'N', '0', 'admin', NOW());

-- ----------------------------
-- 7. 初始化品质-比例-像素对应数据
-- ----------------------------

-- 标清1K像素配置
INSERT INTO `ai_image_pixel_config` (`quality_value`, `quality_label`, `ratio_value`, `ratio_label`, `width`, `height`, `sort_order`, `status`, `create_by`, `create_time`) VALUES
('1k', '标清1K', '1:1', '1:1 正方形', 1328, 1328, 1, '0', 'admin', NOW()),
('1k', '标清1K', '4:3', '4:3 标准', 1472, 1104, 2, '0', 'admin', NOW()),
('1k', '标清1K', '3:2', '3:2 横版', 1584, 1056, 3, '0', 'admin', NOW()),
('1k', '标清1K', '16:9', '16:9 宽屏', 1664, 936, 4, '0', 'admin', NOW()),
('1k', '标清1K', '21:9', '21:9 超宽', 2016, 864, 5, '0', 'admin', NOW());

-- 高清2K像素配置
INSERT INTO `ai_image_pixel_config` (`quality_value`, `quality_label`, `ratio_value`, `ratio_label`, `width`, `height`, `sort_order`, `status`, `create_by`, `create_time`) VALUES
('2k', '高清2K', '1:1', '1:1 正方形', 2048, 2048, 1, '0', 'admin', NOW()),
('2k', '高清2K', '4:3', '4:3 标准', 2304, 1728, 2, '0', 'admin', NOW()),
('2k', '高清2K', '3:2', '3:2 横版', 2496, 1664, 3, '0', 'admin', NOW()),
('2k', '高清2K', '16:9', '16:9 宽屏', 2560, 1440, 4, '0', 'admin', NOW()),
('2k', '高清2K', '21:9', '21:9 超宽', 3024, 1296, 5, '0', 'admin', NOW());

-- ----------------------------
-- 8. 字典类型：生成类型
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) 
VALUES ('图片生成类型', 'ai_generate_type', '0', 'admin', NOW(), 'AI图片生成类型');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (1, '文本生成', '1', 'ai_generate_type', '', 'primary', 'Y', '0', 'admin', NOW(), '文本生成图片');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (2, '图片生成', '2', 'ai_generate_type', '', 'success', 'N', '0', 'admin', NOW(), '图片生成图片');

-- ----------------------------
-- 9. 字典类型：资产类型
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) 
VALUES ('用户资产类型', 'user_asset_type', '0', 'admin', NOW(), '用户资产类型');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (1, 'AI生成图片', '1', 'user_asset_type', '', 'primary', 'Y', '0', 'admin', NOW(), 'AI生成的图片');

-- =============================================
-- 增量更新SQL（用于已有数据库）
-- =============================================

-- ----------------------------
-- 新增会话表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_image_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `session_name` varchar(200) DEFAULT NULL COMMENT '会话名称',
  `last_prompt` text DEFAULT NULL COMMENT '最后一条提示词',
  `generate_count` int(11) DEFAULT 0 COMMENT '生成次数',
  `status` char(1) DEFAULT '0' COMMENT '状态（0:正常, 1:已删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成会话表';

-- ----------------------------
-- 为生成记录表添加会话ID字段
-- ----------------------------
-- 检查字段是否存在，不存在则添加
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_image_generate_record' AND COLUMN_NAME = 'session_id');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE `ai_image_generate_record` ADD COLUMN `session_id` bigint(20) DEFAULT NULL COMMENT ''会话id'' AFTER `id`', 'SELECT ''session_id column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 为生成记录表添加记录类型字段（1:对话,2:画布）
-- ----------------------------
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_image_generate_record' AND COLUMN_NAME = 'type');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE `ai_image_generate_record` ADD COLUMN `type` char(1) NOT NULL DEFAULT ''1'' COMMENT ''记录类型（1:对话,2:画布）'' AFTER `user_id`', 'SELECT ''type column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史数据默认回填为对话类型
UPDATE `ai_image_generate_record` SET `type` = '1' WHERE `type` IS NULL OR `type` = '';

-- 添加索引
SET @exist_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_image_generate_record' AND INDEX_NAME = 'idx_session_id');
SET @sql := IF(@exist_idx = 0, 'ALTER TABLE `ai_image_generate_record` ADD INDEX `idx_session_id` (`session_id`)', 'SELECT ''idx_session_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 为生成记录表添加模型名称字段
-- ----------------------------
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_image_generate_record' AND COLUMN_NAME = 'model_name');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE `ai_image_generate_record` ADD COLUMN `model_name` varchar(100) DEFAULT NULL COMMENT ''模型名称'' AFTER `ai_provider`', 'SELECT ''model_name column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 为生成记录表添加聊天内容字段
-- ----------------------------
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_image_generate_record' AND COLUMN_NAME = 'chat_content');
SET @sql := IF(@exist_col = 0, 'ALTER TABLE `ai_image_generate_record` ADD COLUMN `chat_content` text DEFAULT NULL COMMENT ''聊天内容'' AFTER `prompt`', 'SELECT ''chat_content column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 会话类型字典
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) 
VALUES ('AI会话类型', 'ai_session_type', '0', 'admin', NOW(), 'AI图片生成会话类型');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (1, '主图设计', 'main_image', 'ai_session_type', '', 'primary', 'Y', '0', 'admin', NOW(), '主图设计会话');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (2, '白底图设计', 'white_bg', 'ai_session_type', '', 'success', 'N', '0', 'admin', NOW(), '白底图设计会话');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) 
VALUES (3, '渲染图设计', 'render', 'ai_session_type', '', 'info', 'N', '0', 'admin', NOW(), '渲染图设计会话');

-- ----------------------------
-- 10. AI图片生成会话扩展表
-- ----------------------------
DROP TABLE IF EXISTS `ai_image_session_extend`;
CREATE TABLE `ai_image_session_extend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `session_id` bigint(20) NOT NULL COMMENT '会话id（关联ai_image_session.id）',
  `canvas_params` text COMMENT '画布参数（JSON格式）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成会话扩展表';
