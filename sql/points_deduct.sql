-- ----------------------------
-- 扣分类型表
-- ----------------------------
DROP TABLE IF EXISTS `points_deduct_type`;
CREATE TABLE `points_deduct_type` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `type_code` VARCHAR(50) NOT NULL COMMENT '类型编码',
    `type_name` VARCHAR(100) NOT NULL COMMENT '类型名称',
    `deduct_rule` CHAR(1) NOT NULL DEFAULT '1' COMMENT '扣分规则（1.固定扣除，2.按量扣除）',
    `deduct_points` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '扣除积分数',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='扣分类型表';

-- ----------------------------
-- 初始化扣分类型数据
-- ----------------------------
INSERT INTO `points_deduct_type` (`id`, `type_code`, `type_name`, `deduct_rule`, `deduct_points`, `create_by`, `create_time`) VALUES
(1, 'text_to_image', '文生图', '2', 10, 'admin', NOW()),
(2, 'image_to_image', '图生图', '2', 15, 'admin', NOW()),
(3, 'generate_model', '生成模特', '1', 50, 'admin', NOW());

-- ----------------------------
-- 客户扣分记录表
-- ----------------------------
DROP TABLE IF EXISTS `user_points_deduct_record`;
CREATE TABLE `user_points_deduct_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户id',
    `deduct_type_id` BIGINT(20) NOT NULL COMMENT '扣分类型id',
    `deduct_type_code` VARCHAR(50) NOT NULL COMMENT '扣分类型编码',
    `related_record_id` BIGINT(20) DEFAULT NULL COMMENT '关联记录id（如生成记录id）',
    `package_record_id` BIGINT(20) DEFAULT NULL COMMENT '套餐记录id（积分从哪个套餐扣除）',
    `deduct_points` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '扣除积分数',
    `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '扣分状态（1.预扣除，2.已扣除，3.已回退）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_deduct_type_id` (`deduct_type_id`),
    KEY `idx_related_record_id` (`related_record_id`),
    KEY `idx_package_record_id` (`package_record_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='客户扣分记录表';

-- ----------------------------
-- 为user_points_package_record表添加已使用积分和剩余积分字段
-- ----------------------------
ALTER TABLE `user_points_package_record` 
ADD COLUMN `used_points` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '已使用积分' AFTER `points`,
ADD COLUMN `remaining_points` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '剩余积分' AFTER `used_points`;

-- 初始化现有记录的剩余积分（等于总积分）
UPDATE `user_points_package_record` SET `remaining_points` = `points` WHERE `remaining_points` = 0;

-- ----------------------------
-- 为user_points_package_record表添加套餐名称冗余字段
-- ----------------------------
ALTER TABLE `user_points_package_record` 
ADD COLUMN `package_name` VARCHAR(100) DEFAULT NULL COMMENT '套餐名称' AFTER `package_id`;

-- 初始化现有记录的套餐名称（从套餐表关联更新）
UPDATE `user_points_package_record` r 
INNER JOIN `points_package` p ON r.`package_id` = p.`id` 
SET r.`package_name` = p.`name` 
WHERE r.`package_name` IS NULL;
