-- =====================================================
-- 积分套餐 - 设计稿 5 档套餐初始化数据
-- 表：gh_points_package
-- =====================================================

-- 删除可能已存在的旧默认套餐（保留 id=1 的赠送套餐）
DELETE FROM `gh_points_package` WHERE `package_type` = 2 AND `name` IN ('入门版', '基础版', '专业版', '团队版', '企业版');

-- 1. 入门版
INSERT INTO `gh_points_package` (`name`, `package_type`, `price`, `points`, `validity_days`, `description`, `rights_text`, `status`, `sort`, `create_by`, `create_time`, `remark`)
SELECT '入门版', 2, 19.90, 100, 0, '适合新手体验', '适合个人轻量使用 / 日常基础出图 / 所有基础功能可用', '0', 1, 'admin', NOW(), '设计稿默认套餐'
WHERE NOT EXISTS (SELECT 1 FROM `gh_points_package` WHERE `name` = '入门版' AND `package_type` = 2);

-- 2. 基础版
INSERT INTO `gh_points_package` (`name`, `package_type`, `price`, `points`, `validity_days`, `description`, `rights_text`, `status`, `sort`, `create_by`, `create_time`, `remark`)
SELECT '基础版', 2, 199.00, 500, 0, '适合个人日常使用', '适合个人日常出图 / 更多生成次数 / 所有基础功能可用', '0', 2, 'admin', NOW(), '设计稿默认套餐'
WHERE NOT EXISTS (SELECT 1 FROM `gh_points_package` WHERE `name` = '基础版' AND `package_type` = 2);

-- 3. 专业版（推荐）
INSERT INTO `gh_points_package` (`name`, `package_type`, `price`, `points`, `validity_days`, `description`, `rights_text`, `status`, `sort`, `create_by`, `create_time`, `remark`)
SELECT '专业版', 2, 499.00, 2400, 0, '适合高频个人用户', '适合高频个人使用 / 更多积分，更低单价 / 所有功能畅享', '0', 3, 'admin', NOW(), '设计稿默认套餐 - 个人用户首选'
WHERE NOT EXISTS (SELECT 1 FROM `gh_points_package` WHERE `name` = '专业版' AND `package_type` = 2);

-- 4. 团队版
INSERT INTO `gh_points_package` (`name`, `package_type`, `price`, `points`, `validity_days`, `description`, `rights_text`, `status`, `sort`, `create_by`, `create_time`, `remark`)
SELECT '团队版', 2, 1299.00, 7500, 0, '适合小团队协作', '适合团队日常使用 / 可添加子账号（3人）/ 统一管理，灵活分配', '0', 4, 'admin', NOW(), '设计稿默认套餐'
WHERE NOT EXISTS (SELECT 1 FROM `gh_points_package` WHERE `name` = '团队版' AND `package_type` = 2);

-- 5. 企业版
INSERT INTO `gh_points_package` (`name`, `package_type`, `price`, `points`, `validity_days`, `description`, `rights_text`, `status`, `sort`, `create_by`, `create_time`, `remark`)
SELECT '企业版', 2, 3299.00, 20000, 0, '适合中大型团队/企业', '适合企业大规模使用 / 可添加子账号（10人）/ 专属客户经理服务 / 优先技术支持', '0', 5, 'admin', NOW(), '设计稿默认套餐'
WHERE NOT EXISTS (SELECT 1 FROM `gh_points_package` WHERE `name` = '企业版' AND `package_type` = 2);