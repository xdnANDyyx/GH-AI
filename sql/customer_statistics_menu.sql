-- =============================================
-- 客户统计菜单权限SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. 新增统计管理目录（一级菜单）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (3000, '统计管理', 0, 5, 'statistics', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', NULL, '统计管理目录');

-- ----------------------------
-- 2. 新增客户统计菜单（二级菜单）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (3001, '客户统计', 3000, 1, 'customer', 'statistics/customer/index', '', '', 1, 0, 'C', '0', '0', 'statistics:customer:query', 'peoples', 'admin', sysdate(), '', NULL, '客户统计菜单');

-- ----------------------------
-- 3. 为管理员角色分配菜单权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3001);
