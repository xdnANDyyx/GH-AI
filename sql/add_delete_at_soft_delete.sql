-- ============================================================
-- 逻辑删除字段迁移脚本
-- 为所有管理后台涉及的表增加 delete_at 字段
-- delete_at 为 NULL 表示未删除，有值表示已逻辑删除（值为删除时间）
-- ============================================================

-- 1. 系统参数表 sys_config
ALTER TABLE `sys_config` ADD COLUMN `delete_at` datetime DEFAULT NULL COMMENT '逻辑删除时间（NULL表示未删除）' AFTER `update_time`;

-- 2. 创作配置表 gh_creation_config
ALTER TABLE `gh_creation_config` ADD COLUMN `delete_at` datetime DEFAULT NULL COMMENT '逻辑删除时间（NULL表示未删除）' AFTER `update_time`;

-- 3. 提示词选项库表 gh_prompt_library
ALTER TABLE `gh_prompt_library` ADD COLUMN `delete_at` datetime DEFAULT NULL COMMENT '逻辑删除时间（NULL表示未删除）' AFTER `update_time`;

-- 4. 提示词模板表 gh_prompt_template
ALTER TABLE `gh_prompt_template` ADD COLUMN `delete_at` datetime DEFAULT NULL COMMENT '逻辑删除时间（NULL表示未删除）' AFTER `update_time`;

-- 5. 标签管理表 gh_tag
ALTER TABLE `gh_tag` ADD COLUMN `delete_at` datetime DEFAULT NULL COMMENT '逻辑删除时间（NULL表示未删除）' AFTER `update_time`;

-- ============================================================
-- 验证：确认所有表已增加 delete_at 字段
-- ============================================================
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME = 'delete_at'
  AND TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('sys_config', 'gh_creation_config', 'gh_prompt_library', 'gh_prompt_template', 'gh_tag');
