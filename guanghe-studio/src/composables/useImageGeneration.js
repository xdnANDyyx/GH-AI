/**
 * AI 图片生成 Composable
 * 封装 上传→生成→轮询→展示 的完整流程
 */
import { ref, computed } from 'vue'
import {
  uploadFile,
  generateOmniImage,
  queryGenerateResult,
  createSession,
  getSessionTypes,
  getSessionTypePromptInfo,
  getPixelConfigs,
  getImageOptions,
  getDeductTypes,
  testAiModel,
  listPromptLibrary
} from '@/api/customer'

function getPollingStatusText(seconds) {
  if (seconds <= 4) return `正在初始化模型与预检环境... (${seconds}s)`
  if (seconds <= 10) return `正在分析参考图与输入图片特征... (${seconds}s)`
  if (seconds <= 20) return `正在规划画布主体布局与透视投影... (${seconds}s)`
  if (seconds <= 35) return `正在生成初始轮廓与基础低频结构... (${seconds}s)`
  if (seconds <= 55) return `多步扩散去噪中，正在精细化光影质感... (${seconds}s)`
  if (seconds <= 75) return `正在对产品边缘与接缝进行平滑融合... (${seconds}s)`
  if (seconds <= 95) return `正在针对皮肤与人物细节进行二次超分微调... (${seconds}s)`
  if (seconds <= 115) return `正在校正画面全局色彩平衡与对比度... (${seconds}s)`
  if (seconds <= 145) return `正在进行无损高动态重构与降噪后处理... (${seconds}s)`
  return `正在进行最终质量安全审计与图样生成检查... (${seconds}s)`
}

const FEATURE_TO_SCOPE = {
  white_bg: 'white_bg',
  background: 'change_bg',
  retouch: 'retouch',
  ai_model: 'ai_model',
  main_image: 'main_image',
  detail_img: 'detail',
  size_mark: 'dimension',
  banner: 'banner',
  batch: 'batch',
  dialog_edit: 'dialog_edit'
}

const _promptCache = {}

async function loadSystemPrompts(featureName) {
  const scope = FEATURE_TO_SCOPE[featureName] || featureName
  if (_promptCache[scope]) return _promptCache[scope]
  const result = { positive: '', negative: '' }
  try {
    const [posRes, negRes] = await Promise.all([
      listPromptLibrary('function', scope),
      listPromptLibrary('negative', scope)
    ])
    const posList = posRes.data || posRes || []
    const negList = negRes.data || negRes || []
    if (Array.isArray(posList) && posList.length) result.positive = posList[0].promptText || ''
    if (Array.isArray(negList) && negList.length) result.negative = negList[0].promptText || ''
  } catch { }
  _promptCache[scope] = result
  return result
}

