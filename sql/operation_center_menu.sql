-- =============================================
-- 运营中心模块 - 菜单配置
-- =============================================

-- 插入运营中心一级菜单
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('运营中心', 0, 5, 'operations', NULL, 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', NULL, '运营中心目录');

-- 获取刚插入的运营中心菜单ID（假设为2000，实际运行时需要查询）
SET @operationMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '运营中心' AND parent_id = 0 ORDER BY menu_id DESC LIMIT 1);

-- =============================================
-- 1. Banner管理菜单
-- =============================================
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('Banner管理', @operationMenuId, 1, 'banner', 'ops/banner/index', 1, 0, 'C', '0', '0', 'gh:banner:list', 'guide', 'admin', sysdate(), '', NULL, 'Banner管理菜单');

SET @bannerMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = 'Banner管理' AND parent_id = @operationMenuId ORDER BY menu_id DESC LIMIT 1);

-- Banner管理按钮权限
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('Banner查询', @bannerMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'gh:banner:query', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('Banner新增', @bannerMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'gh:banner:add', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('Banner修改', @bannerMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'gh:banner:edit', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('Banner删除', @bannerMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'gh:banner:remove', '#', 'admin', sysdate(), '', NULL, '');

-- =============================================
-- 2. 官方素材管理菜单
-- =============================================
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('官方素材管理', @operationMenuId, 2, 'material', 'ops/material/index', 1, 0, 'C', '0', '0', 'gh:material:list', 'image', 'admin', sysdate(), '', NULL, '官方素材管理菜单');

SET @materialMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '官方素材管理' AND parent_id = @operationMenuId ORDER BY menu_id DESC LIMIT 1);

-- 官方素材管理按钮权限
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('素材查询', @materialMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'gh:material:query', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('素材新增', @materialMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'gh:material:add', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('素材修改', @materialMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'gh:material:edit', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('素材删除', @materialMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'gh:material:remove', '#', 'admin', sysdate(), '', NULL, '');

-- =============================================
-- 3. 标签管理菜单（如果不存在）
-- =============================================
-- 检查是否已存在标签管理菜单
SET @tagMenuExists = (SELECT COUNT(*) FROM sys_menu WHERE menu_name = '标签管理' AND perms = 'gh:tag:list');

-- 如果不存在，则插入标签管理菜单（作为运营中心的子菜单）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '标签管理', @operationMenuId, 3, 'tag', 'ops/tag/index', 1, 0, 'C', '0', '0', 'gh:tag:list', 'price-tag', 'admin', sysdate(), '', NULL, '标签管理菜单'
WHERE @tagMenuExists = 0;

SET @tagMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '标签管理' AND perms = 'gh:tag:list' ORDER BY menu_id DESC LIMIT 1);

-- 标签管理按钮权限（仅在菜单创建时添加）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '标签查询', @tagMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'gh:tag:query', '#', 'admin', sysdate(), '', NULL, ''
WHERE @tagMenuExists = 0;

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '标签新增', @tagMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'gh:tag:add', '#', 'admin', sysdate(), '', NULL, ''
WHERE @tagMenuExists = 0;

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '标签修改', @tagMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'gh:tag:edit', '#', 'admin', sysdate(), '', NULL, ''
WHERE @tagMenuExists = 0;

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '标签删除', @tagMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'gh:tag:remove', '#', 'admin', sysdate(), '', NULL, ''
WHERE @tagMenuExists = 0;

-- =============================================
-- 4. 字典数据配置
-- =============================================

-- Banner位置字典
INSERT INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('Banner位置', 'banner_position', '0', 'admin', sysdate(), 'Banner展示位置');

SET @bannerPositionDictId = (SELECT dict_id FROM sys_dict_type WHERE dict_type = 'banner_position' ORDER BY dict_id DESC LIMIT 1);

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '首页', 'home', 'banner_position', '', 'primary', 'Y', '0', 'admin', sysdate(), '首页Banner');

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2, '工作流页', 'workflow', 'banner_position', '', 'success', 'N', '0', 'admin', sysdate(), '工作流页Banner');

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (3, '积分页', 'points', 'banner_position', '', 'warning', 'N', '0', 'admin', sysdate(), '积分页Banner');

-- 素材类型字典
INSERT INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('素材类型', 'material_type', '0', 'admin', sysdate(), '官方素材类型');

SET @materialTypeDictId = (SELECT dict_id FROM sys_dict_type WHERE dict_type = 'material_type' ORDER BY dict_id DESC LIMIT 1);

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '图片', 'image', 'material_type', '', 'default', 'Y', '0', 'admin', sysdate(), '图片素材');

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2, '视频', 'video', 'material_type', '', 'success', 'N', '0', 'admin', sysdate(), '视频素材');

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (3, '模型', 'model', 'material_type', '', 'warning', 'N', '0', 'admin', sysdate(), '模型素材');
