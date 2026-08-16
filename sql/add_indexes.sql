-- ============================================
-- 光合AI 数据库索引优化脚本
-- 针对提示词配置、创作配置等查询慢的问题
-- ============================================

-- 提示词选项库 (gh_prompt_library)
-- 高频过滤字段：category, scope, status, model
CREATE INDEX IF NOT EXISTS idx_prompt_library_category ON gh_prompt_library (category);
CREATE INDEX IF NOT EXISTS idx_prompt_library_scope ON gh_prompt_library (scope);
CREATE INDEX IF NOT EXISTS idx_prompt_library_status ON gh_prompt_library (status);
CREATE INDEX IF NOT EXISTS idx_prompt_library_model ON gh_prompt_library (model);
-- 组合索引：category + status（最常见的过滤组合）
CREATE INDEX IF NOT EXISTS idx_prompt_library_category_status ON gh_prompt_library (category, status);
-- 组合索引：scope 模糊查询 + status
CREATE INDEX IF NOT EXISTS idx_prompt_library_scope_status ON gh_prompt_library (scope, status);
-- 排序字段索引
CREATE INDEX IF NOT EXISTS idx_prompt_library_sort ON gh_prompt_library (sort);

-- 创作配置 (gh_creation_config)
-- 高频过滤字段：config_group, config_key, status
CREATE INDEX IF NOT EXISTS idx_creation_config_group ON gh_creation_config (config_group);
CREATE INDEX IF NOT EXISTS idx_creation_config_key ON gh_creation_config (config_key);
CREATE INDEX IF NOT EXISTS idx_creation_config_status ON gh_creation_config (status);
-- 组合索引：config_group + status（最常见的过滤组合）
CREATE INDEX IF NOT EXISTS idx_creation_config_group_status ON gh_creation_config (config_group, status);
-- 排序字段索引
CREATE INDEX IF NOT EXISTS idx_creation_config_group_sort ON gh_creation_config (config_group, sort);

-- 提示词模板 (gh_prompt_template)
-- 高频过滤字段：module, status
CREATE INDEX IF NOT EXISTS idx_prompt_template_module ON gh_prompt_template (module);
CREATE INDEX IF NOT EXISTS idx_prompt_template_status ON gh_prompt_template (status);
-- 组合索引：module + status
CREATE INDEX IF NOT EXISTS idx_prompt_template_module_status ON gh_prompt_template (module, status);
-- 排序字段索引
CREATE INDEX IF NOT EXISTS idx_prompt_template_sort ON gh_prompt_template (sort);

-- 标签 (gh_tag)
-- 高频过滤字段：tag_type, status
CREATE INDEX IF NOT EXISTS idx_tag_type ON gh_tag (tag_type);
CREATE INDEX IF NOT EXISTS idx_tag_status ON gh_tag (status);
-- 组合索引：tag_type + status
CREATE INDEX IF NOT EXISTS idx_tag_type_status ON gh_tag (tag_type, status);
-- 排序字段索引
CREATE INDEX IF NOT EXISTS idx_tag_sort ON gh_tag (sort);

-- 可选：如果数据量超过 10 万条，建议对历史数据归档
-- 示例：将 1 年前已停用的提示词配置归档到历史表
-- INSERT INTO gh_prompt_library_archive SELECT * FROM gh_prompt_library
-- WHERE status = '1' AND update_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);
-- DELETE FROM gh_prompt_library WHERE status = '1' AND update_time < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- ============================================
-- 索引创建完成，请根据实际数据量和查询模式调整
-- ============================================
