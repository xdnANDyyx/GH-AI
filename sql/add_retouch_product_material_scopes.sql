-- ============================================================
-- 1. 为产品精修(retouch)增加「产品类别」和「材质」的创作配置
-- ============================================================

INSERT INTO `gh_creation_config` (`config_group`, `config_key`, `config_name`, `config_value`, `sort`, `status`, `remark`)
SELECT 'retouch', 'product_options', '产品类别', 
 '[{"label":"不指定","value":""},{"label":"沙发","value":"product.sofa"},{"label":"床","value":"product.bed"},{"label":"餐桌","value":"product.dining_table"},{"label":"餐椅/办公椅","value":"product.chair"},{"label":"电视柜/边柜","value":"product.tv_stand"},{"label":"茶几","value":"product.coffee_table"},{"label":"灯具","value":"product.lamp"},{"label":"户外家具","value":"product.outdoor"}]', 
 7, '0', '产品精修-产品类别'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `gh_creation_config`
  WHERE `config_group` = 'retouch' AND `config_key` = 'product_options'
  AND `delete_at` IS NULL
);

INSERT INTO `gh_creation_config` (`config_group`, `config_key`, `config_name`, `config_value`, `sort`, `status`, `remark`)
SELECT 'retouch', 'material_options', '材质', 
 '[{"label":"不指定","value":""},{"label":"实木","value":"material.wood"},{"label":"布艺","value":"material.fabric"},{"label":"皮革","value":"material.leather"},{"label":"金属","value":"material.metal"},{"label":"玻璃","value":"material.glass"},{"label":"石材","value":"material.stone"},{"label":"藤编","value":"material.rattan"}]', 
 8, '0', '产品精修-材质'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `gh_creation_config`
  WHERE `config_group` = 'retouch' AND `config_key` = 'material_options'
  AND `delete_at` IS NULL
);

-- ============================================================
-- 2. 为产品类别(product)和材质(material)提示词库补齐适用功能(scope)
-- 由于后台的提示词筛选采用 LIKE 查询，导致 scope 为 NULL 的全局产品与材质选项
-- 在筛选特定工作台时被过滤掉。本脚本将它们的 scope 设置为覆盖所有适用工作台，
-- 以便前后台可以进行联动配置。
-- ============================================================

UPDATE `gh_prompt_library`
SET `scope` = 'white_bg,change_bg,ai_model,main_image,detail,dimension,retouch,banner,batch'
WHERE `category` IN ('product', 'material')
  AND `delete_at` IS NULL;
