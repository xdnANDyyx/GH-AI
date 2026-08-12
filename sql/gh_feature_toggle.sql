-- =============================================
-- 功能开关表
-- 用于控制前台各 AI 功能模块的可见性和可用性
-- =============================================
DROP TABLE IF EXISTS `gh_feature_toggle`;
CREATE TABLE `gh_feature_toggle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `toggle_key` varchar(100) NOT NULL COMMENT '开关键（唯一标识，如 feature.white_bg）',
  `toggle_name` varchar(100) NOT NULL COMMENT '开关名称（如 AI白底图）',
  `description` varchar(500) DEFAULT NULL COMMENT '功能描述',
  `enabled` char(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0-关闭, 1-启用）',
  `module_group` varchar(50) NOT NULL DEFAULT 'work' COMMENT '模块分组：work-工作台, resource-资源管理, material-素材广场',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_toggle_key` (`toggle_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='功能开关表';

-- =============================================
-- 初始化功能开关数据（与 workMenus 对应）
-- =============================================
INSERT INTO `gh_feature_toggle` (`toggle_key`, `toggle_name`, `description`, `enabled`, `module_group`, `sort_order`) VALUES
('feature.white_bg',      'AI白底图',      '智能识别商品主体，一键生成干净白底图',         '1', 'work', 1),
('feature.white_to_bg',  '白底生成背景',   '智能识别商品主体，一键生成高质量场景背景',    '1', 'work', 2),
('feature.refine',        '产品精修',      'AI智能精修，提升商品质感',                   '1', 'work', 3),
('feature.ai_model',      'AI模特',        '为家居产品匹配适合的模特场景',               '1', 'work', 4),
('feature.main_image',    '主图设计',      '高转化主图设计，提升点击率和销量',            '1', 'work', 5),
('feature.detail_img',    '详情图/A+',     '专业电商视觉设计，提升转化率与品牌形象',      '1', 'work', 6),
('feature.size',          '尺寸标记',      '自动标注商品尺寸信息',                       '1', 'work', 7),
('feature.banner',        'Banner设计',    '专业的Banner设计工具，快速生成高转化Banner',  '1', 'work', 8),
('feature.batch_process', '批量生成',      '上传产品图与参考图，批量生成多套设计方案',      '1', 'work', 9),
('feature.material_plaza','素材广场',      '海量高质量家居场景素材',                     '1', 'material', 10),
('feature.history',       '历史记录',      '查看生成历史记录',                           '1', 'resource', 11),
('feature.points_center', '积分中心',      '积分明细与兑换',                             '1', 'resource', 12),
('feature.asset_manage',  '我的收藏',      '管理收藏的素材和作品',                       '1', 'material', 13);