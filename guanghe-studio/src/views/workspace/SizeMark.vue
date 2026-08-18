<template>
  <div class="workspace-page">
    <!-- Steps bar -->
    <div class="steps-bar">
      <template v-for="(s, i) in workflowSteps" :key="i">
        <div class="step-item" :class="getStepClass(i + 1, 4)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
        <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
      </template>
    </div>

    <!-- Two-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas (50%) ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">

        <!-- Canvas Area -->
        <div class="canvas-box">
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div v-if="!originalImage" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>拖拽图片到画布，或从右侧上传</h3>
            <p>支持 JPG / PNG / WebP 格式，最大 20MB</p>
          </div>
          <div v-else class="dim-canvas-wrapper">
            <!-- Product image with dimension markings -->
            <div class="dim-product" v-for="(view, vi) in productViews" :key="vi" :class="{ small: vi > 0 }">
              <!-- Top dimension -->
              <div class="dim-top">
                <div class="dim-text">{{ dimWidth }}{{ unit }}</div>
                <div class="dim-line"><div class="dim-arrow-l"></div><div class="dim-arrow-r"></div></div>
              </div>
              <!-- Left dimension (for first view) -->
              <div class="dim-left" v-if="vi === 0">
                <div class="dim-text">{{ dimHeight }}{{ unit }}</div>
                <div class="dim-line-v"><div class="dim-arrow-t"></div><div class="dim-arrow-b"></div></div>
              </div>
              <!-- Right dimension (for second view) -->
              <div class="dim-right" v-if="vi === 1">
                <div class="dim-line-v"><div class="dim-arrow-t"></div><div class="dim-arrow-b"></div></div>
                <div class="dim-text">{{ dimDepth }}{{ unit }}</div>
              </div>
              <div class="dim-img-placeholder">
                <img :src="originalImage" v-if="vi === 0" class="dim-product-img" />
                <template v-else>
                  <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                    <rect x="4" y="8" width="24" height="16" rx="2" stroke="#9CA3AF" stroke-width="1.2"/>
                    <path d="M4 24v3M28 24v3M10 24v3M22 24v3" stroke="#9CA3AF" stroke-width="1" stroke-linecap="round"/>
                  </svg>
                  <div style="font-size:10px;color:#9CA3AF;margin-top:4px">{{ vi === 1 ? '侧视图' : '顶视图' }}</div>
                </template>
              </div>
              </div>
            </div>

            <!-- Section: 提示词增强 -->
            <div class="config-section collapsible">
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
              </div>
            </div>
          </div>

        <div class="canvas-bottom-bar">
          <span>提示：在左侧上传需要标注尺寸的商品图，右侧填写尺寸数据、选择模板风格。</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ config -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowRight v-if="!configCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
      </div>

      <!-- ===== RIGHT: Config Panel (25%) ===== -->
      <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
        <el-scrollbar>
          <div class="config-inner">
            <!-- 创作配置 标题 -->
            <div class="panel-header" @click="toggleAllSections">
              <span>创作配置</span>
              <span class="panel-toggle-all">{{ allExpanded ? '全部折叠 ▲' : '全部展开 ▼' }}</span>
            </div>

            <!-- Section: 上传商品图 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label">上传商品图</span>
                <span class="expand-text">
                  {{ sections.upload ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.upload">
                <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                  <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                  <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                  <p class="panel-upload-hint">支持 JPG / PNG / WEBP，最大 20MB</p>
                </div>
                <div v-if="uploadedFiles.length" class="uploaded-images-list">
                  <div v-for="(f, i) in uploadedFiles" :key="i" class="uploaded-thumb-wrap">
                    <div class="uploaded-thumb">
                      <img :src="f" v-if="typeof f === 'string'" />
                      <span v-else class="thumb-placeholder">图片</span>
                    </div>
                    <div class="uploaded-remove" @click.stop="removeProductFile(i)">✕</div>
                  </div>
                  <div
                    v-if="uploadedFiles.length < 10"
                    class="uploaded-thumb-wrap add"
                    @click.stop="triggerUpload"
                  >
                    <div class="uploaded-thumb add-thumb">+</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 尺寸数据 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('size')">
                <span class="section-label">输入尺寸数据</span>
                <span class="expand-text">
                  {{ sections.size ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.size }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.size">
                <div class="unit-switch-row">
                  <button class="unit-btn" :class="{ active: unit === 'cm' }" @click="unit = 'cm'">厘米 cm</button>
                  <button class="unit-btn" :class="{ active: unit === 'in' }" @click="unit = 'in'">英寸 in</button>
                </div>
                <div class="size-grid">
                  <div class="size-col">
                    <label>宽度</label>
                    <input class="size-input" v-model.number="dimWidth" type="number" min="0" />
                    <div class="size-unit">{{ unit }}</div>
                  </div>
                  <div class="size-col">
                    <label>长度</label>
                    <input class="size-input" v-model.number="dimDepth" type="number" min="0" />
                    <div class="size-unit">{{ unit }}</div>
                  </div>
                  <div class="size-col">
                    <label>高度</label>
                    <input class="size-input" v-model.number="dimHeight" type="number" min="0" />
                    <div class="size-unit">{{ unit }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 线条样式 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('lineStyle')">
                <span class="section-label">线条样式</span>
                <span class="expand-text">
                  {{ sections.lineStyle ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.lineStyle }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.lineStyle">
                <div class="line-options">
                  <div class="line-opt" :class="{ active: lineStyle === 'solid' }" @click="lineStyle = 'solid'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2"/></svg>
                  </div>
                  <div class="line-opt" :class="{ active: lineStyle === 'dashed1' }" @click="lineStyle = 'dashed1'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2" stroke-dasharray="4 2"/></svg>
                  </div>
                  <div class="line-opt" :class="{ active: lineStyle === 'dashed2' }" @click="lineStyle = 'dashed2'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2" stroke-dasharray="2 2"/></svg>
                  </div>
                  <div class="color-swatch" :style="{ background: lineColor }">
                    <input type="color" v-model="lineColor" class="color-input" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 输出比例 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('ratio')">
                <span class="section-label">输出比例</span>
                <span class="expand-text">
                  {{ sections.ratio ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.ratio }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.ratio">
                <el-select v-model="selectedRatio" style="width: 100%" size="small">
                  <el-option v-for="r in ratioOptions" :key="r.value" :label="r.label" :value="r.value" />
                </el-select>
                <!-- 自定义比例 -->
                <div v-if="selectedRatio === 'custom'" class="custom-size-row">
                  <div class="custom-size-input">
                    <span>宽</span>
                    <el-input-number v-model="customRatioWidth" :min="1" :max="9999" :step="1" size="small" controls-position="right" />
                  </div>
                  <span class="custom-size-x">:</span>
                  <div class="custom-size-input">
                    <span>高</span>
                    <el-input-number v-model="customRatioHeight" :min="1" :max="9999" :step="1" size="small" controls-position="right" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 模板选择 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('templates')">
                <span class="section-label">模板选择</span>
                <span class="expand-text">
                  {{ sections.templates ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.templates }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.templates">
                <div class="template-grid">
                  <div class="template-item" :class="{ active: selectedTemplate === 'standard' }" @click="selectedTemplate = 'standard'">
                    <div class="tmpl-thumb">
                      <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="3" y="5" width="14" height="10" rx="1" stroke="currentColor" stroke-width="1.2"/><path d="M3 9h14M10 5v10" stroke="currentColor" stroke-width="1" stroke-dasharray="2 1"/></svg>
                    </div>
                    <div class="tmpl-name">标准尺寸图</div>
                  </div>
                  <div class="template-item" :class="{ active: selectedTemplate === 'multi' }" @click="selectedTemplate = 'multi'">
                    <div class="tmpl-thumb">
                      <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="2" y="6" width="6" height="8" rx="1" stroke="currentColor" stroke-width="1"/><rect x="12" y="4" width="6" height="12" rx="1" stroke="currentColor" stroke-width="1"/></svg>
                    </div>
                    <div class="tmpl-name">多尺寸对比</div>
                  </div>
                  <div class="template-item" :class="{ active: selectedTemplate === 'scene' }" @click="selectedTemplate = 'scene'">
                    <div class="tmpl-thumb">
                      <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><rect x="2" y="4" width="16" height="12" rx="1" stroke="currentColor" stroke-width="1"/><path d="M2 14l5-5 3 3 4-5 4 5" stroke="currentColor" stroke-width="1" stroke-linecap="round"/></svg>
                    </div>
                    <div class="tmpl-name">场景尺寸图</div>
                  </div>
                  <div class="template-item add-more" @click="moreTemplates">
                    <div class="tmpl-thumb">
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 4v8M4 8h8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
                    </div>
                    <div class="tmpl-name">更多模板</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 语言 -->
            <div class="config-section collapsible">
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
            </div>

            <!-- Generate Button -->
            <div class="generate-area">
              <button class="gen-btn" :disabled="generating" @click="startGenerate">
                {{ generating ? '生成中...' : '生成尺寸图' }}
              </button>
              <div class="gen-notice">
                <svg viewBox="0 0 12 12" fill="none" width="12" height="12">
                  <circle cx="6" cy="6" r="5" stroke="#22C55E" stroke-width="1.2"/>
                  <path d="M4 6l1.5 1.5L8 4.5" stroke="#22C55E" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                生成的尺寸图均可商用，放心使用
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- Divider handle: config ⇔ AI -->
      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== RIGHT: AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        <div class="ai-resize-handle" @mousedown="startAiResize"></div>
        <div class="ai-header">
          <h3>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="vertical-align:middle;margin-right:4px">
              <path d="M8 1L14 8L8 15L2 8L8 1Z" fill="#2563FF" opacity="0.15"/>
              <path d="M8 3L12 8L8 13L4 8L8 3Z" fill="#2563FF" opacity="0.5"/>
              <path d="M8 5.5L10 8L8 10.5L6 8L8 5.5Z" fill="#2563FF"/>
            </svg>
            AI 助手
          </h3>
          <button class="ai-clear-btn" @click="clearChat">清空对话</button>
        </div>

        <!-- Chat messages -->
        <div class="ai-chat" ref="chatContainer">
          <div class="chat-msg bot">
            <div class="chat-avatar">AI</div>
            <div class="chat-bubble">您好！我可以帮您生成清晰规范的尺寸标记图，请上传商品图片并填写尺寸数据。</div>
          </div>
          <div v-for="(msg, i) in chatMessages" :key="i" class="chat-msg" :class="msg.role">
            <div v-if="msg.role === 'bot'" class="chat-avatar">AI</div>
            <div class="chat-bubble">{{ msg.text }}</div>
          </div>
          <div v-if="generating" class="chat-msg bot">
            <div class="chat-avatar">AI</div>
            <div class="chat-bubble">正在为您生成尺寸标记图...</div>
          </div>
        </div>

        <!-- Prompt suggestion -->
        <div class="prompt-suggestion" v-if="originalImage && !chatMessages.length">
          <div class="prompt-card">
            <p>请自动生成清晰的尺寸标记图，标注宽度{{ dimWidth }}{{ unit }}、长度{{ dimDepth }}{{ unit }}、高度{{ dimHeight }}{{ unit }}，风格简洁，适合电商平台使用。</p>
            <div class="prompt-apply" @click="applyPrompt">应用提示词 ›</div>
          </div>
        </div>

        <!-- Chat input -->
        <div class="chat-input-area">
          <textarea
            class="chat-input"
            v-model="chatPrompt"
            placeholder="请输入您的需求，描述越详细，效果越好..."
            @keydown.enter.exact.prevent="sendMessage"
            maxlength="2000"
          ></textarea>
        </div>
        <div class="chat-footer">
          <div>
            <span class="chat-counter">{{ chatPrompt.length }}/2000</span>
            <br />
            <!-- <span class="chat-cost">本次操作将消耗 2 积分</span> -->
          </div>
          <button class="chat-send" @click="sendMessage" :disabled="!chatPrompt.trim() || generating">
            <el-icon><Promotion /></el-icon>
            -2积分
          </button>
        </div>
      </div>
    </div>

    <!-- Hidden file input -->
    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { UploadFilled, ArrowDown, ArrowLeft, ArrowRight, Promotion } from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import { aiDialogue } from '@/api/customer'
import { ElMessage } from 'element-plus'

export default {
  name: 'SizeMarkView',
  components: { UploadFilled, ArrowDown, ArrowLeft, ArrowRight, Promotion, PromptLibrarySelect },
  setup() {
    // ---- Canvas Interactions ----
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-box',
    //   defaultName: 'size-mark',
    // })
    const gen = useImageGeneration('render')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()
  
    // ---- State ----
    const originalImage = ref(null)
    const originalFile = ref(null)
    const uploadedFiles = ref([])
    const generating = ref(false)
    const zoom = ref(100)
    const unit = ref('cm')

    // Size data
    const dimWidth = ref(160)
    const dimDepth = ref(80)
    const dimHeight = ref(75)

    // Line style
    const lineStyle = ref('solid')
    const lineColor = ref('#1F2937')

    // Output ratio
    const ratioOptions = [
      { label: '1:1', value: '1:1' },
      { label: '4:5', value: '4:5' },
      { label: '16:9', value: '16:9' },
      { label: '自定义', value: 'custom' }
    ]
    const selectedRatio = ref('4:5')
    const customRatioWidth = ref(4)
    const customRatioHeight = ref(5)

    // Template
    const selectedTemplate = ref('standard')

    // Language
    const language = ref('zh-CN')
    const languages = [
      { label: '中文（简体）', value: 'zh-CN' },
      { label: '英语（美国）', value: 'en-US' },
      { label: '英语（英国）', value: 'en-GB' },
      { label: '日语', value: 'ja-JP' },
      { label: '韩语', value: 'ko-KR' },
      { label: '德语', value: 'de-DE' },
      { label: '法语', value: 'fr-FR' },
      { label: '西班牙语', value: 'es-ES' },
    ]

    // Product views for canvas display
    const productViews = computed(() => {
      const views = [{ type: 'front' }]
      if (originalImage.value) {
        views.push({ type: 'side' })
      }
      return views
    })

    // Config sections (all expanded state)
    const sections = reactive({
      upload: true,
      size: true,
      lineStyle: true,
      ratio: true,
      templates: true,
      language: true,
      promptBoost: false,
    })

    const boostProduct = ref('')
    const boostMaterial = ref('')
    const boostProductRef = ref(null)
    const boostMaterialRef = ref(null)

    // Chat
    const chatPrompt = ref('')
    const chatMessages = ref([])

    // ---- Computed ----
    const allExpanded = computed(() => {
      return Object.values(sections).every(v => v)
    })

    // ---- Config collapse ----
    const configCollapsed = ref(false)

    // ---- Layout resize ----
    const _configWidthPx = ref(280)
    const _aiWidthPx = ref(360)
    const canvasFlex = computed(() => '1 1 0%')
    const configFlex = computed(() => {
      if (configCollapsed.value) return '0 0 40px'
      return `0 0 ${_configWidthPx.value}px`
    })
    const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)
    let isResizing = false
    let resizeTarget = ''
    const aiPanel = ref(null)

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
      if (resizeTarget === 'config') {
        const rightWidth = rect.right - e.clientX - 24
        const totalCurrent = _configWidthPx.value + _aiWidthPx.value + 12
        if (totalCurrent > 0 && rightWidth > 200) {
          const ratio = rightWidth / totalCurrent
          _configWidthPx.value = Math.max(150, Math.min(600, Math.round(_configWidthPx.value * ratio)))
          _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(_aiWidthPx.value * ratio)))
        }
      } else if (resizeTarget === 'ai') {
        const aiWidth = rect.right - e.clientX - 6
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

    onMounted(() => {
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
    function triggerUpload() { fileInput.value?.click() }
    function handleFileSelect(e) {
      const files = e.target.files
      if (files.length) addFiles(files)
      fileInput.value.value = ''
    }
    function handleDrop(e) {
      const files = e.dataTransfer.files
      if (files.length) addFiles(files)
    }
    function addFiles(files) {
      for (const f of files) {
        const url = URL.createObjectURL(f)
        uploadedFiles.value.push(url)
        if (!originalImage.value) {
          originalImage.value = url
          originalFile.value = f
        }
      }
    }
    function removeProductFile(index) {
      uploadedFiles.value.splice(index, 1)
      if (index === 0) {
        originalImage.value = uploadedFiles.value[0] || null
        originalFile.value = null
      }
    }

    function toggleAllSections() {
      const val = !allExpanded.value
      Object.keys(sections).forEach(k => sections[k] = val)
    }
    function toggleSection(key) {
      if (sections.hasOwnProperty(key)) sections[key] = !sections[key]
    }

    // function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
    // function zoomOut() { zoom.value = Math.max(10, zoom.value - 10) }
    function toggleFullscreen() { /* placeholder */ }
    function regenerate() { /* placeholder */ }
    function downloadPng() { /* placeholder */ }
    function copyLink() { /* placeholder */ }
    function moreTemplates() { /* placeholder */ }
    async function startGenerate() {
      if (!originalFile.value) { ElMessage.warning('请先上传产品图片'); return }
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      generating.value = true
      try {
        const prompt = `生成尺寸标记图，宽度${dimWidth.value}${unit.value}、长度${dimDepth.value}${unit.value}、高度${dimHeight.value}${unit.value}，风格简洁，适合电商平台`
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const fullPrompt = boostText ? `${prompt}。约束：${boostText}。` : prompt
        await gen.fullGenerate([originalFile.value], fullPrompt, { consumePoints: 2, featureName: 'size_mark', title: '尺寸标记生成', n: 1 })
      } catch (e) {
        console.error('尺寸标记生成失败:', e)
        ElMessage.error('生成失败，请稍后重试')
      } finally {
        generating.value = false
      }
    }

    function applyPrompt() {
      chatPrompt.value = `请自动生成清晰的尺寸标记图，标注宽度${dimWidth.value}${unit.value}、长度${dimDepth.value}${unit.value}、高度${dimHeight.value}${unit.value}，风格简洁，适合电商平台使用。`
    }

    async function sendMessage() {
      const text = chatPrompt.value.trim()
      if (!text || generating.value) return
      chatMessages.value.push({ role: 'user', text })
      chatPrompt.value = ''
      generating.value = true
      await nextTick()
      scrollChat()
      try {
        const historyMessages = chatMessages.value.slice(0, -1).map(m => ({ role: m.role === 'bot' ? 'assistant' : 'user', content: m.text }))
        const res = await aiDialogue({ messages: historyMessages, content: text, model: 'deepseek' })
        chatMessages.value.push({ role: 'bot', text: res?.data?.reply || '好的，已为您处理。' })
      } catch (e) {
        chatMessages.value.push({ role: 'bot', text: '抱歉，AI服务暂时不可用。' })
      } finally {
        generating.value = false
        nextTick(() => scrollChat())
      }
    }

    function clearChat() { chatMessages.value = [] }
    function scrollChat() {
      const el = document.querySelector('.ai-chat')
      if (el) el.scrollTop = el.scrollHeight
    }

    return {
      originalImage, uploadedFiles,
      configCollapsed,
      generating, zoom, unit,
      dimWidth, dimDepth, dimHeight,
      lineStyle, lineColor,
      ratioOptions, selectedRatio,
      customRatioWidth, customRatioHeight,
      selectedTemplate,
      language, languages,
      productViews,
      sections,
      chatPrompt, chatMessages,
      allExpanded,
      canvasFlex, configFlex, aiFlex,
      aiPanel,
      fileInput,
      workflowSteps, getStepClass, isStepLineDone,
      boostProduct, boostMaterial, boostProductRef, boostMaterialRef,
      triggerUpload, handleFileSelect, handleDrop, removeProductFile,
      toggleAllSections, toggleSection,
      toggleFullscreen,
      regenerate, downloadPng, copyLink,
      moreTemplates, startGenerate,
      applyPrompt, sendMessage, clearChat,
      startColResize, startAiResize,
    }
  }
}
</script>

<style scoped>
/* ============================================================
   Layout
   ============================================================ */
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
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #E8EDF5;
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

.prompt-boost-row { margin-bottom: 10px; }
.prompt-boost-row .boost-label {
  display: block; font-size: 12px; color: #6B7280; margin-bottom: 4px;
}

/* ---- Three Column ---- */
.three-col {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ===== Column Divider Wrapper ===== */
.col-divider-wrapper {
  width: 6px;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-divider-wrapper .col-divider {
  position: absolute;
  inset: 0;
  background: transparent;
  cursor: col-resize;
  transition: background 0.2s;
}

.col-divider-wrapper .col-divider:hover,
.col-divider-wrapper .col-divider:active {
  background: #2563FF;
}

/* ===== Config Toggle Button (Left of Config Panel) ===== */
.config-toggle-btn {
  position: absolute;
  top: 50%;
  right: -10px;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #E8EDF5;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6B7280;
  z-index: 6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  padding: 0;
}

.config-toggle-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
  background: #FAFBFF;
  box-shadow: 0 2px 8px rgba(37, 99, 255, 0.15);
}

/* ===== Config Panel Collapsed ===== */
.config-col.collapsed {
  flex: 0 0 0px !important;
  min-width: 0 !important;
  max-width: 0 !important;
  overflow: hidden;
  padding: 0;
}

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

/* Canvas Toolbar */
.canvas-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
  gap: 12px;
}
.unit-switch-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.unit-switch {
  display: flex;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  overflow: hidden;
}
.unit-btn {
  padding: 6px 14px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
  border: none;
  color: #6B7280;
  font-weight: 500;
  transition: all 0.15s;
  white-space: nowrap;
}
.unit-btn.active {
  background: #2563FF;
  color: #fff;
}
.unit-btn:hover:not(.active) { color: #2563FF; }

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}
.tb-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.15s;
  white-space: nowrap;
}
.tb-btn:hover { background: #F7F9FC; color: #2563FF; }
.tb-divider {
  width: 1px; height: 16px; background: #E8EDF5; margin: 0 4px;
}

.zoom-controls { display: flex; align-items: center; gap: 4px; }
.zoom-btn {
  width: 24px; height: 24px; border: 1px solid #E8EDF5; border-radius: 6px;
  background: #fff; cursor: pointer; display: flex; align-items: center;
  justify-content: center; font-size: 12px; color: #6B7280;
}
.zoom-btn:hover { border-color: #2563FF; color: #2563FF; }
.zoom-label { font-size: 12px; color: #6B7280; padding: 0 6px; min-width: 35px; text-align: center; }

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
}

.canvas-placeholder {
  text-align: center;
  color: #9CA3AF;
  padding: 20px;
}
.canvas-placeholder svg { width: 48px; height: 48px; margin-bottom: 12px; opacity: 0.4; }
.canvas-placeholder h3 { font-size: 14px; color: #6B7280; margin-bottom: 6px; font-weight: 500; }
.canvas-placeholder p { font-size: 12px; color: #9CA3AF; }

/* Dimension canvas */
.dim-canvas-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 40px 50px;
  width: 100%; height: 100%;
}

.dim-product { position: relative; }
.dim-product .dim-img-placeholder {
  width: 220px; height: 260px;
  border-radius: 8px;
  background: linear-gradient(135deg, #e8edf5, #f0f4fa);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: #9CA3AF; font-size: 12px; text-align: center;
}
.dim-product.small .dim-img-placeholder { width: 120px; height: 140px; }
.dim-product-img {
  width: 100%; height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

/* Top dimension */
.dim-top {
  position: absolute; top: -32px; left: 0; right: 0;
  display: flex; flex-direction: column; align-items: center;
}
.dim-top .dim-line {
  width: 100%; height: 2px; background: #1F2937; position: relative;
}
.dim-top .dim-line::before, .dim-top .dim-line::after {
  content: ''; position: absolute; top: -4px; width: 2px; height: 10px; background: #1F2937;
}
.dim-top .dim-line::before { left: 0; }
.dim-top .dim-line::after { right: 0; }
.dim-top .dim-arrow-l, .dim-top .dim-arrow-r {
  position: absolute; top: -3px;
  width: 0; height: 0;
  border-top: 4px solid transparent; border-bottom: 4px solid transparent;
}
.dim-top .dim-arrow-l { left: 0; border-right: 6px solid #1F2937; }
.dim-top .dim-arrow-r { right: 0; border-left: 6px solid #1F2937; }
.dim-top .dim-text {
  font-size: 13px; font-weight: 700; color: #1F2937;
  margin-bottom: 4px; background: #fff; padding: 0 6px;
}

/* Left dimension */
.dim-left {
  position: absolute; top: 0; bottom: 0; left: -40px;
  display: flex; flex-direction: row; align-items: center;
}
.dim-left .dim-line-v {
  height: 100%; width: 2px; background: #1F2937; position: relative;
}
.dim-left .dim-line-v::before, .dim-left .dim-line-v::after {
  content: ''; position: absolute; left: -4px; width: 10px; height: 2px; background: #1F2937;
}
.dim-left .dim-line-v::before { top: 0; }
.dim-left .dim-line-v::after { bottom: 0; }
.dim-left .dim-arrow-t, .dim-left .dim-arrow-b {
  position: absolute; left: -3px;
  width: 0; height: 0;
  border-left: 4px solid transparent; border-right: 4px solid transparent;
}
.dim-left .dim-arrow-t { top: 0; border-bottom: 6px solid #1F2937; }
.dim-left .dim-arrow-b { bottom: 0; border-top: 6px solid #1F2937; }
.dim-left .dim-text {
  font-size: 13px; font-weight: 700; color: #1F2937;
  writing-mode: vertical-rl; text-orientation: mixed;
  margin-right: 4px; background: #fff; padding: 4px 0; letter-spacing: 1px;
}

/* Right dimension */
.dim-right {
  position: absolute; top: 0; bottom: 0; right: -40px;
  display: flex; flex-direction: row; align-items: center;
}
.dim-right .dim-line-v {
  height: 100%; width: 2px; background: #1F2937; position: relative;
}
.dim-right .dim-line-v::before, .dim-right .dim-line-v::after {
  content: ''; position: absolute; left: -4px; width: 10px; height: 2px; background: #1F2937;
}
.dim-right .dim-line-v::before { top: 0; }
.dim-right .dim-line-v::after { bottom: 0; }
.dim-right .dim-arrow-t, .dim-right .dim-arrow-b {
  position: absolute; left: -3px;
  width: 0; height: 0;
  border-left: 4px solid transparent; border-right: 4px solid transparent;
}
.dim-right .dim-arrow-t { top: 0; border-bottom: 6px solid #1F2937; }
.dim-right .dim-arrow-b { bottom: 0; border-top: 6px solid #1F2937; }
.dim-right .dim-text {
  font-size: 13px; font-weight: 700; color: #1F2937;
  writing-mode: vertical-rl; text-orientation: mixed;
  margin-left: 4px; background: #fff; padding: 4px 0; letter-spacing: 1px;
}

.canvas-bottom-bar {
  padding: 6px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
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
}

.config-inner {
  padding: 0 0 16px;
  overflow-y: auto;
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
  color: #2563FF;
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: opacity 0.2s;
}
.panel-toggle-all:hover { opacity: 0.7; }

.config-section.collapsible {
  margin-bottom: 0;
  border-bottom: 1px solid #F3F4F6;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 8px 16px;
  transition: opacity 0.2s;
}
.section-header:hover { opacity: 0.75; }
.section-label { font-size: 13px; font-weight: 600; color: #1F2937; }

.section-body { padding: 0 16px 8px; }

.expand-text {
  font-size: 12px; color: #9CA3AF;
  display: flex; align-items: center; gap: 2px;
}
.expand-arrow { transition: transform 0.2s; }
.expand-arrow.expanded { transform: rotate(180deg); }

/* Panel upload zone */
.panel-upload-zone {
  border: 2px dashed #E8EDF5;
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  background: #F7F9FC;
  transition: all 0.2s;
  margin: 0 16px;
}
.panel-upload-zone:hover { border-color: #2563FF; background: #FAFBFF; }
.panel-upload-text { font-size: 12px; color: #6B7280; margin-top: 6px; }
.panel-upload-hint { font-size: 10px; color: #9CA3AF; margin-top: 2px; }

/* Uploaded images list */
.uploaded-images-list {
  display: flex; flex-wrap: wrap; gap: 8px;
  padding: 0 16px; margin-top: 10px;
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

/* Size Grid */
.size-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.size-col { text-align: center; }
.size-col label { font-size: 11px; color: #6B7280; display: block; margin-bottom: 4px; }
.size-input {
  width: 100%; padding: 8px 6px;
  border: 2px solid #2563FF; border-radius: 10px;
  font-size: 14px; font-weight: 600; text-align: center;
  outline: none; color: #1F2937; background: #fff;
}
.size-input:focus { border-color: #1D4ED8; }
.size-unit { font-size: 11px; color: #6B7280; margin-top: 2px; }

/* Line options */
.line-options { display: flex; gap: 8px; align-items: center; }
.line-opt {
  width: 36px; height: 28px;
  border: 1px solid #E8EDF5; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; background: #fff; color: #6B7280;
  transition: all 0.15s;
}
.line-opt.active { border-color: #2563FF; background: #EEF2FF; color: #2563FF; }
.line-opt:hover { border-color: #2563FF; }
.line-opt svg { width: 20px; height: 12px; }

.color-swatch {
  width: 28px; height: 28px; border-radius: 8px;
  border: 2px solid #E8EDF5; cursor: pointer;
  position: relative; overflow: hidden;
}
.color-input {
  width: 100%; height: 100%; opacity: 0; cursor: pointer;
  position: absolute; top: 0; left: 0;
}

/* Ratio options */
.ratio-options { display: flex; gap: 8px; flex-wrap: wrap; }
.ratio-btn {
  padding: 8px 16px;
  border: 1px solid #E8EDF5; border-radius: 10px;
  font-size: 13px; cursor: pointer; background: #fff;
  color: #6B7280; transition: all 0.15s;
}
.ratio-btn.active { background: #EEF2FF; border-color: #2563FF; color: #2563FF; font-weight: 500; }
.ratio-btn:hover { border-color: #2563FF; }

/* Custom size row */
.custom-size-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.custom-size-input {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.custom-size-input span {
  font-size: 11px;
  color: #6B7280;
}
.custom-size-x {
  color: #9CA3AF;
  font-size: 14px;
  margin-top: 14px;
}

/* Template Grid */
.template-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.template-item {
  border: 2px solid #E8EDF5; border-radius: 10px;
  padding: 6px; text-align: center; cursor: pointer; transition: all 0.15s;
}
.template-item.active { border-color: #2563FF; background: #EEF2FF; }
.template-item.active .tmpl-name { color: #2563FF; font-weight: 500; }
.template-item.add-more { border-style: dashed; }
.template-item .tmpl-thumb {
  height: 40px; border-radius: 6px; margin-bottom: 4px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: #9CA3AF;
}
.template-item.active .tmpl-thumb { background: #EEF2FF; color: #2563FF; }
.template-item .tmpl-name { font-size: 10px; color: #6B7280; }

/* Section helper */
.section-helper {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 6px;
  line-height: 1.5;
}

/* Generate Button */
.generate-area { margin-top: 12px; padding: 0 16px 8px; }
.gen-btn {
  width: 100%; padding: 14px;
  border: none; border-radius: 12px;
  background: linear-gradient(135deg, #2563FF, #1D4ED8);
  color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center;
  justify-content: center; gap: 6px;
  transition: opacity 0.15s;
}
.gen-btn:hover { opacity: 0.9; }
.gen-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.gen-notice {
  font-size: 11px; color: #6B7280;
  display: flex; align-items: center; gap: 4px;
  justify-content: center; margin-top: 8px;
}

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
  position: relative;
}

.ai-resize-handle {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 6px;
  cursor: col-resize;
  z-index: 5;
  background: transparent;
  transition: background 0.2s;
}
.ai-resize-handle:hover { background: #2563FF; }

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.ai-header h3 { font-size: 14px; font-weight: 600; margin: 0; display: flex; align-items: center; }
.ai-clear-btn {
  font-size: 11px; color: #2563FF; background: none; border: none;
  cursor: pointer; text-decoration: underline;
}
.ai-clear-btn:hover { opacity: 0.7; }

.ai-chat {
  background: #F7F9FC;
  border-radius: 10px;
  padding: 12px;
  overflow-y: auto;
  flex: 1;
  margin-bottom: 8px;
  min-height: 200px;
}

.chat-msg {
  margin-bottom: 10px;
  display: flex;
  gap: 6px;
}
.chat-msg.bot { flex-direction: row; }
.chat-msg.user { flex-direction: row-reverse; }
.chat-avatar {
  width: 22px; height: 22px; border-radius: 50%;
  background: #2563FF; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 10px; flex-shrink: 0;
}
.chat-bubble {
  padding: 8px 12px; border-radius: 10px;
  font-size: 12px; line-height: 1.5; max-width: 85%;
}
.chat-msg.bot .chat-bubble { background: #fff; color: #1F2937; }
.chat-msg.user .chat-bubble { background: #EEF2FF; color: #2563FF; }

/* Prompt suggestion */
.prompt-suggestion { margin-bottom: 8px; flex-shrink: 0; }
.prompt-card {
  background: linear-gradient(135deg, #EEF2FF, #F0F4FF);
  border-radius: 12px; padding: 12px 14px;
}
.prompt-card p { font-size: 12px; color: #1F2937; line-height: 1.6; margin: 0; }
.prompt-apply {
  font-size: 12px; color: #2563FF; cursor: pointer;
  text-align: right; margin-top: 8px; font-weight: 500;
}
.prompt-apply:hover { opacity: 0.7; }

.chat-input-area { display: flex; gap: 6px; flex-shrink: 0; }
.chat-input {
  flex: 1; padding: 8px 12px; border: 1px solid #E8EDF5; border-radius: 8px;
  font-size: 12px; outline: none; resize: none; height: 50px; font-family: inherit;
}
.chat-input:focus { border-color: #2563FF; }

.chat-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 6px;
  flex-shrink: 0;
}
.chat-counter { font-size: 10px; color: #9CA3AF; }
.chat-cost { font-size: 10px; color: #22C55E; }
.chat-send {
  padding: 8px 16px; background: #2563FF; color: #fff;
  border: none; border-radius: 8px; font-size: 12px; cursor: pointer; font-weight: 500;
  white-space: nowrap;
  display: flex; align-items: center; gap: 4px;
}
.chat-send:hover { opacity: 0.9; }
.chat-send:disabled { opacity: 0.4; cursor: not-allowed; }

/* ============================================================
   Responsive
   ============================================================ */
@media (max-width: 1024px) {
  .steps-bar { padding: 10px 16px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 8px; margin: 0 4px; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .config-col { flex: 0 0 50% !important; }
  .ai-col { flex: 0 0 50% !important; }
  .col-divider { display: none; }
  .template-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .config-col { flex: 0 0 auto !important; max-height: 200px; overflow-y: auto; }
  .ai-col { flex: 1 1 auto !important; min-height: 250px; }
  .ai-resize-handle { display: none; }
  .canvas-toolbar { flex-wrap: wrap; gap: 8px; }
}
</style>