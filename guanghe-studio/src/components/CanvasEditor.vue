<template>
  <div class="canvas-editor" ref="containerRef">
    <!-- Fabric.js 画布容器 -->
    <div class="fabric-wrapper" ref="fabricWrapperRef"></div>

    <!-- 空状态 -->
    <div v-if="images.length === 0 && !isGenerating" class="canvas-empty">
      <slot name="empty">
        <svg viewBox="0 0 48 48" fill="none">
          <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
          <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
          <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>暂无图片，上传或生成后将显示在画布中</p>
      </slot>
    </div>

    <!-- 生成中遮罩 -->
    <div v-if="isGenerating" class="canvas-generating">
      <el-icon class="is-loading" :size="32" color="#2563FF"><Loading /></el-icon>
      <p>{{ genStatus || '正在生成...' }}</p>
    </div>

    <!-- 左上角工具栏：缩放控制 -->
    <div v-if="images.length > 0" class="canvas-toolbar">
      <div class="toolbar-btn" @click="zoomOut" title="缩小">
        <el-icon><Minus /></el-icon>
      </div>
      <span class="zoom-value">{{ Math.round(zoomLevel * 100) }}%</span>
      <div class="toolbar-btn" @click="zoomIn" title="放大">
        <el-icon><Plus /></el-icon>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-btn" @click="zoomToFit" title="适应屏幕">
        <el-icon><FullScreen /></el-icon>
      </div>
      <div class="toolbar-btn" @click="zoomToActual" title="1:1 实际大小">
        <span style="font-size: 11px;">1:1</span>
      </div>
    </div>

    <!-- 点击图片浮动工具栏（即梦AI风格） -->
    <div
      v-if="showEditToolbar && selectedImageData"
      class="edit-toolbar"
      :style="{ left: toolbarPosition.x + 'px', top: toolbarPosition.y + 'px' }"
      @click.stop
    >
      <div class="toolbar-item" @click="handleExtendImage" title="扩图">
        <el-icon><ZoomIn /></el-icon>
        <span>扩图</span>
      </div>
      <div class="toolbar-item" @click="handleMultiAngle" title="多角度">
        <el-icon><Grid /></el-icon>
        <span>多角度</span>
      </div>
      <div class="toolbar-item" @click="handleEditText" title="改文字">
        <el-icon><Edit /></el-icon>
        <span>改文字</span>
      </div>
      <div class="toolbar-item" @click="handlePartialRedraw" title="局部重绘">
        <el-icon><Brush /></el-icon>
        <span>局部重绘</span>
      </div>
      <div class="toolbar-item" @click="handleExplodeLayers" title="图层炸开">
        <el-icon><Files /></el-icon>
        <span>图层炸开</span>
      </div>
    </div>

    <!-- 右键菜单（即梦AI风格） -->
    <div
      v-if="showContextMenu"
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
      @click.stop
    >
      <div class="menu-item danger" @click="handleDelete">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleSendToRetouch">
        <el-icon><MagicStick /></el-icon>
        <span>放入产品精修</span>
      </div>
      <div class="menu-item" @click="handleSendToWhiteBg">
        <el-icon><Picture /></el-icon>
        <span>放入白底图</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleDownload">
        <el-icon><Download /></el-icon>
        <span>下载图片</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleMoveUp">
        <el-icon><ArrowUp /></el-icon>
        <span>向上移一层</span>
      </div>
      <div class="menu-item" @click="handleMoveDown">
        <el-icon><ArrowDown /></el-icon>
        <span>向下移一层</span>
      </div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleBringToFront">
        <el-icon><Top /></el-icon>
        <span>置顶</span>
      </div>
      <div class="menu-item" @click="handleSendToBack">
        <el-icon><Bottom /></el-icon>
        <span>置底</span>
      </div>
    </div>

    <!-- 扩图对话框 -->
    <el-dialog v-model="extendDialogVisible" title="扩图" width="500px" :close-on-click-modal="false" append-to-body>
      <div class="extend-options">
        <div class="option-group">
          <label>扩展比例</label>
          <div class="ratio-options">
            <div v-for="ratio in extendRatios" :key="ratio.value" class="ratio-option" :class="{ active: selectedExtendRatio === ratio.value }" @click="selectedExtendRatio = ratio.value">
              {{ ratio.label }}
            </div>
          </div>
        </div>
        <div class="option-group">
          <label>或自定义尺寸</label>
          <div class="custom-size-inputs">
            <el-input-number v-model="extendWidth" :min="100" :max="4096" :step="100" placeholder="宽" />
            <span class="size-separator">x</span>
            <el-input-number v-model="extendHeight" :min="100" :max="4096" :step="100" placeholder="高" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExtend" :loading="extending">确定</el-button>
      </template>
    </el-dialog>

    <!-- 多角度对话框 -->
    <el-dialog v-model="multiAngleDialogVisible" title="多角度生成" width="500px" :close-on-click-modal="false" append-to-body>
      <div class="angle-options">
        <div class="option-group">
          <label>选择角度数量</label>
          <el-slider v-model="angleCount" :min="2" :max="8" :step="1" show-stops />
        </div>
        <div class="option-group">
          <label>角度类型</label>
          <div class="angle-types">
            <div v-for="type in angleTypes" :key="type.value" class="angle-type-option" :class="{ active: selectedAngleType === type.value }" @click="selectedAngleType = type.value">
              {{ type.label }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="multiAngleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMultiAngle" :loading="generatingAngles">确定</el-button>
      </template>
    </el-dialog>

    <!-- 改文字对话框 -->
    <el-dialog v-model="editTextDialogVisible" title="修改文字" width="500px" :close-on-click-modal="false" append-to-body>
      <div class="text-edit-options">
        <div class="option-group">
          <label>原文字内容</label>
          <el-input v-model="originalText" disabled />
        </div>
        <div class="option-group">
          <label>新文字内容</label>
          <el-input v-model="newText" type="textarea" :rows="3" placeholder="输入新的文字内容" />
        </div>
        <div class="option-group">
          <label>字体样式</label>
          <el-select v-model="selectedFont" placeholder="选择字体" style="width: 100%">
            <el-option v-for="font in fontOptions" :key="font.value" :label="font.label" :value="font.value" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="editTextDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEditText" :loading="editingText">确定</el-button>
      </template>
    </el-dialog>

    <!-- 局部重绘对话框 -->
    <el-dialog v-model="partialRedrawDialogVisible" title="局部重绘" width="600px" :close-on-click-modal="false" append-to-body>
      <div class="partial-redraw-options">
        <div class="option-group">
          <label>重绘描述</label>
          <el-input v-model="redrawDescription" type="textarea" :rows="3" placeholder="描述需要重绘的内容，如：将背景改为海边" />
        </div>
        <div class="option-group">
          <label>重绘区域</label>
          <div class="redraw-area-preview">
            <img :src="selectedImageData?.url || selectedImageData" class="redraw-preview-img" ref="redrawPreviewRef" />
            <div class="redraw-mask" v-if="redrawMask" :style="redrawMaskStyle"></div>
          </div>
          <p class="option-hint">在图片上拖动选择需要重绘的区域</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="partialRedrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPartialRedraw" :loading="redrawing">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图层炸开对话框 -->
    <el-dialog v-model="explodeLayersDialogVisible" title="图层炸开" width="600px" :close-on-click-modal="false" append-to-body>
      <div class="explode-layers-options">
        <div class="option-group">
          <label>检测到的图层</label>
          <div class="layers-list">
            <div v-for="(layer, index) in detectedLayers" :key="index" class="layer-item">
              <el-checkbox v-model="layer.selected">{{ layer.name }}</el-checkbox>
            </div>
          </div>
        </div>
        <div class="option-group">
          <label>导出选项</label>
          <el-radio-group v-model="exportLayersAs">
            <el-radio-button label="png">分别导出PNG</el-radio-button>
            <el-radio-button label="psd">导出为PSD</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="explodeLayersDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExplodeLayers" :loading="exploding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  Plus, Minus, FullScreen, ZoomIn, Grid, Edit, Brush, Files,
  Delete, MagicStick, Picture, Download, ArrowUp, ArrowDown,
  Top, Bottom, Loading
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fabric } from 'fabric'
import { recognizeText, detectLayers } from '@/api/canvasEditor'

