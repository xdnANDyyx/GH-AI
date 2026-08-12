-- 将默认模型字段从 ai_session_type_config 迁移到 points_deduct_type

-- 1) points_deduct_type 增加默认模型字段
ALTER TABLE `points_deduct_type`
ADD COLUMN `default_model` VARCHAR(100) DEFAULT NULL COMMENT '默认模型（如：kling-v3-omni, jimeng）' AFTER `deduct_points`;

-- 2) 将会话类型上的默认模型回填到其默认扣分类型
UPDATE `points_deduct_type` p
INNER JOIN `ai_session_type_config` s ON s.`default_deduct_type_code` = p.`type_code`
SET p.`default_model` = s.`default_model`
WHERE s.`default_model` IS NOT NULL
  AND s.`default_model` <> '';

-- 3) ai_session_type_config 删除默认模型字段
ALTER TABLE `ai_session_type_config`
DROP COLUMN `default_model`;
