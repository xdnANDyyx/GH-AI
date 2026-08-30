<template>
  <div class="workspace-page">

    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas (50%) ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Steps bar（AI 流程图，显示在画布顶部，不超出 AI 配置区域） -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 1)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>

        <div class="canvas-box">
          <!-- 画布浮层：缩放 / 全屏 / 导出 / 右键菜单 -->
          <!--<CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" />-->

          <!-- 未生成：显示空状态 -->
          <div class="canvas-placeholder" v-if="!hasResult">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>AI 白底图生成后将显示在此处</h3>
            <p>请在右侧配置生成参数并点击发送</p>
          </div>

          <!-- 已生成：显示结果图 -->
          <div class="result-view" v-else>
            <img :src="resultImageUrl" class="result-img" alt="白底图" @contextmenu.prevent="openHandoffMenu($event, resultImageUrl)" />
          </div>

        </div>

        <div class="canvas-bottom-bar">
          <!-- <span>提示：上传优质素材被下载即可获得积分奖励 <a class="canvas-link" href="#">去上传 →</a></span> -->
          <!-- <span v-if="!hasResult"><span>本次生成预计消耗：<strong>{{ consumePoints }}</strong> 积分</span></span> -->
          <span v-if="hasResult" class="result-status"><span class="result-dot"></span> 生成完成</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ right panel -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'right')"></div>
        <div class="config-toggle-btn" :class="{ active: !configCollapsed }" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowRight v-if="!configCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
      </div>

      <!-- ===== RIGHT: Config Panel + AI ===== -->
      <div class="right-col" :style="{ flex: rightFlex }">
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'config')"></div>
        
        <!-- Config Panel -->
        <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
          <el-scrollbar v-show="!configCollapsed">
            <div class="config-inner">
              <div class="panel-header" @click="toggleAllSections">
                <span>AI 配置</span>
                <span class="panel-toggle-all" :class="{ active: allExpanded }">{{ allExpanded ? '全部折叠 ▲' : '全部展开 ▼' }}</span>
              </div>

              <!-- 反推提示词入口 -->
              <div class="reverse-prompt-entry">
                <el-button type="primary" plain class="reverse-prompt-btn" @click="openReversePromptDialog">
                  <el-icon><MagicStick /></el-icon>
                  <span>反推提示词</span>
                </el-button>
                <p class="entry-helper">上传参考图，AI 帮你描述想要的画面效果</p>
              </div>


              <!-- Section: Upload -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('upload')">
                  <span class="section-label"><span class="required-mark">*</span>上传图片<span class="required-mark">（必填）</span></span>
                  <span class="expand-text">
                    {{ sections.upload ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.upload">
                  <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                    <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                    <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                    <p class="panel-upload-hint">支持 JPG/PNG/WebP，单张最大 7MB</p>
                  </div>
                  <div v-if="originalFile" class="uploaded-file-info">
                    <div class="file-preview">
                      <img v-if="originalImage" :src="originalImage" class="file-thumb" />
                      <el-icon v-else :size="28" color="#6B7280"><Picture /></el-icon>
                    </div>
                    <div class="file-detail">
                      <span class="file-name">{{ originalFile.name }}</span>
                      <span class="file-size">{{ formatFileSize(originalFile.size) }}</span>
                    </div>
                    <el-icon class="file-remove" :size="16" @click.stop="removeFile"><Close /></el-icon>
                  </div>
                </div>
              </div>

              <!-- Section: Style -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('style')">
                  <span class="section-label">白底样式</span>
                  <span class="expand-text">
                    {{ sections.style ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.style }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.style">
                  <div class="style-cards">
                    <div
                      v-for="s in shadowStyles"
                      :key="s.value"
                      class="style-card"
                      :class="{ active: selectedStyle === s.value }"
                      @click="selectedStyle = (selectedStyle === s.value ? '' : s.value)"
                    >
                      <div class="style-preview" :class="s.value">
                        <img :src="getImageUrl(s.image)" class="style-preview-img" alt="" />
                      </div>
                      <span class="style-name">{{ s.label }}</span>
                    </div>
                  </div>
                  <p class="section-helper">提示：无阴影白底更适合电商主图，自然阴影更具立体感</p>
                </div>
              </div>

              <!-- Section: Size -->
              <!-- 输出尺寸 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('size')">
                  <span class="section-label">输出尺寸</span>
                  <span class="expand-text">
                    {{ sections.size ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.size }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.size">
                  <el-select v-model="outputSize" placeholder="请选择输出尺寸" style="width: 100%">
                    <el-option
                      v-for="s in sizeOptions"
                      :key="s.value"
                      :label="s.label"
                      :value="s.value"
                    />
                  </el-select>
                  <!-- 自定义尺寸 -->
                  <div v-if="outputSize === 'custom'" class="custom-size-row">
                    <div class="custom-size-input">
                      <span>宽</span>
                      <el-input-number v-model="customWidth" :min="64" :max="4096" :step="100" size="small" controls-position="right" />
                    </div>
                    <span class="custom-size-x">×</span>
                    <div class="custom-size-input">
                      <span>高</span>
                      <el-input-number v-model="customHeight" :min="64" :max="4096" :step="100" size="small" controls-position="right" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 提示词增强 -->
              <!-- <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('promptBoost')">
                  <span class="section-label">提示词增强</span>
                  <span class="expand-text">
                    {{ sections.promptBoost ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.promptBoost }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.promptBoost">
                  <div class="prompt-boost-row">
                    <label class="boost-label">产品类别</label>
                    <PromptLibrarySelect ref="boostProductRef" category="product" v-model="boostProduct" placeholder="选择产品类别" />
                  </div>
                  <div class="prompt-boost-row">
                    <label class="boost-label">材质</label>
                    <PromptLibrarySelect ref="boostMaterialRef" category="material" v-model="boostMaterial" placeholder="选择材质" />
                  </div>
                  <div class="prompt-boost-row">
                    <label class="boost-label">镜头距离</label>
                    <PromptLibrarySelect ref="boostCameraDistanceRef" category="camera" key-prefix="camera.distance." v-model="boostCameraDistance" placeholder="选择镜头距离" />
                  </div>
                  <div class="prompt-boost-row">
                    <label class="boost-label">主体占比</label>
                    <PromptLibrarySelect ref="boostCameraOccupationRef" category="camera" key-prefix="camera.occupation." v-model="boostCameraOccupation" placeholder="选择主体占比" />
                  </div>
                  <p class="section-helper">提示：选用的约束词会自动拼接到生图提示词中，提升出图质量</p>
                </div>
              </div> -->

              <!-- Section: Language -->
              <!--<div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('language')">
                  <span class="section-label">语言</span>
                  <span class="expand-text">
                    {{ sections.language ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.language }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.language">
                  <el-select v-model="language" style="width: 100%">
                    <el-option v-for="l in languages" :key="l.value" :label="l.label" :value="l.value" />
                  </el-select>
                  <p class="section-helper">选择输出图片上文字的语言，适配跨境电商场景</p>
                </div>
              </div>-->

              <!-- Results -->
              <div v-if="resultImages.length" class="result-area">
                <div class="section-label">生成结果</div>
                <div class="result-grid">
                  <div v-for="(img, i) in resultImages" :key="i" class="result-item">
                    <img :src="img.url || img" />
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- Divider inside right panel: config ⇔ AI -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'ai')"></div>

        <!-- AI Panel（保持不变） -->
        <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
          <AiAssistant ref="aiAssistantRef" :generate-fn="handleGenerate" :is-generating="isGenerating" :gen-status="genStatus" :gen-progress="genProgress" :gen-error="genError" :has-image="!!originalImage" :on-clear-images="clearWorkspaceImages" />
        </div>
      </div>
    </div>

  

    <input type="file" ref="fileInput" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" hidden @change="handleFileSelect" />

    <!-- 反推提示词模态框 -->
    <el-dialog
      v-model="reverseDialogVisible"
      title="反推提示词"
      width="560px"
      :close-on-click-modal="false"
      append-to-body
      class="reverse-prompt-dialog"
    >
      <div class="reverse-prompt-body">
        <!-- 图片上传区 -->
        <div class="rp-upload-zone" @click="triggerReverseUpload" @dragover.prevent @drop.prevent="handleReverseDrop">
          <img v-if="reverseImagePreview" :src="reverseImagePreview" class="rp-preview-img" alt="" />
          <template v-else>
            <el-icon :size="36" color="#9CA3AF"><UploadFilled /></el-icon>
            <p class="rp-upload-text">点击或拖拽图片到此处</p>
            <p class="rp-upload-hint">支持 JPG/PNG/WebP，单张最大 7MB</p>
          </template>
          <button v-if="reverseImagePreview" class="rp-clear-btn" @click.stop="clearReverseImage">✕</button>
        </div>

        <!-- 提示词输入框 -->
        <div class="rp-prompt-row">
          <label class="rp-label">补充提示词</label>
          <el-input
            v-model="reversePromptInput"
            type="textarea"
            :rows="6"
            maxlength="1000"
            show-word-limit
          />
        </div>

        <!-- 结果区 -->
        <div v-if="reverseResult" class="rp-result-area">
          <div class="rp-result-header">
            <span class="rp-label">AI 推理结果</span>
            <el-button link type="primary" size="small" @click="copyResult(reverseResult)">
              <el-icon><DocumentCopy /></el-icon> 复制
            </el-button>
          </div>
          <div class="rp-result-box">{{ reverseResult }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="reverseDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="reverseLoading" :disabled="!reverseImageFile" @click="submitReversePrompt">
          {{ reverseLoading ? '推理中…' : '发送推理' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 右键接力菜单：将白底结果放入「白底生成背景 / 产品精修」 -->
    <div
      v-if="handoffMenu.show"
      class="context-menu"
      :style="{ left: handoffMenu.x + 'px', top: handoffMenu.y + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="sendToBackground">
        <el-icon><PictureFilled /></el-icon>放入「白底生成背景」
      </div>
      <div class="context-menu-item" @click="sendToRetouch">
        <el-icon><MagicStick /></el-icon>放入「产品精修」
      </div>
      <div class="context-menu-item context-menu-cancel" @click="hideHandoffMenu">取消</div>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'WhiteBg' })
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import AiAssistant from '@/components/AiAssistant.vue'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
import { useUserStore } from '@/store'
import { useImageHandoffStore } from '@/store'
import { getPublicCreationConfigByGroup } from '@/api/customer'
import { reversePrompt } from '@/api/customer'
import { getImageUrl } from '@/utils/image'

import { Plus, Delete, ArrowLeft, ArrowRight, ArrowDown, Download, Right, UploadFilled, Coin, MagicStick, Loading, DocumentCopy, PictureFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const handoffStore = useImageHandoffStore()
const gen = useImageGeneration('white_bg')
const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

const fileInput = ref(null)
const originalImage = ref('')
const originalFile = ref(null)
const selectedStyle = ref('no-shadow')
const outputSize = ref('')
const customWidth = ref(1000)
const customHeight = ref(1000)
const language = ref('zh-CN')
const zoomLevel = ref(100)

// ===== 提示词增强（从 gh_prompt_library 拉取的选项） =====
const boostProduct = ref('')
const boostMaterial = ref('')
const boostCameraDistance = ref('')
const boostCameraOccupation = ref('')
const boostProductRef = ref(null)
const boostMaterialRef = ref(null)
const boostCameraDistanceRef = ref(null)
const boostCameraOccupationRef = ref(null)

const aiAssistantRef = ref(null)

// ===== 反推提示词 =====
const reverseDialogVisible = ref(false)
const reverseImageFile = ref(null)
const reverseImagePreview = ref('')
const reverseResult = ref('')
const reverseLoading = ref(false)
const reverseFileInput = ref(null)
const REVERSE_DEFAULT_PROMPT = `请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。 输出结果应具有高度可复用性，能直接用于引导图像生成。`
const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)

// ===== 上传进度 =====
const uploadProgress = ref(0)


// ===== 对比视图 =====
const comparePosition = ref(50)

// 标点管理
const markers = ref([])
const selectedMarker = ref(null)
let markerIdCounter = 0

// 右键菜单状态
const contextMenu = reactive({
  show: false,
  x: 0,
  y: 0,
  clickX: 0, // 保存点击位置的X坐标（百分比）
  clickY: 0  // 保存点击位置的Y坐标（百分比）
})

// ===== 图片接力右键菜单：将白底结果放入「白底生成背景 / 产品精修」 =====
const handoffMenu = reactive({
  show: false,
  x: 0,
  y: 0,
  imageUrl: ''
})

function openHandoffMenu(e, imageUrl) {
  if (!imageUrl) return
  e.preventDefault()
  handoffMenu.imageUrl = imageUrl
  // 靠近视口右侧时左移，避免菜单溢出
  const menuW = 200
  handoffMenu.x = e.clientX + menuW > window.innerWidth ? e.clientX - menuW : e.clientX
  handoffMenu.y = e.clientY
  handoffMenu.show = true
}

function hideHandoffMenu() {
  handoffMenu.show = false
  handoffMenu.imageUrl = ''
}

function sendToBackground() {
  if (!handoffMenu.imageUrl) return
  handoffStore.setImage(handoffMenu.imageUrl, { from: 'white_bg', to: 'background' })
  hideHandoffMenu()
  router.push('/whiteToBg')
}

function sendToRetouch() {
  if (!handoffMenu.imageUrl) return
  handoffStore.setImage(handoffMenu.imageUrl, { from: 'white_bg', to: 'retouch' })
  hideHandoffMenu()
  router.push('/refine')
}

// Config panel collapsed state
const configCollapsed = ref(false)

// Collapsible sections - only config sections, no AI
const sections = reactive({
  upload: true,
  style: true,
  size: false,
  language: false,
  promptBoost: false
})

// Panel widths - 画布自动占满剩余空间，右侧栏宽度=配置栏+AI栏+分隔线
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const canvasFlex = computed(() => '1 1 0%')
const rightFlex = computed(() => {
  const configW = configCollapsed.value ? 40 : _configWidthPx.value
  return `0 0 ${configW + _aiWidthPx.value + 12}px`
})
const configFlex = computed(() => {
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${_configWidthPx.value}px`
})
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)

let isResizing = false
let resizeTarget = ''
const aiPanel = ref(null)

// All expanded state - only config sections
const allExpanded = computed(() => {
  return sections.upload && sections.style && sections.size && sections.language && sections.promptBoost
})

function toggleAllSections() {
  const newState = !allExpanded.value
  sections.upload = newState
  sections.style = newState
  sections.size = newState
  sections.language = newState
  sections.promptBoost = newState
}

// ===== 4种白底样式 =====
const shadowStyles = ref([
  { label: '无阴影', value: 'no-shadow', image: '/images/chair-white-bg.png' },
  { label: '自然阴影', value: 'natural-shadow', image: '/images/chair-scene-bg.png' },
  { label: '柔和阴影', value: 'soft-shadow', image: '/images/chair-white-bg.png' },
  { label: '硬阴影', value: 'hard-shadow', image: '/images/chair-scene-bg.png' }
])

const sizeOptions = ref([
  { label: '不指定尺寸', value: '' },
  { label: '1:1（800×800）', value: '800:800' },
  { label: '3:4（800×1067）', value: '800:1067' },
  { label: '4:3（1067×800）', value: '1067:800' },
  { label: '自定义', value: 'custom' }
])

const languages = ref([
  { label: '中文（简体）', value: 'zh-CN' },
  { label: '英语（美国）', value: 'en-US' },
  { label: '英语（英国）', value: 'en-GB' },
  { label: '日语', value: 'ja-JP' },
  { label: '韩语', value: 'ko-KR' },
  { label: '德语', value: 'de-DE' },
  { label: '法语', value: 'fr-FR' },
  { label: '西班牙语', value: 'es-ES' },
])

const resultImages = computed(() => gen.resultImages.value)
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)
const genError = computed(() => gen.error.value)
const hasResult = computed(() => resultImages.value.length > 0)
const resultImageUrl = computed(() => {
  const img = resultImages.value[0]
  if (!img) return ''
  return img.url || img
})

// ===== 画布交互浮层 =====
// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   getImage: () => resultImageUrl.value || originalImage.value,
//   defaultName: '白底图'
// })
const canvasTransform = computed(() => `scale(${zoomLevel.value / 100})`)

// ===== 积分相关 =====
const userPoints = computed(() => userStore.userInfo.points || 0)
const consumePoints = ref(2)
const hasEnoughPoints = computed(() => userPoints.value >= consumePoints.value)
const canGenerate = computed(() => !!originalFile.value && !isGenerating.value)

// 实际输出尺寸
const effectiveOutputSize = computed(() => {
  if (outputSize.value === 'custom') {
    return `${customWidth.value}x${customHeight.value}`
  }
  return outputSize.value
})

function toggleSection(key) {
  sections[key] = !sections[key]
}

function triggerUpload() { fileInput.value?.click() }

function handleDrop(e) {
  const file = e.dataTransfer?.files[0]
  if (file) validateAndRead(file)
}

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) validateAndRead(file)
  e.target.value = ''
}

// ===== 文件校验 =====
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_FILE_SIZE = 7 * 1024 * 1024 // 7MB

function validateAndRead(file) {
  // 格式校验
  if (!ALLOWED_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG / WebP 格式的图片')
    return
  }
  // 大小校验
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('图片大小不能超过 7MB')
    return
  }
  readFile(file)
}

function readFile(file) {
  originalFile.value = file
  const reader = new FileReader()

  // 读取进度（模拟真实上传进度）
  uploadProgress.value = 5
  reader.onprogress = (e) => {
    if (e.lengthComputable) {
      const pct = Math.round((e.loaded / e.total) * 90)
      uploadProgress.value = Math.max(5, Math.min(90, pct))
    }
  }
  reader.onload = (ev) => {
    uploadProgress.value = 100
    originalImage.value = ev.target.result
    // 进度完成后延时隐藏
    setTimeout(() => {
      uploadProgress.value = 0
    }, 400)
  }
  reader.onerror = () => {
    uploadProgress.value = 0
    ElMessage.error('图片读取失败，请重试')
  }
  reader.readAsDataURL(file)
}

function removeFile() {
  originalFile.value = null
  originalImage.value = ''
}

function formatFileSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// ===== 生成 =====
async function handleGenerate() {
  if (!originalFile.value) return
  if (!hasEnoughPoints.value) {
    ElMessage.warning('积分不足，请先充值')
    goRecharge()
    return
  }
  try {
    const styleText = stylePromptMap[selectedStyle.value] || '生成干净无阴影的白底图'
    const boostText = [
      boostProductRef.value?.getSelectedItems()[0]?.promptText,
      boostMaterialRef.value?.getSelectedItems()[0]?.promptText,
      boostCameraDistanceRef.value?.getSelectedItems()[0]?.promptText,
      boostCameraOccupationRef.value?.getSelectedItems()[0]?.promptText
    ].filter(Boolean).join('；')
    // 尺寸为可选项：未选择时不发尺寸给 AI
    const sizeText = effectiveOutputSize.value ? `输出图片尺寸为 ${effectiveOutputSize.value}，` : ''
    const prompt = `${styleText}。${sizeText}图片上的文字使用${languageTextMap[language.value] || '中文'}。${boostText ? '约束：' + boostText + '。' : ''}`
    const extraOptions = { shadow_style: selectedStyle.value, language: language.value }
    if (effectiveOutputSize.value) extraOptions.output_size = effectiveOutputSize.value
    await gen.fullGenerate(
      [originalFile.value],
      prompt,
      { extraOptions, consumePoints: consumePoints.value, featureName: 'white_bg', title: 'AI白底图生成' }
    )
    // 复位对比位置
    comparePosition.value = 50
} catch (e) {
if (e?.message?.includes('已取消')) return
console.error('白底图生成失败:', e)
  }
}

const stylePromptMap = {
  'no-shadow': '生成干净无阴影的白底图',
  'natural-shadow': '生成带自然投影的白底图，阴影自然柔和',
  'soft-shadow': '生成带柔和渐变阴影的白底图，阴影过渡平滑',
  'hard-shadow': '生成带硬朗阴影的白底图，阴影边缘清晰对比强烈'
}

const languageTextMap = {
  'zh-CN': '中文',
  'en-US': '英文',
  'en-GB': '英文',
  'ja-JP': '日文',
  'ko-KR': '韩文',
  'de-DE': '德文',
  'fr-FR': '法文',
  'es-ES': '西班牙文'
}

// ===== 反推提示词：打开/上传/提交 =====
function openReversePromptDialog() {
  reverseDialogVisible.value = true
}

function triggerReverseUpload() {
  if (reverseImagePreview.value) return
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp'
  input.onchange = (e) => {
    const file = e.target.files?.[0]
    if (file) handleReverseFile(file)
    e.target.value = ''
  }
  input.click()
}

function handleReverseDrop(e) {
  const file = e.dataTransfer?.files?.[0]
  if (file) handleReverseFile(file)
}

const REVERSE_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const REVERSE_MAX_SIZE = 7 * 1024 * 1024

function handleReverseFile(file) {
  if (!REVERSE_ALLOWED_TYPES.includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG / WebP 格式的图片')
    return
  }
  if (file.size > REVERSE_MAX_SIZE) {
    ElMessage.error('图片大小不能超过 7MB')
    return
  }
  reverseImageFile.value = file
  reverseResult.value = ''
  const reader = new FileReader()
  reader.onload = (ev) => { reverseImagePreview.value = ev.target.result }
  reader.readAsDataURL(file)
}

function clearReverseImage() {
  reverseImageFile.value = null
  reverseImagePreview.value = ''
  reverseResult.value = ''
}

async function submitReversePrompt() {
  if (!reverseImageFile.value) {
    ElMessage.warning('请先上传一张图片')
    return
  }
  reverseLoading.value = true
  reverseResult.value = ''
  try {
    const imageDataUri = reverseImagePreview.value
    const prompt = reversePromptInput.value?.trim()
      ? reversePromptInput.value.trim()
      : REVERSE_DEFAULT_PROMPT
    const res = await reversePrompt({ image: imageDataUri, prompt })
    const data = res?.data || res
    const result = typeof data === 'string' ? data : (data?.prompt || data?.result || '')
    reverseResult.value = result || 'AI 未返回文本结果'
    ElMessage.success('推理完成')
  } catch (e) {
    console.error('反推提示词失败:', e)
    ElMessage.error(e?.message || '反推提示词失败，请重试')
  } finally {
    reverseLoading.value = false
  }
}

async function copyResult(text) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    // 降级方案
    const ta = document.createElement('textarea')
    ta.value = text
    document.body.appendChild(ta)
    ta.select()
    try { document.execCommand('copy'); ElMessage.success('已复制到剪贴板') }
    catch { ElMessage.error('复制失败，请手动选择文本复制') }
    document.body.removeChild(ta)
  }
}

// ===== 下载结果 =====
async function downloadResult() {
  const url = resultImageUrl.value
  if (!url) return
  try {
    const res = await fetch(url)
    const blob = await res.blob()
    const objectUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = objectUrl
    a.download = `白底图_${Date.now()}.png`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(objectUrl)
    ElMessage.success('已开始下载')
  } catch (e) {
    // 跨域失败时退化为新窗口打开
    window.open(url, '_blank')
  }
}

// ===== 进入下一步 =====
function goNextStep() {
  router.push('/whiteToBg')
}

// ===== 积分充值 =====
function goRecharge() {
  router.push('/points-center')
}

// ===== 对比视图分割线拖动 =====
function initCompareDrag() {
  const container = document.querySelector('.compare-container')
  if (!container) return

  const moveHandler = (e) => {
    const rect = container.getBoundingClientRect()
    if (rect.width <= 0) return
    const pct = ((e.clientX - rect.left) / rect.width) * 100
    comparePosition.value = Math.max(0, Math.min(100, pct))
  }

  const upHandler = () => {
    document.removeEventListener('mousemove', moveHandler)
    document.removeEventListener('mouseup', upHandler)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  document.addEventListener('mousemove', moveHandler)
  document.addEventListener('mouseup', upHandler)
  document.body.style.cursor = 'ew-resize'
  document.body.style.userSelect = 'none'
}


// ===== 右键菜单和标点功能 =====

// 处理画布右键点击
function handleCanvasRightClick(e) {
  if (!originalImage.value) return
  
  // 防止浏览器默认右键菜单
  e.preventDefault()
  
  // 获取画布区域的位置信息
  const canvasBox = document.querySelector('.upload-preview')
  if (!canvasBox) return
  
  const rect = canvasBox.getBoundingClientRect()
  
  // 计算点击位置相对于画布的百分比坐标
  const x = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100))
  const y = Math.max(0, Math.min(100, ((e.clientY - rect.top) / rect.height) * 100))
  
  // 保存点击位置
  contextMenu.clickX = x
  contextMenu.clickY = y
  
  // 设置右键菜单位置
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.show = true
  
  // 隐藏选中的标点
  selectedMarker.value = null
}

// 处理标点的右键点击
function handleMarkerRightClick(e, marker) {
  e.preventDefault()
  e.stopPropagation()
  
  // 显示删除标点的菜单选项
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.show = true
  
  // 设置当前选中的标点
  selectedMarker.value = marker
}

// 添加标点
function addMarker() {
  if (!originalImage.value) return
  
  markerIdCounter++
  const newMarker = {
    id: markerIdCounter,
    x: contextMenu.clickX,
    y: contextMenu.clickY,
    label: `标点${markerIdCounter}`
  }
  
  markers.value.push(newMarker)
  selectedMarker.value = newMarker
  
  // 隐藏右键菜单
  hideContextMenu()
}

// 清空所有标点
function clearAllMarkers() {
  markers.value = []
  selectedMarker.value = null
  hideContextMenu()
}

// 选择标点
function selectMarker(marker) {
  selectedMarker.value = marker
}

// 隐藏右键菜单
function hideContextMenu() {
  contextMenu.show = false
  contextMenu.x = 0
  contextMenu.y = 0
}

// 点击页面其他地方隐藏右键菜单
function handleClickOutside(e) {
  if (contextMenu.show && !e.target.closest('.context-menu')) {
    hideContextMenu()
  }
  if (handoffMenu.show && !e.target.closest('.context-menu')) {
    hideHandoffMenu()
  }
}

// ===== Column resize logic (like Retouch.vue) =====
function startColResize(e, target) {
  isResizing = true
  resizeTarget = target
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  e.preventDefault()
}

function onMouseMove(e) {
  if (!isResizing) return
  const threeCol = document.querySelector('.three-col')
  if (!threeCol) return
  const rect = threeCol.getBoundingClientRect()

  if (resizeTarget === 'right' || resizeTarget === 'config') {
    // 画布/右侧栏分隔线：按比例缩放配置栏和AI栏宽度
    const rightWidth = rect.right - e.clientX - 24
    const totalCurrent = _configWidthPx.value + _aiWidthPx.value + 12
    if (totalCurrent > 0 && rightWidth > 200) {
      const ratio = rightWidth / totalCurrent
      _configWidthPx.value = Math.max(150, Math.min(600, Math.round(_configWidthPx.value * ratio)))
      _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(_aiWidthPx.value * ratio)))
    }
  } else if (resizeTarget === 'ai') {
    // 配置栏/AI栏分隔线：只调整AI栏宽度，配置栏宽度不变
    const rightCol = document.querySelector('.right-col')
    if (!rightCol) return
    const rightRect = rightCol.getBoundingClientRect()
    const rightX = e.clientX - rightRect.left
    const configW = configCollapsed.value ? 40 : _configWidthPx.value
    const aiWidth = rightRect.width - rightX - 6
    _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(aiWidth)))
  }
}

function onMouseUp() {
  if (isResizing) {
    isResizing = false
    resizeTarget = ''
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
}

function startRightPanelResize(e, target) {
  isResizing = true
  resizeTarget = target
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  e.preventDefault()
}

// function zoomIn() { if (zoomLevel.value < 200) zoomLevel.value += 10 }
// function zoomOut() { if (zoomLevel.value > 25) zoomLevel.value -= 10 }

// 监听生成结果变化，自动滚动到底部
function watchResult() {
  // 生成完成后自动跳转到结果对比
  if (hasResult.value && !isGenerating.value) {
    nextTick(() => {
      const container = document.querySelector('.compare-container')
      if (container) {
        container.addEventListener('mousedown', (e) => {
          if (e.target.closest('.compare-divider') || e.target.closest('.compare-handle')) {
            initCompareDrag()
          }
        })
      }
    })
  }
}

const defaultShadowImages = {
  'no-shadow': '/images/chair-white-bg.png',
  'natural-shadow': '/images/chair-scene-bg.png',
  'soft-shadow': '/images/chair-white-bg.png',
  'hard-shadow': '/images/chair-scene-bg.png'
}

async function loadCreationConfig() {
  try {
    const res = await getPublicCreationConfigByGroup('white_bg')
    const list = res.data || res.rows || []
    const map = new Map(list.map(c => [c.configKey, c]))

    const shadowCfg = map.get('shadow_styles') || map.get('config')
    if (shadowCfg?.configValue) {
      try {
        const items = JSON.parse(shadowCfg.configValue)
        if (Array.isArray(items)) {
          shadowStyles.value = items.map(s => ({
            ...s,
            image: s.image || defaultShadowImages[s.value] || ''
          })).filter(s => s.value)
        }
      } catch { /* use defaults */ }
    }

    // ---- 输出尺寸 ----
    const sizeCfg = map.get('size_options') || map.get('size_presets') || map.get('output_sizes')
    if (sizeCfg && sizeCfg.configValue) {
      const arr = JSON.parse(sizeCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        const loaded = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
        // Ensure 'custom' option is always present
        const hasCustom = loaded.some(s => s.value === 'custom')
        if (!hasCustom) {
          loaded.push({ label: '自定义', value: 'custom' })
        }
        sizeOptions.value = [{ label: '不指定尺寸', value: '' }, ...loaded]
      }
    }

    const langCfg = map.get('languages') || map.get('config')
    if (langCfg?.configValue) {
      try {
        const items = JSON.parse(langCfg.configValue)
        if (Array.isArray(items)) {
          languages.value = items.filter(l => l.value)
        }
      } catch { /* use defaults */ }
    }
  } catch { /* use defaults */ }
}

onMounted(async () => {
  await loadCreationConfig()
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  document.addEventListener('click', handleClickOutside)
  // 根据容器宽度初始化创作配置栏和 AI 对话栏的像素宽度
  nextTick(() => {
    const rightCol = document.querySelector('.right-col')
    if (rightCol) {
      const w = rightCol.getBoundingClientRect().width
      _configWidthPx.value = Math.round(w * 0.35)
      _aiWidthPx.value = Math.round(w * 0.55)
    }
  })

  // 刷新积分
  userStore.fetchPoints().catch(() => {})
  // 监听结果变化
  watchResult()
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('click', handleClickOutside)
})

// 监听结果变化（computed 会在渲染后更新）
watch(() => [resultImages.value.length, isGenerating.value], () => {
  if (hasResult.value && !isGenerating.value) {
    nextTick(() => {
      const container = document.querySelector('.compare-container')
      if (container) {
        const handleMousedown = (e) => {
          if (e.target.closest('.compare-divider') || e.target.closest('.compare-handle')) {
            initCompareDrag()
          }
        }
        container.addEventListener('mousedown', handleMousedown)
      }
    })
  }
})

function clearWorkspaceImages() {
originalImage.value = ''
originalFile.value = null
outputSize.value = ''
customWidth.value = 1000
customHeight.value = 1000
language.value = 'zh-CN'
gen.reset()
}
</script>

<style scoped>
/* ============================================================
   Layout
   ============================================================ */
.prompt-boost-row {
  margin-bottom: 10px;
}
.prompt-boost-row .boost-label {
  display: block;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
}

.workspace-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}


/* ---- Steps Bar ---- */
.steps-bar {
  display: flex;
  align-items: center;
  padding: 0 0 12px;
  background: transparent;
  flex-shrink: 0;
  overflow-x: auto;
  gap: 0;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6B7280;
  white-space: nowrap;
  cursor: pointer;
}
.step-item.active { color: #2563FF; font-weight: 600; }
.step-num {
  width: 22px; height: 22px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600;
  border: 2px solid #E8EDF5;
  flex-shrink: 0;
}
.step-item.active .step-num {
  background: #2563FF; color: #fff; border-color: #2563FF;
}
.step-item.done { color: #22C55E; }
.step-item.done .step-num {
  background: #22C55E; color: #fff; border-color: #22C55E;
}
.step-line {
  flex: 1; height: 2px; background: #E8EDF5; min-width: 12px; margin: 0 6px;
}
.step-line.done { background: #22C55E; }

/* ---- Three Column ---- */
.three-col {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ---- Column Divider + Toggle Wrapper ---- */
.col-divider-wrapper {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  width: 24px;
}

/* ---- Column Divider ---- */
.col-divider {
  width: 6px;
  height: 100%;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  transition: background 0.2s;
  
}
.col-divider:hover,
.col-divider:active { background: #2563FF; }

/* ---- Column Toggle Button (between canvas and config) ---- */
.col-divider-wrapper .config-toggle-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 20px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #E8EDF5;
  border-radius: 8px 0 0 8px;
  cursor: pointer;
  color: #9CA3AF;
  box-shadow: -2px 0 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease;
}
.col-divider-wrapper .config-toggle-btn.active {
  color: #2563FF;
  border-color: #2563FF;
}
.col-divider-wrapper .config-toggle-btn:hover {
  background: #F0F4FF;
  color: #2563FF;
  border-color: #2563FF;
}

/* ---- Right Panel Divider ---- */
.right-panel-divider {
  width: 6px;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  transition: background 0.2s;
  
}
.right-panel-divider:hover,
.right-panel-divider:active { background: #2563FF; }

/* ============================================================
   Canvas Column
   ============================================================ */
.canvas-col {
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
  background: #F7F9FC;
  min-width: 0;
}

.canvas-box {
  flex: 1;
  border: 2px dashed #E8EDF5;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 0;
  position: relative;
}

.canvas-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #9CA3AF;
  padding: 24px;
  text-align: center;
}
.canvas-placeholder svg {
  width: 48px;
  height: 48px;
  margin-bottom: 4px;
  opacity: .4;
}
.canvas-placeholder h3 { font-size: 14px; color: #6B7280; margin-bottom: 0; font-weight: 500; }
.canvas-placeholder p { font-size: 12px; color: #9CA3AF; margin: 0; }

.upload-zone {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
}

.upload-preview {
  position: relative;
  max-width: 80%;
  max-height: 80%;
  transform-origin: center center;
  transition: transform 0.1s ease;
}
.preview-img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
}
.preview-overlay { position: absolute; top: 8px; right: 8px; }
.preview-del-btn {
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(0,0,0,0.5); color: #fff; border: none;
  cursor: pointer; font-size: 14px; display: flex; align-items: center; justify-content: center;
}
.preview-del-btn:hover { background: #EF4444; }

/* ---- 上传进度条 ---- */
.upload-progress-layer {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.88);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}
.upload-progress-box {
  width: 280px;
  text-align: center;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.upload-progress-text {
  font-size: 13px;
  color: #6B7280;
  margin: 8px 0 12px;
}

/* ---- 对比视图 ---- */
.compare-view {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
}
.compare-container {
  position: relative;
  width: 100%;
  max-width: 520px;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 10px;
  border: 1px solid #E8EDF5;
  user-select: none;
  cursor: default;
  background: #fff;
  transform-origin: center center;
  transition: transform 0.1s ease;
}
.compare-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #fff;
}
.compare-result { z-index: 1; }
.compare-original { z-index: 2; }
.compare-divider {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #2563FF;
  z-index: 3;
  cursor: ew-resize;
  box-shadow: 0 0 4px rgba(37, 99, 255, 0.4);
}
.compare-handle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #2563FF;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(37, 99, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1px;
  cursor: ew-resize;
}
.compare-arrow {
  color: #fff;
  font-size: 13px;
  line-height: 1;
  font-weight: 700;
}
.compare-label {
  position: absolute;
  top: 10px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  z-index: 4;
  pointer-events: none;
}
.label-original {
  left: 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.label-result {
  right: 10px;
  background: rgba(37, 99, 255, 0.85);
  color: #fff;
}
.compare-bar {
  margin-top: 12px;
  font-size: 11px;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ---- 操作按钮条 ---- */
.action-bar {
  display: flex;
  gap: 10px;
  padding: 10px 0 0;
  flex-shrink: 0;
}
.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.action-bar .el-button--primary {
  background: #2563FF;
  border-color: #2563FF;
}

/* 标点显示层 */
.markers-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.marker-point {
  position: absolute;
  pointer-events: auto;
  transform: translate(-50%, -50%);
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;
}

.marker-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #EF4444, #DC2626);
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #fff;
  font-size: 10px;
  animation: markerPulse 2s infinite;
}

.marker-point.active .marker-dot {
  background: linear-gradient(135deg, #F59E0B, #D97706);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.5);
  transform: scale(1.2);
}

.marker-label {
  position: absolute;
  top: -28px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  white-space: nowrap;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.marker-point:hover .marker-label,
.marker-point.active .marker-label {
  opacity: 1;
}

@keyframes markerPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 2px 12px rgba(239, 68, 68, 0.6);
  }
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 4px;
  min-width: 140px;
  animation: menuFadeIn 0.2s ease-out;
  border: 1px solid #E8EDF5;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  font-weight: 400;
}

.context-menu-item:hover {
  background: #F3F4F6;
  color: #2563FF;
}

.context-menu-item .el-icon {
  font-size: 16px;
  color: #6B7280;
  transition: color 0.2s;
}

.context-menu-item:hover .el-icon {
  color: #2563FF;
}

.canvas-bottom-bar {
  padding: 8px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
}
.canvas-link { color: #2563FF; cursor: pointer; text-decoration: none; &:hover { text-decoration: underline; } }
.result-status {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #22C55E;
  font-weight: 500;
}
.result-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22C55E;
  display: inline-block;
}

/* ============================================================
   Right Column (Config + AI)
   ============================================================ */
.right-col {
  display: flex;
  background: #fff;
  min-width: 0;
  overflow: hidden;
}

/* ============================================================
   Config Column
   ============================================================ */
.config-col {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  min-width: 0;
  position: relative;
  transition: flex 0.3s;
}

/* Collapsed state */
.config-col.collapsed {
  flex: 0 0 40px !important;
  min-width: 40px;
}

.config-inner {
  padding: 0 0 16px;
}

.panel-header {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 14px 16px 0;
}

.panel-toggle-all {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: all 0.2s;
}
.panel-toggle-all.active {
  color: var(--gh-primary, #2563FF);
}
.panel-toggle-all:hover { opacity: 0.7; }

.config-section.collapsible {
  margin-bottom: 0;
  border-bottom: 1px solid #F3F4F6;
}

.section-header.collapsible {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 10px 16px;
  transition: opacity 0.2s;
}
.section-header:hover { opacity: 0.75; }
.section-label { font-size: 13px; font-weight: 500; color: #1F2937; }
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }
.expand-text {
  font-size: 11px;
  color: #9CA3AF;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 2px;
}

.expand-arrow {
  transition: transform 0.25s;
}
.expand-arrow.expanded { transform: rotate(180deg); }

.section-body { padding: 4px 16px 8px; }

/* ---- 积分卡片 ---- */
.points-card {
  margin: 0 16px 12px;
  padding: 10px 14px;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.points-card.insufficient {
  background: #FEF2F2;
  border-color: #FECACA;
}
.points-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.points-label {
  font-size: 12px;
  color: #6B7280;
}
.points-value {
  font-size: 15px;
  font-weight: 700;
  color: #D97706;
}
.points-card.insufficient .points-value { color: #EF4444; }
.points-recharge {
  font-size: 12px;
  color: #2563FF;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
}
.points-recharge:hover { opacity: 0.7; }

/* Upload zone in panel */
.panel-upload-zone {
  border: 2px dashed #E8EDF5;
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  background: #F7F9FC;
  transition: all 0.2s;
}
.panel-upload-zone:hover { border-color: #2563FF; background: #FAFBFF; }
.panel-upload-text { font-size: 12px; color: #6B7280; margin-top: 6px; }
.panel-upload-hint { font-size: 10px; color: #9CA3AF; margin-top: 2px; }

.uploaded-file-info {
  display: flex; align-items: center; gap: 8px;
  padding: 8px; margin-top: 8px;
  background: #F7F9FC; border-radius: 8px; border: 1px solid #F3F4F6;
}
.file-preview { width: 36px; height: 36px; border-radius: 6px; overflow: hidden; background: #E5E7EB; display: flex; align-items: center; justify-content: center; flex-shrink: 0; .file-thumb { width: 100%; height: 100%; object-fit: cover; } }
.file-detail { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 1px;
  .file-name { font-size: 11px; font-weight: 500; color: #1F2937; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .file-size { font-size: 10px; color: #9CA3AF; }
}
.file-remove { color: #9CA3AF; cursor: pointer; flex-shrink: 0; transition: color 0.2s; &:hover { color: #EF4444; } }

/* Style cards（4种样式，预览图参考仓库椅子图） */
.style-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.style-card {
  border: 2px solid #EBEDF5;
  border-radius: 10px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
  &.active { border-color: var(--gh-primary, #2563FF); background: #EEF2FF; }
  .style-preview {
    width: 100%;
    height: 64px;
    margin: 0 auto 6px;
    border-radius: 6px;
    background: #fff;
    display: flex; align-items: center; justify-content: center;
    position: relative;
    overflow: hidden;
  }
  .style-preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
  .style-name { font-size: 12px; font-weight: 500; color: #1F2937; }
}

.section-helper { font-size: 11px; color: #9CA3AF; margin-top: 8px; }

/* ---- 尺寸选择（下拉框） ---- */
.custom-size-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #E8EDF5;
}
.custom-size-input {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.custom-size-input span {
  font-size: 11px;
  color: #6B7280;
}
.custom-size-x {
  color: #9CA3AF;
  font-size: 13px;
  padding-top: 14px;
}

/* Generate area（配置面板内） */
.generate-area {
  padding: 16px 0 12px;
  border-bottom: 1px solid #F3F4F6;
  margin-bottom: 12px;
}
.generate-btn { width: 100%; }
.generate-progress { margin-top: 10px; }
.gen-status-text { font-size: 11px; color: #6B7280; display: block; margin-top: 4px; }
.gen-error { color: #EF4444; font-size: 12px; margin-top: 6px; }

.result-area { padding-top: 4px; }
.result-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 8px; }
.result-item {
  border-radius: 6px; overflow: hidden; aspect-ratio: 1; background: #F3F4F6; cursor: pointer;
  img { width: 100%; height: 100%; object-fit: cover; }
}

/* ============================================================
   AI Column（保持不变）
   ============================================================ */
.ai-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  padding: 16px;
  overflow: hidden;
  min-width: 240px;
}

/* ============================================================
   底部生成 CTA 按钮
   ============================================================ */
.generate-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 24px;
  background: #fff;
  border-top: 1px solid #E8EDF5;
  flex-shrink: 0;
  gap: 16px;
}
.gen-bar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 220px;
  padding: 12px 32px;
  background: linear-gradient(135deg, #2563FF, #1D4ED8);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(37, 99, 255, 0.3);
}
.gen-bar-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 255, 0.4);
}
.gen-bar-btn:disabled {
  background: #D1D5DB;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.7;
}
.gen-bar-btn .el-icon.is-loading {
  animation: rotating 1.2s linear infinite;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ============================================================
   Responsive
   ============================================================ */
@media (max-width: 1024px) {
  .steps-bar { padding: 0 0 8px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 8px; margin: 0 4px; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .right-col { flex: 0 0 100% !important; max-height: 50vh; }
  .col-divider { display: none; }
  .generate-bar { padding: 10px 16px; flex-direction: column; gap: 10px; }
  .gen-bar-btn { width: 100%; }
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .right-col { flex: 1 1 auto !important; min-height: 250px; }
  .right-panel-divider { display: none; }
  .config-col { max-height: 200px; overflow-y: auto; }

}

/* ===== 反推提示词入口按钮 ===== */
.reverse-prompt-entry {
  margin: 0 0 12px 0;
}
.reverse-prompt-btn {
  width: 100%;
  justify-content: center;
}
.entry-helper {
  font-size: 12px;
  color: #9CA3AF;
  margin: 6px 0 0 0;
  text-align: center;
}

/* ===== 反推提示词模态框 ===== */
.reverse-prompt-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.rp-upload-zone {
  position: relative;
  border: 1px dashed #D1D5DB;
  border-radius: 8px;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  background: #FAFBFC;
  transition: border-color 0.2s;
}
.rp-upload-zone:hover {
  border-color: #2563FF;
}
.rp-upload-text {
  font-size: 14px;
  color: #4B5563;
  margin: 8px 0 0 0;
}
.rp-upload-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin: 4px 0 0 0;
}
.rp-preview-img {
  width: 100%;
  max-height: 320px;
  object-fit: contain;
  display: block;
}
.rp-clear-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rp-clear-btn:hover {
  background: #EF4444;
}
.rp-label {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  display: block;
}
.rp-prompt-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.rp-result-area {
  border-top: 1px solid #E5E7EB;
  padding-top: 12px;
}
.rp-result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.rp-result-box {
  background: #F3F4F6;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #1F2937;
  white-space: pre-wrap;
  max-height: 180px;
  overflow-y: auto;
}
</style>