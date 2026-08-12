-- AI图片生成结果查询定时任务
-- 每30秒执行一次，查询待处理和处理中的AI图片生成记录

-- 方式一：直接插入任务（需要重启服务才能生效）
-- 查询当前最大job_id
SELECT IFNULL(MAX(job_id), 0) INTO @max_job_id FROM sys_job;

-- 插入定时任务配置（状态为正常，需要重启服务）
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) 
VALUES (@max_job_id + 1, 'AI图片生成结果查询', 'DEFAULT', 'aiImageGenerateTask.queryGenerateResult()', '0/30 * * * * ?', '2', '1', '0', 'admin', NOW(), '每30秒查询AI图片生成任务结果，处理待处理和处理中状态的记录');

-- ========================================
-- 重要说明：
-- 1. 执行上述SQL后，需要重启服务才能让定时任务生效
-- 2. 或者通过若依管理后台操作：
--    a. 先将任务状态改为"暂停"
--    b. 再将任务状态改为"正常"
--    c. 这样会触发任务注册到Quartz调度器
-- 3. 任务调用目标：aiImageGenerateTask.queryGenerateResult()
--    对应Bean名称：aiImageGenerateTask（由@Component("aiImageGenerateTask")定义）
-- ========================================

-- 方式二（可选）：插入暂停状态的任务（可通过管理界面启用）
-- INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) 
-- VALUES (@max_job_id + 1, 'AI图片生成结果查询', 'DEFAULT', 'aiImageGenerateTask.queryGenerateResult()', '0/30 * * * * ?', '2', '1', '1', 'admin', NOW(), '每30秒查询AI图片生成任务结果，处理待处理和处理中状态的记录');
-- 然后在管理后台"定时任务"菜单中启用该任务