// ===== Props =====
const props = defineProps({
  images: { type: Array, default: () => [] },
  isGenerating: { type: Boolean, default: false },
  genStatus: { type: String, default: '' },
  featureName: { type: String, default: 'canvas' }
})

// ===== Emits =====
const emit = defineEmits([
  'extend', 'multi-angle', 'edit-text', 'partial-redraw', 'explode-layers',
  'delete', 'send-to-retouch', 'send-to-white-bg', 'download',
  'move-up', 'move-down', 'bring-to-front', 'send-to-back',
  'update:images', 'image-selected'
])

// ===== DOM Refs =====
const containerRef = ref(null)
const fabricWrapperRef = ref(null)

// ===== Fabric Canvas =====
let canvas = null
const zoomLevel = ref(1)

// ===== 选中状态 =====
const selectedImageData = ref(null)   // 选中图片的数据对象 {url, name, ...}
let selectedFabricObj = null           // 选中图片的 Fabric 对象引用

// ===== 浮动工具栏 =====
const showEditToolbar = ref(false)
const toolbarPosition = reactive({ x: 0, y: 0 })

// ===== 右键菜单 =====
const showContextMenu = ref(false)
const contextMenuPosition = reactive({ x: 0, y: 0 })

// ===== 扩图 =====
const extendDialogVisible = ref(false)
const extendRatios = [
  { label: '1:1', value: '1:1' },
  { label: '4:3', value: '4:3' },
  { label: '16:9', value: '16:9' },
  { label: '9:16', value: '9:16' },
  { label: '3:2', value: '3:2' },
  { label: '2:3', value: '2:3' }
]
const selectedExtendRatio = ref('16:9')
const extendWidth = ref(1920)
const extendHeight = ref(1080)
const extending = ref(false)

