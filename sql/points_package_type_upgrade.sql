-- 积分套餐新增业务套餐类型字段
ALTER TABLE `points_package`
ADD COLUMN `package_type` char(1) NOT NULL DEFAULT '2' COMMENT '套餐类型（1.赠送套餐, 2.正常套餐）' AFTER `type`;

-- 历史数据默认视为正常套餐
UPDATE `points_package`
SET `package_type` = '2'
WHERE `package_type` IS NULL OR `package_type` = '';

-- 默认赠送套餐固定为 id=1
UPDATE `points_package`
SET `package_type` = '1'
WHERE `id` = 1;

-- 初始化字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '积分套餐类型', 'points_package_type', '0', 'admin', NOW(), '积分套餐业务类型'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'points_package_type'
);

-- 初始化字典数据：赠送套餐
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '赠送套餐', '1', 'points_package_type', '', 'warning', 'N', '0', 'admin', NOW(), '赠送套餐'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'points_package_type' AND `dict_value` = '1'
);

-- 初始化字典数据：正常套餐
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '正常套餐', '2', 'points_package_type', '', 'primary', 'Y', '0', 'admin', NOW(), '正常套餐'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'points_package_type' AND `dict_value` = '2'
);
