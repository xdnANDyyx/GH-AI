-- =============================================
-- AI图片生成明细异步改造脚本
-- 执行时间：按需执行
-- =============================================

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'request_snapshot'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record` ADD COLUMN `request_snapshot` json DEFAULT NULL COMMENT ''完整请求快照'' AFTER `options`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'total_count'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record` ADD COLUMN `total_count` int NOT NULL DEFAULT 0 COMMENT ''生成总数'' AFTER `status`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @dict_exists := (
    SELECT COUNT(1)
    FROM sys_dict_data
    WHERE dict_type = 'ai_generate_type'
      AND dict_value = 'image_prompt'
);
SET @ddl := IF(@dict_exists = 0,
    'INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES (3, ''图片反推提示词'', ''image_prompt'', ''ai_generate_type'', '''', ''warning'', ''N'', ''0'', ''admin'', NOW(), ''图片反推提示词'')',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'success_count'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record` ADD COLUMN `success_count` int NOT NULL DEFAULT 0 COMMENT ''成功数量'' AFTER `total_count`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'failed_count'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record` ADD COLUMN `failed_count` int NOT NULL DEFAULT 0 COMMENT ''失败数量'' AFTER `success_count`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'processing_count'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record` ADD COLUMN `processing_count` int NOT NULL DEFAULT 0 COMMENT ''处理中数量'' AFTER `failed_count`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_meta := (
    SELECT CHARACTER_MAXIMUM_LENGTH
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record'
      AND COLUMN_NAME = 'generate_type'
);
SET @ddl := IF(@col_meta IS NOT NULL AND @col_meta < 20,
    'ALTER TABLE `ai_image_generate_record` MODIFY COLUMN `generate_type` varchar(20) NOT NULL COMMENT ''生成类型（如：1、2、image_prompt）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `ai_image_generate_record_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint(20) NOT NULL COMMENT '关联ai_image_generate_record.id',
  `item_no` int NOT NULL COMMENT '第几张，从1开始',
  `final_prompt` text DEFAULT NULL COMMENT '本次生成实际提示词',
  `option_snapshot` json DEFAULT NULL COMMENT '本次生成实际使用的选项快照',
  `result_prompt` text DEFAULT NULL COMMENT '模型反推后的最终提示词',
  `call_type` char(1) NOT NULL DEFAULT '1' COMMENT '调用类型：1-图片生成，2-图片反推',
  `ai_task_id` varchar(100) DEFAULT NULL COMMENT 'AI任务ID',
  `status` varchar(20) NOT NULL COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED/RETRYABLE_FAILED',
  `error_msg` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `processing_attempt_id` varchar(64) DEFAULT NULL COMMENT '当前处理尝试ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_id_item_no` (`record_id`, `item_no`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_status_update_time` (`status`, `update_time`),
  KEY `idx_processing_attempt_id` (`processing_attempt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI图片生成记录明细表';

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record_detail'
      AND COLUMN_NAME = 'option_snapshot'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record_detail` ADD COLUMN `option_snapshot` json DEFAULT NULL COMMENT ''本次生成实际使用的选项快照'' AFTER `final_prompt`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record_detail'
      AND COLUMN_NAME = 'result_prompt'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record_detail` ADD COLUMN `result_prompt` text DEFAULT NULL COMMENT ''模型反推后的最终提示词'' AFTER `final_prompt`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_image_generate_record_detail'
      AND COLUMN_NAME = 'call_type'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `ai_image_generate_record_detail` ADD COLUMN `call_type` char(1) NOT NULL DEFAULT ''1'' COMMENT ''调用类型：1-图片生成，2-图片反推'' AFTER `result_prompt`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_asset'
      AND COLUMN_NAME = 'ai_generate_detail_id'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user_asset` ADD COLUMN `ai_generate_detail_id` bigint(20) DEFAULT NULL COMMENT ''关联ai_image_generate_record_detail.id'' AFTER `source_id`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_asset'
      AND INDEX_NAME = 'idx_ai_generate_detail_id'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `user_asset` ADD KEY `idx_ai_generate_detail_id` (`ai_generate_detail_id`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_points_deduct_record'
      AND COLUMN_NAME = 'related_detail_id'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user_points_deduct_record` ADD COLUMN `related_detail_id` bigint(20) DEFAULT NULL COMMENT ''关联ai_image_generate_record_detail.id'' AFTER `related_record_id`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_points_deduct_record'
      AND INDEX_NAME = 'idx_related_detail_id'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE `user_points_deduct_record` ADD KEY `idx_related_detail_id` (`related_detail_id`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
