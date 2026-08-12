-- ----------------------------
-- 1. 积分套餐表
-- ----------------------------
DROP TABLE IF EXISTS `points_package`;
CREATE TABLE `points_package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(100) DEFAULT NULL COMMENT '套餐名称',
  `type` char(1) DEFAULT '1' COMMENT '类型（1.月度, 2.季度, 3.年度）',
  `package_type` char(1) DEFAULT '2' COMMENT '套餐类型（1.赠送套餐, 2.正常套餐）',
  `points` bigint(20) DEFAULT NULL COMMENT '积分数',
  `validity_period` int(11) DEFAULT NULL COMMENT '有效期(天)',
  `original_price` decimal(10, 2) DEFAULT NULL COMMENT '原价',
  `discount_price` decimal(10, 2) DEFAULT NULL COMMENT '优惠价格',
  `description` varchar(500) DEFAULT NULL COMMENT '套餐描述',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='积分套餐表';

-- ----------------------------
-- 2. 用户积分套餐购买记录表
-- ----------------------------
DROP TABLE IF EXISTS `user_points_package_record`;
CREATE TABLE `user_points_package_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `type` char(1) DEFAULT NULL COMMENT '类型（1.购买, 2.赠送）',
  `status` char(1) DEFAULT NULL COMMENT '状态（1.无效，2.有效）',
  `package_id` bigint(20) DEFAULT NULL COMMENT '套餐id',
  `points` bigint(20) DEFAULT NULL COMMENT '积分数',
  `validity_start_date` datetime DEFAULT NULL COMMENT '有效期开始日期',
  `validity_end_date` datetime DEFAULT NULL COMMENT '有效期结束日期',
  `purchase_price` decimal(10, 2) DEFAULT NULL COMMENT '购买价格',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户积分套餐购买记录表';

-- ----------------------------
-- 3. 用户支付记录表
-- ----------------------------
DROP TABLE IF EXISTS `user_payment_record`;
CREATE TABLE `user_payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `third_party_order_id` varchar(64) DEFAULT NULL COMMENT '三方返回的订单id',
  `system_order_no` varchar(64) DEFAULT NULL COMMENT '系统生成的订单号',
  `payment_method` char(1) DEFAULT NULL COMMENT '支付方式(1.微信, 2.支付宝)',
  `payment_status` char(1) DEFAULT NULL COMMENT '支付状态 (1.初始状态, 2.发起支付, 3.支付成功, 4.支付失败, 5.取消支付, 6.退款)',
  `callback_success_time` datetime DEFAULT NULL COMMENT '支付回调成功时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标识',
  `version` bigint(20) DEFAULT 0 COMMENT '版本号',
  `buyer_id` bigint(20) DEFAULT NULL COMMENT '购买人/企业id',
  `payment_scenario` char(1) DEFAULT NULL COMMENT '支付场景（1.积分购买）',
  `scenario_related_id` bigint(20) DEFAULT NULL COMMENT '场景关联id',
  `total_price` decimal(10, 2) DEFAULT NULL COMMENT '总价',
  `description` varchar(500) DEFAULT NULL COMMENT '说明',
  `wechat_union_id` varchar(64) DEFAULT NULL COMMENT '微信unionId',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户支付记录表';

-- ----------------------------
-- 积分套餐表新增类型字段
-- ----------------------------
ALTER TABLE `points_package` ADD COLUMN `type` char(1) DEFAULT '1' COMMENT '类型（1.月度, 2.季度, 3.年度）' AFTER `name`;
