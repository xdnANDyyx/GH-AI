-- ============================================================
-- 更新上传图片大小和数量限制
-- 单张最大上传：20MB → 7MB
-- 批量生成产品图上限：50 → 10
-- 批量生成参考图上限：20 → 10
-- ============================================================

-- 通用配置：单张最大上传 20MB → 7MB
UPDATE `gh_creation_config`
SET `config_value` = '7', `update_time` = NOW()
WHERE `config_group` = 'common'
  AND `config_key` = 'max_upload_size'
  AND `config_value` = '20';

-- 批量生成：产品图最大数量 50 → 10
UPDATE `gh_creation_config`
SET `config_value` = '10', `update_time` = NOW()
WHERE `config_group` = 'batch_process'
  AND `config_key` = 'max_product_images'
  AND `config_value` = '50';

-- 批量生成：参考图最大数量 20 → 10
UPDATE `gh_creation_config`
SET `config_value` = '10', `update_time` = NOW()
WHERE `config_group` = 'batch_process'
  AND `config_key` = 'max_ref_images'
  AND `config_value` = '20';
