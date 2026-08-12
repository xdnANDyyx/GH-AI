-- 用户表新增邀请码字段
ALTER TABLE `sys_user`
ADD COLUMN `invite_code` varchar(16) DEFAULT NULL COMMENT '邀请码' AFTER `wechat_union_id`;

ALTER TABLE `sys_user`
ADD UNIQUE KEY `uk_sys_user_invite_code` (`invite_code`);

-- 邀请关系表
CREATE TABLE IF NOT EXISTS `user_invite_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inviter_user_id` bigint(20) NOT NULL COMMENT '邀请人用户ID',
  `invitee_user_id` bigint(20) NOT NULL COMMENT '被邀请人用户ID',
  `invite_code` varchar(16) NOT NULL COMMENT '邀请码',
  `reward_points` bigint(20) NOT NULL DEFAULT 500 COMMENT '奖励积分',
  `reward_status` char(1) NOT NULL DEFAULT '1' COMMENT '奖励状态（1待发放 2已发放）',
  `inviter_reward_order_no` varchar(64) DEFAULT NULL COMMENT '邀请人奖励单号',
  `invitee_reward_order_no` varchar(64) DEFAULT NULL COMMENT '被邀请人奖励单号',
  `bind_time` datetime DEFAULT NULL COMMENT '绑定时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_invite_record_invitee` (`invitee_user_id`),
  UNIQUE KEY `uk_user_invite_record_inviter_reward` (`inviter_reward_order_no`),
  UNIQUE KEY `uk_user_invite_record_invitee_reward` (`invitee_reward_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请奖励记录表';

-- 邀请奖励记录类型说明
-- user_points_package_record.type = 4 表示邀请奖励
