-- AI图片配置：将“选项配置/像素配置”拆分为两个页面菜单
-- 执行前请先备份 sys_menu / sys_role_menu

SET @option_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'ai:option:config:list'
    AND menu_type = 'C'
  ORDER BY menu_id
  LIMIT 1
);

SET @parent_menu_id := (
  SELECT parent_id
  FROM sys_menu
  WHERE menu_id = @option_menu_id
  LIMIT 1
);

SET @next_order := (
  SELECT IFNULL(MAX(order_num), 0) + 1
  FROM sys_menu
  WHERE parent_id = @parent_menu_id
);

-- 1) 选项配置菜单指向独立页面（保留原 path）
UPDATE sys_menu
SET menu_name = '选项配置',
    component = 'aiImage/optionConfig/index',
    route_name = 'AiImageOptionConfig',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = @option_menu_id;

-- 2) 新增像素配置菜单（若不存在）
SET @pixel_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'ai:pixel:config:list'
    AND menu_type = 'C'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name,
  parent_id,
  order_num,
  path,
  component,
  query,
  route_name,
  is_frame,
  is_cache,
  menu_type,
  visible,
  status,
  perms,
  icon,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
)
SELECT
  '像素配置',
  @parent_menu_id,
  @next_order,
  'pixelConfig',
  'aiImage/pixelConfig/index',
  '',
  'AiImagePixelConfig',
  1,
  0,
  'C',
  '0',
  '0',
  'ai:pixel:config:list',
  'build',
  'admin',
  NOW(),
  '',
  NULL,
  'AI图片像素配置菜单'
FROM dual
WHERE @option_menu_id IS NOT NULL
  AND @pixel_menu_id IS NULL;

-- 3) 像素配置按钮权限（若不存在则补齐）
SET @pixel_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'ai:pixel:config:list'
    AND menu_type = 'C'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '像素配置查询', @pixel_menu_id, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'ai:pixel:config:query', '#', 'admin', NOW(), '', NULL, ''
FROM dual
WHERE @pixel_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @pixel_menu_id AND perms = 'ai:pixel:config:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '像素配置新增', @pixel_menu_id, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'ai:pixel:config:add', '#', 'admin', NOW(), '', NULL, ''
FROM dual
WHERE @pixel_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @pixel_menu_id AND perms = 'ai:pixel:config:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '像素配置修改', @pixel_menu_id, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'ai:pixel:config:edit', '#', 'admin', NOW(), '', NULL, ''
FROM dual
WHERE @pixel_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @pixel_menu_id AND perms = 'ai:pixel:config:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '像素配置删除', @pixel_menu_id, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'ai:pixel:config:remove', '#', 'admin', NOW(), '', NULL, ''
FROM dual
WHERE @pixel_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @pixel_menu_id AND perms = 'ai:pixel:config:remove');
