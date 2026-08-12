-- 积分套餐记录删除权限

-- 查询积分套餐记录菜单ID
SET @recordMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'points:record:package:list' LIMIT 1);

-- 删除权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('记录删除', @recordMenuId, 10, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'points:record:package:remove', '#', 'admin', NOW(), '', NULL, '');