// ===== 多角度 =====
const multiAngleDialogVisible = ref(false)
const angleCount = ref(4)
const angleTypes = [
  { label: '产品旋转', value: 'rotate' },
  { label: '多视角', value: 'multi-view' },
  { label: '全方位', value: '360' }
]
const selectedAngleType = ref('rotate')
const generatingAngles = ref(false)

// ===== 改文字 =====
const editTextDialogVisible = ref(false)
const originalText = ref('')
const newText = ref('')
const selectedFont = ref('default')
const fontOptions = [
  { label: '默认字体', value: 'default' },
  { label: '思源黑体', value: 'source-han' },
  { label: '阿里巴巴普惠体', value: 'alibaba' },
  { label: '站酷高端黑', value: 'zcool' }
]
const editingText = ref(false)

// ===== 局部重绘 =====
const partialRedrawDialogVisible = ref(false)
const redrawDescription = ref('')
const redrawing = ref(false)
const redrawMask = ref(null)
const redrawPreviewRef = ref(null)
const redrawMaskStyle = computed(() => {
  if (!redrawMask.value) return {}
  return {
    left: redrawMask.value.x + 'px',
    top: redrawMask.value.y + 'px',
    width: redrawMask.value.width + 'px',
    height: redrawMask.value.height + 'px'
  }
})

// ===== 图层炸开 =====
const explodeLayersDialogVisible = ref(false)
const detectedLayers = ref([])
const exportLayersAs = ref('png')
const exploding = ref(false)

// ===== 图片数据管理 =====
// 用于跟踪 Fabric 对象与原始图片数据的映射
const fabricObjMap = new Map()  // fabricObj.id -> imageData

