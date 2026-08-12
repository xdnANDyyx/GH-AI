<template>
  <div class="canvas-overlay" :class="{ 'is-fullscreen': overlay.isFullscreen.value, 'is-drag-over': overlay.isDragOver?.value }">
    <!-- 拖拽上传视觉反馈：蓝色虚线框 -->
    <div v-if="overlay.isDragOver?.value" class="drag-overlay">
      <div class="drag-overlay-inner">
        <el-icon class="drag-icon"><UploadFilled /></el-icon>
        <span class="drag-text">松开鼠标上传文件</span>
      </div>
    </div>

    <!-- 标点层 -->
    <div v-if="overlay.markers?.value?.length || overlay.dimensions?.value?.length || overlay.liveMeasure?.value || overlay.measureResult?.value" class="markers-layer">
      <!-- 持久尺寸标注连线 -->
      <svg v-if="overlay.dimensions?.value?.length" class="measure-line" :style="{ position: 'absolute', left: 0, top: 0, width: '100%', height: '100%', pointerEvents: 'none' }">
        <template v-for="d in overlay.dimensions.value" :key="'dim-line-' + d.id">
          <line :x1="d.startX" :y1="d.startY" :x2="d.endX" :y2="d.endY" stroke="#10B981" stroke-width="2" />
          <circle :cx="d.startX" :cy="d.startY" r="3" fill="#10B981" />
          <circle :cx="d.endX" :cy="d.endY" r="3" fill="#10B981" />
        </template>
      </svg>
      <!-- 尺寸标注标签 -->
      <template v-for="d in overlay.dimensions?.value || []" :key="'dim-label-' + d.id">
        <div
          class="dim-label"
          :style="{ left: ((d.startX + d.endX) / 2) + 'px', top: ((d.startY + d.endY) / 2) + 'px' }"
          @contextmenu.prevent="overlay.removeDimension?.(d.id)"
        >{{ d.label }}</div>
      </template>
      <!-- 实时测量预览线 -->
      <svg v-if="overlay.liveMeasure?.value" class="measure-line live" :style="{ position: 'absolute', left: 0, top: 0, width: '100%', height: '100%', pointerEvents: 'none' }">
        <line
          :x1="overlay.liveMeasure.value.startX"
          :y1="overlay.liveMeasure.value.startY"
          :x2="overlay.liveMeasure.value.endX"
          :y2="overlay.liveMeasure.value.endY"
          stroke="#2563FF"
          stroke-width="2"
          stroke-dasharray="6,3"
        />
      </svg>
      <!-- 实时测量距离标签 -->
      <div
        v-if="overlay.liveMeasure?.value"
        class="live-measure-label"
        :style="{ left: ((overlay.liveMeasure.value.startX + overlay.liveMeasure.value.endX) / 2) + 'px', top: ((overlay.liveMeasure.value.startY + overlay.liveMeasure.value.endY) / 2) + 'px' }"
      >{{ overlay.liveMeasure.value.label }}</div>
      <!-- 标点 -->
      <template v-for="m in overlay.markers.value" :key="m.id">
        <div
          class="marker"
          :class="`marker-${m.type}`"
          :style="{ left: m.x + 'px', top: m.y + 'px' }"
          @contextmenu.prevent="overlay.removeMarker(m.id)"
          @dblclick.stop="m.type === 'note' && overlay.editNote?.(m)"
        >
          <span v-if="m.label" class="marker-label" :class="{ 'note-label': m.type === 'note' }">{{ m.label }}</span>
        </div>
      </template>
      <!-- 测量结果线 -->
      <svg v-if="overlay.measureResult?.value" class="measure-line" :style="{ position: 'absolute', left: 0, top: 0, width: '100%', height: '100%', pointerEvents: 'none' }">
        <line
          :x1="overlay.measureResult.value.startX"
          :y1="overlay.measureResult.value.startY"
          :x2="overlay.measureResult.value.endX"
          :y2="overlay.measureResult.value.endY"
          stroke="#2563FF"
          stroke-width="2"
          stroke-dasharray="6,3"
        />
      </svg>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="overlay.contextMenu?.visible"
      class="context-menu"
      :style="{ left: overlay.contextMenu.x + 'px', top: overlay.contextMenu.y + 'px' }"
      @click.stop
    >
      <button class="ctx-item" @click="handleAction('dimension')">
        <el-icon><Aim /></el-icon>
        <span>标注尺寸</span>
      </button>
      <button class="ctx-item" @click="handleAction('note')">
        <el-icon><EditPen /></el-icon>
        <span>添加备注</span>
      </button>
      <button class="ctx-item" @click="handleAction('measure')">
        <el-icon><Histogram /></el-icon>
        <span>测量距离</span>
      </button>
      <div class="ctx-divider" />
      <button class="ctx-item ctx-danger" @click="handleAction('clear')">
        <el-icon><Delete /></el-icon>
        <span>清空所有标点</span>
      </button>
    </div>

    <!-- 测量模式提示 -->
    <div v-if="overlay.measureMode?.value" class="measure-hint">
      <el-icon><Aim /></el-icon>
      <span>{{ overlay.showToast?.value || '请点击两个点' }}</span>
      <span v-if="overlay.liveMeasure?.value" class="measure-live-value">{{ overlay.liveMeasure.value.label }}</span>
      <button class="measure-cancel" @click="overlay.cancelMeasure?.()">取消</button>
    </div>

    <!-- 底部浮动工具条（缩放/下载/全屏）- 位于画布边界 -->
    <div v-if="overlay.showZoomBar.value && !overlay.hideZoomBar?.value" class="overlay-toolbar" @mousedown.stop @contextmenu.prevent @wheel.stop>
      <button class="overlay-btn" :disabled="overlay.isZoomMin.value" title="缩小" @click="overlay.zoomOut()">
        <el-icon><Minus /></el-icon>
      </button>
      <button class="overlay-percent" title="双击重置缩放" @click="overlay.resetZoom()">
        {{ overlay.zoomPercent.value }}
      </button>
      <button class="overlay-btn" :disabled="overlay.isZoomMax.value" title="放大" @click="overlay.zoomIn()">
        <el-icon><Plus /></el-icon>
      </button>
      <span class="overlay-divider" />
      <button class="overlay-btn" title="导出图片" :disabled="overlay.isExporting.value" @click="handleExport">
        <el-icon><Download /></el-icon>
      </button>
      <button class="overlay-btn" :title="overlay.isFullscreen.value ? '退出全屏 (F)' : '全屏预览 (F)'" @click="overlay.toggleFullscreen()">
        <el-icon><FullScreen v-if="!overlay.isFullscreen.value" /><Close v-else /></el-icon>
      </button>
    </div>

    <!-- 右下角 FAB 开关 - 已注释
    <button v-if="!overlay.hideZoomBar?.value" class="overlay-fab" :title="overlay.showZoomBar.value ? '隐藏工具栏' : '显示工具栏'" @click="toggleZoomBar">
      <el-icon><ArrowDown v-if="overlay.showZoomBar.value" /><Setting v-else /></el-icon>
    </button>
    -->

    <!-- 轻提示 -->
    <Transition name="toast-fade">
      <div v-if="overlay.showToast.value && !overlay.measureMode?.value" class="overlay-toast" @click="overlay.clearToast()">
        {{ overlay.showToast.value }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { Plus, Minus, Download, ArrowDown, Setting, UploadFilled, Aim, EditPen, Histogram, Delete } from '@element-plus/icons-vue'
import { FullScreen, Close } from '@element-plus/icons-vue'

const props = defineProps({
  overlay: { type: Object, required: true },
})

const emit = defineEmits(['export', 'context-action'])

function toggleZoomBar() {
  props.overlay.showZoomBar.value = !props.overlay.showZoomBar.value
}

function handleExport() {
  emit('export')
}

function handleAction(action) {
  const o = props.overlay
  o.closeContextMenu?.()
  if (action === 'dimension') {
    o.startDimension?.()
  } else if (action === 'note') {
    o.addNote?.(o.contextMenu.x, o.contextMenu.y, '双击编辑备注')
  } else if (action === 'measure') {
    o.startMeasure?.()
  } else if (action === 'clear') {
    o.clearMarkers?.()
  }
  emit('context-action', action)
}
</script>

<style scoped>
.canvas-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 20;
}
.canvas-overlay.is-fullscreen { position: fixed; }

