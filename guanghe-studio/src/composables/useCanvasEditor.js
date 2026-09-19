/**
 * Centralized Canvas Editor Composable
 * 为所有工作台模块提供统一且完整的画布编辑能力。
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { useImageHandoffStore } from '@/store'
import {
  extendImage,
  generateMultiAngle,
  editImageText,
  partialRedraw,
  downloadLayers
} from '@/api/canvasEditor'

export const useCanvasEditor = (resultImagesRef, featureName = 'canvas') => {
  const router = useRouter()
  const handoffStore = useImageHandoffStore()

  // 包装对外部 ref 的访问，确保所有层级操作和图片添加/删除完全响应式，且两边同步
  const images = computed({
    get: () => resultImagesRef.value || [],
    set: (val) => {
      resultImagesRef.value = val
    }
  })

  // ========== 图片管理 ==========

  // 删除图片
  const handleDelete = (index) => {
    if (index >= 0 && index < images.value.length) {
      const newImages = [...images.value]
      newImages.splice(index, 1)
      images.value = newImages
      ElMessage.success('图片已删除')
    }
  }

  // 向上移动一层（往前交换）
  const handleMoveUp = (index) => {
    if (index > 0 && index < images.value.length) {
      const newImages = [...images.value]
      const temp = newImages[index]
      newImages[index] = newImages[index - 1]
      newImages[index - 1] = temp
      images.value = newImages
      ElMessage.success('已向上移动一层')
    }
  }

  // 向下移动一层（往后交换）
  const handleMoveDown = (index) => {
    if (index >= 0 && index < images.value.length - 1) {
      const newImages = [...images.value]
      const temp = newImages[index]
      newImages[index] = newImages[index + 1]
      newImages[index + 1] = temp
      images.value = newImages
      ElMessage.success('已向下移动一层')
    }
  }

  // 置顶（移动到最上层，即数组末尾）
  const handleBringToFront = (index) => {
    if (index >= 0 && index < images.value.length - 1) {
      const newImages = [...images.value]
      const img = newImages.splice(index, 1)[0]
      newImages.push(img)
      images.value = newImages
      ElMessage.success('已置顶')
    }
  }

  // 置底（移动到最下层，即数组开头）
  const handleSendToBack = (index) => {
    if (index > 0 && index < images.value.length) {
      const newImages = [...images.value]
      const img = newImages.splice(index, 1)[0]
      newImages.unshift(img)
      images.value = newImages
      ElMessage.success('已置底')
    }
  }

  // 下载图片
  const handleDownload = (img) => {
    if (!img) return
    const url = img.url || img
    const link = document.createElement('a')
    link.href = url
    link.download = img.name || `image_${Date.now()}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('开始下载图片')
  }

  // ========== 图片生成/编辑功能 ==========

  /**
   * 智能扩图（支持交互式拖拽扩展信息）
   * @param {Object} params - { image, ratio, width, height, extend }
   */
  const handleExtend = async (params) => {
    const { image, ratio, width, height, extend } = params
    const imageUrl = image.url || image
    const loading = ElLoading.service({ text: '正在进行智能扩图，请稍候...', background: 'rgba(0, 0, 0, 0.7)' })
    
    try {
      const res = await extendImage({ imageUrl, ratio, width, height, extend })
      if (res.code === 200 && res.data?.url) {
        images.value = [...images.value, {
          url: res.data.url,
          name: `扩图_${ratio}_${Date.now()}`
        }]
        ElMessage.success('扩图生成成功')
      } else {
        throw new Error(res.msg || '后台未返回图片URL')
      }
    } catch (err) {
      console.error('扩图失败:', err)
      ElMessage.error(`扩图失败: ${err.message || '网络繁忙，请稍后重试'}`)
    } finally {
      loading.close()
    }
  }

  /**
   * 多角度生成（支持自定义摄像机方位，结果直接铺在画布上）
   * @param {Object} params - { image, count, type, horizontal, vertical, isCustomAngle }
   * @param {Function} onResults - 可选回调，接收生成结果URL数组，用于直接铺在画布上
   */
  const handleMultiAngle = async (params, onResults) => {
    const { image, count, type, horizontal, vertical, isCustomAngle } = params
    const imageUrl = image.url || image
    const loadingText = isCustomAngle
      ? `正在智能生成指定方位 (水平:${horizontal}°, 垂直:${vertical}°) 的商品图...`
      : `正在智能生成 ${count} 个角度的商品图...`
    const loading = ElLoading.service({ text: loadingText, background: 'rgba(0, 0, 0, 0.7)' })

    try {
      const res = await generateMultiAngle({
        imageUrl,
        count,
        type,
        horizontal,
        vertical,
        isCustomAngle
      })
      const genImages = res.data?.images || res.images || []
      if (genImages.length > 0) {
        const newImages = genImages.map((img, i) => ({
          url: img.url,
          name: isCustomAngle
            ? `多角度_方位_${horizontal}_${vertical}_${Date.now()}`
            : `多角度_${img.angle || (i + 1)}_${Date.now()}`
        }))
        images.value = [...images.value, ...newImages]
        // 如果提供了回调，将结果直接铺到画布上
        if (onResults && typeof onResults === 'function') {
          await onResults(newImages)
        }
        ElMessage.success(`多角度图片生成成功，共 ${newImages.length} 张，已铺展到画布上`)
      } else {
        throw new Error('未返回多角度生成结果')
      }
    } catch (err) {
      console.error('多角度生成失败:', err)
      ElMessage.error(`多角度生成失败: ${err.message || '网络繁忙，请稍后重试'}`)
    } finally {
      loading.close()
    }
  }

  /**
   * 改文字
   * @param {Object} params - { image, originalText, newText, font }
   */
  const handleEditText = async (params) => {
    const { image, originalText, newText, font } = params
    const imageUrl = image.url || image
    const loading = ElLoading.service({ text: '正在智能修改图片文字...', background: 'rgba(0, 0, 0, 0.7)' })

    try {
      const res = await editImageText({ imageUrl, originalText, newText, font })
      if (res.code === 200 && res.data?.url) {
        images.value = [...images.value, {
          url: res.data.url,
          name: `改文字_${Date.now()}`
        }]
        ElMessage.success('文字修改成功')
      } else {
        throw new Error(res.msg || '后台未返回图片URL')
      }
    } catch (err) {
      console.error('修改文字失败:', err)
      ElMessage.error(`修改文字失败: ${err.message || '网络繁忙，请稍后重试'}`)
    } finally {
      loading.close()
    }
  }

  /**
   * 局部重绘
   * @param {Object} params - { image, description, mask }
   */
  const handlePartialRedraw = async (params) => {
    const { image, description, mask } = params
    const imageUrl = image.url || image
    const loading = ElLoading.service({ text: '正在进行局部区域重绘...', background: 'rgba(0, 0, 0, 0.7)' })

    try {
      const res = await partialRedraw({ imageUrl, description, mask })
      if (res.code === 200 && res.data?.url) {
        images.value = [...images.value, {
          url: res.data.url,
          name: `局部重绘_${Date.now()}`
        }]
        ElMessage.success('局部重绘成功')
      } else {
        throw new Error(res.msg || '后台未返回图片URL')
      }
    } catch (err) {
      console.error('局部重绘失败:', err)
      ElMessage.error(`局部重绘失败: ${err.message || '网络繁忙，请稍后重试'}`)
    } finally {
      loading.close()
    }
  }

  /**
   * 图层炸开并下载分离后的图层包
   * @param {Object} params - { image, layers, format }
   */
  const handleExplodeLayers = async (params) => {
    const { image, layers, format } = params
    const imageUrl = image.url || image
    const layerNames = layers.map(l => l.name)
    const loading = ElLoading.service({ text: '正在对图片进行智能图层分离，打包并下载中...', background: 'rgba(0, 0, 0, 0.7)' })

    try {
      const res = await downloadLayers({ imageUrl, layers: layerNames, format })
      let downloadUrl = ''
      if (res.code === 200 && res.data?.downloadUrl) {
        downloadUrl = res.data.downloadUrl
      } else if (res.downloadUrl) {
        downloadUrl = res.downloadUrl
      } else if (res.data) {
        downloadUrl = res.data
      }

      if (downloadUrl) {
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = `layers_${Date.now()}.${format === 'psd' ? 'psd' : 'zip'}`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        ElMessage.success('图层包打包下载成功')
      } else {
        throw new Error('未获取到有效的打包下载链接')
      }
    } catch (err) {
      console.error('图层打包下载失败:', err)
      ElMessage.error(`图层分离下载失败: ${err.message || '网络繁忙，请稍后重试'}`)
    } finally {
      loading.close()
    }
  }

  // ========== 跨模块跳转 / 图片接力 ==========

  /**
   * 发送到产品精修
   * @param {Object} img - 图片对象
   */
  const handleSendToRetouch = (img) => {
    const url = img.url || img
    if (!url) return
    handoffStore.setImage(url, { from: featureName, to: 'retouch' })
    ElMessage.success('已放入产品精修，正在为您跳转...')
    router.push('/refine')
  }

  /**
   * 发送到白底图
   * @param {Object} img - 图片对象
   */
  const handleSendToWhiteBg = (img) => {
    const url = img.url || img
    if (!url) return
    handoffStore.setImage(url, { from: featureName, to: 'whiteBg' })
    ElMessage.success('已放入白底图，正在为您跳转...')
    router.push('/whiteBg')
  }

  return {
    handleDelete,
    handleMoveUp,
    handleMoveDown,
    handleBringToFront,
    handleSendToBack,
    handleDownload,
    handleExtend,
    handleMultiAngle,
    handleEditText,
    handlePartialRedraw,
    handleExplodeLayers,
    handleSendToRetouch,
    handleSendToWhiteBg
  }
}
