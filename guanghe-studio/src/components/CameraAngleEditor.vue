<template>
  <div class="camera-angle-panel">
    <!-- Header Section -->
    <div class="panel-header">
      <span class="panel-title">多角度编辑</span>
      <button class="reset-btn" @click="resetParameters">
        <span class="reset-icon">↺</span>
        <span>重置参数</span>
      </button>
    </div>

    <!-- Main Content Layout -->
    <div class="panel-body">
      <!-- Left Column: Interactive 3D Sphere -->
      <div class="sphere-column">
        <div class="sphere-outer-container">
          <!-- Edge Navigation Arrows -->
          <button class="nav-arrow nav-up" @click="adjustAngle('up')" title="向上倾斜">▲</button>
          <button class="nav-arrow nav-down" @click="adjustAngle('down')" title="向下倾斜">▼</button>
          <button class="nav-arrow nav-left" @click="adjustAngle('left')" title="向左旋转">◀</button>
          <button class="nav-arrow nav-right" @click="adjustAngle('right')" title="向右旋转">▶</button>

          <!-- 3D Sphere Interactive Area -->
          <div 
            class="sphere-interactive-area" 
            ref="sphereContainerRef"
            @mousedown="handleStartDrag"
          >
            <!-- Canvas for 3D sphere wireframe and camera spotlight beam -->
            <canvas ref="sphereCanvasRef" width="260" height="260" class="sphere-canvas"></canvas>

            <!-- Center Product Preview Card -->
            <div class="product-preview-card">
              <img :src="imageUrl || defaultProductImg" alt="product" class="preview-img" />
            </div>

            <!-- Floating 3D Camera Indicator -->
            <div class="camera-indicator" :style="cameraIndicatorStyle">
              <!-- Highly stylized camera SVG with glassmorphism/neon effect -->
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 8C4 6.89543 4.89543 6 6 8H14C15.1046 8 16 6.89543 16 8V16C16 17.1046 15.1046 18 14 18H6C4.89543 18 4 17.1046 4 16V8Z" fill="#ffffff" />
                <path d="M18 10.5L21.5 8.5V15.5L18 13.5V10.5Z" fill="#ffffff" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Settings and Controls -->
      <div class="settings-column">
        <!-- Section: Camera Position Presets -->
        <div class="section-title">摄像机方位</div>
        <div class="preset-grid">
          <button 
            v-for="preset in presets" 
            :key="preset.label"
            class="preset-btn-item"
            :class="{ active: isPresetActive(preset) }"
            @click="applyPreset(preset)"
          >
            <span class="preset-arrow">{{ preset.arrow }}</span>
            <span class="preset-label">{{ preset.label }}</span>
          </button>
        </div>

        <!-- Section: Manual Adjustment Sliders -->
        <div class="slider-section">
          <div class="slider-row">
            <span class="slider-label">水平</span>
            <el-slider 
              v-model="localHorizontal" 
              :min="-180" 
              :max="180" 
              :step="1"
              :show-tooltip="false"
              class="custom-dark-slider"
              @input="onSliderChange"
            />
            <span class="slider-value-display">{{ localHorizontal }}°</span>
          </div>

          <div class="slider-row">
            <span class="slider-label">垂直</span>
            <el-slider 
              v-model="localVertical" 
              :min="-90" 
              :max="90" 
              :step="1"
              :show-tooltip="false"
              class="custom-dark-slider"
              @input="onSliderChange"
            />
            <span class="slider-value-display">{{ localVertical }}°</span>
          </div>
        </div>

        <!-- Section: Actions (Footer panel) -->
        <div class="action-section">
          <div class="cost-indicator">
            <span class="star-icon">✦</span>
            <span class="cost-val">1</span>
          </div>
          <div class="btn-group">
            <el-button class="cancel-btn" @click="$emit('cancel')">取消</el-button>
            <el-button 
              type="primary" 
              class="generate-btn" 
              :loading="loading" 
              @click="handleGenerate"
            >
              生成
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  imageUrl: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['cancel', 'generate'])

const defaultProductImg = '/images/logo-guanghe-ai.png'

// Camera Angles (State)
const localHorizontal = ref(60) // -180 to 180 (centered at 0)
const localVertical = ref(35)   // -90 to 90

const isDragging = ref(false)
const sphereCanvasRef = ref(null)
const sphereContainerRef = ref(null)

