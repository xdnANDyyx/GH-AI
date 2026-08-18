import { ref } from 'vue'
import { getFeatureToggleList, getPublicAllToggles } from '@/api/operation'

const featureToggleMap = ref({})
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
    if (loadingPromise) return loadingPromise
    loadingPromise = (async () => {
      try {
        let list = []
        try {
          const res = await getFeatureToggleList()
          if (res.code === 200) {
            list = res.rows || res.data || []
          }
        } catch {
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
      } catch {
        // 加载失败，fail-secure：标记全部为已禁用
        featureToggleMap.value = {}
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
