-- =============================================
-- 客户管理菜单权限SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. 新增客户管理菜单（二级菜单）
-- 父菜单ID：1（系统管理）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2000, '客户管理', 1, 10, 'customer', 'system/customer/index', '', '', 1, 0, 'C', '0', '0', 'system:customer:list', 'peoples', 'admin', sysdate(), '', NULL, '客户管理菜单');

-- ----------------------------
-- 2. 新增客户管理按钮权限
-- ----------------------------
-- 客户查询
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2001, '客户查询', 2000, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:customer:query', '#', 'admin', sysdate(), '', NULL, '');

-- 客户修改（启用/禁用）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2002, '客户修改', 2000, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:customer:edit', '#', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 3. 为管理员角色分配客户管理菜单权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2002);

-- 为普通角色分配客户管理菜单权限（可选，根据实际需求调整）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 2000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 2001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 2002);
