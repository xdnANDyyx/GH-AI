<template>
  <div class="canvas-editor" @contextmenu.prevent="handleContextMenu">
    <!-- 图片网格 -->
    <div class="canvas-images-grid" v-if="images.length > 0">
      <div
        v-for="(img, index) in images"
        :key="img.id || index"
        class="canvas-image-item"
        :class="{ selected: selectedImageId === (img.id || index) }"
        @click="handleImageClick(img, index)"
        @contextmenu.prevent="handleImageContextMenu($event, img, index)"
      >
        <img :src="img.url || img" :alt="`图片${index + 1}`" />
        <div class="image-index">{{ index + 1 }}</div>
        <div v-if="selectedImageId === (img.id || index)" class="image-selected-badge">
          <el-icon><Check /></el-icon>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="canvas-empty">
      <slot name="empty">
        <svg viewBox="0 0 48 48" fill="none">
          <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
          <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
          <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>暂无图片</p>
      </slot>
    </div>

    <!-- 图片点击工具栏 -->
    <div
      v-if="showEditToolbar && selectedImage"
      class="edit-toolbar"
      :style="{ left: toolbarPosition.x + 'px', top: toolbarPosition.y + 'px' }"
    >
      <div class="toolbar-item" @click="handleExtendImage">
        <el-icon><ZoomIn /></el-icon>
        <span>扩图</span>
      </div>
      <div class="toolbar-item" @click="handleMultiAngle">
        <el-icon><Grid /></el-icon>
        <span>多角度</span>
      </div>
      <div class="toolbar-item" @click="handleEditText">
        <el-icon><Edit /></el-icon>
        <span>改文字</span>
      </div>
      <div class="toolbar-item" @click="handlePartialRedraw">
        <el-icon><Brush /></el-icon>
        <span>局部重绘</span>
      </div>
      <div class="toolbar-item" @click="handleExplodeLayers">
        <el-icon><Files /></el-icon>
        <span>图层炸开</span>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="showContextMenu"
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
    >
      <div class="menu-item" @click="handleDelete">
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
    <el-dialog
      v-model="extendDialogVisible"
      title="扩图"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="extend-options">
        <div class="option-group">
          <label>扩展比例</label>
          <div class="ratio-options">
            <div
              v-for="ratio in extendRatios"
              :key="ratio.value"
              class="ratio-option"
              :class="{ active: selectedExtendRatio === ratio.value }"
              @click="selectedExtendRatio = ratio.value"
            >
              {{ ratio.label }}
            </div>
          </div>
        </div>
        <div class="option-group">
          <label>或自定义尺寸</label>
          <div class="custom-size-inputs">
            <el-input-number v-model="extendWidth" :min="100" :max="4096" :step="100" placeholder="宽" />
            <span class="size-separator">×</span>
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
    <el-dialog
      v-model="multiAngleDialogVisible"
      title="多角度生成"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="angle-options">
        <div class="option-group">
          <label>选择角度数量</label>
          <el-slider v-model="angleCount" :min="2" :max="8" :step="1" show-stops />
        </div>
        <div class="option-group">
          <label>角度类型</label>
          <div class="angle-types">
            <div
              v-for="type in angleTypes"
              :key="type.value"
              class="angle-type-option"
              :class="{ active: selectedAngleType === type.value }"
              @click="selectedAngleType = type.value"
            >
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
    <el-dialog
      v-model="editTextDialogVisible"
      title="修改文字"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="text-edit-options">
        <div class="option-group">
          <label>原文字内容</label>
          <el-input v-model="originalText" disabled />
        </div>
        <div class="option-group">
          <label>新文字内容</label>
          <el-input
            v-model="newText"
            type="textarea"
            :rows="3"
            placeholder="输入新的文字内容"
          />
        </div>
        <div class="option-group">
          <label>字体样式</label>
          <el-select v-model="selectedFont" placeholder="选择字体" style="width: 100%">
            <el-option
              v-for="font in fontOptions"
              :key="font.value"
              :label="font.label"
              :value="font.value"
            />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="editTextDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEditText" :loading="editingText">确定</el-button>
      </template>
    </el-dialog>

    <!-- 局部重绘对话框 -->
    <el-dialog
      v-model="partialRedrawDialogVisible"
      title="局部重绘"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="partial-redraw-options">
        <div class="option-group">
          <label>重绘区域</label>
          <div class="redraw-area-preview">
            <img :src="selectedImage?.url || selectedImage" class="redraw-preview-img" />
            <div class="redraw-mask" :style="redrawMaskStyle"></div>
          </div>
          <p class="option-hint">在图片上拖动选择需要重绘的区域</p>
        </div>
        <div class="option-group">
          <label>重绘描述</label>
          <el-input
            v-model="redrawDescription"
            type="textarea"
            :rows="3"
            placeholder="描述需要重绘的内容，如：将背景改为海边"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="partialRedrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPartialRedraw" :loading="redrawing">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图层炸开对话框 -->
    <el-dialog
      v-model="explodeLayersDialogVisible"
      title="图层炸开"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="explode-layers-options">
        <div class="option-group">
          <label>检测到的图层</label>
          <div class="layers-list">
            <div
              v-for="(layer, index) in detectedLayers"
              :key="index"
              class="layer-item"
            >
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import {
  Check,
  ZoomIn,
  Grid,
  Edit,
  Brush,
  Files,
  Delete,
  MagicStick,
  Picture,
  Download,
  ArrowUp,
  ArrowDown,
  Top,
  Bottom
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  featureName: {
    type: String,
    default: 'canvas'
  }
})