// Presets mapping to specific angles
const presets = [
  { label: '左上', arrow: '↘', h: -45, v: 30 },
  { label: '上方', arrow: '↓', h: 0, v: 60 },
  { label: '右上', arrow: '↙', h: 45, v: 30 },
  { label: '左侧', arrow: '→', h: -90, v: 0 },
  { label: '正面', arrow: '•', h: 0, v: 0 },
  { label: '右侧', arrow: '←', h: 90, v: 0 },
  { label: '左下', arrow: '↗', h: -45, v: -30 },
  { label: '下方', arrow: '↑', h: 0, v: -60 },
  { label: '右下', arrow: '↖', h: 45, v: -30 }
]

// Check if a preset is currently active (using a small delta to allow minor slider adjustments)
const isPresetActive = (preset) => {
  return Math.abs(localHorizontal.value - preset.h) < 4 && Math.abs(localVertical.value - preset.v) < 4
}

// Reset angles to defaults
const resetParameters = () => {
  localHorizontal.value = 60
  localVertical.value = 35
  drawSphere()
}

// Apply preset camera angles
const applyPreset = (preset) => {
  localHorizontal.value = preset.h
  localVertical.value = preset.v
  drawSphere()
}

// Adjust camera angle using edge navigation arrows
const adjustAngle = (direction) => {
  if (direction === 'up') {
    localVertical.value = Math.min(90, localVertical.value + 10)
  } else if (direction === 'down') {
    localVertical.value = Math.max(-90, localVertical.value - 10)
  } else if (direction === 'left') {
    let newH = localHorizontal.value - 15
    if (newH < -180) newH += 360
    localHorizontal.value = newH
  } else if (direction === 'right') {
    let newH = localHorizontal.value + 15
    if (newH > 180) newH -= 360
    localHorizontal.value = newH
  }
  drawSphere()
}

// Recalculate 3D canvas when slider changes
const onSliderChange = () => {
  drawSphere()
}

// --- Drag & Drop logic for 3D rotation ---
let dragStartX = 0
let dragStartY = 0
let dragStartH = 0
let dragStartV = 0

const handleStartDrag = (e) => {
  // Prevent selecting text or dragging images natively
  e.preventDefault()
  
  isDragging.value = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartH = localHorizontal.value
  dragStartV = localVertical.value

  window.addEventListener('mousemove', handleDragging)
  window.addEventListener('mouseup', handleStopDrag)
}

const handleDragging = (e) => {
  if (!isDragging.value) return

  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY

  // Sensitivity coefficient: pixels to degrees
  const sensitivity = 0.7

  // Update angles based on dragging deltas
  let newH = dragStartH + dx * sensitivity
  // Keep horizontal clamped/wrapped between -180 and 180
  if (newH > 180) newH = ((newH + 180) % 360) - 180
  if (newH < -180) newH = ((newH - 180) % 360) + 180
  
  localHorizontal.value = Math.round(newH)
  localVertical.value = Math.round(Math.max(-89, Math.min(89, dragStartV - dy * sensitivity)))

  drawSphere()
}

const handleStopDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', handleDragging)
  window.removeEventListener('mouseup', handleStopDrag)
}