// ===== 初始化 Fabric 画布 =====
function initCanvas() {
  const wrapper = fabricWrapperRef.value
  if (!wrapper) return

  const width = wrapper.clientWidth || 800
  const height = wrapper.clientHeight || 600

  canvas = new fabric.Canvas('fabricCanvas', {
    width,
    height,
    backgroundColor: '#fafbfc',
    preserveObjectStacking: true,
    selection: true,
    controlsAboveOverlay: true
  })

  // 画布鼠标按下事件（统一处理左键和右键）
  canvas.on('mouse:down', (opt) => {
    const target = opt.target
    const isRightClick = opt.e && opt.e.button === 2

    if (isRightClick) {
      // ===== 右键：显示右键菜单 =====
      if (target) {
        const fid = target._fabricId
        const imageData = fabricObjMap.get(fid)
        if (imageData) {
          selectedImageData.value = imageData
          selectedFabricObj = target
        }
      }
      contextMenuPosition.x = opt.e.clientX
      contextMenuPosition.y = opt.e.clientY
      showContextMenu.value = true
      showEditToolbar.value = false
      opt.e.preventDefault()
      return
    }

    // ===== 左键：选中图片，显示浮动工具栏 =====
    if (target && target._fabricId) {
      const imageData = fabricObjMap.get(target._fabricId)
      if (imageData) {
        selectedImageData.value = imageData
        selectedFabricObj = target
        emit('image-selected', imageData)

        // 显示浮动工具栏（在图片上方居中）
        const bound = target.getBoundingRect()
        const canvasRect = canvas.getElement().getBoundingClientRect()
        toolbarPosition.x = bound.left + canvasRect.left + bound.width / 2 - 150
        toolbarPosition.y = bound.top + canvasRect.top - 55
        showEditToolbar.value = true
        showContextMenu.value = false
      }
    } else {
      // 点击空白区域，取消选中
      selectedImageData.value = null
      selectedFabricObj = null
      showEditToolbar.value = false
      showContextMenu.value = false
      emit('image-selected', null)
    }
  })

  // 对象移动事件 - 实时更新工具栏位置
  canvas.on('object:moving', () => {
    if (showEditToolbar.value && selectedFabricObj) {
      const bound = selectedFabricObj.getBoundingRect()
      const canvasRect = canvas.getElement().getBoundingClientRect()
      toolbarPosition.x = bound.left + canvasRect.left + bound.width / 2 - 150
      toolbarPosition.y = bound.top + canvasRect.top - 55
    }
  })

  // 对象缩放事件
  canvas.on('object:scaling', () => {
    if (showEditToolbar.value && selectedFabricObj) {
      const bound = selectedFabricObj.getBoundingRect()
      const canvasRect = canvas.getElement().getBoundingClientRect()
      toolbarPosition.x = bound.left + canvasRect.left + bound.width / 2 - 150
      toolbarPosition.y = bound.top + canvasRect.top - 55
    }
  })

  // 阻止画布上的默认右键菜单，并在右键时显示自定义菜单
  wrapper.addEventListener('contextmenu', (e) => {
    e.preventDefault()
    e.stopPropagation()

    // 查找右键位置下方的 Fabric 对象
    const pointer = canvas.getPointer(e)
    const target = canvas.findTarget(e)

    if (target && target._fabricId) {
      const imageData = fabricObjMap.get(target._fabricId)
      if (imageData) {
        selectedImageData.value = imageData
        selectedFabricObj = target
      }
    }

    // 显示右键菜单
    contextMenuPosition.x = e.clientX
    contextMenuPosition.y = e.clientY
    showContextMenu.value = true
    showEditToolbar.value = false

    return false
  })

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (!canvas || !fabricWrapperRef.value) return
  const wrapper = fabricWrapperRef.value
  canvas.setWidth(wrapper.clientWidth)
  canvas.setHeight(wrapper.clientHeight)
  canvas.renderAll()
}

// ===== 加载图片到画布 =====
async function loadImagesToCanvas(newImages) {
  if (!canvas) return

  // 清除现有对象（保留画布本身）
  const objects = canvas.getObjects()
  objects.forEach(obj => {
    canvas.remove(obj)
    fabricObjMap.delete(obj.id || obj._fabricId)
  })

  if (!newImages || newImages.length === 0) return

  // 按顺序加载每张图片
  for (let i = 0; i < newImages.length; i++) {
    const imgData = newImages[i]
    const url = imgData.url || imgData
    if (!url) continue

    try {
      const fabricImg = await loadFabricImage(url)
      if (!fabricImg) continue

      // 设置图片ID用于映射
      fabricImg._fabricId = 'img_' + Date.now() + '_' + i
      fabricObjMap.set(fabricImg._fabricId, imgData)

      // 居中排列，多张图片错开排列
      const offset = i * 30
      fabricImg.set({
        left: canvas.width / 2 - fabricImg.width / 2 + offset,
        top: canvas.height / 2 - fabricImg.height / 2 + offset,
        cornerColor: '#2563FF',
        cornerStrokeColor: '#2563FF',
        borderColor: '#2563FF',
        cornerSize: 10,
        transparentCorners: false,
        cornerStyle: 'circle',
        padding: 4
      })

      // 自动缩放到合适大小（最大边不超过画布的 60%）
      const maxSide = Math.min(canvas.width, canvas.height) * 0.6
      if (fabricImg.width > maxSide || fabricImg.height > maxSide) {
        const scale = maxSide / Math.max(fabricImg.width, fabricImg.height)
        fabricImg.scale(scale)
      }

      canvas.add(fabricImg)
    } catch (err) {
      console.error('加载图片到画布失败:', url, err)
    }
  }

  canvas.renderAll()
  zoomToFit()
}

