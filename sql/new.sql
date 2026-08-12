-- 一级目录：积分管理
INSERT INTO `sys_menu` VALUES (2000, '积分管理', 0, 5, 'points', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', NOW(), '', NULL, '积分管理目录');

-- 二级菜单：积分套餐管理
INSERT INTO `sys_menu` VALUES (2001, '积分套餐管理', 2000, 1, 'package', 'points/package/index', '', '', 1, 0, 'C', '0', '0', 'points:package:list', 'list', 'admin', NOW(), '', NULL, '积分套餐管理菜单');

-- 按钮权限
INSERT INTO `sys_menu` VALUES (2002, '套餐查询', 2001, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:package:list',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '套餐详情', 2001, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:package:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '套餐新增', 2001, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:package:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '套餐修改', 2001, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:package:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '套餐删除', 2001, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:package:remove', '#', 'admin', NOW(), '', NULL, '');