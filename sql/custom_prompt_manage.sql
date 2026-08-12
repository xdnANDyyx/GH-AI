-- 自定义提示词管理

CREATE TABLE IF NOT EXISTS `custom_prompt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `prompt_content` text NOT NULL COMMENT '提示词内容',
  `source` varchar(32) NOT NULL DEFAULT 'system' COMMENT '来源（system-系统，customer_upload-客户自定义）',
  `create_user_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建用户ID',
  `tag` varchar(64) DEFAULT '' COMMENT '标签',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义提示词表';

-- 增量：为已有表新增标签字段（如已存在则忽略）
-- ALTER TABLE `custom_prompt` ADD COLUMN `tag` varchar(64) DEFAULT '' COMMENT '标签' AFTER `create_user_id`;

-- 在“AI图片配置”(menu_id=3100)下新增“自定义提示词管理”
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3312, '自定义提示词管理', 3100, 20, 'customPrompt', 'prompt/custom/index', '', '', 1, 0, 'C', '0', '0', 'custom:prompt:list', 'edit', 'admin', NOW(), '', NULL, '自定义提示词管理菜单');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3313, '自定义提示词查询', 3312, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'custom:prompt:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3314, '自定义提示词新增', 3312, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'custom:prompt:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3315, '自定义提示词修改', 3312, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'custom:prompt:edit', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3316, '自定义提示词删除', 3312, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'custom:prompt:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3317, '自定义提示词列表', 3312, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'custom:prompt:list', '#', 'admin', NOW(), '', NULL, '');
