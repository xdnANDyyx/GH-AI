-- =============================================
-- 系统流水统计菜单权限SQL脚本
-- 执行时间：按需执行
-- =============================================

-- ----------------------------
-- 1. 新增流水统计菜单（二级菜单，挂在统计管理目录下）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (3002, '流水统计', 3000, 2, 'paymentFlow', 'statistics/paymentFlow/index', '', '', 1, 0, 'C', '0', '0', 'statistics:paymentFlow:query', 'money', 'admin', sysdate(), '', NULL, '流水统计菜单');

-- ----------------------------
-- 2. 为管理员角色分配菜单权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3002);
