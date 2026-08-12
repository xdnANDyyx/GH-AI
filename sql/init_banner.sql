-- =============================================
-- 插入 Banner 记录（首页/工作流/积分页 三个位置各 3 张）
-- 图片已提取到前端 public/banner/ 目录，通过 /banner/banner_N.jpg 引用
-- 可重复执行（先删后插）
-- =============================================

-- 先清理旧的初始化 Banner（通过 title 匹配）
DELETE FROM gh_banner WHERE title IN (
  '光合AI Studio 2.0 上线',
  'AI模特功能升级',
  '邀请好友得积分',
  'AI 模特创作新体验',
  '素材广场全新上线'
);

-- 为每个位置（0=首页, 1=工作流, 2=积分页）各插入 3 条 Banner
INSERT INTO gh_banner (title, image_url, link_url, position, sort, status, click_count, create_by, create_time, update_by, update_time)
VALUES
  -- 首页（position=0）
  ('光合AI Studio 2.0 上线', '/banner/banner_1.jpg', '', '0', 30, '0', 0, 'admin', NOW(), '', NULL),
  ('AI模特功能升级',         '/banner/banner_2.jpg', '', '0', 20, '0', 0, 'admin', NOW(), '', NULL),
  ('邀请好友得积分',         '/banner/banner_3.jpg', '', '0', 10, '0', 0, 'admin', NOW(), '', NULL),
  -- 工作流（position=1）
  ('光合AI Studio 2.0 上线', '/banner/banner_1.jpg', '', '1', 30, '0', 0, 'admin', NOW(), '', NULL),
  ('AI模特功能升级',         '/banner/banner_2.jpg', '', '1', 20, '0', 0, 'admin', NOW(), '', NULL),
  ('邀请好友得积分',         '/banner/banner_3.jpg', '', '1', 10, '0', 0, 'admin', NOW(), '', NULL),
  -- 积分页（position=2）
  ('光合AI Studio 2.0 上线', '/banner/banner_1.jpg', '', '2', 30, '0', 0, 'admin', NOW(), '', NULL),
  ('AI模特功能升级',         '/banner/banner_2.jpg', '', '2', 20, '0', 0, 'admin', NOW(), '', NULL),
  ('邀请好友得积分',         '/banner/banner_3.jpg', '', '2', 10, '0', 0, 'admin', NOW(), '', NULL);
