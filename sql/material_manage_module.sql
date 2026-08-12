-- =============================================
-- 素材管理模块：建表 + 菜单权限 SQL
-- 执行日期：2026-04-11
-- =============================================

-- ----------------------------
-- 1. 素材类型表
-- ----------------------------
DROP TABLE IF EXISTS `material_type`;
CREATE TABLE `material_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级类型ID，0为顶级',
  `ancestors` varchar(500) NOT NULL DEFAULT '0' COMMENT '祖级链路，如：0,1,3',
  `sort_num` int NOT NULL DEFAULT '0' COMMENT '排序值，越小越靠前',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_material_type_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材类型表';

-- ----------------------------
-- 2. 素材表
-- ----------------------------
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `image_url` varchar(500) NOT NULL COMMENT '图片地址',
  `material_type_id` bigint NOT NULL COMMENT '素材类型ID',
  `source` varchar(32) NOT NULL COMMENT '来源：system-系统，customer_upload-客户上传',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传图片的用户ID',
  `shelf_status` char(1) NOT NULL DEFAULT '1' COMMENT '上下架状态（1:上架,0:下架）',
  `sort_num` int NOT NULL DEFAULT '0' COMMENT '排序值，越小越靠前',
  `points` int NOT NULL DEFAULT '5' COMMENT '积分',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏人数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_material_type_id` (`material_type_id`),
  KEY `idx_material_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';

-- ----------------------------
-- 3. 素材收藏关系表
-- ----------------------------
DROP TABLE IF EXISTS `material_favorite`;
CREATE TABLE `material_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '素材ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_material` (`user_id`, `material_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材收藏关系表';

-- ----------------------------
-- 4. 菜单与按钮权限（素材管理）
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3300, '素材管理', 0, 7, 'material', '', '', '', 1, 0, 'M', '0', '0', '', 'image', 'admin', sysdate(), '', NULL, '素材管理目录');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3301, '素材类型管理', 3300, 1, 'type', 'material/type/index', '', '', 1, 0, 'C', '0', '0', 'material:type:list', 'tree', 'admin', sysdate(), '', NULL, '素材类型管理菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3302, '素材广场', 3300, 2, 'plaza', 'material/plaza/index', '', '', 1, 0, 'C', '0', '0', 'material:library:list', 'picture', 'admin', sysdate(), '', NULL, '素材广场菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3303, '素材类型查询', 3301, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'material:type:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3304, '素材类型新增', 3301, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'material:type:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3305, '素材类型修改', 3301, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'material:type:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3306, '素材类型删除', 3301, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'material:type:remove', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3307, '素材查询', 3302, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'material:library:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3308, '素材新增', 3302, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'material:library:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3309, '素材修改', 3302, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'material:library:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3310, '素材删除', 3302, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'material:library:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3311, '素材排序', 3302, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'material:library:sort', '#', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 5. 角色授权（管理员）
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3300);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3301);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3302);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3303);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3304);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3305);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3306);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3307);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3308);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3309);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3310);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3311);
