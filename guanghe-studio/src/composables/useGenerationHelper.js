/**
 * useGenerationHelper.js
 * 轻 mock 工作台使用的轻量积分/历史辅助函数
 */
import { useHistory } from './useHistory'

/** 检查积分是否足够 */
export async function checkPoints(consumePoints) {
  try {
    const { useUserStore } = await import('@/store')
    const userStore = useUserStore()
    const points = userStore.userInfo.points || 0
    return points >= consumePoints
  } catch {
    return true
  }
}

/** 记录生成结果：扣减积分 + 保存历史 */
export async function recordGeneration({ featureName, consumePoints = 2, status = 'success', resultImages = [], prompt = '', title = '' }) {
  if (status === 'success') {
    try {
      const { useUserStore } = await import('@/store')
      const userStore = useUserStore()
      userStore.deductPoints(consumePoints)
      userStore.fetchPoints?.()
    } catch (e) { console.warn('积分扣减失败:', e) }
  }
  try {
    const history = useHistory()
    history.saveHistoryRecord({
      featureType: featureName,
      title: title || `${featureName} 生成`,
      description: (prompt || '').slice(0, 80),
      status,
      consumePoints: status === 'success' ? consumePoints : 0,
      resultImages,
      prompt,
    })
  } catch (e) { console.warn('保存历史失败:', e) }
}