-- 将 system_order_no 字段从 user_payment_record 表迁移到 user_points_package_record 表
-- 执行前请备份数据

-- 1. 在 user_points_package_record 表中新增 system_order_no 字段
ALTER TABLE `user_points_package_record` 
ADD COLUMN `system_order_no` varchar(64) DEFAULT NULL COMMENT '系统订单号' AFTER `purchase_price`;

-- 2. 为新字段添加索引
ALTER TABLE `user_points_package_record` 
ADD INDEX `idx_system_order_no` (`system_order_no`);

-- 3. 将现有数据从 user_payment_record 迁移到 user_points_package_record
UPDATE `user_points_package_record` upr
INNER JOIN `user_payment_record` upay ON upay.scenario_related_id = upr.id
SET upr.system_order_no = upay.system_order_no
WHERE upay.payment_scenario = '1' AND upay.del_flag = '0';

-- 4. 从 user_payment_record 表中删除 system_order_no 字段（可选，建议先保留观察一段时间）
-- ALTER TABLE `user_payment_record` DROP COLUMN `system_order_no`;
