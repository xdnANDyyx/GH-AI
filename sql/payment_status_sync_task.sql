-- 支付状态同步定时任务
-- 每5分钟执行一次，查询5分钟前状态为"发起支付"的记录并同步状态

-- 方式一：直接插入任务（需要重启服务才能生效）
-- 查询当前最大job_id
SELECT IFNULL(MAX(job_id), 0) INTO @max_job_id FROM sys_job;

-- 插入定时任务配置（状态为正常，需要重启服务）
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) 
VALUES (@max_job_id + 1, '支付状态同步', 'DEFAULT', 'paymentStatusSyncTask.syncPaymentStatus()', '0 */5 * * * ?', '2', '1', '0', 'admin', NOW(), '每5分钟查询5分钟前状态为发起支付的记录，调用支付渠道查询结果并更新状态：支付成功/支付失败/取消支付');

-- ========================================
-- 重要说明：
-- 1. 执行上述SQL后，需要重启服务才能让定时任务生效
-- 2. 或者通过若依管理后台操作：
--    a. 先将任务状态改为"暂停"
--    b. 再将任务状态改为"正常"
--    c. 这样会触发任务注册到Quartz调度器
-- 3. 任务调用目标：paymentStatusSyncTask.syncPaymentStatus()
--    对应Bean名称：paymentStatusSyncTask（由@Component("paymentStatusSyncTask")定义）
-- 4. cron表达式：0 */5 * * * ? 表示每5分钟执行一次
-- 5. misfire_policy：2表示立即执行（错过执行时间后立即执行）
-- 6. concurrent：1表示允许并发执行
-- ========================================

-- 方式二（可选）：插入暂停状态的任务（可通过管理界面启用）
-- INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) 
-- VALUES (@max_job_id + 1, '支付状态同步', 'DEFAULT', 'paymentStatusSyncTask.syncPaymentStatus()', '0 */5 * * * ?', '2', '1', '1', 'admin', NOW(), '每5分钟查询5分钟前状态为发起支付的记录，调用支付渠道查询结果并更新状态');
-- 然后在管理后台"定时任务"菜单中启用该任务
