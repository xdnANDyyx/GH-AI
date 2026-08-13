-- ============================================================
-- 白底生成背景：创作配置 ↔ 提示词库 关联打通 V1.0
--
-- 目标（用户诉求）：
--   "用户选的是标签，但发送给 AI 的提示词是其对应的提示词"
--   创作配置(bg_generation) 里的每个选项的 value = 提示词库(gh_prompt_library)
--   里的 prompt_key，二者用 value/key 关联。工作台选中某标签后，
--   按 value 反查提示词库，把对应 prompt_text 拼进发给 AI 的提示词。
--
-- 关联目标选用 UI 选项库(opt_*)，因为它的 change_bg 条目与工作台标签完全吻合：
--   opt_platform.change_bg.*   使用平台  淘宝/京东/拼多多/抖音/小红书/亚马逊
--   opt_scene.change_bg.*      场景分类  居家/户外/工作室/节日主题/极简/自然/都市
--   opt_light.change_bg.*      光线选项  自然光/柔光/硬光/逆光/暖光/冷光
--   opt_style.change_bg.*      风格预设  简约/轻奢/活力/复古/科技感/ins风
--   （size_options 的 value 仍是尺寸字串如 "800:800"，作为出图参数，不进提示词）
--
-- 本脚本做两件事：
--   A. 为 opt_*.change_bg.* 行填充真实 prompt_text（当前为空 {}）
--   B. 重写 bg_generation 6 行创作配置：scene_list/light_options/style_presets
--      由"纯字符串数组"改为"{label,value}"对象数组，value 即提示词库 prompt_key
--
-- 幂等：可重复执行。依赖 gh_prompt_library_ui_options.sql 已建好 opt_* 行；
--      若未建，A 部分的 INSERT 会自行创建。
-- ============================================================

-- ============================================================
-- A. 填充 opt_*.change_bg.* 的 prompt_text（纯文本提示词）
-- ============================================================

-- A.1 使用平台 opt_platform.change_bg.*
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_platform','opt_platform.change_bg.taobao','淘宝/天猫','符合淘宝电商主图视觉习惯，商品主体突出，卖点清晰，适合移动端浏览，提升点击吸引力。','change_bg',1,'0'),
('opt_platform','opt_platform.change_bg.jd','京东','符合京东商品图片展示逻辑，信息明确，结构清晰，兼顾品牌感与转化效率。','change_bg',2,'0'),
('opt_platform','opt_platform.change_bg.pdd','拼多多','符合拼多多高转化电商视觉，突出价格优势与促销氛围，商品主体清晰。','change_bg',3,'0'),
('opt_platform','opt_platform.change_bg.douyin','抖音','符合抖音电商视觉风格，视觉冲击力强，突出商品卖点与场景化氛围。','change_bg',4,'0'),
('opt_platform','opt_platform.change_bg.xhs','小红书','符合小红书种草视觉，生活化场景与氛围感强，色调柔和有质感。','change_bg',5,'0'),
('opt_platform','opt_platform.change_bg.amazon','亚马逊','符合Amazon图片规范，画面简洁，主体清晰，信息准确，避免夸张促销元素。','change_bg',6,'0')
ON DUPLICATE KEY UPDATE `label` = VALUES(`label`), `prompt_text` = VALUES(`prompt_text`), `scope` = 'change_bg', `status` = '0';

-- A.2 场景分类 opt_scene.change_bg.*
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_scene','opt_scene.change_bg.home','居家','场景为居家环境，沙发茶几电视柜组合，温馨居家氛围。','change_bg',1,'0'),
('opt_scene','opt_scene.change_bg.outdoor','户外','场景为户外环境，自然光与植被，户外生活氛围。','change_bg',2,'0'),
('opt_scene','opt_scene.change_bg.studio','工作室','场景为摄影工作室，纯色或渐变背景，专业布光，突出主体。','change_bg',3,'0'),
('opt_scene','opt_scene.change_bg.festival','节日主题','场景为节日主题布置，符合节日氛围的装饰与色彩。','change_bg',4,'0'),
('opt_scene','opt_scene.change_bg.minimal','极简','场景为极简风格，留白充足，线条简洁，主体突出。','change_bg',5,'0'),
('opt_scene','opt_scene.change_bg.nature','自然','场景为自然环境，绿植与自然材质，清新氛围。','change_bg',6,'0'),
('opt_scene','opt_scene.change_bg.urban','都市','场景为都市空间，现代建筑与都市质感。','change_bg',7,'0')
ON DUPLICATE KEY UPDATE `label` = VALUES(`label`), `prompt_text` = VALUES(`prompt_text`), `scope` = 'change_bg', `status` = '0';

-- A.3 光线选项 opt_light.change_bg.*
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_light','opt_light.change_bg.natural','自然光','光线为自然光，柔和自然，色温适中。','change_bg',1,'0'),
('opt_light','opt_light.change_bg.soft','柔光','光线为柔光，漫射光，阴影柔和过渡。','change_bg',2,'0'),
('opt_light','opt_light.change_bg.hard','硬光','光线为硬光，方向性强，阴影边缘清晰对比强烈。','change_bg',3,'0'),
('opt_light','opt_light.change_bg.backlight','逆光','光线为逆光，勾勒主体轮廓，营造氛围感。','change_bg',4,'0'),
('opt_light','opt_light.change_bg.warm','暖光','光线为暖光，色温偏低，氛围温暖。','change_bg',5,'0'),
('opt_light','opt_light.change_bg.cool','冷光','光线为冷光，色温偏高，清冷通透。','change_bg',6,'0')
ON DUPLICATE KEY UPDATE `label` = VALUES(`label`), `prompt_text` = VALUES(`prompt_text`), `scope` = 'change_bg', `status` = '0';