// 加载 Fabric 图片对象
function loadFabricImage(url) {
  return new Promise((resolve) => {
    // 如果是 data URL，直接用
    if (url.startsWith('data:')) {
      fabric.Image.fromURL(url, (img) => {
        if (img && img.width > 0) {
          resolve(img)
        } else {
          resolve(null)
        }
      })
      return
    }

    // 网络URL，设置 crossOrigin
    fabric.Image.fromURL(url, (img) => {
      if (img && img.width > 0) {
        resolve(img)
      } else {
        resolve(null)
      }
    }, { crossOrigin: 'anonymous' })
  })
}

// ===== 缩放控制 =====
function zoomIn() {
  zoomLevel.value = Math.min(zoomLevel.value * 1.2, 5)
  applyZoom()
}

function zoomOut() {
  zoomLevel.value = Math.max(zoomLevel.value / 1.2, 0.1)
  applyZoom()
}

function zoomToFit() {
  if (!canvas) return
  const objects = canvas.getObjects()
  if (objects.length === 0) {
    zoomLevel.value = 1
    applyZoom()
    return
  }

  // 计算所有对象的包围盒
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  objects.forEach(obj => {
    const bound = obj.getBoundingRect()
    minX = Math.min(minX, bound.left)
    minY = Math.min(minY, bound.top)
    maxX = Math.max(maxX, bound.left + bound.width)
    maxY = Math.max(maxY, bound.top + bound.height)
  })

  const padding = 40
  const contentWidth = maxX - minX + padding * 2
  const contentHeight = maxY - minY + padding * 2
  const canvasWidth = canvas.getWidth()
  const canvasHeight = canvas.getHeight()

  const scale = Math.min(canvasWidth / contentWidth, canvasHeight / contentHeight)
  zoomLevel.value = scale

  // 居中所有对象
  const offsetX = (canvasWidth - (maxX - minX) * scale) / 2 - minX * scale
  const offsetY = (canvasHeight - (maxY - minY) * scale) / 2 - minY * scale

  const delta = new fabric.Point(offsetX, offsetY)
  canvas.relativePan(delta)
  applyZoom()
}

function zoomToActual() {
  zoomLevel.value = 1
  applyZoom()
}

function applyZoom() {
  if (!canvas) return
  canvas.setZoom(zoomLevel.value)
  canvas.renderAll()
}

// ===== 隐藏菜单工具函数 =====
function hideAllMenus() {
  showEditToolbar.value = false
  showContextMenu.value = false
}

// ===== 点击图片编辑功能 =====
function handleExtendImage() {
  showEditToolbar.value = false
  extendDialogVisible.value = true
}

function handleMultiAngle() {
  showEditToolbar.value = false
  multiAngleDialogVisible.value = true
}

async function handleEditText() {
  showEditToolbar.value = false
  originalText.value = '正在智能识别文字...'
  newText.value = ''
  editTextDialogVisible.value = true

  try {
    const imageUrl = selectedImageData.value?.url || selectedImageData.value
    const res = await recognizeText(imageUrl)
    const data = res.data || res
    if (data?.texts && Array.isArray(data.texts) && data.texts.length > 0) {
      originalText.value = data.texts.map(t => t.text).join(' ')
    } else if (data?.text) {
      originalText.value = data.text
    } else {
      originalText.value = '未在图片中检测到文字'
    }
  } catch (error) {
    console.error('文字识别失败:', error)
    originalText.value = '文字识别失败，请手动输入新文字'
  }
}

function handlePartialRedraw() {
  showEditToolbar.value = false
  redrawDescription.value = ''
  redrawMask.value = null
  partialRedrawDialogVisible.value = true

  nextTick(() => {
    setupRedrawMaskSelection()
  })
}