// Emits
const emit = defineEmits([
  'extend',
  'multi-angle',
  'edit-text',
  'partial-redraw',
  'explode-layers',
  'delete',
  'send-to-retouch',
  'send-to-white-bg',
  'download',
  'move-up',
  'move-down',
  'bring-to-front',
  'send-to-back',
  'update:images'
])

// 状态
const selectedImageId = ref(null)
const selectedImage = ref(null)
const selectedImageIndex = ref(-1)
const showEditToolbar = ref(false)
const toolbarPosition = ref({ x: 0, y: 0 })
const showContextMenu = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })

// 扩图
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

// 多角度
const multiAngleDialogVisible = ref(false)
const angleCount = ref(4)
const angleTypes = [
  { label: '产品旋转', value: 'rotate' },
  { label: '多视角', value: 'multi-view' },
  { label: '全方位', value: '360' }
]
const selectedAngleType = ref('rotate')
const generatingAngles = ref(false)

// 改文字
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

// 局部重绘
const partialRedrawDialogVisible = ref(false)
const redrawDescription = ref('')
const redrawMaskStyle = ref({})
const redrawing = ref(false)

// 图层炸开
const explodeLayersDialogVisible = ref(false)
const detectedLayers = ref([])
const exportLayersAs = ref('png')
const exploding = ref(false)

// 点击图片
const handleImageClick = (img, index) => {
  selectedImageId.value = img.id || index
  selectedImage.value = img
  selectedImageIndex.value = index

  // 显示编辑工具栏
  const event = window.event
  if (event) {
    toolbarPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
    showEditToolbar.value = true
  }

  // 点击其他地方隐藏工具栏
  setTimeout(() => {
    const hideToolbar = (e) => {
      if (!e.target.closest('.edit-toolbar')) {
        showEditToolbar.value = false
        document.removeEventListener('click', hideToolbar)
      }
    }
    document.addEventListener('click', hideToolbar)
  }, 0)
}

// 图片右键菜单
const handleImageContextMenu = (event, img, index) => {
  selectedImageId.value = img.id || index
  selectedImage.value = img
  selectedImageIndex.value = index

  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  showContextMenu.value = true

  // 点击其他地方隐藏菜单
  setTimeout(() => {
    const hideMenu = (e) => {
      if (!e.target.closest('.context-menu')) {
        showContextMenu.value = false
        document.removeEventListener('click', hideMenu)
      }
    }
    document.addEventListener('click', hideMenu)
  }, 0)
}

// 画布右键菜单（未选中图片时）
const handleContextMenu = (event) => {
  if (!selectedImage.value) {
    contextMenuPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
    showContextMenu.value = true

    setTimeout(() => {
      const hideMenu = (e) => {
        if (!e.target.closest('.context-menu')) {
          showContextMenu.value = false
          document.removeEventListener('click', hideMenu)
        }
      }
      document.addEventListener('click', hideMenu)
    }, 0)
  }
}

// 编辑功能
const handleExtendImage = () => {
  showEditToolbar.value = false
  extendDialogVisible.value = true
}

const handleMultiAngle = () => {
  showEditToolbar.value = false
  multiAngleDialogVisible.value = true
}

const handleEditText = () => {
  showEditToolbar.value = false
  originalText.value = '检测到的文字内容...' // TODO: OCR识别
  newText.value = ''
  editTextDialogVisible.value = true
}

const handlePartialRedraw = () => {
  showEditToolbar.value = false
  redrawDescription.value = ''
  partialRedrawDialogVisible.value = true
}

const handleExplodeLayers = async () => {
  showEditToolbar.value = false
  // TODO: 调用后端API检测图层
  // 模拟图层检测
  detectedLayers.value = [
    { name: '背景层', selected: true },
    { name: '产品层', selected: true },
    { name: '文字层', selected: true },
    { name: '装饰层', selected: false }
  ]
  explodeLayersDialogVisible.value = true
}

// 右键菜单功能
const handleDelete = async () => {
  showContextMenu.value = false
  try {
    await ElMessageBox.confirm('确定删除这张图片吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete', selectedImageIndex.value)
  } catch {
    // 用户取消
  }
}

const handleSendToRetouch = () => {
  showContextMenu.value = false
  emit('send-to-retouch', selectedImage.value)
  ElMessage.success('已添加到产品精修')
}

