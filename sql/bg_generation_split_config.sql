-- ============================================================
-- 白底生成背景配置拆分迁移 V1.0
-- 把旧的单对象配置 bg_generation/config
--   { "sceneList":[...], "lightOptions":[...], "stylePresets":[...], "maxCount":4 }
-- 拆成 6 行独立的简单配置，与 AI白底图(white_bg) 的配置形式一致，
-- 这样在「管理后台 → 系统设置 → 创作配置 → 白底图生成背景」里，
-- 每一项都能用通用编辑器渲染成友好的表单（选项列表 / 文本选项 / 数字），
-- 普通用户无需手写 JSON。
--
-- 6 个键：
--   platform_options  选项列表（label/value）  使用平台
--   scene_list        文本选项（标签输入）     场景列表
--   light_options     文本选项（标签输入）     光线选项
--   style_presets     文本选项（标签输入）     风格预设
--   size_options      选项列表（label/value）  输出尺寸
--   max_count         数字                     生图数量上限
--
-- 幂等：可重复执行。已有同名行不会被覆盖（ON DUPLICATE KEY UPDATE 仅刷新内容）。
-- ============================================================

-- 1) 若存在旧的单对象配置 bg_generation/config，则先把它的内容拆到新键里。
--    使用临时变量保存旧值，避免多次解析。
SET @old_cfg := (
  SELECT `config_value`
  FROM `gh_creation_config`
  WHERE `config_group` = 'bg_generation' AND `config_key` = 'config'
  LIMIT 1
);

-- 1.1 使用平台 platform_options（旧配置里没有，用默认值）
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','platform_options','使用平台',
  '[{"label":"淘宝/天猫","value":"taobao"},{"label":"京东","value":"jd"},{"label":"拼多多","value":"pdd"},{"label":"抖音","value":"douyin"},{"label":"小红书","value":"xhs"},{"label":"亚马逊","value":"amazon"}]',
  1, '0', '白底生成背景-目标平台选择')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '使用平台', `remark` = '白底生成背景-目标平台选择';

-- 1.2 场景列表 scene_list（优先用旧配置里的 sceneList，否则用默认值）
SET @scene_val := IFNULL(
  JSON_UNQUOTE(JSON_EXTRACT(@old_cfg, '$.sceneList')),
  JSON_ARRAY('客厅','卧室','餐厅','厨房','书房','户外花园','阳台','酒店','办公室','商场')
);
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','scene_list','场景列表', @scene_val, 2, '0', '白底生成背景-场景选择（文本选项）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '场景列表', `remark` = '白底生成背景-场景选择（文本选项）';

-- 1.3 光线选项 light_options
SET @light_val := IFNULL(
  JSON_UNQUOTE(JSON_EXTRACT(@old_cfg, '$.lightOptions')),
  JSON_ARRAY('自然光','暖光','冷光','柔光','强光','侧光','逆光','氛围灯')
);
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','light_options','光线选项', @light_val, 3, '0', '白底生成背景-光线选择（文本选项）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '光线选项', `remark` = '白底生成背景-光线选择（文本选项）';

-- 1.4 风格预设 style_presets
SET @style_val := IFNULL(
  JSON_UNQUOTE(JSON_EXTRACT(@old_cfg, '$.stylePresets')),
  JSON_ARRAY('现代简约','北欧风','日式','工业风','轻奢','中式古典','美式乡村','地中海','极简','复古')
);
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','style_presets','风格预设', @style_val, 4, '0', '白底生成背景-风格选择（文本选项）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '风格预设', `remark` = '白底生成背景-风格选择（文本选项）';

-- 1.5 输出尺寸 size_options（旧配置里没有，用默认值）
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','size_options','输出尺寸',
  '[{"label":"1:1（800×800）","value":"800:800"},{"label":"3:4（800×1067）","value":"800:1067"},{"label":"4:3（1067×800）","value":"1067:800"},{"label":"自定义","value":"custom"}]',
  5, '0', '白底生成背景-输出尺寸选择')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '输出尺寸', `remark` = '白底生成背景-输出尺寸选择';

-- 1.6 生图数量上限 max_count
SET @max_val := IFNULL(
  JSON_UNQUOTE(JSON_EXTRACT(@old_cfg, '$.maxCount')),
  '4'
);
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','max_count','生图数量上限', @max_val, 6, '0', '白底生成背景-单次生成数量上限')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '生图数量上限', `remark` = '白底生成背景-单次生成数量上限';

-- 2) 清理旧的单对象配置行（数据已拆分到上面 6 行，不再需要）
DELETE FROM `gh_creation_config`
WHERE `config_group` = 'bg_generation' AND `config_key` = 'config';

-- 3) 校验：迁移后 bg_generation 组应有且仅有 6 行
SELECT `config_key`, `config_name`, `config_value`, `sort`, `status`
FROM `gh_creation_config`
WHERE `config_group` = 'bg_generation'
ORDER BY `sort`;
