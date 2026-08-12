-- 提示词选项库管理菜单
-- 确保父菜单2000存在（光合业务管理目录）
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT 2000,'光合业务',0,5,'guanghe','',1,0,'M','0','0','','component','admin',NOW(),'光合业务管理目录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2000);

-- 菜单ID 2080-2085
DELETE FROM `sys_menu` WHERE `menu_id` IN (2080,2081,2082,2083,2084,2085);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
(2080,'提示词选项库',2000,30,'promptLibrary','business/promptLibrary/index',1,0,'C','0','0','gh:promptLibrary:list','dict','admin',NOW(),'光合AI提示词选项库管理');
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`) VALUES
(2081,'提示词选项库查询',2080,1,'','','1','0','F','0','0','gh:promptLibrary:query','#','admin',NOW(),''),
(2082,'提示词选项库新增',2080,2,'','','1','0','F','0','0','gh:promptLibrary:add','#','admin',NOW(),''),
(2083,'提示词选项库修改',2080,3,'','','1','0','F','0','0','gh:promptLibrary:edit','#','admin',NOW(),''),
(2084,'提示词选项库删除',2080,4,'','','1','0','F','0','0','gh:promptLibrary:remove','#','admin',NOW(),''),
(2085,'提示词选项库导出',2080,5,'','','1','0','F','0','0','gh:promptLibrary:export','#','admin',NOW(),'');

-- 给admin角色(role_id=1)授权
INSERT INTO `sys_role_menu` (`role_id`,`menu_id`) VALUES
(1,2000),(1,2080),(1,2081),(1,2082),(1,2083),(1,2084),(1,2085)
ON DUPLICATE KEY UPDATE `role_id`=`role_id`;