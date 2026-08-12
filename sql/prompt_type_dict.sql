-- =============================================
-- 提示词类型字典SQL脚本
-- =============================================

-- 新增字典类型：提示词类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) 
VALUES ('提示词类型', 'prompt_type', '0', 'admin', sysdate(), '提示词类型字典');

-- 新增字典数据：提示词类型选项（根据实际业务调整）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (1, 'AI绘画', 'ai_draw', 'prompt_type', '', 'primary', 'Y', '0', 'admin', sysdate(), 'AI绘画提示词');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (2, 'AI写作', 'ai_write', 'prompt_type', '', 'success', 'N', '0', 'admin', sysdate(), 'AI写作提示词');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (3, 'AI翻译', 'ai_translate', 'prompt_type', '', 'info', 'N', '0', 'admin', sysdate(), 'AI翻译提示词');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (4, 'AI编程', 'ai_code', 'prompt_type', '', 'warning', 'N', '0', 'admin', sysdate(), 'AI编程提示词');
