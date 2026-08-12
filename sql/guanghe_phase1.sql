-- =============================================
-- 光合AI 一期(MVP) 数据库建表脚本
-- 版本：v1.0
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 说明：包含积分套餐、支付记录、积分变动、退款记录、
--       Banner管理、创作配置、提示词模板、标签管理
--       及对应的菜单权限和字典数据
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 第一部分：业务数据表
-- =============================================

-- ----------------------------
-- 1. 积分套餐表
-- ----------------------------
DROP TABLE IF EXISTS `gh_points_package`;
CREATE TABLE `gh_points_package` (
  `id`            bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`          varchar(100)  NOT NULL                COMMENT '套餐名称',
  `package_type`  tinyint(1)    NOT NULL DEFAULT 0      COMMENT '套餐类型（0免费 1付费）',
  `price`         decimal(10,2) NOT NULL DEFAULT 0.00   COMMENT '价格（元）',
  `points`        int(11)       NOT NULL DEFAULT 0      COMMENT '包含积分数',
  `validity_days` int(11)       NOT NULL DEFAULT 0      COMMENT '有效期（天），0=永久',
  `description`   varchar(500)  DEFAULT NULL             COMMENT '套餐描述',
  `rights_text`   text          DEFAULT NULL             COMMENT '权益说明（富文本）',
  `status`        char(1)       NOT NULL DEFAULT '0'    COMMENT '状态（0上架 1下架）',
  `sort`          int(11)       NOT NULL DEFAULT 0      COMMENT '排序权重',
  `create_by`     varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime      DEFAULT NULL             COMMENT '创建时间',
  `update_by`     varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime      DEFAULT NULL             COMMENT '更新时间',
  `remark`        varchar(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_package_type` (`package_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分套餐表';

-- ----------------------------
-- 2. 充值/支付记录表
-- ----------------------------
DROP TABLE IF EXISTS `gh_payment_record`;
CREATE TABLE `gh_payment_record` (
  `id`             bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no`       varchar(64)   NOT NULL                COMMENT '订单号',
  `user_id`        bigint(20)    NOT NULL                COMMENT '用户ID',
  `package_id`     bigint(20)    DEFAULT NULL             COMMENT '关联套餐ID',
  `amount`         decimal(10,2) NOT NULL DEFAULT 0.00   COMMENT '支付金额',
  `points`         int(11)       NOT NULL DEFAULT 0      COMMENT '获得积分',
  `pay_method`     varchar(20)   DEFAULT NULL             COMMENT '支付方式（wechat/alipay）',
  `pay_status`     char(1)       NOT NULL DEFAULT '0'    COMMENT '支付状态（0待支付 1已支付 2已退款）',
  `pay_time`       datetime      DEFAULT NULL             COMMENT '支付时间',
  `transaction_id` varchar(64)   DEFAULT NULL             COMMENT '第三方交易号',
  `create_by`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      DEFAULT NULL             COMMENT '创建时间',
  `update_by`      varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`    datetime      DEFAULT NULL             COMMENT '更新时间',
  `remark`         varchar(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_package_id` (`package_id`),
  KEY `idx_pay_status` (`pay_status`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='充值/支付记录表';

-- ----------------------------
-- 3. 积分变动明细表
-- ----------------------------
DROP TABLE IF EXISTS `gh_points_record`;
CREATE TABLE `gh_points_record` (
  `id`              bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         bigint(20)  NOT NULL                COMMENT '用户ID',
  `change_type`     tinyint(2)  NOT NULL                COMMENT '变动类型（1充值获得 2创作消耗 3上传奖励 4下载奖励 5退款返还 6手动调整）',
  `change_points`   int(11)     NOT NULL                COMMENT '变动积分（正数增加，负数减少）',
  `balance_after`   int(11)     NOT NULL DEFAULT 0      COMMENT '变动后余额',
  `related_order_no` varchar(64) DEFAULT NULL            COMMENT '关联订单号',
  `reason`          varchar(200) DEFAULT NULL            COMMENT '变动原因',
  `operator_id`     bigint(20)  DEFAULT NULL             COMMENT '操作人ID（手动调整时）',
  `create_by`       varchar(64) DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime    DEFAULT NULL             COMMENT '创建时间',
  `update_by`       varchar(64) DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime    DEFAULT NULL             COMMENT '更新时间',
  `remark`          varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分变动明细表';

-- ----------------------------
-- 4. 退款记录表
-- ----------------------------
DROP TABLE IF EXISTS `gh_refund_record`;
CREATE TABLE `gh_refund_record` (
  `id`                bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no`         varchar(64)   NOT NULL                COMMENT '退款单号',
  `original_order_no` varchar(64)   NOT NULL                COMMENT '原订单号',
  `user_id`           bigint(20)    NOT NULL                COMMENT '用户ID',
  `refund_amount`     decimal(10,2) NOT NULL DEFAULT 0.00   COMMENT '退款金额',
  `refund_points`     int(11)       NOT NULL DEFAULT 0      COMMENT '扣回积分',
  `refund_reason`     varchar(500)  DEFAULT NULL             COMMENT '退款原因',
  `status`            char(1)       NOT NULL DEFAULT '0'    COMMENT '状态（0待处理 1已退款 2已拒绝）',
  `process_time`      datetime      DEFAULT NULL             COMMENT '处理时间',
  `process_remark`    varchar(500)  DEFAULT NULL             COMMENT '处理备注',
  `create_by`         varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`       datetime      DEFAULT NULL             COMMENT '创建时间',
  `update_by`         varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`       datetime      DEFAULT NULL             COMMENT '更新时间',
  `remark`            varchar(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_original_order_no` (`original_order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款记录表';

-- ----------------------------
-- 5. Banner管理表
-- ----------------------------
DROP TABLE IF EXISTS `gh_banner`;
CREATE TABLE `gh_banner` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`       varchar(100) NOT NULL                COMMENT 'Banner标题',
  `image_url`   varchar(500) NOT NULL                COMMENT '图片地址',
  `link_url`    varchar(500) DEFAULT NULL             COMMENT '跳转链接',
  `position`    varchar(50)  NOT NULL DEFAULT 'home_top' COMMENT '展示位置（home_top/white_bg_page等）',
  `sort`        int(11)      NOT NULL DEFAULT 0      COMMENT '排序权重',
  `status`      char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0上架 1下架）',
  `click_count` int(11)      NOT NULL DEFAULT 0      COMMENT '点击次数',
  `start_time`  datetime     DEFAULT NULL             COMMENT '定时上架时间（NULL=立即）',
  `end_time`    datetime     DEFAULT NULL             COMMENT '定时下架时间（NULL=永不下架）',
  `create_by`   varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Banner管理表';

-- ----------------------------
-- 6. 创作配置表（系统设置）
-- ----------------------------
DROP TABLE IF EXISTS `gh_creation_config`;
CREATE TABLE `gh_creation_config` (
  `id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_group` varchar(50)  NOT NULL                COMMENT '配置分组（image_recognition/shadow_type/size_select/prompt等）',
  `config_key`   varchar(100) NOT NULL                COMMENT '配置键',
  `config_value` text         DEFAULT NULL             COMMENT '配置值（JSON格式）',
  `config_name`  varchar(100) DEFAULT NULL             COMMENT '配置名称（中文）',
  `sort`         int(11)      NOT NULL DEFAULT 0      COMMENT '排序权重',
  `status`       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  `create_by`    varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL             COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_key` (`config_group`, `config_key`),
  KEY `idx_config_group` (`config_group`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='创作配置表';

-- ----------------------------
-- 7. 提示词模板表
-- ----------------------------
DROP TABLE IF EXISTS `gh_prompt_template`;
CREATE TABLE `gh_prompt_template` (
  `id`         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module`     varchar(50)  NOT NULL                COMMENT '所属功能模块（white_bg/change_bg/main_image/detail/banner）',
  `name`       varchar(100) NOT NULL                COMMENT '模板名称',
  `content`    text         NOT NULL                 COMMENT '提示词内容',
  `is_default` char(1)      NOT NULL DEFAULT '0'    COMMENT '是否默认（0否 1是）',
  `sort`       int(11)      NOT NULL DEFAULT 0      COMMENT '排序权重',
  `status`     char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  `create_by`  varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time` datetime    DEFAULT NULL             COMMENT '创建时间',
  `update_by`  varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time` datetime    DEFAULT NULL             COMMENT '更新时间',
  `remark`     varchar(500) DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提示词模板表';

-- ----------------------------
-- 8. 标签管理表
-- ----------------------------
DROP TABLE IF EXISTS `gh_tag`;
CREATE TABLE `gh_tag` (
  `id`         bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_name`   varchar(50) NOT NULL                COMMENT '标签名称',
  `tag_type`   varchar(30) NOT NULL DEFAULT 'material' COMMENT '标签类型（material/user等）',
  `sort`       int(11)     NOT NULL DEFAULT 0      COMMENT '排序权重',
  `status`     char(1)     NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  `create_by`  varchar(64) DEFAULT ''              COMMENT '创建者',
  `create_time` datetime   DEFAULT NULL             COMMENT '创建时间',
  `update_by`  varchar(64) DEFAULT ''              COMMENT '更新者',
  `update_time` datetime   DEFAULT NULL             COMMENT '更新时间',
  `remark`     varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_name` (`tag_type`, `tag_name`),
  KEY `idx_tag_type` (`tag_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签管理表';


-- =============================================
-- 第二部分：菜单权限配置
-- 说明：menu_id 从 4000 起，避免与现有菜单冲突
--       现有占用：2000-2017（客户/购买记录）
--                  3000-3012（扣分/流水统计）
-- 菜单类型：M=目录  C=菜单  F=按钮
-- =============================================

-- ----------------------------
-- 一级目录：订单中心
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4000, '订单中心', 0, 6, 'order', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '', NULL, '订单中心目录');

-- ----------------------------
-- 二级菜单：套餐管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4001, '套餐管理', 4000, 1, 'package', 'order/package/index', '', '', 1, 0, 'C', '0', '0', 'gh:package:list', 'money', 'admin', NOW(), '', NULL, '积分套餐管理菜单');
-- 套餐管理按钮权限
INSERT INTO `sys_menu` VALUES (4002, '套餐查询', 4001, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:package:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4003, '套餐新增', 4001, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:package:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4004, '套餐修改', 4001, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:package:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4005, '套餐删除', 4001, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:package:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4006, '套餐导出', 4001, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:package:export', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 二级菜单：充值管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4010, '充值管理', 4000, 2, 'payment', 'order/payment/index', '', '', 1, 0, 'C', '0', '0', 'gh:payment:list', 'money', 'admin', NOW(), '', NULL, '充值/支付记录菜单');
-- 充值管理按钮权限
INSERT INTO `sys_menu` VALUES (4011, '充值查询', 4010, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:payment:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4012, '充值导出', 4010, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:payment:export', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 二级菜单：积分管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4020, '积分管理', 4000, 3, 'points', 'order/points/index', '', '', 1, 0, 'C', '0', '0', 'gh:points:list', 'education', 'admin', NOW(), '', NULL, '积分变动明细菜单');
-- 积分管理按钮权限
INSERT INTO `sys_menu` VALUES (4021, '积分查询', 4020, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:points:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4022, '积分调整', 4020, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:points:adjust', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4023, '积分导出', 4020, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:points:export', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 二级菜单：退款记录
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4030, '退款记录', 4000, 4, 'refund', 'order/refund/index', '', '', 1, 0, 'C', '0', '0', 'gh:refund:list', 'log', 'admin', NOW(), '', NULL, '退款记录菜单');
-- 退款记录按钮权限
INSERT INTO `sys_menu` VALUES (4031, '退款查询', 4030, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:refund:query',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4032, '退款处理', 4030, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:refund:process', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4033, '退款导出', 4030, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:refund:export',  '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 一级目录：运营中心
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4100, '运营中心', 0, 7, 'operation', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', NOW(), '', NULL, '运营中心目录');

-- ----------------------------
-- 二级菜单：Banner管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4101, 'Banner管理', 4100, 1, 'banner', 'operation/banner/index', '', '', 1, 0, 'C', '0', '0', 'gh:banner:list', 'picture', 'admin', NOW(), '', NULL, 'Banner管理菜单');
-- Banner管理按钮权限
INSERT INTO `sys_menu` VALUES (4102, 'Banner查询', 4101, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:banner:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4103, 'Banner新增', 4101, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:banner:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4104, 'Banner修改', 4101, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:banner:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4105, 'Banner删除', 4101, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:banner:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4106, 'Banner导出', 4101, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:banner:export', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 一级目录：创作配置（独立顶级菜单）
-- 说明：此处使用"创作配置"作为一级目录名称，
--       包含创作配置、提示词配置、标签管理三个子菜单。
--       如需挂载到若依默认"系统管理"(menu_id=1)下，
--       可将 parent_id 改为 1 并调整 order_num。
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4200, '创作配置', 0, 8, 'creation', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', NOW(), '', NULL, '创作配置目录');

-- ----------------------------
-- 二级菜单：创作参数配置
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4201, '创作参数', 4200, 1, 'config', 'creation/config/index', '', '', 1, 0, 'C', '0', '0', 'gh:config:list', 'edit', 'admin', NOW(), '', NULL, '创作参数配置菜单');
-- 创作配置按钮权限
INSERT INTO `sys_menu` VALUES (4202, '配置查询', 4201, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:config:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4203, '配置新增', 4201, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:config:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4204, '配置修改', 4201, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:config:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4205, '配置删除', 4201, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:config:remove', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 二级菜单：提示词配置
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4210, '提示词配置', 4200, 2, 'prompt', 'creation/prompt/index', '', '', 1, 0, 'C', '0', '0', 'gh:prompt:list', 'form', 'admin', NOW(), '', NULL, '提示词模板管理菜单');
-- 提示词配置按钮权限
INSERT INTO `sys_menu` VALUES (4211, '提示词查询', 4210, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:prompt:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4212, '提示词新增', 4210, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:prompt:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4213, '提示词修改', 4210, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:prompt:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4214, '提示词删除', 4210, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:prompt:remove', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 二级菜单：标签管理
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4220, '标签管理', 4200, 3, 'tag', 'creation/tag/index', '', '', 1, 0, 'C', '0', '0', 'gh:tag:list', 'tree-table', 'admin', NOW(), '', NULL, '标签管理菜单');
-- 标签管理按钮权限
INSERT INTO `sys_menu` VALUES (4221, '标签查询', 4220, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:tag:query',  '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4222, '标签新增', 4220, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:tag:add',    '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4223, '标签修改', 4220, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:tag:edit',   '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (4224, '标签删除', 4220, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'gh:tag:remove', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 管理员角色菜单权限分配（role_id=1 为超级管理员）
-- ----------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 4000), (1, 4001), (1, 4002), (1, 4003), (1, 4004), (1, 4005), (1, 4006),
(1, 4010), (1, 4011), (1, 4012),
(1, 4020), (1, 4021), (1, 4022), (1, 4023),
(1, 4030), (1, 4031), (1, 4032), (1, 4033),
(1, 4100), (1, 4101), (1, 4102), (1, 4103), (1, 4104), (1, 4105), (1, 4106),
(1, 4200), (1, 4201), (1, 4202), (1, 4203), (1, 4204), (1, 4205),
(1, 4210), (1, 4211), (1, 4212), (1, 4213), (1, 4214),
(1, 4220), (1, 4221), (1, 4222), (1, 4223), (1, 4224);


-- =============================================
-- 第三部分：字典类型与字典数据
-- 说明：使用 INSERT ... SELECT ... WHERE NOT EXISTS
--       保证可重复执行不会报主键冲突
-- =============================================

-- ----------------------------
-- 1. 字典类型：gh_package_type（套餐类型）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-套餐类型', 'gh_package_type', '0', 'admin', NOW(), '积分套餐类型（免费/付费）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_package_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '免费', '0', 'gh_package_type', '', 'success', 'N', '0', 'admin', NOW(), '免费套餐'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_package_type' AND `dict_value` = '0');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '付费', '1', 'gh_package_type', '', 'primary', 'N', '0', 'admin', NOW(), '付费套餐'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_package_type' AND `dict_value` = '1');

-- ----------------------------
-- 2. 字典类型：gh_pay_status（支付状态）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-支付状态', 'gh_pay_status', '0', 'admin', NOW(), '支付/充值状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_pay_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '待支付', '0', 'gh_pay_status', '', 'info',    'Y', '0', 'admin', NOW(), '订单待支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_pay_status' AND `dict_value` = '0');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '已支付', '1', 'gh_pay_status', '', 'success', 'N', '0', 'admin', NOW(), '订单已支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_pay_status' AND `dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '已退款', '2', 'gh_pay_status', '', 'warning', 'N', '0', 'admin', NOW(), '订单已退款'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_pay_status' AND `dict_value` = '2');

-- ----------------------------
-- 3. 字典类型：gh_points_change_type（积分变动类型）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-积分变动类型', 'gh_points_change_type', '0', 'admin', NOW(), '积分变动明细类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_points_change_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '充值获得', '1', 'gh_points_change_type', '', 'success', 'N', '0', 'admin', NOW(), '购买套餐充值获得积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '创作消耗', '2', 'gh_points_change_type', '', 'danger',  'N', '0', 'admin', NOW(), 'AI创作消耗积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '2');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '上传奖励', '3', 'gh_points_change_type', '', 'primary', 'N', '0', 'admin', NOW(), '上传素材奖励积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '3');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '下载奖励', '4', 'gh_points_change_type', '', 'primary', 'N', '0', 'admin', NOW(), '下载素材奖励积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '4');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '退款返还', '5', 'gh_points_change_type', '', 'warning', 'N', '0', 'admin', NOW(), '退款时返还积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '5');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '手动调整', '6', 'gh_points_change_type', '', 'info',    'N', '0', 'admin', NOW(), '管理员手动调整积分'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_points_change_type' AND `dict_value` = '6');

-- ----------------------------
-- 4. 字典类型：gh_banner_position（Banner展示位置）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-Banner展示位置', 'gh_banner_position', '0', 'admin', NOW(), 'Banner广告展示位置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_banner_position');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '首页顶部',   'home_top',      'gh_banner_position', '', 'primary', 'Y', '0', 'admin', NOW(), '首页顶部轮播Banner'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_banner_position' AND `dict_value` = 'home_top');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '白底图页面', 'white_bg_page', 'gh_banner_position', '', 'default', 'N', '0', 'admin', NOW(), '白底图功能页面Banner'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_banner_position' AND `dict_value` = 'white_bg_page');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '换背景页面', 'change_bg_page', 'gh_banner_position', '', 'default', 'N', '0', 'admin', NOW(), '换背景功能页面Banner'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_banner_position' AND `dict_value` = 'change_bg_page');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '主图页面',   'main_image_page', 'gh_banner_position', '', 'default', 'N', '0', 'admin', NOW(), '主图功能页面Banner'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_banner_position' AND `dict_value` = 'main_image_page');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '详情页页面', 'detail_page',   'gh_banner_position', '', 'default', 'N', '0', 'admin', NOW(), '详情页功能页面Banner'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_banner_position' AND `dict_value` = 'detail_page');

-- ----------------------------
-- 5. 字典类型：gh_creation_config_group（创作配置分组）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-创作配置分组', 'gh_creation_config_group', '0', 'admin', NOW(), '创作参数配置分组'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_creation_config_group');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '图像识别',   'image_recognition', 'gh_creation_config_group', '', 'primary', 'N', '0', 'admin', NOW(), '图像识别相关配置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_creation_config_group' AND `dict_value` = 'image_recognition');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '阴影类型',   'shadow_type',       'gh_creation_config_group', '', 'default', 'N', '0', 'admin', NOW(), '阴影类型配置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_creation_config_group' AND `dict_value` = 'shadow_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '尺寸选择',   'size_select',       'gh_creation_config_group', '', 'default', 'N', '0', 'admin', NOW(), '输出尺寸选择配置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_creation_config_group' AND `dict_value` = 'size_select');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '提示词配置', 'prompt',            'gh_creation_config_group', '', 'default', 'N', '0', 'admin', NOW(), '提示词相关配置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_creation_config_group' AND `dict_value` = 'prompt');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '模型参数',   'model_param',       'gh_creation_config_group', '', 'default', 'N', '0', 'admin', NOW(), 'AI模型调用参数配置'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_creation_config_group' AND `dict_value` = 'model_param');

