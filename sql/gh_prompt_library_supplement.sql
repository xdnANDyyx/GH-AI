-- ============================================================
-- 光合AI Studio 提示词库补充脚本 V1.0
-- 1. 为 gh_prompt_library 增加 ab_group / success_rate 字段（对应文档第11节后台配置字段）
-- 2. 补充缺失的负向约束（retouch/detail/dimension/banner/batch，对应文档6.3/6.6/6.7/6.8/6.9）
-- 3. 新增模型适配层配置（GPT/Gemini 适配规则，对应文档第10节）
-- 4. 新增提示词效果评估配置（对应文档第12节）
-- ============================================================

-- ============================================================
-- 1. 扩展字段：ab_group / success_rate
-- ============================================================
ALTER TABLE `gh_prompt_library` ADD COLUMN IF NOT EXISTS `ab_group` varchar(10) DEFAULT NULL COMMENT 'A/B测试组（A/B）' AFTER `version`;
ALTER TABLE `gh_prompt_library` ADD COLUMN IF NOT EXISTS `success_rate` decimal(5,2) DEFAULT NULL COMMENT '效果成功率统计（%）' AFTER `ab_group`;

-- ============================================================
-- 2. 补充缺失的负向约束 category=negative
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`priority`,`is_default`,`sort`,`remark`) VALUES
('negative','negative.retouch','产品精修负向约束','不塑料化，不改变材质类型，不添加部件，不改变产品颜色，不产生重复纹理、过度反光或油腻高光。','retouch',95,'0',7,'对应文档6.3'),
('negative','negative.detail','详情图/A+负向约束','不得虚构尺寸、认证、材质、功能和售后承诺；不得生成乱码；不得出现不同页面商品结构不一致。','detail',95,'0',8,'对应文档6.6'),
('negative','negative.dimension','尺寸标记负向约束','不改变产品比例，不错误连接尺寸线，不混用单位，不遮挡关键结构。','dimension',95,'0',9,'对应文档6.7'),
('negative','negative.banner','Banner负向约束','不生成乱码；不虚构价格、折扣和活动规则；不遮挡商品，不堆叠过多装饰。','banner',95,'0',10,'对应文档6.8'),
('negative','negative.batch','批量生成负向约束','禁止每张图出现不同产品结构；禁止同一套页面风格失控；禁止重复方案和明显低质量结果。','batch',95,'0',11,'对应文档6.9');

-- ============================================================
-- 3. 模型适配层配置 category=model_adapt（对应文档第10节）
--    存储GPT/Gemini在不同处理阶段的适配规则
-- ============================================================
INSERT INTO `gh_prompt_library` (`category`,`prompt_key`,`label`,`prompt_text`,`scope`,`model`,`priority`,`is_default`,`sort`,`remark`) VALUES
('model_adapt','model_adapt.gpt.image_understanding','GPT-图片理解','要求输出结构化JSON：产品、材质、视角、问题点。先分析图片再提取固定字段。','all','gpt',200,'1',1,'GPT图片理解阶段适配'),
('model_adapt','model_adapt.gemini.image_understanding','Gemini-图片理解','要求先分析图片，再输出固定字段JSON。对每张图编号并说明用途。','all','gemini',200,'1',2,'Gemini图片理解阶段适配'),
('model_adapt','model_adapt.gpt.prompt_combine','GPT-提示词组合','指令层级清晰，先规则后任务，强调不得改变结构。明确"产品图""参考图""材质图"的角色。','all','gpt',200,'1',3,'GPT提示词组合阶段适配'),
('model_adapt','model_adapt.gemini.prompt_combine','Gemini-提示词组合','减少冲突描述，按主体→环境→约束顺序组织。对每张图编号并说明用途，避免图像角色混淆。','all','gemini',200,'1',4,'Gemini提示词组合阶段适配'),
('model_adapt','model_adapt.gpt.multi_image','GPT-多图引用','明确"产品图""参考图""材质图"的角色，避免图像角色混淆。','all','gpt',200,'1',5,'GPT多图引用适配'),
('model_adapt','model_adapt.gemini.multi_image','Gemini-多图引用','对每张图编号并说明用途，避免图像角色混淆。','all','gemini',200,'1',6,'Gemini多图引用适配'),
('model_adapt','model_adapt.gpt.local_edit','GPT-局部改图','强调只修改指定区域，其他区域保持不变。','all','gpt',200,'1',7,'GPT局部改图适配'),
('model_adapt','model_adapt.gemini.local_edit','Gemini-局部改图','明确目标对象、位置和保持项，避免全局重绘。','all','gemini',200,'1',8,'Gemini局部改图适配'),
('model_adapt','model_adapt.gpt.result_check','GPT-结果校验','返回生成结果前进行结构、变形、文字和平台规则检查。','all','gpt',200,'1',9,'GPT结果校验适配'),
('model_adapt','model_adapt.gemini.result_check','Gemini-结果校验','调用后端视觉校验流程进行二次检测。','all','gemini',200,'1',10,'Gemini结果校验适配');

