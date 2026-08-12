ALTER TABLE `ai_image_option_value`
ADD COLUMN `icon_url` varchar(500) DEFAULT NULL COMMENT '图标图片' AFTER `label`;