-- ----------------------------
-- 6. 字典类型：gh_prompt_module（提示词功能模块）
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '光合-提示词功能模块', 'gh_prompt_module', '0', 'admin', NOW(), '提示词模板所属功能模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'gh_prompt_module');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '白底图',   'white_bg',    'gh_prompt_module', '', 'primary', 'N', '0', 'admin', NOW(), '白底图生成模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_module' AND `dict_value` = 'white_bg');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '换背景',   'change_bg',   'gh_prompt_module', '', 'default', 'N', '0', 'admin', NOW(), '换背景生成模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_module' AND `dict_value` = 'change_bg');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '主图',     'main_image',  'gh_prompt_module', '', 'default', 'N', '0', 'admin', NOW(), '主图生成模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_module' AND `dict_value` = 'main_image');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '详情页',   'detail',      'gh_prompt_module', '', 'default', 'N', '0', 'admin', NOW(), '详情页生成模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_module' AND `dict_value` = 'detail');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, 'Banner',   'banner',      'gh_prompt_module', '', 'default', 'N', '0', 'admin', NOW(), 'Banner生成模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'gh_prompt_module' AND `dict_value` = 'banner');


-- =============================================
-- 附录：菜单ID分配速查表
-- =============================================
--
-- 4000  订单中心（一级目录）
--   4001  套餐管理（菜单）
--     4002  套餐查询    4003  套餐新增
--     4004  套餐修改    4005  套餐删除    4006  套餐导出
--   4010  充值管理（菜单）
--     4011  充值查询    4012  充值导出
--   4020  积分管理（菜单）
--     4021  积分查询    4022  积分调整    4023  积分导出
--   4030  退款记录（菜单）
--     4031  退款查询    4032  退款处理    4033  退款导出
--
-- 4100  运营中心（一级目录）
--   4101  Banner管理（菜单）
--     4102  Banner查询  4103  Banner新增
--     4104  Banner修改  4105  Banner删除  4106  Banner导出
--
-- 4200  创作配置（一级目录）
--   4201  创作参数（菜单）
--     4202  配置查询    4203  配置新增
--     4204  配置修改    4205  配置删除
--   4210  提示词配置（菜单）
--     4211  提示词查询  4212  提示词新增
--     4213  提示词修改  4214  提示词删除
--   4220  标签管理（菜单）
--     4221  标签查询    4222  标签新增
--     4223  标签修改    4224  标签删除
--
-- =============================================

SET FOREIGN_KEY_CHECKS = 1;