const handleSendToWhiteBg = () => {
  showContextMenu.value = false
  emit('send-to-white-bg', selectedImage.value)
  ElMessage.success('已添加到白底图')
}

const handleDownload = () => {
  showContextMenu.value = false
  emit('download', selectedImage.value)
}

const handleMoveUp = () => {
  showContextMenu.value = false
  emit('move-up', selectedImageIndex.value)
}

const handleMoveDown = () => {
  showContextMenu.value = false
  emit('move-down', selectedImageIndex.value)
}

const handleBringToFront = () => {
  showContextMenu.value = false
  emit('bring-to-front', selectedImageIndex.value)
}

const handleSendToBack = () => {
  showContextMenu.value = false
  emit('send-to-back', selectedImageIndex.value)
}

// 确认操作
const confirmExtend = async () => {
  extending.value = true
  try {
    emit('extend', {
      image: selectedImage.value,
      ratio: selectedExtendRatio.value,
      width: extendWidth.value,
      height: extendHeight.value
    })
    extendDialogVisible.value = false
  } finally {
    extending.value = false
  }
}

const confirmMultiAngle = async () => {
  generatingAngles.value = true
  try {
    emit('multi-angle', {
      image: selectedImage.value,
      count: angleCount.value,
      type: selectedAngleType.value
    })
    multiAngleDialogVisible.value = false
  } finally {
    generatingAngles.value = false
  }
}

const confirmEditText = async () => {
  if (!newText.value.trim()) {
    ElMessage.warning('请输入新的文字内容')
    return
  }
  editingText.value = true
  try {
    emit('edit-text', {
      image: selectedImage.value,
      originalText: originalText.value,
      newText: newText.value,
      font: selectedFont.value
    })
    editTextDialogVisible.value = false
  } finally {
    editingText.value = false
  }
}

const confirmPartialRedraw = async () => {
  if (!redrawDescription.value.trim()) {
    ElMessage.warning('请描述需要重绘的内容')
    return
  }
  redrawing.value = true
  try {
    emit('partial-redraw', {
      image: selectedImage.value,
      description: redrawDescription.value,
      mask: redrawMaskStyle.value
    })
    partialRedrawDialogVisible.value = false
  } finally {
    redrawing.value = false
  }
}

const confirmExplodeLayers = async () => {
  exploding.value = true
  try {
    const selectedLayers = detectedLayers.value.filter(l => l.selected)
    emit('explode-layers', {
      image: selectedImage.value,
      layers: selectedLayers,
      format: exportLayersAs.value
    })
    explodeLayersDialogVisible.value = false
  } finally {
    exploding.value = false
  }
}

// 键盘事件
const handleKeydown = (e) => {
  // Delete 或 Backspace 删除选中图片
  if ((e.key === 'Delete' || e.key === 'Backspace') && selectedImage.value) {
    // 避免在输入框中触发
    if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
      handleDelete()
    }
  }
  // Escape 关闭对话框
  if (e.key === 'Escape') {
    showEditToolbar.value = false
    showContextMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// 暴露方法
defineExpose({
  selectImage: (index) => {
    if (index >= 0 && index < props.images.length) {
      const img = props.images[index]
      handleImageClick(img, index)
    }
  },
  clearSelection: () => {
    selectedImageId.value = null
    selectedImage.value = null
    selectedImageIndex.value = -1
    showEditToolbar.value = false
    showContextMenu.value = false
  }
})
</script>

<style scoped lang="scss">
.canvas-editor {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 图片网格 */
.canvas-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  padding: 16px;
  width: 100%;
  height: 100%;
  overflow: auto;
}

.canvas-image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  background: #f5f5f5;

  &:hover {
    border-color: #2563ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(37, 99, 255, 0.15);
  }

  &.selected {
    border-color: #2563ff;
    box-shadow: 0 0 0 3px rgba(37, 99, 255, 0.2);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-index {
    position: absolute;
    top: 8px;
    left: 8px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
  }

  .image-selected-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: #2563ff;
    color: #fff;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
  }
}

/* 空状态 */
.canvas-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px;
  color: #9ca3af;

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

/* 编辑工具栏 */
.edit-toolbar {
  position: fixed;
  z-index: 1000;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 8px;
  display: flex;
  gap: 4px;
  animation: fadeIn 0.15s ease;

  .toolbar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    min-width: 60px;

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
    }
  }
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 1001;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 180px;
  animation: fadeIn 0.15s ease;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 13px;

    &:hover {
      background: #f0f4ff;
      color: #2563ff;
    }

    .el-icon {
      font-size: 16px;
    }
  }

  .menu-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 4px 0;
  }
}

/* 扩图选项 */
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

    &:hover {
      border-color: #2563ff;
    }

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

  .size-separator {
    color: #999;
  }
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

    &:hover {
      border-color: #2563ff;
    }

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

/* 重绘区域预览 */
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
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(37, 99, 255, 0.2);
    border: 2px dashed #2563ff;
  }
}

/* 图层列表 */
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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