async function handleExplodeLayers() {
  showEditToolbar.value = false
  exploding.value = true

  try {
    const imageUrl = selectedImageData.value?.url || selectedImageData.value
    const res = await detectLayers({ imageUrl })
    const layers = res.data?.layers || res.layers || []
    if (layers.length > 0) {
      detectedLayers.value = layers.map((l, i) => ({
        name: l.name || `图层 ${i + 1}`,
        selected: true,
        ...l
      }))
    } else {
      throw new Error('未检测到有效图层')
    }
    explodeLayersDialogVisible.value = true
  } catch (error) {
    console.error('图层检测失败:', error)
    ElMessage.warning('智能图层识别失败，已加载默认图层结构')
    detectedLayers.value = [
      { name: '背景层', selected: true, type: 'background' },
      { name: '主体产品层', selected: true, type: 'product' },
      { name: '文本图层', selected: true, type: 'text' }
    ]
    explodeLayersDialogVisible.value = true
  } finally {
    exploding.value = false
  }
}

// ===== 右键菜单功能 =====
async function handleDelete() {
  showContextMenu.value = false
  try {
    await ElMessageBox.confirm('确定删除这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 从画布删除
    if (selectedFabricObj && canvas) {
      canvas.remove(selectedFabricObj)
      fabricObjMap.delete(selectedFabricObj._fabricId)
      canvas.renderAll()
    }

    // 找到在 images 数组中的索引
    const url = selectedImageData.value?.url || selectedImageData.value
    const index = props.images.findIndex(img => (img.url || img) === url)
    emit('delete', index)

    selectedImageData.value = null
    selectedFabricObj = null
  } catch {
    // 用户取消
  }
}

function handleSendToRetouch() {
  showContextMenu.value = false
  if (selectedImageData.value) {
    emit('send-to-retouch', selectedImageData.value)
    ElMessage.success('已添加到产品精修')
  }
}

function handleSendToWhiteBg() {
  showContextMenu.value = false
  if (selectedImageData.value) {
    emit('send-to-white-bg', selectedImageData.value)
    ElMessage.success('已添加到白底图')
  }
}

function handleDownload() {
  showContextMenu.value = false
  if (selectedImageData.value) {
    emit('download', selectedImageData.value)
  }
}

function handleMoveUp() {
  showContextMenu.value = false
  if (selectedFabricObj && canvas) {
    canvas.bringForward(selectedFabricObj)
    canvas.renderAll()
    ElMessage.success('已向上移动一层')
  }
}

function handleMoveDown() {
  showContextMenu.value = false
  if (selectedFabricObj && canvas) {
    canvas.sendBackwards(selectedFabricObj)
    canvas.renderAll()
    ElMessage.success('已向下移动一层')
  }
}

function handleBringToFront() {
  showContextMenu.value = false
  if (selectedFabricObj && canvas) {
    canvas.bringToFront(selectedFabricObj)
    canvas.renderAll()
    ElMessage.success('已置顶')
  }
}

function handleSendToBack() {
  showContextMenu.value = false
  if (selectedFabricObj && canvas) {
    canvas.sendToBack(selectedFabricObj)
    canvas.renderAll()
    ElMessage.success('已置底')
  }
}

// ===== 确认操作 =====
function confirmExtend() {
  extending.value = true
  try {
    emit('extend', {
      image: selectedImageData.value,
      ratio: selectedExtendRatio.value,
      width: extendWidth.value,
      height: extendHeight.value
    })
    extendDialogVisible.value = false
  } finally {
    extending.value = false
  }
}

function confirmMultiAngle() {
  generatingAngles.value = true
  try {
    emit('multi-angle', {
      image: selectedImageData.value,
      count: angleCount.value,
      type: selectedAngleType.value
    })
    multiAngleDialogVisible.value = false
  } finally {
    generatingAngles.value = false
  }
}

function confirmEditText() {
  if (!newText.value.trim()) {
    ElMessage.warning('请输入新的文字内容')
    return
  }
  editingText.value = true
  try {
    emit('edit-text', {
      image: selectedImageData.value,
      originalText: originalText.value,
      newText: newText.value,
      font: selectedFont.value
    })
    editTextDialogVisible.value = false
  } finally {
    editingText.value = false
  }
}

function confirmPartialRedraw() {
  if (!redrawDescription.value.trim()) {
    ElMessage.warning('请描述需要重绘的内容')
    return
  }
  redrawing.value = true
  try {
    emit('partial-redraw', {
      image: selectedImageData.value,
      description: redrawDescription.value,
      mask: redrawMask.value
    })
    partialRedrawDialogVisible.value = false
  } finally {
    redrawing.value = false
  }
}

function confirmExplodeLayers() {
  exploding.value = true
  try {
    const selectedLayers = detectedLayers.value.filter(l => l.selected)
    emit('explode-layers', {
      image: selectedImageData.value,
      layers: selectedLayers,
      format: exportLayersAs.value
    })
    explodeLayersDialogVisible.value = false
  } finally {
    exploding.value = false
  }
}

// ===== 局部重绘区域选择 =====
function setupRedrawMaskSelection() {
  const previewImg = redrawPreviewRef.value
  if (!previewImg) return

  let isDrawing = false
  let startX = 0, startY = 0

  const onMouseDown = (e) => {
    isDrawing = true
    const rect = previewImg.getBoundingClientRect()
    startX = e.clientX - rect.left
    startY = e.clientY - rect.top
    redrawMask.value = { x: startX, y: startY, width: 0, height: 0 }
  }

  const onMouseMove = (e) => {
    if (!isDrawing) return
    const rect = previewImg.getBoundingClientRect()
    const curX = e.clientX - rect.left
    const curY = e.clientY - rect.top
    redrawMask.value = {
      x: Math.min(startX, curX),
      y: Math.min(startY, curY),
      width: Math.abs(curX - startX),
      height: Math.abs(curY - startY)
    }
  }

  const onMouseUp = () => {
    isDrawing = false
  }

  previewImg.addEventListener('mousedown', onMouseDown)
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)

  // 清理函数（在对话框关闭时移除事件）
  previewImg._cleanupMask = () => {
    previewImg.removeEventListener('mousedown', onMouseDown)
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
}

// ===== 键盘事件 =====
function handleKeydown(e) {
  if ((e.key === 'Delete' || e.key === 'Backspace') && selectedFabricObj) {
    if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
      e.preventDefault()
      handleDelete()
    }
  }
  if (e.key === 'Escape') {
    hideAllMenus()
  }
}

