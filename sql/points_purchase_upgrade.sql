-- ----------------------------
-- 积分套餐购买与支付增强 SQL
-- ----------------------------

-- 1) 用户积分套餐购买记录表索引优化（用于防重与列表查询）
ALTER TABLE `user_points_package_record`
  ADD INDEX `idx_user_package_status_end` (`user_id`, `package_id`, `status`, `validity_end_date`),
  ADD INDEX `idx_user_points_create_time` (`user_id`, `create_time`);

-- 2) 用户支付记录表约束与索引优化（用于订单幂等与退款定位）
ALTER TABLE `user_payment_record`
  ADD UNIQUE INDEX `uk_system_order_no` (`system_order_no`),
  ADD INDEX `idx_scenario_related` (`payment_scenario`, `scenario_related_id`),
  ADD INDEX `idx_user_payment_create_time` (`user_id`, `payment_method`, `payment_status`, `create_time`);
