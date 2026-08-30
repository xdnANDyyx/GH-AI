-- ============================================================
-- 将尺寸标记(size_mark)中 ratio_options 的 config_name
-- 从"输出比例"改为"尺寸"，与提示词配置中的"尺寸"分类保持一致
-- ============================================================

UPDATE `gh_creation_config`
SET `config_name` = '尺寸',
    `remark` = '尺寸选择',
    `update_time` = NOW()
WHERE `config_group` = 'size_mark'
  AND `config_key` = 'ratio_options'
  AND `delete_at` IS NULL;