-- A.4 风格预设 opt_style.change_bg.*
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`sort`,`status`) VALUES
('opt_style','opt_style.change_bg.minimal','简约','风格为简约，低装饰高留白，现代线条。','change_bg',1,'0'),
('opt_style','opt_style.change_bg.luxury','轻奢','风格为轻奢，金属与质感材质，精致高级。','change_bg',2,'0'),
('opt_style','opt_style.change_bg.vibrant','活力','风格为活力，色彩明快，氛围生动。','change_bg',3,'0'),
('opt_style','opt_style.change_bg.retro','复古','风格为复古，怀旧色调与材质，年代质感。','change_bg',4,'0'),
('opt_style','opt_style.change_bg.tech','科技感','风格为科技感，冷色调与几何线条，未来感。','change_bg',5,'0'),
('opt_style','opt_style.change_bg.ins','ins风','风格为ins风，柔和色调与生活美学，注重氛围与质感。','change_bg',6,'0')
ON DUPLICATE KEY UPDATE `label` = VALUES(`label`), `prompt_text` = VALUES(`prompt_text`), `scope` = 'change_bg', `status` = '0';

-- ============================================================
-- B. 重写 bg_generation 6 行创作配置：value = 提示词库 prompt_key
--    scene_list/light_options/style_presets 由纯字符串数组改为 {label,value} 对象数组
-- ============================================================

-- B.1 使用平台 platform_options
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','platform_options','使用平台',
  '[{"label":"淘宝/天猫","value":"opt_platform.change_bg.taobao"},{"label":"京东","value":"opt_platform.change_bg.jd"},{"label":"拼多多","value":"opt_platform.change_bg.pdd"},{"label":"抖音","value":"opt_platform.change_bg.douyin"},{"label":"小红书","value":"opt_platform.change_bg.xhs"},{"label":"亚马逊","value":"opt_platform.change_bg.amazon"}]',
  1, '0', '白底生成背景-目标平台（value=提示词库key）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '使用平台', `remark` = '白底生成背景-目标平台（value=提示词库key）';

-- B.2 场景列表 scene_list
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','scene_list','场景列表',
  '[{"label":"居家","value":"opt_scene.change_bg.home"},{"label":"户外","value":"opt_scene.change_bg.outdoor"},{"label":"工作室","value":"opt_scene.change_bg.studio"},{"label":"节日主题","value":"opt_scene.change_bg.festival"},{"label":"极简","value":"opt_scene.change_bg.minimal"},{"label":"自然","value":"opt_scene.change_bg.nature"},{"label":"都市","value":"opt_scene.change_bg.urban"}]',
  2, '0', '白底生成背景-场景（value=提示词库key）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '场景列表', `remark` = '白底生成背景-场景（value=提示词库key）';

-- B.3 光线选项 light_options
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','light_options','光线选项',
  '[{"label":"自然光","value":"opt_light.change_bg.natural"},{"label":"柔光","value":"opt_light.change_bg.soft"},{"label":"硬光","value":"opt_light.change_bg.hard"},{"label":"逆光","value":"opt_light.change_bg.backlight"},{"label":"暖光","value":"opt_light.change_bg.warm"},{"label":"冷光","value":"opt_light.change_bg.cool"}]',
  3, '0', '白底生成背景-光线（value=提示词库key）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '光线选项', `remark` = '白底生成背景-光线（value=提示词库key）';

-- B.4 风格预设 style_presets
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','style_presets','风格预设',
  '[{"label":"简约","value":"opt_style.change_bg.minimal"},{"label":"轻奢","value":"opt_style.change_bg.luxury"},{"label":"活力","value":"opt_style.change_bg.vibrant"},{"label":"复古","value":"opt_style.change_bg.retro"},{"label":"科技感","value":"opt_style.change_bg.tech"},{"label":"ins风","value":"opt_style.change_bg.ins"}]',
  4, '0', '白底生成背景-风格（value=提示词库key）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '风格预设', `remark` = '白底生成背景-风格（value=提示词库key）';

-- B.5 输出尺寸 size_options（value 仍为尺寸字串，作为出图参数，不进提示词）
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','size_options','输出尺寸',
  '[{"label":"1:1（800×800）","value":"800:800"},{"label":"3:4（800×1067）","value":"800:1067"},{"label":"4:3（1067×800）","value":"1067:800"},{"label":"自定义","value":"custom"}]',
  5, '0', '白底生成背景-输出尺寸（value=尺寸字串，出图参数）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '输出尺寸', `remark` = '白底生成背景-输出尺寸（value=尺寸字串，出图参数）';

-- B.6 生图数量上限 max_count
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
VALUES ('bg_generation','max_count','生图数量上限', '4', 6, '0', '白底生成背景-单次生成数量上限')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_name` = '生图数量上限', `remark` = '白底生成背景-单次生成数量上限';

-- 清理旧的单对象配置行（若存在）
DELETE FROM `gh_creation_config`
WHERE `config_group` = 'bg_generation' AND `config_key` = 'config';

-- ============================================================
-- 校验：bg_generation 应有 6 行；opt_*.change_bg.* 应有真实 prompt_text
-- ============================================================
SELECT `config_key`, `config_name`, `config_value` FROM `gh_creation_config` WHERE `config_group` = 'bg_generation' ORDER BY `sort`;
SELECT `category`, `prompt_key`, `label`, `prompt_text` FROM `gh_prompt_library` WHERE `prompt_key` LIKE 'opt_%.change_bg.%' ORDER BY `category`, `sort`;