// --- Dynamic 3D Camera Indicator Positioning ---
const cameraIndicatorStyle = computed(() => {
  const canvas = sphereCanvasRef.value
  if (!canvas) return { display: 'none' }

  const cx = canvas.width / 2
  const cy = canvas.height / 2
  const R = Math.min(canvas.width, canvas.height) * 0.42

  // Fixed Viewport Angles (to present sphere in elegant tilted 3D)
  const viewportTheta = -30 * Math.PI / 180
  const viewportPhi = 15 * Math.PI / 180

  const cosT = Math.cos(viewportTheta)
  const sinT = Math.sin(viewportTheta)
  const cosP = Math.cos(viewportPhi)
  const sinP = Math.sin(viewportPhi)

  // Camera Spherical coordinate components
  const hRad = localHorizontal.value * Math.PI / 180
  const vRad = localVertical.value * Math.PI / 180

  // Camera 3D point relative to center of the sphere
  const camX = R * Math.cos(vRad) * Math.sin(hRad)
  const camY = -R * Math.sin(vRad)
  const camZ = R * Math.cos(vRad) * Math.cos(hRad)

  // Apply Viewport Rotation Matrix
  // 1. Rotate Y (theta)
  const x1 = camX * cosT + camZ * sinT
  const z1 = -camX * sinT + camZ * cosT
  const y1 = camY

  // 2. Rotate X (phi)
  const x2 = x1
  const y2 = y1 * cosP - z1 * sinP
  const z2 = y1 * sinP + z1 * cosP

  const screenX = cx + x2
  const screenY = cy + y2

  // Calculate 2D direction angle pointing directly back to center (cx, cy)
  const angleRad = Math.atan2(cy - screenY, cx - screenX)

  // Depth-sorting: positive z2 means camera is in the front hemisphere
  const isFront = z2 >= 0

  return {
    left: `${screenX}px`,
    top: `${screenY}px`,
    transform: `translate(-50%, -50%) rotate(${angleRad}rad)`,
    opacity: isFront ? 1.0 : 0.45,
    zIndex: isFront ? 10 : 1,
    boxShadow: isFront ? '0 0 15px rgba(255, 255, 255, 0.4)' : 'none',
    transition: isDragging.value ? 'none' : 'left 0.25s ease, top 0.25s ease, transform 0.25s ease'
  }
})

// --- Canvas 2D 3D Sphere Projection Renderer ---
const drawSphere = () => {
  const canvas = sphereCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  const cx = canvas.width / 2
  const cy = canvas.height / 2
  const R = Math.min(canvas.width, canvas.height) * 0.42

  // Viewport Perspective Rotation Matrix constants
  const viewportTheta = -30 * Math.PI / 180
  const viewportPhi = 15 * Math.PI / 180

  const cosT = Math.cos(viewportTheta)
  const sinT = Math.sin(viewportTheta)
  const cosP = Math.cos(viewportPhi)
  const sinP = Math.sin(viewportPhi)

  // Math: 3D point to 2D Screen projection mapping
  const projectPoint = (x, y, z) => {
    // Rotate Y (theta)
    const x1 = x * cosT + z * sinT
    const z1 = -x * sinT + z * cosT
    // Rotate X (phi)
    const x2 = x1
    const y2 = y * cosP - z1 * sinP
    const z2 = y * sinP + z1 * cosP

    return {
      x: cx + x2,
      y: cy + y2,
      z: z2
    }
  }

  const backSegments = []
  const frontSegments = []

  // 1. Generate Latitudinal grid lines (circles parallel to X-Z plane)
  const latitudes = [-60, -30, 0, 30, 60]
  latitudes.forEach(lat => {
    const latRad = lat * Math.PI / 180
    const y = -R * Math.sin(latRad)
    const r = R * Math.cos(latRad)

    const points = []
    for (let deg = 0; deg <= 360; deg += 10) {
      const rad = deg * Math.PI / 180
      const x = r * Math.sin(rad)
      const z = r * Math.cos(rad)
      points.push(projectPoint(x, y, z))
    }

    // Slice circles into segments and depth-sort them
    for (let i = 0; i < points.length - 1; i++) {
      const p1 = points[i]
      const p2 = points[i + 1]
      const avgZ = (p1.z + p2.z) / 2
      if (avgZ < 0) {
        backSegments.push({ p1, p2 })
      } else {
        frontSegments.push({ p1, p2 })
      }
    }
  })

  // 2. Generate Longitudinal grid lines (meridians passing through poles)
  for (let lon = 0; lon < 360; lon += 30) {
    const lonRad = lon * Math.PI / 180
    const points = []
    for (let lat = -90; lat <= 90; lat += 10) {
      const latRad = lat * Math.PI / 180
      const x = R * Math.cos(latRad) * Math.sin(lonRad)
      const y = -R * Math.sin(latRad)
      const z = R * Math.cos(latRad) * Math.cos(lonRad)
      points.push(projectPoint(x, y, z))
    }

    for (let i = 0; i < points.length - 1; i++) {
      const p1 = points[i]
      const p2 = points[i + 1]
      const avgZ = (p1.z + p2.z) / 2
      if (avgZ < 0) {
        backSegments.push({ p1, p2 })
      } else {
        frontSegments.push({ p1, p2 })
      }
    }
  }

  // --- RENDERING PHASE ---

  // A. Draw Back segments (faint, behind product card)
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.04)'
  ctx.lineWidth = 1
  backSegments.forEach(seg => {
    ctx.beginPath()
    ctx.moveTo(seg.p1.x, seg.p1.y)
    ctx.lineTo(seg.p2.x, seg.p2.y)
    ctx.stroke()
  })

  // B. Draw outer sphere boundary boundary
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)'
  ctx.lineWidth = 1.2
  ctx.beginPath()
  ctx.arc(cx, cy, R, 0, Math.PI * 2)
  ctx.stroke()

  // C. Calculate Camera Position & Depth
  const hRad = localHorizontal.value * Math.PI / 180
  const vRad = localVertical.value * Math.PI / 180
  const camX = R * Math.cos(vRad) * Math.sin(hRad)
  const camY = -R * Math.sin(vRad)
  const camZ = R * Math.cos(vRad) * Math.cos(hRad)
  const camProjected = projectPoint(camX, camY, camZ)

  // D. Draw Spotlight Frustum Beam (Camera Lens Flare/Cone to Center)
  const dx = cx - camProjected.x
  const dy = cy - camProjected.y
  const dist = Math.sqrt(dx * dx + dy * dy)
  if (dist > 5) {
    const px = -dy / dist
    const py = dx / dist

    const wLens = 4   // Small tip at camera lens
    const wTarget = 24 // Broad base at center image card

    ctx.beginPath()
    ctx.moveTo(camProjected.x + px * wLens, camProjected.y + py * wLens)
    ctx.lineTo(camProjected.x - px * wLens, camProjected.y - py * wLens)
    ctx.lineTo(cx - px * wTarget, cy - py * wTarget)
    ctx.lineTo(cx + px * wTarget, cy + py * wTarget)
    ctx.closePath()

    const grad = ctx.createLinearGradient(camProjected.x, camProjected.y, cx, cy)
    const isFront = camProjected.z >= 0
    if (isFront) {
      grad.addColorStop(0, 'rgba(255, 255, 255, 0.22)')
      grad.addColorStop(0.5, 'rgba(255, 255, 255, 0.06)')
      grad.addColorStop(1, 'rgba(255, 255, 255, 0.0)')
    } else {
      // Behind central card, draw a highly faded ray
      grad.addColorStop(0, 'rgba(255, 255, 255, 0.08)')
      grad.addColorStop(1, 'rgba(255, 255, 255, 0.0)')
    }
    ctx.fillStyle = grad
    ctx.fill()
  }

  // E. Draw Front segments (brighter, overlays spotlight beam)
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.12)'
  ctx.lineWidth = 1
  frontSegments.forEach(seg => {
    ctx.beginPath()
    ctx.moveTo(seg.p1.x, seg.p1.y)
    ctx.lineTo(seg.p2.x, seg.p2.y)
    ctx.stroke()
  })
}

