-- ============================================================
-- 为 Banner设计(banner)添加语言配置项
-- 前台 Banner.vue 之前从 common.languages 回退加载，
-- 现在补齐专属语言配置，使后台创作配置可统一管理。
-- ============================================================

INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_name`,`config_value`,`sort`,`status`,`remark`)
SELECT 'banner', 'language_options', '语言列表',
       '[{"label":"中文（简体）","value":"zh-CN"},{"label":"英语（美国）","value":"en-US"},{"label":"英语（英国）","value":"en-GB"},{"label":"日语","value":"ja-JP"},{"label":"韩语","value":"ko-KR"},{"label":"德语","value":"de-DE"},{"label":"法语","value":"fr-FR"},{"label":"西班牙语","value":"es-ES"}]',
       7, '0', 'Banner语言选项'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `gh_creation_config`
  WHERE `config_group` = 'banner' AND `config_key` = 'language_options'
  AND `delete_at` IS NULL
);
