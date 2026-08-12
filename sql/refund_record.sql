-- 退款记录表
CREATE TABLE IF NOT EXISTS `user_refund_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id` BIGINT NOT NULL COMMENT '用户id',
    `points_record_id` BIGINT NOT NULL COMMENT '积分购买记录id',
    `payment_record_id` BIGINT NOT NULL COMMENT '支付记录id',
    `system_order_no` VARCHAR(64) NOT NULL COMMENT '系统订单号',
    `refund_order_no` VARCHAR(64) DEFAULT NULL COMMENT '退款单号',
    `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    `refund_status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '退款状态（1.退款中, 2.退款成功, 3.退款失败）',
    `refund_method` CHAR(1) NOT NULL COMMENT '退款方式（1.微信, 2.支付宝）',
    `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标识（0.正常, 1.删除）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_points_record_id` (`points_record_id`),
    KEY `idx_system_order_no` (`system_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户退款记录表';

-- 修改购买记录表，添加退款总金额字段
ALTER TABLE `user_points_package_record` ADD COLUMN `refund_total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '退款总金额' AFTER `purchase_price`;