const handleGenerate = () => {
  emit('generate', {
    horizontal: localHorizontal.value,
    vertical: localVertical.value
  })
}

// Re-draw when component mounts or image changes
onMounted(() => {
  drawSphere()
})

onBeforeUnmount(() => {
  handleStopDrag()
})

watch(() => props.imageUrl, () => {
  drawSphere()
})
</script>

<style lang="scss" scoped>
.camera-angle-panel {
  background-color: #121216;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 24px;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  user-select: none;
}

/* Header styling */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #f3f4f6;
  }

  .reset-btn {
    background: transparent;
    border: none;
    color: #9ca3af;
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: color 0.2s ease;

    &:hover {
      color: #ffffff;
    }

    .reset-icon {
      font-size: 14px;
      font-weight: bold;
    }
  }
}

/* Body layout */
.panel-body {
  display: flex;
  gap: 32px;
  align-items: stretch;
}

/* Sphere Column */
.sphere-column {
  flex: 1.1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.sphere-outer-container {
  position: relative;
  width: 280px;
  height: 280px;
  background-color: #1a1a22;
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.6);
}

/* Edge navigation arrows on sphere boundary */
.nav-arrow {
  position: absolute;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.35);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 15;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;

  &:hover {
    color: #ffffff;
    transform: scale(1.15);
  }

  &.nav-up {
    top: 8px;
    left: 50%;
    transform: translateX(-50%);
    &:hover { transform: translateX(-50%) scale(1.15); }
  }

  &.nav-down {
    bottom: 8px;
    left: 50%;
    transform: translateX(-50%);
    &:hover { transform: translateX(-50%) scale(1.15); }
  }

  &.nav-left {
    left: 8px;
    top: 50%;
    transform: translateY(-50%);
    &:hover { transform: translateY(-50%) scale(1.15); }
  }

  &.nav-right {
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    &:hover { transform: translateY(-50%) scale(1.15); }
  }
}

