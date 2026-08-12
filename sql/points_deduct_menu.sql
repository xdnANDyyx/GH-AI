-- ----------------------------
-- 扣分管理菜单权限SQL
-- 菜单ID从3000开始
-- ----------------------------

-- ----------------------------
-- 1. 扣分管理目录
-- ----------------------------
INSERT INTO `sys_menu` VALUES (3000, '扣分管理', 2000, 3, 'deduct', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', NOW(), '', NULL, '扣分管理目录');

-- ----------------------------
-- 2. 扣分类型管理菜单
-- ----------------------------
INSERT INTO `sys_menu` VALUES (3001, '扣分类型', 3000, 1, 'type', 'points/deduct/type/index', '', '', 1, 0, 'C', '0', '0', 'points:deduct:type:list', 'build', 'admin', NOW(), '', NULL, '扣分类型菜单');

-- 扣分类型按钮权限
INSERT INTO `sys_menu` VALUES (3002, '扣分类型查询', 3001, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:type:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (3003, '扣分类型新增', 3001, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:type:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (3004, '扣分类型修改', 3001, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:type:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (3005, '扣分类型删除', 3001, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:type:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (3006, '扣分类型导出', 3001, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:type:export', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 3. 扣分记录管理菜单
-- ----------------------------
INSERT INTO `sys_menu` VALUES (3010, '扣分记录', 3000, 2, 'record', 'points/deduct/record/index', '', '', 1, 0, 'C', '0', '0', 'points:deduct:record:list', 'log', 'admin', NOW(), '', NULL, '扣分记录菜单');

-- 扣分记录按钮权限（仅查询）
INSERT INTO `sys_menu` VALUES (3011, '扣分记录查询', 3010, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:record:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (3012, '扣分记录导出', 3010, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:deduct:record:export', '#', 'admin', NOW(), '', NULL, '');
