import { ref } from 'vue'
import { getFeatureToggleList, getPublicAllToggles } from '@/api/operation'

const featureToggleMap = ref({})
let loaded = false
let loadingPromise = null

const pathToToggleKey = {
  '/whiteBg': 'white_bg',
  '/whiteToBg': 'white_to_bg',
  '/refine': 'refine',
  '/aiModel': 'ai_model',
  '/mainImage': 'main_image',
  '/detailImg': 'detail_img',
  '/size': 'size_mark',
  '/banner': 'banner',
  '/batchProcess': 'batch_process'
}

export function useFeatureToggles() {
  async function loadFeatureToggles() {
    if (loaded) return featureToggleMap.value
    if (loadingPromise) return loadingPromise
    loadingPromise = (async () => {
      try {
        // 先尝试用list接口（管理员可用），失败则用公开接口获取全部开关（含关闭的）
        let list = []
        try {
          const res = await getFeatureToggleList()
          if (res.code === 200) {
            list = res.rows || res.data || []
          }
        } catch {
          // 非管理员，使用公开接口获取全部开关
          const res = await getPublicAllToggles()
          if (res.code === 200) {
            list = res.data || []
          }
        }
        const map = {}
        list.forEach(item => {
          const key = item.toggleKey.replace(/^feature\./, '')
          map[key] = item.enabled || '1'
        })
        featureToggleMap.value = map
        loaded = true
      } catch {
        // 加载失败，默认全部可见
      } finally {
        loadingPromise = null
      }
      return featureToggleMap.value
    })()
    return loadingPromise
  }

  function isFeatureEnabled(toggleKey) {
    const enabled = featureToggleMap.value[toggleKey]
    if (enabled === '0') return false
    return true
  }

  function isPathEnabled(path) {
    const toggleKey = pathToToggleKey[path]
    if (!toggleKey) return true
    return isFeatureEnabled(toggleKey)
  }

  return {
    featureToggleMap,
    loadFeatureToggles,
    isFeatureEnabled,
    isPathEnabled
  }
}
