/**
 * useCanvasInteractions.js
 * 工作台画布通用交互 composable：
 *  - 缩放控制（50%-400%，滚轮直接缩放，双击还原100%）
 *  - 画布拖拽平移（查看细节）
 *  - 右键菜单（标注尺寸、添加备注、测量距离、清空标点）
 *  - 标点/备注/测量管理
 *  - 拖拽上传视觉反馈（蓝色虚线框）
 *  - 全屏切换
 *  - 键盘快捷键（Ctrl+= / Ctrl+- / Ctrl+0 / F 全屏）
 *  - 图片导出辅助
 */
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'

export function useCanvasInteractions(options = {}) {
  const {
    canvasSelector = '.canvas-box',
    getImage = null,
    getImageUrl = null,
    defaultName = 'canvas',
    minZoom = 50,
    maxZoom = 400,
    zoomStep = 10,
  } = options

  const resolveImage = typeof getImage === 'function'
    ? getImage
    : (typeof getImageUrl === 'function' ? getImageUrl : null)

  // ---- 缩放状态 ----
  const zoomLevel = ref(100)
  const isFullscreen = ref(false)
  const isExporting = ref(false)
  const showZoomBar = ref(true)
  const showToast = ref('')

  const zoomPercent = computed(() => `${zoomLevel.value}%`)
  const isZoomMax = computed(() => zoomLevel.value >= maxZoom)
  const isZoomMin = computed(() => zoomLevel.value <= minZoom)

  function zoomIn() {
    zoomLevel.value = Math.min(maxZoom, zoomLevel.value + zoomStep)
    showToast.value = `画布缩放 ${zoomPercent.value}`
  }
  function zoomOut() {
    zoomLevel.value = Math.max(minZoom, zoomLevel.value - zoomStep)
    showToast.value = `画布缩放 ${zoomPercent.value}`
  }
  function resetZoom() {
    zoomLevel.value = 100
    panX.value = 0
    panY.value = 0
    showToast.value = '缩放已重置'
  }
  function clearToast() {
    showToast.value = ''
  }

  // ---- 画布平移 ----
  const panX = ref(0)
  const panY = ref(0)
  const isPanning = ref(false)
  let panStartX = 0
  let panStartY = 0
  let panOriginX = 0
  let panOriginY = 0

  function startPan(e) {
    if (e.button !== 0) return
    isPanning.value = true
    panStartX = e.clientX
    panStartY = e.clientY
    panOriginX = panX.value
    panOriginY = panY.value
    document.body.style.cursor = 'grabbing'
    document.body.style.userSelect = 'none'
  }

  function onPanMove(e) {
    if (!isPanning.value) return
    panX.value = panOriginX + (e.clientX - panStartX)
    panY.value = panOriginY + (e.clientY - panStartY)
  }

  function onPanEnd() {
    if (!isPanning.value) return
    isPanning.value = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  const canvasTransform = computed(() =>
    `translate(${panX.value}px, ${panY.value}px) scale(${zoomLevel.value / 100})`
  )

  // ---- 双击还原 ----
  function handleDoubleClick() {
    resetZoom()
  }

  // ---- 自动将缩放变换应用到画布内图片元素 ----
  const IMAGE_SELECTORS = [
    '.upload-preview',
    '.compare-container',
    '.preview-img',
    '.uploaded-img',
    '.result-img',
    '.canvas-result',
    '.result-grid',
    '.canvas-placeholder',
  ]

  watch([zoomLevel, panX, panY], () => {
    nextTick(() => {
      const canvasEl = document.querySelector(canvasSelector)
      if (!canvasEl) return
      const transformStr = `translate(${panX.value}px, ${panY.value}px) scale(${zoomLevel.value / 100})`
      IMAGE_SELECTORS.forEach(sel => {
        const els = canvasEl.querySelectorAll(sel)
        els.forEach(el => {
          if (el.classList.contains('canvas-overlay')) return
          el.style.transform = transformStr
          el.style.transformOrigin = 'center center'
        })
      })
    })
  })

  // ---- 滚轮缩放（无需Ctrl，直接滚轮） ----
  function handleWheel(e) {
    if (e.ctrlKey) { e.preventDefault(); if (e.deltaY < 0) zoomIn(); else zoomOut(); return }
    const canvasEl = e.target.closest?.(canvasSelector)
    if (!canvasEl) return
    e.preventDefault()
    if (e.deltaY < 0) zoomIn(); else zoomOut()
  }

  // ---- 右键菜单 ----
  const contextMenu = reactive({
    visible: false,
    x: 0,
    y: 0,
  })

  function openContextMenu(e) {
    e.preventDefault()
    e.stopPropagation()
    const rect = e.currentTarget.getBoundingClientRect()
    let x = e.clientX - rect.left
    let y = e.clientY - rect.top
    const menuW = 180, menuH = 200
    if (x + menuW > rect.width) x = rect.width - menuW - 8
    if (y + menuH > rect.height) y = rect.height - menuH - 8
    contextMenu.x = Math.max(8, x)
    contextMenu.y = Math.max(8, y)
    contextMenu.visible = true
  }

  function closeContextMenu() {
    contextMenu.visible = false
  }

  function _handleClickOutside(e) {
    if (!contextMenu.visible) return
    const target = e.target
    if (target?.closest?.('.context-menu')) return
    closeContextMenu()
  }

  // ---- 标点管理 ----
  const markers = ref([])
  const measureMode = ref(false)
  const measureType = ref('') // 'measure' 测量距离 | 'dimension' 标注尺寸
  let measureStartPoint = null

  // 实时测量预览（鼠标移动时）
  const liveMeasure = ref(null)

  // 持久尺寸标注列表
  const dimensions = ref([])

  function addMarker(x, y, type = 'point') {
    markers.value.push({
      id: Date.now() + Math.random(),
      x,
      y,
      type,
      label: type === 'note' ? '备注' : '',
    })
  }

  function addNote(x, y, text = '双击编辑备注') {
    markers.value.push({
      id: Date.now() + Math.random(),
      x,
      y,
      type: 'note',
      label: text,
      editable: true,
    })
  }

  function editNote(marker) {
    const text = window.prompt('编辑备注内容', marker.label || '')
    if (text !== null && text.trim() !== '') {
      marker.label = text.trim()
    }
  }

  function removeMarker(id) {
    markers.value = markers.value.filter(m => m.id !== id)
  }

  function removeDimension(id) {
    dimensions.value = dimensions.value.filter(d => d.id !== id)
  }

  function clearMarkers() {
    markers.value = []
    dimensions.value = []
    measureMode.value = false
    measureStartPoint = null
    liveMeasure.value = null
    closeContextMenu()
  }

  // ---- 测量距离 / 标注尺寸 ----
  const measureResult = ref(null)

  // 像素 → 物理距离（按 96 DPI 换算为毫米）
  function pxToMm(px) {
    return (px / 96 * 25.4).toFixed(1)
  }

  function startMeasure() {
    measureMode.value = true
    measureType.value = 'measure'
    measureStartPoint = null
    measureResult.value = null
    liveMeasure.value = null
    closeContextMenu()
    showToast.value = '请点击两个点测量距离（移动鼠标实时预览）'
  }

  function startDimension() {
    closeContextMenu()
    measureMode.value = true
    measureType.value = 'dimension'
    measureStartPoint = null
    measureResult.value = null
    liveMeasure.value = null
    showToast.value = '请点击两个点标注尺寸（移动鼠标实时预览）'
  }

  function cancelMeasure() {
    measureMode.value = false
    measureType.value = ''
    measureStartPoint = null
    measureResult.value = null
    liveMeasure.value = null
    showToast.value = ''
  }

  function handleCanvasClick(e) {
    if (!measureMode.value) return
    const rect = e.currentTarget.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    if (!measureStartPoint) {
      measureStartPoint = { x, y }
      markers.value.push({ id: Date.now(), x, y, type: 'measure-start' })
    } else {
      const dx = x - measureStartPoint.x
      const dy = y - measureStartPoint.y
      const distance = Math.round(Math.sqrt(dx * dx + dy * dy))
      const physMm = pxToMm(distance)
      const label = `${distance}px ≈ ${physMm}mm`
      measureResult.value = { distance, physMm, startX: measureStartPoint.x, startY: measureStartPoint.y, endX: x, endY: y, label }

      if (measureType.value === 'dimension') {
        // 标注尺寸：保留为持久标注
        dimensions.value.push({ id: Date.now(), distance, physMm, startX: measureStartPoint.x, startY: measureStartPoint.y, endX: x, endY: y, label })
      } else {
        // 测量距离：在终点显示距离标签
        markers.value.push({ id: Date.now(), x, y, type: 'measure-end', label })
      }

      measureMode.value = false
      measureType.value = ''
      measureStartPoint = null
      liveMeasure.value = null
      showToast.value = `两点距离: ${label}`
    }
  }

  // 实时测量：鼠标移动时预览距离与连线
  function handleCanvasMove(e) {
    if (!measureMode.value || !measureStartPoint) return
    const rect = e.currentTarget.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    const dx = x - measureStartPoint.x
    const dy = y - measureStartPoint.y
    const distance = Math.round(Math.sqrt(dx * dx + dy * dy))
    const physMm = pxToMm(distance)
    liveMeasure.value = { distance, physMm, startX: measureStartPoint.x, startY: measureStartPoint.y, endX: x, endY: y, label: `${distance}px ≈ ${physMm}mm` }
  }

  // ---- 拖拽上传视觉反馈 ----
  const isDragOver = ref(false)
  let dragCounter = 0

  function handleDragEnter() {
    dragCounter++
    isDragOver.value = true
  }

  function handleDragLeave() {
    dragCounter--
    if (dragCounter <= 0) {
      isDragOver.value = false
      dragCounter = 0
    }
  }

  function handleDragReset() {
    isDragOver.value = false
    dragCounter = 0
  }

  // ---- 全屏 ----
  function toggleFullscreen() {
    const el = document.querySelector(canvasSelector)
    if (!el) return
    if (!document.fullscreenElement) {
      el.requestFullscreen?.()
        .then(() => { isFullscreen.value = true })
        .catch(() => {})
    } else {
      document.exitFullscreen?.()
        .then(() => { isFullscreen.value = false })
        .catch(() => {})
    }
  }

  function handleFullscreenChange() {
    isFullscreen.value = !!document.fullscreenElement
  }

  // ---- 导出 ----
  async function exportCanvas(filename = defaultName) {
    const url = resolveImage ? resolveImage() : ''
    if (!url) {
      showToast.value = '暂无可导出的图片'
      return
    }
    isExporting.value = true
    try {
      const a = document.createElement('a')
      a.href = url
      a.download = `${filename}.png`
      document.body.appendChild(a)
      a.click()
      a.remove()
      showToast.value = '导出成功'
    } catch (err) {
      showToast.value = '导出失败'
      console.error('[CanvasInteractions] export error:', err)
    } finally {
      isExporting.value = false
    }
  }

  async function handleCanvasExport() {
    await exportCanvas()
  }

  // ---- 键盘快捷键 ----
  function handleKeydown(e) {
    const tag = document.activeElement?.tagName
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return
    if (e.metaKey) return

    if (e.ctrlKey && e.key === '=') { e.preventDefault(); zoomIn() }
    else if (e.ctrlKey && e.key === '-') { e.preventDefault(); zoomOut() }
    else if (e.ctrlKey && e.key === '0') { e.preventDefault(); resetZoom() }
    else if (e.key === 'F' && !e.ctrlKey && !e.altKey) { toggleFullscreen() }
    else if (e.key === 'Escape') {
      closeContextMenu()
      if (measureMode.value) {
        measureMode.value = false
        measureType.value = ''
        measureStartPoint = null
        liveMeasure.value = null
        showToast.value = ''
      }
    }
  }

  // ---- 生命周期 ----
  function _onPanMove(e) { onPanMove(e) }
  function _onPanEnd() { onPanEnd() }

  onMounted(() => {
    document.addEventListener('keydown', handleKeydown)
    document.addEventListener('wheel', handleWheel, { passive: false })
    document.addEventListener('fullscreenchange', handleFullscreenChange)
    document.addEventListener('mousemove', _onPanMove)
    document.addEventListener('mouseup', _onPanEnd)
    document.addEventListener('click', _handleClickOutside, true)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('keydown', handleKeydown)
    document.removeEventListener('wheel', handleWheel)
    document.removeEventListener('fullscreenchange', handleFullscreenChange)
    document.removeEventListener('mousemove', _onPanMove)
    document.removeEventListener('mouseup', _onPanEnd)
    document.removeEventListener('click', _handleClickOutside, true)
  })

  // ---- 命名空间对象 ----
  const canvasUI = {
    zoomLevel, zoomPercent, isFullscreen, isExporting, showZoomBar, showToast,
    isZoomMax, isZoomMin, zoomIn, zoomOut, resetZoom, toggleFullscreen, exportCanvas, clearToast,
    panX, panY, isPanning, canvasTransform, startPan,
    contextMenu, openContextMenu, closeContextMenu,
    markers, addMarker, addNote, editNote, removeMarker, clearMarkers,
    dimensions, removeDimension,
    measureMode, measureType, measureResult, liveMeasure,
    startMeasure, cancelMeasure, startDimension, handleCanvasClick, handleCanvasMove,
    isDragOver, handleDragEnter, handleDragLeave, handleDragReset,
    handleDoubleClick,
  }

  return {
    canvasUI,
    handleCanvasExport,
    zoomLevel, zoomPercent, isFullscreen, isExporting, showZoomBar, showToast,
    isZoomMax, isZoomMin, zoomIn, zoomOut, resetZoom, toggleFullscreen, exportCanvas, clearToast,
    panX, panY, isPanning, canvasTransform, startPan, onPanMove, onPanEnd,
    contextMenu, openContextMenu, closeContextMenu,
    markers, addMarker, addNote, editNote, removeMarker, clearMarkers,
    dimensions, removeDimension,
    measureMode, measureType, measureResult, liveMeasure,
    startMeasure, cancelMeasure, startDimension, handleCanvasClick, handleCanvasMove,
    isDragOver, handleDragEnter, handleDragLeave, handleDragReset,
    handleDoubleClick, handleWheel,
  }
}
