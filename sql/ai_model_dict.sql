-- AI模型字典

-- 1) 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'AI模型', 'ai_model', '0', 'admin', NOW(), 'AI模型字典'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 'ai_model'
);

-- 2) 字典数据：即梦
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '即梦', 'jimeng', 'ai_model', '', 'primary', 'N', '0', 'admin', NOW(), '即梦模型'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'ai_model' AND dict_value = 'jimeng'
);

-- 3) 字典数据：可灵V3 Omni
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '可灵V3 Omni', 'kling-v3-omni', 'ai_model', '', 'success', 'Y', '0', 'admin', NOW(), '可灵V3 Omni模型'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'ai_model' AND dict_value = 'kling-v3-omni'
);