export function useImageGeneration(sessionType) {
  // 状态
  const generating = ref(false)
  const uploading = ref(false)
  const polling = ref(false)
  const currentRecordId = ref(null)
  const currentSessionId = ref(null)
  const resultImages = ref([])
  const error = ref('')

  // 进度
  const progress = ref(0)
  const statusText = ref('')

  // 配置数据
  const sessionTypes = ref([])
  const promptInfo = ref(null)
  const pixelConfigs = ref([])
  const options = ref(null)
  const deductTypes = ref([])

  /** 加载会话类型配置 */
  async function loadSessionTypes() {
    try {
      const res = await getSessionTypes()
      sessionTypes.value = res.data || []
    } catch (e) {
      console.warn('加载会话类型失败:', e)
    }
  }

  /** 加载提示词信息 */
  async function loadPromptInfo() {
    if (!sessionType) return
    try {
      const res = await getSessionTypePromptInfo(sessionType)
      promptInfo.value = res.data || null
    } catch (e) {
      console.warn('加载提示词信息失败:', e)
    }
  }

  /** 加载像素配置 */
  async function loadPixelConfigs() {
    try {
      const res = await getPixelConfigs()
      pixelConfigs.value = res.data || []
    } catch (e) {
      console.warn('加载像素配置失败:', e)
    }
  }

  /** 加载选项配置 */
  async function loadOptions(optionKey) {
    try {
      const res = await getImageOptions(optionKey)
      options.value = res.data || null
    } catch (e) {
      console.warn('加载选项配置失败:', e)
    }
  }

  /** 加载扣分类型 */
  async function loadDeductTypes() {
    try {
      const res = await getDeductTypes()
      deductTypes.value = res.data || []
    } catch (e) {
      console.warn('加载扣分类型失败:', e)
    }
  }

  /** 上传文件到服务器 */
  async function uploadImage(file) {
    uploading.value = true
    error.value = ''
    try {
      const res = await uploadFile(file, sessionType)
      let url = res.data?.url || res.url || res.fileName
      // 如果返回的是相对路径，补全为完整 URL
      if (url && !url.startsWith('http://') && !url.startsWith('https://') && !url.startsWith('data:image/')) {
        const baseUrl = window.location.origin + import.meta.env.BASE_URL
        url = baseUrl.replace(/\/$/, '') + (url.startsWith('/') ? url : '/' + url)
      }
      return url
    } catch (e) {
      error.value = e.message || '图片上传失败'
      throw e
    } finally {
      uploading.value = false
    }
  }

  /** 批量上传文件 */
  async function uploadImages(files) {
    const urls = []
    for (const file of files) {
      if (typeof file === 'string') {
        urls.push(file)
      } else {
        const url = await uploadImage(file)
        if (url) urls.push(url)
      }
    }
    return urls
  }

  /** 创建/获取会话 */
  async function ensureSession(prompt) {
    if (currentSessionId.value) return currentSessionId.value
    try {
      const res = await createSession(sessionType, prompt)
      currentSessionId.value = res.data || res.msg
      return currentSessionId.value
    } catch (e) {
      console.warn('创建会话失败:', e)
      return null
    }
  }

  /** 发起生成请求 */
  async function generate(params) {
    generating.value = true
    error.value = ''
    resultImages.value = []
    progress.value = 0
    statusText.value = '正在提交生成任务...'

    // 开启一个虚拟的进度模拟定时器，即使是同步阻塞生图，也能在等待 HTTP 响应期间给用户展示生图阶段
    let fakeSeconds = 0
    const fakeTimer = setInterval(() => {
      fakeSeconds += 1.5
      progress.value = Math.min(90, Math.round((fakeSeconds / 45) * 100)) // 假设普通同步生图大约 45 秒内
      statusText.value = getPollingStatusText(Math.round(fakeSeconds))
    }, 1500)

    try {
      const res = await generateOmniImage({
        sessionType,
        sessionId: currentSessionId.value,
        n: 1,
        type: '1',
        ...params
      })

      // 只要拿到了响应，就清除虚拟定时器
      clearInterval(fakeTimer)

      const data = res.data || res
      console.log('生成响应数据:', data)
      currentRecordId.value = data.id
      currentSessionId.value = data.sessionId || currentSessionId.value

      // 检查是否有图片返回
      if (data.images && data.images.length > 0) {
        console.log('直接返回的图片数量:', data.images.length)
        console.log('图片数据示例:', data.images[0])
        resultImages.value = data.images
        statusText.value = '生成完成'
        progress.value = 100
        return data
      }

      // 开始轮询
      return await pollResult(data.id, fakeSeconds)
    } catch (e) {
      clearInterval(fakeTimer)
      // 按实际情况提示，不再把超时/网络错误一律误报成"API Key 无效"
      if (e.code === 'ECONNABORTED' || /timeout/i.test(e.message || '')) {
        error.value = '生成超时，AI 处理较慢，请稍后重试'
      } else {
        error.value = e.message || '生成失败，请稍后重试'
      }
      statusText.value = '生成失败'
      throw e
    } finally {
      generating.value = false
    }
  }

  /** 轮询生成结果 */
  async function pollResult(recordId, initialSeconds = 0) {
    polling.value = true
    const maxAttempts = 120 // 最多轮询 2 分钟（每秒一次）
    let attempts = Math.round(initialSeconds / 1.5)

    return new Promise((resolve, reject) => {
      const poll = async () => {
        // 如果已被 reset（如用户点击清空对话），停止轮询
        if (!polling.value) {
          reject(new Error('已取消'))
          return
        }

        if (attempts >= maxAttempts) {
          polling.value = false
          statusText.value = '生成超时，请稍后在历史记录中查看'
          reject(new Error('生成超时'))
          return
        }

        attempts++
        const seconds = Math.round(attempts * 1.5)
        progress.value = Math.min(90, Math.round((seconds / 180) * 100))
        statusText.value = getPollingStatusText(seconds)

        try {
          const res = await queryGenerateResult(recordId)
          const data = res.data || res

          if (data.status === '2') {
            // 成功
            resultImages.value = data.images || []
            progress.value = 100
            statusText.value = '生成完成'
            polling.value = false
            resolve(data)
            return
          }

          if (data.status === '3') {
            // 失败
            polling.value = false
            statusText.value = '生成失败'
            error.value = data.errorMsg || 'AI 生成失败'
            reject(new Error(data.errorMsg || '生成失败'))
            return
          }

          // 还在处理中，继续轮询（如果已被 reset 则不再调度）
          if (polling.value) {
            setTimeout(poll, 1500)
          }
        } catch (e) {
          polling.value = false
          error.value = e.message || '查询结果失败'
          reject(e)
        }
      }

      poll()
    })
  }

  /** 测试 AI 模型是否可用 */
  async function checkAiModel() {
    try {
      statusText.value = '正在检测 AI 服务...'
      const res = await testAiModel()
      const data = res.data || res
      
      if (data.available) {
        console.log('✅ AI 模型检测通过:', data.message)
        return true
      } else {
        // 统一错误提示：Key 无效
        error.value = 'API Key 无效，请更换'
        statusText.value = '检测失败'
        console.error('❌ AI 模型检测失败:', data.message)
        return false
      }
    } catch (e) {
      // 统一错误提示：Key 无效
      error.value = 'API Key 无效，请更换'
      statusText.value = '检测失败'
      console.error('❌ AI 模型检测异常:', e.message)
      return false
    }
  }

  /** 完整的生成流程：上传 → 会话 → 生成 → 展示（不做预检，直接生图） */
  async function fullGenerate(productFiles, prompt, extraParams = {}) {
    // 提取积分/历史相关参数，不传给 generate
    const { consumePoints = 2, featureName = sessionType || 'ai_image', title, ...generateParams } = extraParams
    const maxRetries = 2
    let lastError = null

    // 判断是否是多轮对话（即对话框/画布生成的上一张图片结果已存在）
    const isMultiTurn = resultImages.value && resultImages.value.length > 0
    const finalProductFiles = isMultiTurn 
      ? [resultImages.value[resultImages.value.length - 1].url || resultImages.value[resultImages.value.length - 1]] 
      : productFiles

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        // 0. 加载系统级正向/负向约束提示词并拼接（从第二轮多轮对话开始不需要带默认的正向和负向约束）
        let finalPrompt = prompt
        if (!isMultiTurn) {
          try {
            const sys = await loadSystemPrompts(featureName)
            if (sys.positive) finalPrompt = `${sys.positive}\n\n用户需求：${prompt}`
            if (sys.negative) finalPrompt += `\n\n负向约束：${sys.negative}`
          } catch { }
        }

        // 1. 上传产品图
        statusText.value = attempt === 0 ? '正在上传图片...' : `正在重试(${attempt}/${maxRetries})...`
        const productUrls = await uploadImages(finalProductFiles)

        // 2. 确保有会话
        await ensureSession(finalPrompt)

        // 3. 发起生成
        statusText.value = attempt === 0 ? '正在生成...' : `正在重试生成(${attempt}/${maxRetries})...`
        const result = await generate({
          prompt: finalPrompt,
          productImages: productUrls,
          ...generateParams
        })

        // 4. 生成成功后：扣减积分 + 标记步骤完成 + 保存历史记录
        if (resultImages.value.length > 0) {
          // 扣减本地积分（立即生效），延迟从后端刷新避免竞态覆盖
          try {
            const { useUserStore } = await import('@/store')
            const userStore = useUserStore()
            userStore.deductPoints(consumePoints)
            setTimeout(() => userStore.fetchPoints?.(), 3000)
          } catch (e) { console.warn('积分扣减失败:', e) }

          // 标记工作流步骤完成
          try {
            const { useWorkflowProgress } = await import('@/composables/useWorkflowProgress')
            useWorkflowProgress().markStepCompleted(featureName)
          } catch { }

          // 保存历史记录
          try {
            const { useHistory } = await import('@/composables/useHistory')
            const history = useHistory()
            history.saveHistoryRecord({
              featureType: featureName,
              title: title || `${featureName} 生成`,
              description: (prompt || '').slice(0, 80),
              status: 'success',
              consumePoints,
              resultImages: resultImages.value.map(img => img.url || img),
              prompt,
            })
          } catch (e) { console.warn('保存历史失败:', e) }
        }

        return result
      } catch (e) {
        lastError = e
        console.error(`[AI生成] 第${attempt + 1}次尝试失败:`, {
          feature: featureName,
          prompt: (prompt || '').slice(0, 100),
          error: e.message || String(e),
          time: new Date().toISOString()
        })

        // 保存失败记录到历史（仅最后一次失败时保存，用户主动取消除外）
        if (attempt === maxRetries) {
          // 用户主动取消时不显示错误、不保存失败历史
          if (e.message?.includes('已取消')) {
            throw e
          }
          error.value = e.message || '操作失败'
          try {
            const { useHistory } = await import('@/composables/useHistory')
            const history = useHistory()
            history.saveHistoryRecord({
              featureType: featureName,
              title: title || `${featureName} 生成`,
              description: (prompt || '').slice(0, 80),
              status: 'failed',
              consumePoints: 0,
              resultImages: [],
              prompt,
            })
          } catch (e2) { console.warn('保存失败历史:', e2) }
        }

        // 判断是否可重试（非积分不足、非参数错误、非用户主动取消等）
        const isRetryable = !(
          e.message?.includes('积分不足') ||
          e.message?.includes('参数') ||
          e.message?.includes('invalid') ||
          e.message?.includes('余额') ||
          e.message?.includes('已取消')
        )

        if (attempt < maxRetries && isRetryable) {
          statusText.value = `第${attempt + 1}次失败，${2}秒后重试...`
          error.value = ''
          await new Promise(resolve => setTimeout(resolve, 2000))
          continue
        }

        throw e
      }
    }

    throw lastError
  }

  /** 重置状态 */
  function reset() {
    generating.value = false
    uploading.value = false
    polling.value = false
    currentRecordId.value = null
    resultImages.value = []
    error.value = ''
    progress.value = 0
    statusText.value = ''
  }

  /** 检查积分是否足够（供各工作台调用） */
  async function checkPoints(consumePoints) {
    try {
      const { useUserStore } = await import('@/store')
      const userStore = useUserStore()
      const points = userStore.userInfo.points || 0
      return points >= consumePoints
    } catch {
      return true
    }
  }

  /** 记录生成结果（供 mock 工作台使用）：扣减积分 + 保存历史 */
  async function recordGeneration({ featureName, consumePoints = 2, status = 'success', resultImages = [], prompt = '', title = '' }) {
    if (status === 'success' && resultImages.length > 0) {
      try {
        const { useUserStore } = await import('@/store')
        const userStore = useUserStore()
        userStore.deductPoints(consumePoints)
        setTimeout(() => userStore.fetchPoints?.(), 3000)
      } catch (e) { console.warn('积分扣减失败:', e) }
      try {
        const { useWorkflowProgress } = await import('@/composables/useWorkflowProgress')
        useWorkflowProgress().markStepCompleted(featureName)
      } catch { }
    }
    try {
      const { useHistory } = await import('@/composables/useHistory')
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

  return {
    // 状态
    generating,
    uploading,
    polling,
    currentRecordId,
    currentSessionId,
    resultImages,
    error,
    progress,
    statusText,

    // 配置数据
    sessionTypes,
    promptInfo,
    pixelConfigs,
    options,
    deductTypes,

    // 方法
    loadSessionTypes,
    loadPromptInfo,
    loadPixelConfigs,
    loadOptions,
    loadDeductTypes,
    uploadImage,
    uploadImages,
    ensureSession,
    generate,
    pollResult,
    checkAiModel,
    fullGenerate,
    reset,
    checkPoints,
    recordGeneration,
  }
}
