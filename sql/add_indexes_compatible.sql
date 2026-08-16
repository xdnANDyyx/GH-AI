-- ============================================
-- 光合AI 数据库索引优化脚本（兼容 MySQL 5.7+）
-- 针对提示词配置、创作配置等查询慢的问题
-- ============================================

-- 提示词选项库 (gh_prompt_library)
-- 检查索引是否存在并创建
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_category');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_category already exists''', 'CREATE INDEX idx_prompt_library_category ON gh_prompt_library (category)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_scope');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_scope already exists''', 'CREATE INDEX idx_prompt_library_scope ON gh_prompt_library (scope)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_status already exists''', 'CREATE INDEX idx_prompt_library_status ON gh_prompt_library (status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_model');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_model already exists''', 'CREATE INDEX idx_prompt_library_model ON gh_prompt_library (model)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 组合索引：category + status
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_category_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_category_status already exists''', 'CREATE INDEX idx_prompt_library_category_status ON gh_prompt_library (category, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 组合索引：scope + status
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_scope_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_scope_status already exists''', 'CREATE INDEX idx_prompt_library_scope_status ON gh_prompt_library (scope, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排序字段索引
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_library' AND index_name = 'idx_prompt_library_sort');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_library_sort already exists''', 'CREATE INDEX idx_prompt_library_sort ON gh_prompt_library (sort)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================

-- 创作配置 (gh_creation_config)
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_creation_config' AND index_name = 'idx_creation_config_group');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_creation_config_group already exists''', 'CREATE INDEX idx_creation_config_group ON gh_creation_config (config_group)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_creation_config' AND index_name = 'idx_creation_config_key');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_creation_config_key already exists''', 'CREATE INDEX idx_creation_config_key ON gh_creation_config (config_key)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_creation_config' AND index_name = 'idx_creation_config_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_creation_config_status already exists''', 'CREATE INDEX idx_creation_config_status ON gh_creation_config (status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 组合索引：config_group + status
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_creation_config' AND index_name = 'idx_creation_config_group_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_creation_config_group_status already exists''', 'CREATE INDEX idx_creation_config_group_status ON gh_creation_config (config_group, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排序字段索引
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_creation_config' AND index_name = 'idx_creation_config_group_sort');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_creation_config_group_sort already exists''', 'CREATE INDEX idx_creation_config_group_sort ON gh_creation_config (config_group, sort)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================

-- 提示词模板 (gh_prompt_template)
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_template' AND index_name = 'idx_prompt_template_module');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_template_module already exists''', 'CREATE INDEX idx_prompt_template_module ON gh_prompt_template (module)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_template' AND index_name = 'idx_prompt_template_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_template_status already exists''', 'CREATE INDEX idx_prompt_template_status ON gh_prompt_template (status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 组合索引：module + status
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_template' AND index_name = 'idx_prompt_template_module_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_template_module_status already exists''', 'CREATE INDEX idx_prompt_template_module_status ON gh_prompt_template (module, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排序字段索引
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_prompt_template' AND index_name = 'idx_prompt_template_sort');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_prompt_template_sort already exists''', 'CREATE INDEX idx_prompt_template_sort ON gh_prompt_template (sort)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================

-- 标签 (gh_tag)
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_tag' AND index_name = 'idx_tag_type');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_tag_type already exists''', 'CREATE INDEX idx_tag_type ON gh_tag (tag_type)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_tag' AND index_name = 'idx_tag_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_tag_status already exists''', 'CREATE INDEX idx_tag_status ON gh_tag (status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 组合索引：tag_type + status
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_tag' AND index_name = 'idx_tag_type_status');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_tag_type_status already exists''', 'CREATE INDEX idx_tag_type_status ON gh_tag (tag_type, status)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排序字段索引
SET @exist_index = (SELECT COUNT(*) FROM information_schema.STATISTICS
WHERE table_schema = DATABASE() AND table_name = 'gh_tag' AND index_name = 'idx_tag_sort');
SET @sql = IF(@exist_index > 0, 'SELECT ''Index idx_tag_sort already exists''', 'CREATE INDEX idx_tag_sort ON gh_tag (sort)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 索引创建完成
-- 可通过以下 SQL 验证索引是否创建成功：
-- SHOW INDEX FROM gh_prompt_library;
-- SHOW INDEX FROM gh_creation_config;
-- SHOW INDEX FROM gh_prompt_template;
-- SHOW INDEX FROM gh_tag;
-- ============================================
