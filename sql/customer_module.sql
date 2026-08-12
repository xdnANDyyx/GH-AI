-- =============================================
-- 客户管理模块SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. 修改sys_user表user_type字段注释
-- ----------------------------
ALTER TABLE sys_user MODIFY COLUMN user_type varchar(2) DEFAULT '1' COMMENT '用户类型（1:系统管理员 2:客户）';

-- ----------------------------
-- 2. 更新现有数据，将系统用户的user_type设置为1
-- ----------------------------
UPDATE sys_user SET user_type = '1' WHERE user_type = '00' OR user_type IS NULL;

-- ----------------------------
-- 3. 新增字典类型：用户类型
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) 
VALUES ('用户类型', 'sys_user_type', '0', 'admin', sysdate(), '用户类型列表');

-- ----------------------------
-- 4. 新增字典数据：用户类型选项
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (1, '系统管理员', '1', 'sys_user_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '系统管理员');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) 
VALUES (2, '客户', '2', 'sys_user_type', '', 'success', 'N', '0', 'admin', sysdate(), '客户用户');
