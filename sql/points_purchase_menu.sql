-- ----------------------------
-- 积分购买记录菜单 SQL
-- ----------------------------

-- 一级目录：购买记录管理（挂载到积分管理目录下）
INSERT INTO `sys_menu` VALUES (2010, '购买记录管理', 2000, 2, 'record', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '', NULL, '购买记录管理目录');

-- 二级菜单：积分套餐购买记录
INSERT INTO `sys_menu` VALUES (2011, '积分套餐购买记录', 2010, 1, 'packageRecord', 'points/record/packageRecord', '', '', 1, 0, 'C', '0', '0', 'points:record:package:list', 'form', 'admin', NOW(), '', NULL, '积分套餐购买记录菜单');
INSERT INTO `sys_menu` VALUES (2012, '购买下单', 2011, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:record:package:purchase', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2013, '记录查询', 2011, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:record:package:list', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2014, '订单退款', 2011, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:record:package:refund', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2015, '积分赠送', 2011, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:record:package:gift', '#', 'admin', NOW(), '', NULL, '');

-- 二级菜单：用户支付记录
INSERT INTO `sys_menu` VALUES (2016, '用户支付记录', 2010, 2, 'paymentRecord', 'points/record/paymentRecord', '', '', 1, 0, 'C', '0', '0', 'points:record:payment:list', 'money', 'admin', NOW(), '', NULL, '用户支付记录菜单');
INSERT INTO `sys_menu` VALUES (2017, '支付记录查询', 2016, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'points:record:payment:list', '#', 'admin', NOW(), '', NULL, '');
