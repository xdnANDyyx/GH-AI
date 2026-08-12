-- =============================================
-- 运营中心模块 - 数据库表结构
-- =============================================

-- ----------------------------
-- 1. Banner管理表
-- ----------------------------
DROP TABLE IF EXISTS `gh_banner`;
CREATE TABLE `gh_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT 'Banner标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片地址',
  `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `position` varchar(50) NOT NULL COMMENT '投放位置（home-首页/workflow-工作流/points-积分页）',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态（0-已发布 1-已下架 2-定时发布）',
  `click_count` int(11) DEFAULT '0' COMMENT '点击次数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner管理表';

-- ----------------------------
-- 2. Banner点击统计表
-- ----------------------------
DROP TABLE IF EXISTS `gh_banner_click_stat`;
CREATE TABLE `gh_banner_click_stat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `banner_id` bigint(20) NOT NULL COMMENT 'Banner ID',
  `click_date` date NOT NULL COMMENT '点击日期',
  `click_count` int(11) DEFAULT '0' COMMENT '点击次数',
  `unique_visitor` int(11) DEFAULT '0' COMMENT '独立访客数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_banner_date` (`banner_id`,`click_date`),
  KEY `idx_banner_id` (`banner_id`),
  KEY `idx_click_date` (`click_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner点击统计表';

-- ----------------------------
-- 3. 官方素材表
-- ----------------------------
DROP TABLE IF EXISTS `gh_official_material`;
CREATE TABLE `gh_official_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_name` varchar(100) NOT NULL COMMENT '素材名称',
  `material_url` varchar(500) NOT NULL COMMENT '素材地址',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图地址',
  `material_type` varchar(50) NOT NULL COMMENT '素材类型（image-图片/video-视频/model-模型）',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `width` int(11) DEFAULT NULL COMMENT '宽度（像素）',
  `height` int(11) DEFAULT NULL COMMENT '高度（像素）',
  `duration` int(11) DEFAULT NULL COMMENT '时长（秒，视频专用）',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0-已发布 1-已下架）',
  `download_count` int(11) DEFAULT '0' COMMENT '下载次数',
  `favorite_count` int(11) DEFAULT '0' COMMENT '收藏次数',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_type` (`material_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='官方素材表';

-- ----------------------------
-- 4. 素材标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `gh_material_tag_relation`;
CREATE TABLE `gh_material_tag_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` bigint(20) NOT NULL COMMENT '素材ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_tag` (`material_id`,`tag_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材标签关联表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 插入Banner示例数据
INSERT INTO `gh_banner` (`title`, `image_url`, `link_url`, `position`, `sort`, `status`, `click_count`) VALUES
('首页欢迎Banner', '/images/banner/welcome.jpg', '/workflow', 'home', 1, '0', 0),
('工作流引导Banner', '/images/banner/workflow-guide.jpg', '/workflow/create', 'workflow', 1, '0', 0);

-- 插入标签数据（用于素材分类）
INSERT INTO `gh_tag` (`tag_name`, `tag_type`, `sort`, `status`) VALUES
('人物', 'material', 1, '0'),
('风景', 'material', 2, '0'),
('动物', 'material', 3, '0'),
('建筑', 'material', 4, '0'),
('科技', 'material', 5, '0'),
('抽象', 'material', 6, '0'),
('节日', 'material', 7, '0');
