-- =============================================
-- 素材收藏能力升级脚本
-- 执行日期：2026-07-01
-- =============================================

SET @col_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'material'
      AND COLUMN_NAME = 'favorite_count'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `material` ADD COLUMN `favorite_count` int NOT NULL DEFAULT 0 COMMENT ''收藏人数'' AFTER `points`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `material_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '素材ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_material` (`user_id`, `material_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材收藏关系表';

