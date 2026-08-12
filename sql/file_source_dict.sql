-- 文件来源字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) 
VALUES ('文件来源', 'file_source', '0', 'admin', NOW(), '文件上传来源');

-- 文件来源字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES 
(1, '管理端用户上传', 'admin_upload', 'file_source', '', 'primary', 'N', '0', 'admin', NOW(), '管理端用户上传的文件'),
(2, '客户用户上传', 'customer_upload', 'file_source', '', 'success', 'N', '0', 'admin', NOW(), '客户用户上传的文件');
