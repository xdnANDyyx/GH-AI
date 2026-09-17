-- Canvas Editor 编辑历史表
-- 创建时间: 2026-09-07
-- 说明: 存储画布编辑器的所有编辑操作历史

CREATE TABLE IF NOT EXISTS `canvas_edit_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `image_id` VARCHAR(255) NOT NULL COMMENT '图片ID或URL',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型: extend/multi-angle/edit-text/partial-redraw/detect-layers/download-layers',
    `params` JSON COMMENT '操作参数(JSON格式)',
    `result_url` TEXT COMMENT '结果图片URL',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(同一图片递增)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_image_id` (`image_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`),
    UNIQUE KEY `uk_image_version` (`image_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画布编辑器编辑历史表';

-- 添加注释
ALTER TABLE `canvas_edit_history` COMMENT = '画布编辑器编辑历史表';
