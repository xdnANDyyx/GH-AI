-- 删除未被引用的 opt_ratio 提示词库数据
-- 这些数据 scope 为 dimension，但 SizeMark.vue 查询 scope 为 size_mark，查不到
-- 且创作配置中也没有引用 opt_ratio 的任何 promptKey
-- SizeMark.vue 的尺寸选项是前端硬编码的，不依赖这些数据
-- 前端 unifiedCategoryOptions 中也删除了 opt_ratio 分类项

-- 软删除
UPDATE `gh_prompt_library` 
SET `delete_at` = NOW() 
WHERE `category` = 'opt_ratio';

-- 如需物理删除，取消注释以下语句：
-- DELETE FROM `gh_prompt_library` WHERE `category` = 'opt_ratio';