/* 3D Interactive area */
.sphere-interactive-area {
  position: relative;
  width: 260px;
  height: 260px;
  cursor: grab;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;

  &:active {
    cursor: grabbing;
  }
}

.sphere-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  pointer-events: none;
}

/* Center Product card mockup */
.product-preview-card {
  position: absolute;
  width: 76px;
  height: 76px;
  left: 50%;
  top: 50%;
  background: #23232c;
  border: 1.5px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
  /* Pivot coordinates and perspective projection tilt to align with world axes */
  transform: translate(-50%, -50%) rotateY(-30deg) rotateX(15deg);
  transform-style: preserve-3d;
  z-index: 5; /* Sits directly in-between front and back wireframe elements */
  pointer-events: none;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2px;

  .preview-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 6px;
  }
}

/* Camera pointer styling */
.camera-indicator {
  position: absolute;
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
  border: 2px solid #ffffff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  pointer-events: none; /* Mouse events capture on the sphere container */
  box-sizing: border-box;

  svg {
    // Offset standard center translation
    margin-right: -1px;
  }
}

/* Settings Column */
.settings-column {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.section-title {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 12px;
  font-weight: 500;
}

/* 3x3 Grid camera presets */
.preset-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.preset-btn-item {
  background-color: #1a1a22;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 10px 4px;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background-color: #242430;
    color: #ffffff;
    border-color: rgba(255, 255, 255, 0.15);
  }

  &.active {
    background-color: rgba(79, 70, 229, 0.12);
    border-color: #4f46e5;
    color: #818cf8;
    box-shadow: 0 0 12px rgba(79, 70, 229, 0.2);
  }

  .preset-arrow {
    font-size: 15px;
    font-weight: 700;
  }

  .preset-label {
    font-size: 12px;
  }
}

/* Sliders Adjustment Panel */
.slider-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #1a1a22;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.03);
  margin-bottom: 24px;
}

.slider-row {
  display: flex;
  align-items: center;
  gap: 12px;

  .slider-label {
    width: 32px;
    font-size: 13px;
    color: #9ca3af;
  }

  .custom-dark-slider {
    flex: 1;

    /* Element Plus slider customization */
    :deep(.el-slider__runway) {
      background-color: #121216 !important;
      height: 4px;
    }

    :deep(.el-slider__bar) {
      background-color: #4f46e5 !important;
      height: 4px;
    }

    :deep(.el-slider__button) {
      border: 2px solid #ffffff !important;
      background-color: #4f46e5 !important;
      width: 14px;
      height: 14px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.5);
      transition: transform 0.1s ease;

      &:hover {
        transform: scale(1.2);
      }
    }
  }

  .slider-value-display {
    width: 44px;
    text-align: right;
    font-size: 13px;
    color: #e5e7eb;
    font-variant-numeric: tabular-nums;
  }
}

/* Action panel / footer styling */
.action-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 16px;

  .cost-indicator {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #9ca3af;

    .star-icon {
      color: #fbbf24;
      font-size: 15px;
    }

    .cost-val {
      font-size: 14px;
      font-weight: 500;
    }
  }

  .btn-group {
    display: flex;
    gap: 12px;

    .cancel-btn {
      background-color: transparent;
      border: 1px solid rgba(255, 255, 255, 0.12);
      color: #d1d5db;
      padding: 8px 16px;
      border-radius: 8px;
      height: 36px;
      transition: all 0.2s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.05);
        color: #ffffff;
      }
    }

    .generate-btn {
      background-color: #ffffff;
      border: none;
      color: #121216;
      font-weight: 600;
      padding: 8px 24px;
      border-radius: 8px;
      height: 36px;
      transition: all 0.2s;

      &:hover {
        background-color: #f3f4f6;
        box-shadow: 0 4px 12px rgba(255, 255, 255, 0.15);
      }
    }
  }
}
</style>
