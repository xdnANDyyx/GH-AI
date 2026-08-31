<template>
  <div class="workspace-page">

    <!-- Two-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas (50%) ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Steps bar -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 4)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>

        <!-- Canvas Area -->
        <div class="canvas-box">
          <!-- 有结果图时显示在画布中 -->
          <div v-if="resultImages.length > 0" class="canvas-result" :class="{ generating: isGenerating }">
            <el-image
              v-for="(img, i) in resultImages"
              :key="i"
              :src="img.url || img"
              :preview-src-list="resultImages.map(r => r.url || r)"
              :initial-index="i"
              fit="contain"
              class="result-img"
            />
          </div>
          <!-- 加载中 -->
          <div v-else-if="isGenerating" class="canvas-loading">
            <p>{{ genStatus || '正在生成...' }}</p>
          </div>
          <!-- 空状态占位符 -->
          <div v-else class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>上传产品图并配置参数后生成</h3>
            <p>生成结果将同时显示在此画布和右侧 AI 助手中</p>
          </div>
        </div>

        <div class="canvas-bottom-bar">
          <span>提示：在右侧上传需要标注尺寸的商品图，填写尺寸数据、选择模板风格。</span>
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

            <!-- 反推提示词入口 -->
            <div class="reverse-prompt-entry">
              <el-button type="primary" plain class="reverse-prompt-btn" @click="openReversePromptDialog">
                <el-icon><MagicStick /></el-icon>
                <span>反推提示词</span>
              </el-button>
              <p class="entry-helper">上传参考图，AI 帮你描述想要的画面效果</p>
            </div>

            <!-- Section: 上传商品图 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label"><span class="required-mark">*</span>上传商品图<span class="required-mark">（必填）</span></span>
                <span class="expand-text">
                  {{ sections.upload ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.upload">
                <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                  <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                  <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                  <p class="panel-upload-hint">支持 JPG / PNG / WEBP，单张最大 7MB</p>
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
                  <button class="unit-btn" :class="{ active: unit === 'cm' }" @click="unit = unit === 'cm' ? '' : 'cm'">厘米 cm</button>
                  <button class="unit-btn" :class="{ active: unit === 'in' }" @click="unit = unit === 'in' ? '' : 'in'">英寸 in</button>
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
                  <div class="line-opt" :class="{ active: lineStyle === 'solid' }" @click="lineStyle = lineStyle === 'solid' ? '' : 'solid'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2"/></svg>
                  </div>
                  <div class="line-opt" :class="{ active: lineStyle === 'dashed1' }" @click="lineStyle = lineStyle === 'dashed1' ? '' : 'dashed1'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2" stroke-dasharray="4 2"/></svg>
                  </div>
                  <div class="line-opt" :class="{ active: lineStyle === 'dashed2' }" @click="lineStyle = lineStyle === 'dashed2' ? '' : 'dashed2'">
                    <svg viewBox="0 0 20 12"><line x1="2" y1="6" x2="18" y2="6" stroke="currentColor" stroke-width="2" stroke-dasharray="2 2"/></svg>
                  </div>
                  <div class="color-swatch" :style="{ background: lineColor }">
                    <input type="color" v-model="lineColor" class="color-input" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 输出尺寸 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('outputSize')">
                <span class="section-label">输出尺寸</span>
                <span class="expand-text">
                  {{ sections.outputSize ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.outputSize }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.outputSize">
                <el-select v-model="outputSize" placeholder="请选择输出尺寸" style="width: 100%" size="small">
                  <el-option v-for="s in sizeOptions" :key="s.value" :label="s.label" :value="s.value" />
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

            <!-- Section: 模板选择 -->
            <div class="config-section collapsible">
              <!-- <div class="section-header collapsible" @click="toggleSection('templates')">
                <span class="section-label">模板选择</span>
                <span class="expand-text">
                  {{ sections.templates ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.templates }"><ArrowDown /></el-icon>
                </span>
              </div> -->
              <div class="section-body" v-show="sections.templates">
                <!-- <div class="template-grid">
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
                </div> -->
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
              </div>
            </div> -->

          </div>
        </el-scrollbar>
      </div>

      <!-- Divider handle: config ⇔ AI -->
      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== RIGHT: AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        <div class="ai-resize-handle" @mousedown="startAiResize"></div>
        <AiAssistant
          ref="aiAssistantRef"
          :generate-fn="handleGenerate"
          :is-generating="isGenerating"
          :gen-status="genStatus"
          :gen-progress="genProgress"
          :gen-error="genError"
          :has-image="!!originalImage"
          :on-clear-images="clearWorkspaceImages"
        />
      </div>
    </div>

    <!-- Hidden file input -->
    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />

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
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { compressImage } from '@/utils/compress'
import { UploadFilled, ArrowDown, ArrowLeft, ArrowRight, MagicStick, DocumentCopy } from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { ElMessage } from 'element-plus'
import { reversePrompt, getPublicCreationConfigByGroup, listPromptLibraryBatch } from '@/api/customer'

export default {
  name: 'SizeMarkView',
  components: { UploadFilled, ArrowDown, ArrowLeft, ArrowRight, MagicStick, DocumentCopy, PromptLibrarySelect, AiAssistant },
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
    const resultImages = ref([])
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

    // Output size
    const sizeOptions = ref([
      { label: '不指定尺寸', value: '' },
      { label: '1:1（800×800）', value: '800:800' },
      { label: '3:4（800×1067）', value: '800:1067' },
      { label: '4:3（1067×800）', value: '1067:800' },
      { label: '自定义', value: 'custom' }
    ])
    const outputSize = ref('')
    const customWidth = ref(1000)
    const customHeight = ref(1000)

    // 实际输出尺寸
    const effectiveOutputSize = computed(() => {
      if (outputSize.value === 'custom') {
        return `${customWidth.value}x${customHeight.value}`
      }
      return outputSize.value
    })

    // 模板列表（从后台加载）
    const templateOptions = ref([
      { label: '标准尺寸图', value: 'standard' },
      { label: '多尺寸对比', value: 'compare' },
      { label: '场景尺寸图', value: 'scene' },
      { label: '更多模板', value: 'more' },
    ])
    // 提示词映射
    const promptMap = ref({})

    // Template
    const selectedTemplate = ref('standard')

    // Language
    const language = ref('zh-CN')
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
      outputSize: true,
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
    const aiPanel = ref(null)
    const canvasFlex = computed(() => '1 1 0%')
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

    // AI panel vertical resize (drag top border)
    let isAiHeightResizing = false
    let aiHeightStartY = 0
    let aiHeightStart = 0

    function startAiHeightResize(e) {
      isAiHeightResizing = true
      aiHeightStartY = e.clientY
      const aiEl = aiPanel.value
      aiHeightStart = aiEl
        ? (_aiHeightPx.value > 0 ? _aiHeightPx.value : aiEl.getBoundingClientRect().height)
        : 400
      _aiHeightPx.value = Math.round(aiHeightStart)
      document.body.style.cursor = 'ns-resize'
      document.body.style.userSelect = 'none'
      e.preventDefault()
      e.stopPropagation()
    }

    function onAiHeightMouseMove(e) {
      if (!isAiHeightResizing) return
      const delta = e.clientY - aiHeightStartY
      let newHeight = aiHeightStart + delta
      newHeight = Math.max(300, Math.min(window.innerHeight - 100, Math.round(newHeight)))
      _aiHeightPx.value = newHeight
    }

    function onAiHeightMouseUp() {
      if (isAiHeightResizing) {
        isAiHeightResizing = false
        document.body.style.cursor = ''
        document.body.style.userSelect = ''
      }
    }

    onMounted(() => {
      document.addEventListener('mousemove', onMouseMove)
      document.addEventListener('mouseup', onMouseUp)
      document.addEventListener('mousemove', onAiMouseMove)
      document.addEventListener('mouseup', onAiMouseUp)
      loadCreationConfig()
    })

    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
      document.removeEventListener('mousemove', onAiMouseMove)
      document.removeEventListener('mouseup', onAiMouseUp)
    })

    // ===== 从后台创作配置读取尺寸标记配置 =====
    async function loadCreationConfig() {
      try {
        const res = await getPublicCreationConfigByGroup('size_mark')
        const list = res.data || res.rows || []
        const map = {}
        list.forEach(c => { map[c.configKey] = c })

        // ---- 线条样式 ----
        const lineCfg = map.line_styles
        if (lineCfg && lineCfg.configValue) {
          // line_styles 是选项列表，这里不做模板替换（模板是硬编码 SVG），仅保留配置
        }

        // ---- 输出尺寸 ----
        const sizeCfg = map.output_sizes || map.size_options || map.size_presets
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

        // ---- 模板选择 ----
        const tplCfg = map.templates
        if (tplCfg && tplCfg.configValue) {
          const arr = JSON.parse(tplCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            templateOptions.value = arr.map(s => ({ label: s.label || s.value, value: s.value }))
          }
        }

        // ---- 语言列表（优先从尺寸标记配置加载，回退到通用配置） ----
        try {
          const langCfg = map.language_options
          if (langCfg && langCfg.configValue) {
            const items = JSON.parse(langCfg.configValue)
            if (Array.isArray(items) && items.length) {
              languages.value = items.filter(l => l.value)
            }
          } else {
            // 回退到通用配置
            const commonRes = await getPublicCreationConfigByGroup('common')
            const commonList = commonRes.data || commonRes.rows || []
            const commonMap = {}
            commonList.forEach(c => { commonMap[c.configKey] = c })
            const commonLangCfg = commonMap.languages
            if (commonLangCfg && commonLangCfg.configValue) {
              const items = JSON.parse(commonLangCfg.configValue)
              if (Array.isArray(items) && items.length) {
                languages.value = items.filter(l => l.value)
              }
            }
          }
        } catch { /* use defaults */ }

        // 加载提示词映射
        await loadPromptMap()
      } catch { /* use defaults */ }
    }

    // 加载提示词库映射
    async function loadPromptMap() {
      try {
        const res = await listPromptLibraryBatch('opt_language', 'size_mark')
        const items = res.data || res || []
        const map = {}
        items.forEach(item => {
          if (item.promptKey && item.promptText) {
            map[item.promptKey] = item.promptText
          }
        })
        promptMap.value = map
      } catch {
        promptMap.value = {}
      }
    }

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
        if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
      } catch (e) {
        if (e?.message?.includes('已取消')) return
        console.error('尺寸标记生成失败:', e)
        ElMessage.error('生成失败，请稍后重试')
      } finally {
        generating.value = false
      }
    }

    function applyPrompt() {
      chatPrompt.value = `请自动生成清晰的尺寸标记图，标注宽度${dimWidth.value}${unit.value}、长度${dimDepth.value}${unit.value}、高度${dimHeight.value}${unit.value}，风格简洁，适合电商平台使用。`
    }

    async function handleGenerate(opts = {}) {
      const text = aiAssistantRef.value?.inputText?.trim() || ''
      if (!originalFile.value) { ElMessage.warning('请先上传产品图片'); return }
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      try {
        const basePrompt = `生成尺寸标记图，宽度${dimWidth.value}${unit.value}、长度${dimDepth.value}${unit.value}、高度${dimHeight.value}${unit.value}，风格简洁，适合电商平台`
        const sizeText = effectiveOutputSize.value ? `输出图片尺寸为 ${effectiveOutputSize.value}，` : ''
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const prompt = boostText ? `${basePrompt}；${sizeText}${text ? text + '。' : ''}约束：${boostText}。` : `${basePrompt}${sizeText ? '。' + sizeText : ''}${text ? '。' + text : ''}`
        const extraOptions = {}
        if (effectiveOutputSize.value) extraOptions.output_size = effectiveOutputSize.value
        await gen.fullGenerate([originalFile.value], prompt, { ...extraOptions, consumePoints: 2, featureName: 'size_mark', title: '尺寸标记生成', n: 1, model: opts.model })
        if (gen.resultImages.value.length > 0) {
          resultImages.value = gen.resultImages.value
          aiAssistantRef.value?.addResultImages(gen.resultImages.value)
        }
      } catch (e) {
        if (e?.message?.includes('已取消')) return
        console.error('尺寸标记生成失败:', e)
        const isTimeout = e?.code === 'ECONNABORTED'
          || /timeout|超时|人数过多|繁忙|busy/i.test(e?.message || '')
        ElMessage.error(isTimeout
          ? '当前模型使用人数过多，可选用其他模型生图或稍后再试'
          : '生成失败，请稍后重试')
      }
    }

    // ---- 生成状态（供 AiAssistant 组件使用） ----
    const isGenerating = computed(() => gen.generating.value)
    const genProgress = computed(() => gen.progress.value)
    const genStatus = computed(() => gen.statusText.value)
    const genError = computed(() => gen.error.value)
    const aiAssistantRef = ref(null)

    // ===== 反推提示词 =====
    const reverseDialogVisible = ref(false)
    const reverseImageFile = ref(null)
    const reverseImagePreview = ref('')
    const reverseResult = ref('')
    const reverseLoading = ref(false)
    const REVERSE_DEFAULT_PROMPT = `请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。 输出结果应具有高度可复用性，能直接用于引导图像生成。`
    const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)

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
    const REVERSE_MAX_SIZE = 1.5 * 1024 * 1024

    async function handleReverseFile(file) {
      if (!REVERSE_ALLOWED_TYPES.includes(file.type)) {
        ElMessage.error('仅支持 JPG / PNG / WebP 格式的图片')
        return
      }
      let targetFile = file
      if (targetFile.size > REVERSE_MAX_SIZE) {
        targetFile = await compressImage(targetFile, 1.5)
      }
      reverseImageFile.value = targetFile
      reverseResult.value = ''
      const reader = new FileReader()
      reader.onload = (ev) => { reverseImagePreview.value = ev.target.result }
      reader.readAsDataURL(targetFile)
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
        ElMessage.error('复制失败')
      }
    }

    function clearWorkspaceImages() {
      originalImage.value = null
      uploadedFiles.value = []
      resultImages.value = []
      outputSize.value = ''
      customWidth.value = 1000
      customHeight.value = 1000
      selectedTemplate.value = 'standard'
      language.value = 'zh-CN'
      unit.value = 'cm'
      gen.reset()
    }

    return {
      originalImage, uploadedFiles,
      resultImages,
      configCollapsed,
      generating, zoom, unit,
      dimWidth, dimDepth, dimHeight,
      lineStyle, lineColor,
      sizeOptions, outputSize, customWidth, customHeight, effectiveOutputSize,
      templateOptions,
      selectedTemplate,
      language, languages,
      productViews,
      sections,
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
      applyPrompt,
      startColResize, startAiResize,
      // ---- AiAssistant ----
      isGenerating, genProgress, genStatus, genError, aiAssistantRef, handleGenerate,
      clearWorkspaceImages,
      // ---- 反推提示词 ----
      reverseDialogVisible, reverseImageFile, reverseImagePreview, reverseResult, reverseLoading,
      reversePromptInput, openReversePromptDialog, triggerReverseUpload, handleReverseDrop,
      clearReverseImage, submitReversePrompt, copyResult,
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

/* Result grid */
.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 8px;
  width: 100%;
  height: 100%;
  min-height: 300px;
  padding: 8px;
}
.result-card {
  border-radius: 8px;
  overflow: hidden;
  background: #F7F9FC;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.result-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
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
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }

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
  .steps-bar { padding: 0 0 8px; gap: 4px; }
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