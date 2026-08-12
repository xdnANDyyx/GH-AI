-- =========================================================
-- 可灵选项升级SQL（按你要求：ai_image_option_config 新增 model_name 字段）
-- 目标：
-- 1) ai_image_option_config 表新增 model_name 字段
-- 2) 现有数据默认回填为 jimeng
-- 3) 新增 kling-v3-omni 的 img_quality / img_ratio 配置与选项值
-- =========================================================

-- ----------------------------
-- 1. ai_image_option_config 新增 model_name 字段（默认 jimeng）
-- ----------------------------
ALTER TABLE ai_image_option_config
    ADD COLUMN model_name VARCHAR(64) NOT NULL DEFAULT 'jimeng' COMMENT '模型名（如 jimeng、kling-v3-omni）' AFTER option_name;

-- 兼容历史数据（保险）
UPDATE ai_image_option_config
SET model_name = 'jimeng'
WHERE model_name IS NULL OR TRIM(model_name) = '';

-- ----------------------------
-- 2. 唯一约束改为 (option_key, model_name)
--    原表通常是 uk_option_key(option_key)
-- ----------------------------
ALTER TABLE ai_image_option_config
    DROP INDEX uk_option_key,
    ADD UNIQUE KEY uk_option_key_model (option_key, model_name);

-- ----------------------------
-- 3. 新增 kling-v3-omni 的 img_quality / img_ratio 选项配置
-- ----------------------------
INSERT INTO ai_image_option_config
    (option_key, option_name, model_name, option_type, sort_order, status, create_by, create_time, remark)
SELECT 'img_quality', '图片品质', 'kling-v3-omni', 'select', 1, '0', 'admin', NOW(), '可灵V3图片品质'
WHERE NOT EXISTS (
    SELECT 1
    FROM ai_image_option_config
    WHERE option_key = 'img_quality' AND model_name = 'kling-v3-omni'
);

INSERT INTO ai_image_option_config
    (option_key, option_name, model_name, option_type, sort_order, status, create_by, create_time, remark)
SELECT 'img_ratio', '图片比例', 'kling-v3-omni', 'select', 2, '0', 'admin', NOW(), '可灵V3图片比例'
WHERE NOT EXISTS (
    SELECT 1
    FROM ai_image_option_config
    WHERE option_key = 'img_ratio' AND model_name = 'kling-v3-omni'
);

SET @kling_quality_option_id := (
    SELECT id
    FROM ai_image_option_config
    WHERE option_key = 'img_quality' AND model_name = 'kling-v3-omni'
    LIMIT 1
);

SET @kling_ratio_option_id := (
    SELECT id
    FROM ai_image_option_config
    WHERE option_key = 'img_ratio' AND model_name = 'kling-v3-omni'
    LIMIT 1
);

-- ----------------------------
-- 4. img_quality（可灵常用：1K/2K）
-- ----------------------------
INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_quality_option_id, '1k', '标清1K', 1, 'Y', '0', 'admin', NOW(), '可灵V3品质'
WHERE @kling_quality_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_quality_option_id AND value = '1k'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_quality_option_id, '2k', '高清2K', 2, 'N', '0', 'admin', NOW(), '可灵V3品质'
WHERE @kling_quality_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_quality_option_id AND value = '2k'
  );

-- ----------------------------
-- 5. img_ratio（可灵常用比例）
--    1:1, 16:9, 4:3, 3:2, 2:3, 3:4, 9:16, 21:9
-- ----------------------------
INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '1:1', '1:1 正方形', 1, 'Y', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '1:1'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '16:9', '16:9 宽屏', 2, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '16:9'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '4:3', '4:3 标准', 3, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '4:3'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '3:2', '3:2 横版', 4, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '3:2'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '2:3', '2:3 竖版', 5, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '2:3'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '3:4', '3:4 竖版', 6, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '3:4'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '9:16', '9:16 竖屏', 7, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '9:16'
  );

INSERT INTO ai_image_option_value
    (option_id, value, label, sort_order, is_default, status, create_by, create_time, remark)
SELECT @kling_ratio_option_id, '21:9', '21:9 超宽', 8, 'N', '0', 'admin', NOW(), '可灵V3比例'
WHERE @kling_ratio_option_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM ai_image_option_value
      WHERE option_id = @kling_ratio_option_id AND value = '21:9'
  );
