<template>
  <div class="workspace-page">
    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Steps bar（显示在画布顶部，不超出画布区域） -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 3)">
              <div class="step-num">{{ i + 1 }}</div>
              <span class="step-label">{{ s.label }}</span>
            </div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>

        <!-- Canvas Area -->
        <div class="canvas-box">
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <!-- 未生成：显示空状态 -->
          <div v-if="resultImages.length === 0" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>AI 主图生成后将显示在此处</h3>
            <p>请在右侧配置生成参数并点击发送</p>
          </div>
          <!-- 有结果图时：2×2 网格展示 -->
          <div v-else class="result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, idx) in resultImages" :key="'r'+idx" class="result-card">
              <img :src="img.url || img" class="result-img" />
            </div>
          </div>
        </div>

        <!-- <div class="canvas-bottom-bar">AI生成的内容仅供参考，请注意核对细节与版权信息。</div> -->
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
        <!-- Right panel divider: config ⇔ AI -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'config')"></div>

        <!-- Config Panel -->
        <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
          <el-scrollbar v-show="!configCollapsed">
            <div class="config-inner">
              <!-- 创作配置 总折叠 -->
              <div class="panel-header" @click="toggleAllSections">
                <span class="panel-title">创作配置</span>
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

              <!-- Section: 上传图片 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('upload')">
                  <span class="section-label"><span class="required-mark">*</span>上传图片（最多10张）<span class="required-mark">（必填）</span></span>
                  <span class="expand-text">
                    {{ sections.upload ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.upload">
                  <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                    <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                    <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                    <p class="panel-upload-hint">支持 JPG / PNG，最多 10 张</p>
                  </div>
                  <div v-if="productFiles.length" class="uploaded-images-list">
                    <div v-for="(f, i) in productFiles" :key="i" class="uploaded-thumb-wrap">
                      <div class="uploaded-thumb">
                        <img :src="getObjectUrl(f)" />
                      </div>
                      <div class="uploaded-remove" @click.stop="removeProductFile(i)">✕</div>
                    </div>
                    <div
                      v-if="productFiles.length < 10"
                      class="uploaded-thumb-wrap add"
                      @click.stop="triggerUpload"
                    >
                      <div class="uploaded-thumb add-thumb">+</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 参考图（可选） -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('ref')">
                  <span class="section-label">参考图（可选）</span>
                  <span class="expand-text">
                    {{ sections.ref ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.ref }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.ref" class="section-body">
                  <div class="panel-upload-zone small" @click.stop="triggerRefUpload" @dragover.prevent @drop.prevent="handleRefDrop">
                    <el-icon :size="24" color="#9CA3AF"><PictureFilled /></el-icon>
                    <p class="panel-upload-hint">{{ refImage ? '已选择参考图' : '点击上传参考图' }}</p>
                  </div>
                  <div v-if="refImage" class="ref-preview-row">
                    <img :src="refImage" class="ref-thumb" />
                    <button class="ref-remove" @click.stop="refImage = ''">×</button>
                  </div>
                </div>
              </div>

              <!-- Section: 平台与语言 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('platform')">
                  <span class="section-label">目标平台</span>
                  <span class="expand-text">
                    {{ sections.platform ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.platform }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.platform" class="section-body">
                  <span class="config-label">目标平台</span>
                  <div class="option-tags">
                    <div
                      v-for="p in platforms"
                      :key="p.value"
                      class="option-tag"
                      :class="{ active: activePlatform === p.value }"
                      @click="activePlatform = (activePlatform === p.value ? '' : p.value)"
                    >{{ p.label }}</div>
                  </div>
                  <span class="config-label" style="margin-top:12px;">语言</span>
                  <select class="form-select full" v-model="language">
                    <option v-for="l in languages" :key="l.value" :value="l.value">{{ l.label }}</option>
                  </select>
                </div>
              </div>

              <!-- Section: 画布尺寸 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('size')">
                  <span class="section-label">输出尺寸</span>
                  <span class="expand-text">
                    {{ sections.size ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.size }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.size" class="section-body">
                  <el-select v-model="outputSize" placeholder="请选择输出尺寸" class="size-select">
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

              <!-- Section: 主图用途 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('purpose')">
                  <span class="section-label">主图用途</span>
                  <span class="expand-text">
                    {{ sections.purpose ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.purpose }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.purpose" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="p in purposes"
                      :key="p.value"
                      class="option-tag"
                      :class="{ active: activePurpose === p.value }"
                      @click="activePurpose = (activePurpose === p.value ? '' : p.value)"
                    >{{ p.label }}</div>
                  </div>
                </div>
              </div>

              <!-- Section: 核心卖点 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('sellingPoints')">
                  <span class="section-label">核心卖点</span>
                  <span class="expand-text">
                    {{ sections.sellingPoints ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.sellingPoints }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.sellingPoints" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="sp in sellingPoints"
                      :key="sp.value"
                      class="option-tag"
                      :class="{ active: activeSellingPoints.includes(sp.value) }"
                      @click="toggleSellingPoint(sp.value)"
                    >{{ sp.label }}</div>
                  </div>
                </div>
              </div>

              <!-- Section: 生成设置 -->
              <div class="config-section collapsible">
                <div class="section-header collapsible" @click="toggleSection('output')">
                  <span class="section-label">生成数量</span>
                  <span class="expand-text">
                    {{ sections.output ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.output }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.output" class="section-body">
                  <div class="gen-count-row">
                    <!-- <span class="config-label">生成数量</span> -->
                    <el-input-number v-model="generateCount" :min="1" :max="maxGenerateCount" size="small" controls-position="right" style="width: 120px" />
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- Divider inside right panel: config ⇔ AI -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'ai')"></div>

        <!-- AI Assistant column -->
        <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
          <AiAssistant
            ref="aiAssistantRef"
            :generate-fn="handleGenerate"
            :is-generating="isGenerating"
            :gen-status="genStatus"
            :gen-progress="genProgress"
            :gen-error="genError"
            :has-image="productFiles.length > 0"
            :on-clear-images="clearWorkspaceImages"
          />
        </div>
      </div>
    </div>

    <!-- Hidden file inputs -->
    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />
    <input type="file" ref="refFileInput" accept="image/*" hidden @change="handleRefFileSelect" />

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
        <div class="rp-upload-zone" @click="triggerReverseUpload" @dragover.prevent @drop.prevent="handleReverseDrop">
          <img v-if="reverseImagePreview" :src="reverseImagePreview" class="rp-preview-img" alt="" />
          <template v-else>
            <el-icon :size="36" color="#9CA3AF"><UploadFilled /></el-icon>
            <p class="rp-upload-text">点击或拖拽图片到此处</p>
            <p class="rp-upload-hint">支持 JPG/PNG/WebP，最多 20MB</p>
          </template>
          <button v-if="reverseImagePreview" class="rp-clear-btn" @click.stop="clearReverseImage">✕</button>
        </div>

        <div class="rp-prompt-row">
          <label class="rp-label">补充提示词</label>
          <el-input
            v-model="reversePromptInput"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可填写你想要的效果，如：电商主图、自然光、白色背景、高细节。不填则使用默认描述。"
          />
        </div>

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

  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ArrowDown, ArrowLeft, ArrowRight, UploadFilled, PictureFilled, MagicStick, DocumentCopy } from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { aiDialogue, getPublicCreationConfigByGroup, listPromptLibraryBatch, reversePrompt } from '@/api/customer'
import { ElMessage } from 'element-plus'

export default {
  name: 'HeroImageView',
  components: { ArrowDown, ArrowLeft, ArrowRight, UploadFilled, PictureFilled, MagicStick, DocumentCopy, PromptLibrarySelect, AiAssistant },
  setup() {
    // ---- Canvas Interactions ----
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-box',
    //   defaultName: 'hero-image',
    // })
    const gen = useImageGeneration('main_image')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

    // ---- State ----
    const configCollapsed = ref(false)
    const refImage = ref(null)
    const productFiles = ref([])
    const generating = ref(false)
    const zoom = ref(100)

    // Config sections
    const sections = reactive({
      upload: true,
      ref: false,
      platform: false,
      size: false,
      purpose: false,
      sellingPoints: false,
      output: false,
    })

    // Generation state from composable
    const resultImages = computed(() => gen.resultImages.value)
    const isGenerating = computed(() => gen.generating.value)
    const genProgress = computed(() => gen.progress.value)
    const genStatus = computed(() => gen.statusText.value)
    const genError = computed(() => gen.error.value)
    const aiAssistantRef = ref(null)

    // All expanded state
    const allExpanded = computed(() => {
      return sections.upload && sections.ref && sections.platform && sections.size && sections.purpose && sections.sellingPoints && sections.output
    })

    // 反推提示词
    const REVERSE_DEFAULT_PROMPT = '请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。输出结果应具有高度可复用性，能直接用于引导图像生成。'
    const reverseDialogVisible = ref(false)
    const reverseImageFile = ref(null)
    const reverseImagePreview = ref('')
    const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)
    const reverseResult = ref('')
    const reverseLoading = ref(false)

    // Platform & Language - 从后台创作配置读取，此处为默认值
    const platforms = ref(['亚马逊', 'Shopee', 'Lazada', '速卖通', '淘宝', '京东', '独立站', '其他'])
    const activePlatform = ref('')
    const languages = ref([
      { label: '英语（美国）', value: 'en-US' },
      { label: '英语（英国）', value: 'en-GB' },
      { label: '中文（简体）', value: 'zh-CN' },
      { label: '日语', value: 'ja-JP' },
      { label: '德语', value: 'de-DE' },
      { label: '法语', value: 'fr-FR' },
      { label: '西班牙语', value: 'es-ES' },
    ])
    const language = ref('en-US')

    // Size options - 从后台创作配置读取，此处为默认值
    const outputSize = ref('')
    const customWidth = ref(1000)
    const customHeight = ref(1000)
    const sizeOptions = ref([
      { label: '不指定尺寸', value: '' },
      { label: '1:1（800×800）', value: '800:800' },
      { label: '3:4（800×1067）', value: '800:1067' },
      { label: '4:3（1067×800）', value: '1067:800' },
      { label: '自定义', value: 'custom' }
    ])
    // 实际输出尺寸
    const effectiveOutputSize = computed(() => {
      if (outputSize.value === 'custom') {
        return `${customWidth.value}x${customHeight.value}`
      }
      return outputSize.value
    })

    // Purpose - 从后台创作配置读取，此处为默认值
    const purposes = ref(['新品上市', '提升转化', '季节/节日', '促销活动', '品牌宣传', '其他用途'])
    const activePurpose = ref('')

    // Selling points - 从后台创作配置读取，此处为默认值
    const sellingPoints = ref([
      '高品质材料', '耐用性强', '舒适体验', '易于安装',
      '多功能', '大容量收纳', '环保健康', '节省空间',
      '防水防污', '安全可靠', '轻便便携', '设计感强',
    ])
    const activeSellingPoints = ref([])

    // 提示词映射：option value → promptText（用于传给 AI）
    const promptMap = ref({})

    function toggleSellingPoint(value) {
      const idx = activeSellingPoints.value.indexOf(value)
      if (idx > -1) {
        activeSellingPoints.value.splice(idx, 1)
      } else if (activeSellingPoints.value.length < 3) {
        activeSellingPoints.value.push(value)
      }
    }

    // Generate count - 从后台创作配置读取上限，默认为5
    const maxGenerateCount = ref(5)
    const generateCountOptions = computed(() => {
      const max = maxGenerateCount.value
      const arr = []
      for (let i = 1; i <= max; i++) arr.push(i)
      return arr
    })
    const generateCount = ref(1)

    // Prompt
    const chatPrompt = ref('')
    const chatMessages = ref([])

    // AI model selection
    const selectedModel = ref('deepseek')
    const modelOptions = [
      { label: 'DeepSeek', value: 'deepseek' },
      { label: '通义千问 Qwen', value: 'qwen-plus' },
      { label: '智谱 GLM-4', value: 'glm-4' },
      { label: '豆包 Doubao', value: 'doubao' }
    ]

    // ---- Layout resize ----
    const _configWidthPx = ref(280)
    const _aiWidthPx = ref(360)
    const aiPanel = ref(null)
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
        const rightWidth = rect.right - e.clientX - 24
        const totalCurrent = _configWidthPx.value + _aiWidthPx.value + 12
        if (totalCurrent > 0 && rightWidth > 200) {
          const ratio = rightWidth / totalCurrent
          _configWidthPx.value = Math.max(150, Math.min(600, Math.round(_configWidthPx.value * ratio)))
          _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(_aiWidthPx.value * ratio)))
        }
      } else if (resizeTarget === 'ai') {
        const rightCol = document.querySelector('.right-col')
        if (!rightCol) return
        const rightRect = rightCol.getBoundingClientRect()
        const rightX = e.clientX - rightRect.left
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

    // AI panel independent resize
    let isAiResizing = false
    let aiStartX = 0
    let aiStartWidth = 0

    function startAiResize(e) {
      isAiResizing = true
      aiStartX = e.clientX
      const aiEl = aiPanel.value
      aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 360
      document.body.style.cursor = 'col-resize'
      document.body.style.userSelect = 'none'
      e.preventDefault()
      e.stopPropagation()
    }

    function onAiMouseMove(e) {
      if (!isAiResizing) return
      const delta = aiStartX - e.clientX
      let newWidth = aiStartWidth + delta
      newWidth = Math.max(240, Math.min(600, newWidth))
      _aiWidthPx.value = newWidth
    }

    function onAiMouseUp() {
      if (isAiResizing) {
        isAiResizing = false
        document.body.style.cursor = ''
        document.body.style.userSelect = ''
      }
    }

    // ===== 从后台创作配置读取主图设计配置 =====
    async function loadCreationConfig() {
      try {
        const res = await getPublicCreationConfigByGroup('main_image')
        const list = res.data || res.rows || []
        const map = {}
        list.forEach(c => { map[c.configKey] = c })

        // ---- 目标平台 ----
        const platformCfg = map.platform_options
        if (platformCfg && platformCfg.configValue) {
          const arr = JSON.parse(platformCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            platforms.value = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
          }
        }

        // ---- 画布尺寸 ----
        const sizeCfg = map.size_options || map.size_presets
        if (sizeCfg && sizeCfg.configValue) {
          const arr = JSON.parse(sizeCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            const loaded = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
            const hasCustom = loaded.some(s => s.value === 'custom')
            if (!hasCustom) {
              loaded.push({ label: '自定义', value: 'custom' })
            }
            sizeOptions.value = [{ label: '不指定尺寸', value: '' }, ...loaded]
          }
        }

        // ---- 主图用途 ----
        const purposeCfg = map.purposes
        if (purposeCfg && purposeCfg.configValue) {
          const arr = JSON.parse(purposeCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            purposes.value = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
          }
        }

        // ---- 核心卖点 ----
        const sellingCfg = map.selling_points
        if (sellingCfg && sellingCfg.configValue) {
          const arr = JSON.parse(sellingCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            sellingPoints.value = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
          }
        }

        // ---- 生成数量上限 ----
        const maxCountCfg = map.max_generate_count
        if (maxCountCfg && maxCountCfg.configValue) {
          const n = Number(JSON.parse(maxCountCfg.configValue))
          if (n > 0) maxGenerateCount.value = n
        }

        // ---- 语言列表 ----
        const langCfg = map.language_options
        if (langCfg && langCfg.configValue) {
          const arr = JSON.parse(langCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            languages.value = arr.filter(l => l.value)
          }
        }

        // 加载提示词映射：value → promptText
        await loadPromptMap()
      } catch { /* use defaults */ }
    }

    async function loadPromptMap() {
      try {
        const res = await listPromptLibraryBatch('opt_platform,opt_purpose,opt_selling', 'main_image')
        const items = res.data || res || []
        const map = {}
        items.forEach(item => {
          if (item.value && item.promptText) {
            map[item.value] = item.promptText
          }
        })
        promptMap.value = map
      } catch {
        promptMap.value = {}
      }
    }

    onMounted(() => {
      loadCreationConfig()
      document.addEventListener('mousemove', onMouseMove)
      document.addEventListener('mouseup', onMouseUp)
      document.addEventListener('mousemove', onAiMouseMove)
      document.addEventListener('mouseup', onAiMouseUp)
    })

    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
      document.removeEventListener('mousemove', onAiMouseMove)
      document.removeEventListener('mouseup', onAiMouseUp)
    })

    // ---- Methods ----
    const fileInput = ref(null)
    const refFileInput = ref(null)

    function triggerUpload() { fileInput.value?.click() }
    function triggerRefUpload() { refFileInput.value?.click() }

    function handleFileSelect(e) {
      const files = e.target.files
      if (files.length) addFiles(files)
      fileInput.value.value = ''
    }
    function handleRefFileSelect(e) {
      const files = e.target.files
      if (files.length) {
        refImage.value = URL.createObjectURL(files[0])
      }
      refFileInput.value.value = ''
    }
    function handleDrop(e) {
      const files = e.dataTransfer.files
      if (files.length) addFiles(files)
    }
    function handleRefDrop(e) {
      const files = e.dataTransfer.files
      if (files.length) {
        refImage.value = URL.createObjectURL(files[0])
      }
    }
    function addFiles(files) {
      for (const f of files) {
        productFiles.value.push(f)
      }
    }
    function clearImage() {
      productFiles.value = []
      resultImages.value = []
    }
    function removeProductFile(index) {
      productFiles.value.splice(index, 1)
    }
    function getObjectUrl(file) {
      return file instanceof File ? URL.createObjectURL(file) : file
    }

    function undo() { /* placeholder */ }
    function redo() { /* placeholder */ }
    function clearCanvas() { clearImage() }
    // function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
    // function zoomOut() { zoom.value = Math.max(10, zoom.value - 10) }
    function fitToScreen() { zoom.value = 100 }
    function toggleFullscreen() { /* placeholder */ }

    function toggleAllSections() {
      const val = !allExpanded.value
      Object.keys(sections).forEach(k => sections[k] = val)
    }

    function toggleSection(key) {
      if (sections.hasOwnProperty(key)) sections[key] = !sections[key]
    }

    function useSuggestion(text) {
      if (aiAssistantRef.value) aiAssistantRef.value.inputText = text
    }

    async function handleGenerate() {
      const text = aiAssistantRef.value?.inputText?.trim() || ''
      if (!productFiles.value.length) { ElMessage.warning('请先上传产品图片'); return }
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      generating.value = true
      try {
        const extra = { consumePoints: 2, featureName: 'main_image', title: '主图设计', model: selectedModel.value }
        if (activePlatform.value) {
          // 使用提示词库中该平台对应的 promptText 传给 AI，而非 raw value
          const promptText = promptMap.value[activePlatform.value]
          if (promptText) extra.platformPrompt = promptText
        }
        if (effectiveOutputSize.value) extra.outputSize = effectiveOutputSize.value
        if (generateCount.value) extra.n = Number(generateCount.value)
        await gen.fullGenerate(productFiles.value, text, extra)
} catch (e) {
if (e?.message?.includes('已取消')) return
console.error('主图生成失败:', e)
        const isTimeout = e?.code === 'ECONNABORTED'
          || /timeout|超时|人数过多|繁忙|busy/i.test(e?.message || '')
        ElMessage.error(isTimeout
          ? '当前模型使用人数过多，可选用其他模型生图或稍后再试'
          : '生成失败，请稍后重试')
      } finally {
        generating.value = false
      }
    }

    function clearWorkspaceImages() {
      productFiles.value = []
      language.value = 'en-US'
      outputSize.value = ''
      customWidth.value = 1000
      customHeight.value = 1000
      generateCount.value = 1
      gen.reset()
    }

    function clearChat() {
      chatMessages.value = []
      productFiles.value = []
      gen.reset()
    }
    function scrollChat() {
      const el = document.querySelector('.ai-chat')
      if (el) el.scrollTop = el.scrollHeight
    }

    // ===== 反推提示词 =====
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
    const REVERSE_MAX_SIZE = 20 * 1024 * 1024

    function handleReverseFile(file) {
      if (!REVERSE_ALLOWED_TYPES.includes(file.type)) {
        ElMessage.error('仅支持 JPG / PNG / WebP 格式的图片')
        return
      }
      if (file.size > REVERSE_MAX_SIZE) {
        ElMessage.error('图片大小不能超过 20MB')
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
        const ta = document.createElement('textarea')
        ta.value = text
        document.body.appendChild(ta)
        ta.select()
        try { document.execCommand('copy'); ElMessage.success('已复制到剪贴板') }
        catch { ElMessage.error('复制失败，请手动选择文本复制') }
        document.body.removeChild(ta)
      }
    }

    return {
      refImage, productFiles,
      generating, zoom, resultImages,
      sections,
      platforms, activePlatform, languages, language,
      sizeOptions, outputSize, customWidth, customHeight, effectiveOutputSize,
      purposes, activePurpose,
      sellingPoints, activeSellingPoints, toggleSellingPoint,
      generateCount, maxGenerateCount,
      chatPrompt, chatMessages, selectedModel, modelOptions,
      allExpanded,
      isGenerating, genProgress, genStatus, genError, aiAssistantRef,
      canvasFlex, configFlex, aiFlex, rightFlex,
      configCollapsed,
      aiPanel,
      fileInput, refFileInput,
      workflowSteps, getStepClass, isStepLineDone,
      triggerUpload, triggerRefUpload, handleFileSelect, handleRefFileSelect,
      handleRefDrop, removeProductFile,
      undo, redo, clearCanvas, fitToScreen, toggleFullscreen,
      getObjectUrl,
      toggleAllSections, toggleSection,
      useSuggestion, handleGenerate, clearWorkspaceImages, clearChat,
      startColResize, startRightPanelResize, startAiResize,
      // 反推提示词
      reverseDialogVisible, reverseImageFile, reverseImagePreview, reversePromptInput,
      reverseResult, reverseLoading,
      openReversePromptDialog, triggerReverseUpload, handleReverseDrop,
      clearReverseImage, submitReversePrompt, copyResult,
      // canvasUI, handleCanvasExport,
    }
  }
}
</script>

<style scoped lang="scss">
/* ============================================================
   Layout
   ============================================================ */
.workspace-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* ---- Steps Bar (inside canvas area, transparent) ---- */
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

  &.active { color: #2563FF; font-weight: 600; }
  &.done { color: #22C55E; }
}
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
.step-item.done .step-num {
  background: #22C55E; color: #fff; border-color: #22C55E;
}
.step-label {
  font-size: 12px;
  white-space: nowrap;
}
.step-line {
  flex: 1; height: 2px; background: #E8EDF5; min-width: 8px; margin: 0 6px;
}
.step-line.done { background: #22C55E; }

/* ---- Three Column ---- */
.three-col {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ---- Column Divider Wrapper + Toggle Button ---- */
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

/* ============================================================
   Config Column collapsed
   ============================================================ */
.config-col.collapsed {
  flex: 0 0 40px !important;
  min-width: 40px;
  width: 40px;
  overflow: hidden;
}

/* ============================================================
   Canvas Column
   ============================================================ */
.canvas-col {
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
  background: var(--gh-bg-page, #F7F9FC);
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
  min-height: 0;
  overflow: hidden;
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

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 4px;
    opacity: .4;
  }

  h3 {
    font-size: 14px;
    color: #6B7280;
    margin-bottom: 0;
    font-weight: 500;
  }

  p {
    font-size: 12px;
    color: #9CA3AF;
  }
}

.canvas-result {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;

  .uploaded-img, .result-img {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    object-fit: contain;
  }

  &.generating {
    opacity: .7;
  }
}

// ========== Result Grid (2×2) ==========
.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 12px;
  padding: 16px;
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;

  &.generating {
    opacity: .7;
  }
}

.result-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;

  .result-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}


.canvas-bottom-bar {
  padding: 6px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
  text-align: center;
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
  min-width: 260px;
  position: relative;
  transition: flex 0.3s;
}

.config-inner {
  padding: 0 0 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  cursor: pointer;
  user-select: none;
  padding: 14px 16px 0;

  &:hover .panel-toggle-all { opacity: 0.7; }
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
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
  color: #2563FF;
}
.panel-toggle-all:hover { opacity: 0.7; }

.config-section.collapsible {
  margin-bottom: 0;
  border-bottom: 1px solid #F3F4F6;
}

.section-header.collapsible {
  padding: 10px 16px;
  transition: opacity 0.2s;
}
.section-header:hover { opacity: 0.75; }
.section-label { font-size: 13px; font-weight: 500; color: #1F2937; }
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }
.expand-text {
  font-size: 11px;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  gap: 2px;
}
.expand-arrow { transition: transform 0.25s; }
.expand-arrow.expanded { transform: rotate(180deg); }

.section-body { padding: 4px 16px 10px; }

.config-label {
  display: block;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
}

/* Upload zone */
.panel-upload-zone {
  border: 2px dashed #E8EDF5;
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  background: #F7F9FC;
  transition: all 0.2s;
  margin-bottom: 8px;
}
.panel-upload-zone:hover { border-color: #2563FF; background: #FAFBFF; }
.panel-upload-zone.small { padding: 10px; }
.panel-upload-text { font-size: 12px; color: #6B7280; margin-top: 6px; }
.panel-upload-hint { font-size: 10px; color: #9CA3AF; margin-top: 2px; }
.upload-note { font-size: 11px; color: #6B7280; margin-top: 6px; }

/* Uploaded images list */
.uploaded-images-list {
  display: flex; flex-wrap: wrap; gap: 8px;
  margin-top: 10px;
}
.uploaded-thumb-wrap {
  position: relative; width: 48px; height: 48px; border-radius: 8px;
  overflow: hidden; border: 1px solid #E8EDF5;
}
.uploaded-thumb-wrap.add { border-style: dashed; cursor: pointer; }
.uploaded-thumb {
  width: 100%; height: 100%; display: flex; align-items: center;
  justify-content: center; background: #F7F9FC;
}
.uploaded-thumb img { width: 100%; height: 100%; object-fit: cover; }
.uploaded-thumb.add-thumb { font-size: 18px; color: #9CA3AF; font-weight: 500; }
.thumb-placeholder { font-size: 9px; color: #9CA3AF; }
.uploaded-remove {
  position: absolute; top: 2px; right: 2px;
  width: 16px; height: 16px; border-radius: 50%;
  background: rgba(0,0,0,0.45); color: #fff; font-size: 10px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; opacity: 0; transition: opacity 0.2s;
}
.uploaded-thumb-wrap:hover .uploaded-remove { opacity: 1; }

/* Reference preview */
.ref-preview-row {
  display: flex; align-items: center; gap: 8px; margin-top: 8px;
}
.ref-thumb {
  width: 48px; height: 48px; border-radius: 8px; object-fit: cover;
  border: 1px solid #E8EDF5;
}
.ref-remove {
  width: 20px; height: 20px; border-radius: 50%;
  background: #EF4444; color: #fff; border: none;
  cursor: pointer; font-size: 12px; display: flex;
  align-items: center; justify-content: center;
}

/* ========== Option Tags (toggleable, click to select/deselect) ========== */
.option-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 0;
}

.option-tag {
  padding: 4px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;

  &:hover {
    border-color: #2563FF;
    color: #2563FF;
    background: #FAFBFF;
  }

  &.active {
    background: #2563FF;
    border-color: #2563FF;
    color: #fff;
  }
}

/* Form select */
.form-select {
  width: 100%;
  padding: 7px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  font-size: 13px;
  outline: none;
  background: #fff;
  color: #1F2937;
  cursor: pointer;
  margin-top: 2px;
}
.form-select:focus { border-color: #2563FF; }
.form-select.full { width: 100%; }

.size-select {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}

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
  margin-top: 12px;
}

.select-hint { font-size: 11px; color: #9CA3AF; margin-top: 6px; }

/* ============================================================
   Right Panel Divider
   ============================================================ */
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
   AI Column
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
   反推提示词入口按钮
   ============================================================ */
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

/* ============================================================
   反推提示词模态框
   ============================================================ */
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
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .right-col { flex: 1 1 auto !important; min-height: 250px; }
  .right-panel-divider { display: none; }
  .config-col { max-height: 200px; overflow-y: auto; }
}
</style>