-- ============================================================
-- 4. 提示词效果评估配置（存入 gh_creation_config，对应文档第12节）
--    前台生成时记录提示词版本、模型、功能、耗时、积分成本和结果
--    用户重新生成、继续修改、下载、收藏或放弃均作为效果信号
--    高频失败问题标签：结构变化、材质失真、透视错误、手部异常、文字乱码、光影冲突
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_value`,`config_name`,`sort`,`status`,`create_by`,`create_time`,`remark`) VALUES
('prompt_evaluation','config',
'{"enabled":true,"trackFields":["promptVersion","model","functionType","duration","pointsCost","resultStatus"],"userSignals":["regenerate","continueEdit","download","favorite","abandon"],"failureTags":["structure_change","material_distortion","perspective_error","hand_anomaly","text_garble","light_conflict"],"statsDimensions":["productType","functionType"],"versionRollback":true,"abTestEnabled":true,"showFullPromptToAdminOnly":true}',
'提示词效果评估配置',0,'0','admin',NOW(),'对应文档第12节：提示词效果评估与迭代')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `update_time` = NOW();

-- ============================================================
-- 5. 对话改图追问配置（对应文档6.10 系统追问条件）
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_value`,`config_name`,`sort`,`status`,`create_by`,`create_time`,`remark`) VALUES
('dialog_edit','follow_up_config',
'{"enabled":true,"fuzzyExpressions":["放大一点","左一点","右一点","稍微俯视","稍微仰视","小一点","高一点","低一点"],"followUpPrompt":"您的描述比较模糊，请选择明确的比例、方向或角度：\\n- 比例：30%/40%/50%/60%/70%/80%\\n- 方向：左30°/左45°/右30°/右45°\\n- 角度：俯视10°/俯视15°/俯视30°/仰视10°","protectionRules":["只修改用户明确指出的区域","未提及区域保持不变","保持原图尺寸、构图、视角、光线和整体风格","调整大小必须转化为明确比例或画面占比","调整角度必须转化为明确度数","修改后重新匹配阴影、透视、色温和环境反射"]}',
'对话改图追问与保护规则',0,'0','admin',NOW(),'对应文档6.10：对话改图/局部修改')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `update_time` = NOW();

-- ============================================================
-- 6. 批量生成一致性配置（对应文档6.9）
-- ============================================================
INSERT INTO `gh_creation_config` (`config_group`,`config_key`,`config_value`,`config_name`,`sort`,`status`,`create_by`,`create_time`,`remark`) VALUES
('batch_generation','consistency_config',
'{"enabled":true,"consistencyRules":["同一商品在所有结果中保持结构、颜色、材质、比例和视角逻辑一致","不同方案只改变场景、构图、营销方向或版式","每套方案分配版本号","保持品牌、平台和尺寸规则一致"],"autoCheck":["deformation","duplicate","failed","lowQuality"],"negativeRules":["禁止每张图出现不同产品结构","禁止同一套页面风格失控","禁止重复方案和明显低质量结果"]}',
'批量生成一致性配置',0,'0','admin',NOW(),'对应文档6.9：批量生成')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `update_time` = NOW();

-- ============================================================
-- 7. 更新字典：增加 model_adapt 分类
-- ============================================================
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`remark`) VALUES
(13,'模型适配','model_adapt','gh_prompt_library_category','','primary','N','0','admin',NOW(),'GPT/Gemini模型适配层配置')
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================================
-- 8. 为现有 function 类目设置 A/B 测试组默认值
-- ============================================================
UPDATE `gh_prompt_library` SET `ab_group` = 'A' WHERE `category` = 'function' AND `ab_group` IS NULL;
UPDATE `gh_prompt_library` SET `success_rate` = 0.00 WHERE `category` = 'function' AND `success_rate` IS NULL;