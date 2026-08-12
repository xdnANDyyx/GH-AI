-- =============================================
-- 客户端资产上架/下架素材广场能力升级
-- 涉及表：user_asset、material、material_type
-- 执行日期：2026-04-14
-- =============================================

-- ----------------------------
-- 1. user_asset 增加上架状态字段
-- ----------------------------
ALTER TABLE `user_asset`
ADD COLUMN `on_shelf_status` char(1) NOT NULL DEFAULT '0' COMMENT '上架状态（0:未上架, 1:已上架）' AFTER `status`;

-- ----------------------------
-- 2. material 增加用户资产关联字段
-- ----------------------------
ALTER TABLE `material`
ADD COLUMN `user_asset_id` bigint DEFAULT NULL COMMENT '关联用户资产ID' AFTER `upload_user_id`,
ADD KEY `idx_material_user_asset_id` (`user_asset_id`);

-- ----------------------------
-- 3. 新增素材上架下架操作记录表
-- ----------------------------
DROP TABLE IF EXISTS `material_shelf_operation_log`;
CREATE TABLE `material_shelf_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_asset_id` bigint NOT NULL COMMENT '用户资产ID',
  `material_id` bigint DEFAULT NULL COMMENT '素材ID',
  `material_type_id` bigint DEFAULT NULL COMMENT '素材类型ID',
  `operation_type` varchar(32) NOT NULL COMMENT '操作类型（upload:上架, remove:下架）',
  `operation_remark` varchar(255) DEFAULT '' COMMENT '操作备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_shelf_log_user_id` (`user_id`),
  KEY `idx_material_shelf_log_asset_id` (`user_asset_id`),
  KEY `idx_material_shelf_log_material_id` (`material_id`),
  KEY `idx_material_shelf_log_type_id` (`material_type_id`),
  KEY `idx_material_shelf_log_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材上架下架操作记录表';

-- ----------------------------
-- 4. material_type 说明
-- ----------------------------
-- material_type 表结构本次无需变更，仍用于上架时校验素材类型有效性。

