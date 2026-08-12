-- Material reward upgrade script

-- 1) user_points_deduct_record: add points direction
ALTER TABLE `user_points_deduct_record`
    ADD COLUMN `points_direction` CHAR(1) NOT NULL DEFAULT '1' COMMENT '1=deduct,2=add' AFTER `deduct_points`;

-- 3) user_points_package_record.type: add material bonus type
ALTER TABLE `user_points_package_record`
    MODIFY COLUMN `type` CHAR(1) DEFAULT NULL COMMENT '1=purchase,2=gift,3=material bonus';

-- 4) Note:
-- material_ids are stored in ai_image_generate_record.options.material_ids.