/* ---- 拖拽上传视觉反馈 ---- */
.drag-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 50;
}
.drag-overlay-inner {
  position: absolute;
  inset: 8px;
  border: 3px dashed #2563FF;
  border-radius: 12px;
  background: rgba(37, 99, 255, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  animation: drag-pulse 1.5s ease infinite;
}
.drag-icon { font-size: 48px; color: #2563FF; }
.drag-text { font-size: 16px; color: #2563FF; font-weight: 600; }
@keyframes drag-pulse {
  0%, 100% { background: rgba(37, 99, 255, 0.06); }
  50% { background: rgba(37, 99, 255, 0.12); }
}

/* ---- 标点层 ---- */
.markers-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 15;
}
.marker {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #2563FF;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
  transform: translate(-50%, -50%);
  pointer-events: auto;
  cursor: pointer;
}
.marker-note { background: #F59E0B; }
.marker-measure-start { background: #10B981; }
.marker-measure-end { background: #EF4444; }
.marker-label {
  position: absolute;
  left: 16px;
  top: -6px;
  white-space: nowrap;
  background: rgba(17, 24, 39, 0.85);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  pointer-events: none;
}
.measure-line { z-index: 14; }
.measure-line.live { z-index: 16; }

/* ---- 尺寸标注标签 ---- */
.dim-label {
  position: absolute;
  transform: translate(-50%, -50%);
  white-space: nowrap;
  background: rgba(16, 185, 129, 0.95);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 4px;
  pointer-events: auto;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.4);
  z-index: 17;
}
.dim-label:hover { background: #059669; }

/* ---- 实时测量标签 ---- */
.live-measure-label {
  position: absolute;
  transform: translate(-50%, -50%);
  white-space: nowrap;
  background: rgba(37, 99, 255, 0.95);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 4px;
  pointer-events: none;
  box-shadow: 0 2px 6px rgba(37, 99, 255, 0.4);
  z-index: 18;
}

/* ---- 备注标签 ---- */
.note-label {
  left: 14px;
  top: -10px;
  background: rgba(245, 158, 11, 0.95);
  pointer-events: auto;
  cursor: pointer;
  padding: 3px 8px;
  font-size: 12px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.note-label:hover { background: #D97706; }

/* ---- 实时距离值 ---- */
.measure-live-value {
  margin-left: 8px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 4px;
  font-weight: 600;
}

/* ---- 右键菜单 ---- */
.context-menu {
  position: absolute;
  z-index: 60;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.12);
  padding: 4px;
  pointer-events: auto;
  min-width: 160px;
}
.ctx-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: #374151;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.ctx-item:hover { background: #F0F4FF; color: #2563FF; }
.ctx-item .el-icon { font-size: 15px; }
.ctx-danger:hover { background: #FEF2F2; color: #EF4444; }
.ctx-divider { height: 1px; background: #F0F0F0; margin: 4px 0; }

/* ---- 测量模式提示 ---- */
.measure-hint {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(37, 99, 255, 0.95);
  color: #fff;
  font-size: 13px;
  border-radius: 8px;
  pointer-events: auto;
  box-shadow: 0 4px 16px rgba(37, 99, 255, 0.3);
  z-index: 55;
}
.measure-cancel {
  margin-left: 8px;
  padding: 2px 10px;
  background: rgba(255,255,255,0.2);
  border: none;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
.measure-cancel:hover { background: rgba(255,255,255,0.3); }

/* ---- 底部浮动工具条 ---- */
.overlay-toolbar {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 6px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  pointer-events: auto;
  z-index: 30;
}
.overlay-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #4B5563;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.15s ease;
}
.overlay-btn:hover:not(:disabled) { background: #F0F4FF; color: #2563FF; }
.overlay-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.overlay-btn .el-icon { font-size: 15px; }
.overlay-percent {
  min-width: 48px;
  height: 28px;
  border: none;
  background: transparent;
  color: #2563FF;
  font-size: 13px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.overlay-percent:hover { background: #F0F4FF; }
.overlay-divider { width: 1px; height: 16px; background: #E8EDF5; margin: 0 4px; }

/* ---- 右下角 FAB ---- */
.overlay-fab {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid #E8EDF5;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  color: #4B5563;
  font-size: 16px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.2s ease;
}
.overlay-fab:hover { background: #F0F4FF; color: #2563FF; border-color: #2563FF; }

/* ---- 轻提示 ---- */
.overlay-toast {
  position: absolute;
  top: 64px;
  right: 16px;
  padding: 8px 14px;
  background: rgba(17, 24, 39, 0.85);
  color: #fff;
  font-size: 12px;
  border-radius: 8px;
  pointer-events: auto;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.toast-fade-enter-active, .toast-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
