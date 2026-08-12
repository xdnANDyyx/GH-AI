-- =============================================
-- AI模特管理 & 创作功能管理 - 菜单配置
-- 运行前需确认运营中心菜单已存在
-- =============================================

-- 获取运营中心菜单ID
SET @operationMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '运营中心' AND parent_id = 0 ORDER BY menu_id DESC LIMIT 1);

-- =============================================
-- 1. AI模特管理菜单
-- =============================================
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('AI模特管理', @operationMenuId, 6, 'aimodel', 'ops/aiModel/index', 1, 0, 'C', '0', '0', 'gh:aimodel:list', 'peoples', 'admin', sysdate(), '', NULL, 'AI模特管理菜单');

SET @aiModelMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'AI模特管理' AND parent_id = @operationMenuId ORDER BY menu_id DESC LIMIT 1);

-- AI模特管理按钮权限
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('AI模特查询', @aiModelMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'gh:aimodel:query', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('AI模特新增', @aiModelMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'gh:aimodel:add', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('AI模特修改', @aiModelMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'gh:aimodel:edit', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('AI模特删除', @aiModelMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'gh:aimodel:remove', '#', 'admin', sysdate(), '', NULL, '');

-- =============================================
-- 2. 创作功能管理菜单
-- =============================================
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('创作功能管理', @operationMenuId, 7, 'creationConfig', 'ops/creationConfig/index', 1, 0, 'C', '0', '0', 'gh:creation:config', 'system', 'admin', sysdate(), '', NULL, '创作功能管理菜单');

SET @creationConfigMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '创作功能管理' AND parent_id = @operationMenuId ORDER BY menu_id DESC LIMIT 1);

-- 创作功能管理按钮权限
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('创作配置查询', @creationConfigMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'gh:creation:config:query', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('创作配置修改', @creationConfigMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'gh:creation:config:edit', '#', 'admin', sysdate(), '', NULL, '');