/**
 * 全局图片状态管理
 * 支持跨工作台模块的图片传递
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useImageStore = defineStore('image', () => {
  // 临时存储待传递的图片
  const pendingImages = ref([])

  // 存储从其他模块接收的图片
  const receivedImages = ref([])

  // 来源模块标识
  const sourceFeature = ref('')

  /**
   * 设置待传递的图片（用于跨模块跳转）
   * @param {Array} images - 图片数组
   * @param {String} from - 来源模块
   */
  const setPendingImages = (images, from = '') => {
    pendingImages.value = images.map(img => ({
      url: img.url || img,
      name: img.name || `图片_${Date.now()}`,
      feature: from
    }))
    sourceFeature.value = from
  }

  /**
   * 添加待传递图片
   * @param {Object} img - 图片对象
   */
  const addPendingImage = (img) => {
    pendingImages.value.push({
      url: img.url || img,
      name: img.name || `图片_${Date.now()}`,
      feature: sourceFeature.value
    })
  }

  /**
   * 获取并清空待传递图片
   * @returns {Array}
   */
  const consumePendingImages = () => {
    const images = [...pendingImages.value]
    pendingImages.value = []
    return images
  }

  /**
   * 设置接收到的图片
   * @param {Array} images - 图片数组
   */
  const setReceivedImages = (images) => {
    receivedImages.value = images
  }

  /**
   * 清空所有图片
   */
  const clearAll = () => {
    pendingImages.value = []
    receivedImages.value = []
    sourceFeature.value = ''
  }

  return {
    pendingImages,
    receivedImages,
    sourceFeature,
    setPendingImages,
    addPendingImage,
    consumePendingImages,
    setReceivedImages,
    clearAll
  }
})