// ===== 全局点击隐藏菜单 =====
function handleGlobalClick(e) {
  // 不要在点击 Fabric.js 画布区域时隐藏工具栏（由 Fabric 的 mouse:down 事件处理）
  if (e.target.closest('.upper-canvas') || e.target.closest('.lower-canvas')) return
  if (e.target.closest('.edit-toolbar') || e.target.closest('.context-menu')) return
  showEditToolbar.value = false
  showContextMenu.value = false
}

// ===== 监听 images 变化 =====
watch(() => props.images, (newImages) => {
  if (canvas) {
    loadImagesToCanvas(newImages)
  }
}, { deep: true })

// ===== 生命周期 =====
onMounted(() => {
  nextTick(() => {
    // 创建 canvas 元素
    const wrapper = fabricWrapperRef.value
    if (wrapper) {
      const canvasEl = document.createElement('canvas')
      canvasEl.id = 'fabricCanvas'
      wrapper.appendChild(canvasEl)
    }

    initCanvas()

    // 如果有初始图片，加载到画布
    if (props.images.length > 0) {
      loadImagesToCanvas(props.images)
    }
  })

  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('click', handleGlobalClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('click', handleGlobalClick)
  window.removeEventListener('resize', handleResize)

  // 清理画布
  if (canvas) {
    canvas.dispose()
    canvas = null
  }
  fabricObjMap.clear()
})

// ===== 暴露方法 =====
defineExpose({
  zoomToFit,
  zoomIn,
  zoomOut,
  zoomToActual,
  clearSelection: () => {
    if (canvas) {
      canvas.discardActiveObject()
      canvas.renderAll()
    }
    selectedImageData.value = null
    selectedFabricObj = null
    showEditToolbar.value = false
    showContextMenu.value = false
  },
  getCanvas: () => canvas,
  addImage: async (url, options = {}) => {
    if (!canvas) return
    const fabricImg = await loadFabricImage(url)
    if (!fabricImg) return

    fabricImg._fabricId = 'img_' + Date.now() + '_' + Math.random()
    const imageData = { url, ...options }
    fabricObjMap.set(fabricImg._fabricId, imageData)

    fabricImg.set({
      left: options.left || canvas.width / 2 - fabricImg.width / 2,
      top: options.top || canvas.height / 2 - fabricImg.height / 2,
      cornerColor: '#2563FF',
      cornerStrokeColor: '#2563FF',
      borderColor: '#2563FF',
      cornerSize: 10,
      transparentCorners: false,
      cornerStyle: 'circle',
      padding: 4
    })

    const maxSide = Math.min(canvas.width, canvas.height) * 0.6
    if (fabricImg.width > maxSide || fabricImg.height > maxSide) {
      const scale = maxSide / Math.max(fabricImg.width, fabricImg.height)
      fabricImg.scale(scale)
    }

    canvas.add(fabricImg)
    canvas.renderAll()
  }
})
</script>

<style scoped lang="scss">
.canvas-editor {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #fafbfc;
}

.fabric-wrapper {
  width: 100%;
  height: 100%;
  position: relative;

  :deep(canvas) {
    position: absolute;
    top: 0;
    left: 0;
  }
}

/* 空状态 */
.canvas-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #9ca3af;
  pointer-events: none;
  z-index: 1;

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.4;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

/* 生成中遮罩 */
.canvas-generating {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: rgba(250, 251, 252, 0.85);
  z-index: 10;

  p {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

/* 左上角工具栏 */
.canvas-toolbar {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 4px 8px;
  border: 1px solid #f0f0f0;

  .toolbar-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    color: #6b7280;

    &:hover {
      background: #f0f4ff;
      color: #2563ff;
    }

    .el-icon {
      font-size: 14px;
    }
  }

  .zoom-value {
    font-size: 12px;
    color: #6b7280;
    min-width: 42px;
    text-align: center;
    font-weight: 500;
  }

  .toolbar-divider {
    width: 1px;
    height: 16px;
    background: #f0f0f0;
    margin: 0 4px;
  }
}

/* 点击图片浮动工具栏 */
.edit-toolbar {
  position: fixed;
  z-index: 1000;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  padding: 6px;
  display: flex;
  gap: 2px;
  animation: fadeInDown 0.15s ease;

  .toolbar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    min-width: 56px;

    &:hover {
      background: #f0f4ff;
      color: #2563ff;
    }

    .el-icon {
      font-size: 18px;
    }

    span {
      font-size: 11px;
      white-space: nowrap;
      font-weight: 500;
    }
  }
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 1001;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  padding: 6px;
  min-width: 180px;
  animation: fadeInScale 0.15s ease;
  border: 1px solid #f0f0f0;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    cursor: pointer;
    transition: all 0.15s;
    font-size: 13px;
    color: #374151;
    border-radius: 6px;

    &:hover {
      background: #f0f4ff;
      color: #2563ff;
    }

    &.danger:hover {
      background: #fef2f2;
      color: #ef4444;
    }

    .el-icon {
      font-size: 16px;
    }
  }

  .menu-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 4px 8px;
  }
}

/* 对话框通用选项样式 */
.extend-options,
.angle-options,
.text-edit-options,
.partial-redraw-options,
.explode-layers-options {
  .option-group {
    margin-bottom: 20px;

    label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 10px;
    }
  }
}

.ratio-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;

  .ratio-option {
    padding: 10px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 13px;

    &:hover { border-color: #2563ff; }
    &.active {
      background: #2563ff;
      color: #fff;
      border-color: #2563ff;
    }
  }
}

.custom-size-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
  .size-separator { color: #999; }
}

.angle-types {
  display: flex;
  gap: 8px;

  .angle-type-option {
    padding: 8px 16px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 13px;

    &:hover { border-color: #2563ff; }
    &.active {
      background: #2563ff;
      color: #fff;
      border-color: #2563ff;
    }
  }
}

.option-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.redraw-area-preview {
  position: relative;
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
  overflow: hidden;
  cursor: crosshair;

  .redraw-preview-img {
    width: 100%;
    display: block;
  }

  .redraw-mask {
    position: absolute;
    background: rgba(37, 99, 255, 0.2);
    border: 2px dashed #2563ff;
    pointer-events: none;
  }
}

.layers-list {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .layer-item {
    padding: 10px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    background: #f9fafb;
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
