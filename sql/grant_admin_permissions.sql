-- =============================================
-- 授予 admin 账户最高权限
-- 确保 admin 用户、角色、菜单权限完整
-- 可重复执行
-- =============================================

-- 1. 确保 admin 用户为系统管理员类型且启用
UPDATE sys_user
SET user_type = '1',
    status = '0',
    del_flag = '0'
WHERE user_name = 'admin';

-- 2. 确保 admin 用户绑定超级管理员角色 (role_id=1)
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, 1
FROM sys_user u
WHERE u.user_name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = u.user_id AND ur.role_id = 1
  );

-- 3. 确保超级管理员角色存在且启用
UPDATE sys_role
SET status = '0',
    del_flag = '0'
WHERE role_key = 'admin';

-- 4. 确保超级管理员角色拥有所有 gh:points 相关菜单权限
--    菜单 ID: 4020(积分管理), 4021(积分查询), 4022(积分调整), 4023(积分导出)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (4020, 4021, 4022, 4023)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 5. 确保超级管理员角色拥有所有 gh:order 相关菜单权限
--    菜单 ID: 4000(订单中心), 4010(套餐管理), 4011~4013, 4030(充值管理), 4031~4033
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (4000, 4001, 4002, 4003, 4004, 4005, 4006,
                    4010, 4011, 4012,
                    4030, 4031, 4032, 4033)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 6. 确保超级管理员角色拥有所有运营中心相关菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (4100, 4101, 4102, 4103, 4104, 4105, 4106,
                    4200, 4201, 4202, 4203, 4204, 4205,
                    4210, 4211, 4212, 4213, 4214,
                    4220, 4221, 4222, 4223, 4224)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 7. 添加积分记录删除权限菜单（gh:points:remove）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time,
                      update_by, update_time, remark)
SELECT 4024, '积分删除', 4020, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:points:remove',
       '#', 'admin', NOW(), '', NULL, ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 4024);

-- 8. 确保超级管理员角色拥有积分删除权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 4024
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 4024);

-- 9. 确保超级管理员角色拥有所有 gh:banner 和 gh:material 相关菜单权限
--    通过权限字符串匹配，覆盖所有 Banner 和素材菜单（无论 menu_id 是多少）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE (m.perms LIKE 'gh:banner:%' OR m.perms LIKE 'gh:material:%' OR m.perms LIKE 'gh:tag:%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 10. 确保超级管理员角色拥有所有运营中心顶级目录及其子菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'gh:%' OR parent_id IN (
        SELECT menu_id FROM sys_menu WHERE perms LIKE 'gh:%'
    )
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );