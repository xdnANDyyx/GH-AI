<template>
  <div class="workspace-page">
    <!-- Steps bar -->
    <div class="steps-bar">
      <template v-for="(s, i) in workflowSteps" :key="i">
        <div class="step-item" :class="getStepClass(i + 1, 3)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
        <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
      </template>
    </div>

    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas (50%) ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">


        <!-- Canvas Area -->
        <div class="canvas-box"
          @drop.prevent="handleDrop"
        >
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div v-if="!originalImage" class="canvas-placeholder" @click="triggerUpload">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <h3>拖拽图片到画布，或点击右侧上传</h3>
            <p>支持 JPG / PNG，建议尺寸 ≥ 2000px</p>
          </div>
          <div class="canvas-result" v-else-if="resultImages.length">
            <img :src="resultImages[activeResult]?.url || resultImages[activeResult]" class="result-img" />
            <div class="result-nav" v-if="resultImages.length > 1">
              <button @click.stop="activeResult = Math.max(0, activeResult - 1)" :disabled="activeResult === 0">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
              </button>
              <span>{{ activeResult + 1 }} / {{ resultImages.length }}</span>
              <button @click.stop="activeResult = Math.min(resultImages.length - 1, activeResult + 1)" :disabled="activeResult === resultImages.length - 1">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </div>
          </div>
          <div class="upload-preview" v-else-if="originalImage">
            <img :src="originalImage" class="preview-img" />
            <div class="preview-overlay">
              <button class="preview-del-btn" @click.stop="clearImage">✕</button>
            </div>
          </div>
        </div>

        <div class="canvas-bottom-bar">
          <span>提示：建议上传高质量的产品图片，以获得更好的生成效果。</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ config -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowLeft v-if="!configCollapsed" /><ArrowRight v-else /></el-icon>
        </div>
      </div>

      <!-- ===== CENTER: Config Panel ===== -->
      <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
        <el-scrollbar v-show="!configCollapsed">
          <div class="config-inner">
            <!-- 创作配置 标题 (带全部展开/折叠) -->
            <div class="panel-header" @click="toggleAllSections">
              <span>创作配置</span>
              <span class="panel-toggle-all">{{ allExpanded ? '全部折叠 ▲' : '全部展开 ▼' }}</span>
            </div>

            <!-- Section: 上传图片 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label">
                  上传图片
                  <span style="font-size:11px;color:#EF4444;font-weight:400;">（必选）</span>
                </span>
                <span class="expand-text">
                  {{ sections.upload ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.upload">
                <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                  <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                  <p class="panel-upload-text">上传商品图</p>
                  <p class="panel-upload-hint">支持 JPG / PNG，最多 10 张</p>
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

            <!-- Section: 参考图 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('ref')">
                <span class="section-label">
                  参考图
                  <span style="font-size:11px;color:#6B7280;font-weight:400;">（可选）</span>
                </span>
                <span class="expand-text">
                  {{ sections.ref ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.ref }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.ref">
                <div class="panel-upload-zone small" @click.stop="triggerRefUpload" @dragover.prevent @drop.prevent="handleRefDrop">
                  <el-icon :size="24" color="#9CA3AF"><PictureFilled /></el-icon>
                  <p class="panel-upload-hint">{{ refImage ? '已选择参考图' : '点击上传参考图' }}</p>
                </div>
                <div v-if="refImage" class="ref-preview-row">
                  <img :src="refImage" class="ref-thumb" />
                  <button class="ref-remove" @click.stop="refImage = ''">×</button>
                </div>
                <p class="upload-note">参考图用于提供风格、构图或场景参考（非必需）</p>
              </div>
            </div>

            <!-- Section: 平台与语言 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('platform')">
                <span class="section-label">平台与语言</span>
                <span class="expand-text">
                  {{ sections.platform ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.platform }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.platform">
                <span class="config-label">目标平台</span>
                <div class="platform-grid">
                  <div
                    v-for="p in platforms"
                    :key="p"
                    class="platform-btn"
                    :class="{ active: activePlatform === p }"
                    @click="activePlatform = p"
                  >{{ p }}</div>
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
                <span class="section-label">画布尺寸</span>
                <span class="expand-text">
                  {{ sections.size ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.size }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.size">
                <span class="config-label">选择尺寸</span>
                <select class="form-select full" v-model="selectedSize">
                  <option v-for="s in sizeOptions" :key="s.value" :value="s.value">{{ s.label }} ({{ s.value }})</option>
                </select>
                <p class="select-hint">© 亚马逊主图建议：2000px × 2000px 以上效果更佳</p>
              </div>
            </div>

            <!-- Section: 主图用途 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('purpose')">
                <span class="section-label">
                  主图用途
                  <span class="section-sub">（选择本次主图的主要目的）</span>
                </span>
                <span class="expand-text">
                  {{ sections.purpose ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.purpose }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.purpose">
                <div class="purpose-grid">
                  <div
                    v-for="p in purposes"
                    :key="p"
                    class="purpose-btn"
                    :class="{ active: activePurpose === p }"
                    @click="activePurpose = p"
                  >{{ p }}</div>
                </div>
              </div>
            </div>

            <!-- Section: 核心卖点 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('sellingPoints')">
                <span class="section-label">
                  核心卖点
                  <span class="section-sub">（选择1~3个卖点）</span>
                </span>
                <span class="expand-text">
                  {{ sections.sellingPoints ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.sellingPoints }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.sellingPoints">
                <div class="sp-header">
                  <span class="config-label" style="margin-bottom:0;">常用卖点</span>
                  <span class="add-link">+ 自定义卖点</span>
                </div>
                <div class="sp-grid">
                  <div
                    v-for="sp in sellingPoints"
                    :key="sp"
                    class="sp-tag"
                    :class="{ active: activeSellingPoints.includes(sp) }"
                    @click="toggleSellingPoint(sp)"
                  >{{ sp }}</div>
                </div>
              </div>
            </div>

            <!-- Section: 生成设置 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('output')">
                <span class="section-label">生成设置</span>
                <span class="expand-text">
                  {{ sections.output ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.output }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.output">
                <span class="config-label">生成数量</span>
                <div class="gen-row">
                  <button
                    v-for="n in [1,2,3,4,5]"
                    :key="n"
                    class="gen-btn"
                    :class="{ active: generateCount === n }"
                    @click="generateCount = n"
                  >{{ n }}张</button>
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
                <div class="prompt-boost-row">
                  <label class="boost-label">镜头距离</label>
                  <PromptLibrarySelect ref="boostCameraRef" category="camera" key-prefix="camera.distance." v-model="boostCamera" placeholder="选择镜头距离" />
                </div>
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
          <h3>AI 助手</h3>
          <button class="ai-clear-btn" @click="clearChat">清空对话</button>
        </div>

        <!-- Suggestions -->
        <div class="ai-suggestions">
          <span class="ai-sug-tag" @click="useSuggestion('如何提升主图点击率？')">如何提升主图点击率？</span>
          <span class="ai-sug-tag" @click="useSuggestion('亚马逊主图规范是什么？')">亚马逊主图规范是什么？</span>
          <span class="ai-sug-tag" @click="useSuggestion('推荐更适合的卖点')">推荐更适合的卖点</span>
        </div>

        <!-- Chat messages -->
        <div class="ai-chat" ref="chatContainer">
          <div class="chat-msg bot">
            <div class="chat-avatar">AI</div>
            <div class="chat-bubble">您好！我是光合AI助手，有什么可以帮您？</div>
          </div>
          <div v-for="(msg, i) in chatMessages" :key="i" class="chat-msg" :class="msg.role">
            <div v-if="msg.role === 'bot'" class="chat-avatar">AI</div>
            <div class="chat-bubble">{{ msg.text }}</div>
          </div>
          <div v-if="generating" class="chat-msg bot">
            <div class="chat-avatar">AI</div>
            <div class="chat-bubble">正在为您生成中...</div>
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
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            -2积分
          </button>
        </div>
      </div>
    </div>

    <!-- Hidden file inputs -->
    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />
    <input type="file" ref="refFileInput" accept="image/*" hidden @change="handleRefFileSelect" />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ArrowDown, ArrowLeft, ArrowRight, UploadFilled, PictureFilled } from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import { aiDialogue } from '@/api/customer'
import { ElMessage } from 'element-plus'

export default {
  name: 'HeroImageView',
  components: { ArrowDown, ArrowLeft, ArrowRight, UploadFilled, PictureFilled, PromptLibrarySelect },
  setup() {
    // ---- Canvas Interactions ----
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-box',
    //   defaultName: 'hero-image',
    // })
    const gen = useImageGeneration('main_image')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

    // ---- State ----
    const originalImage = ref(null)
    const configCollapsed = ref(false)
    const refImage = ref(null)
    const productFiles = ref([])
    const uploadedFiles = ref([])
    const generating = ref(false)
    const zoom = ref(100)
    const resultImages = ref([])
    const activeResult = ref(0)

    // Config sections (all expanded by default)
    const sections = reactive({
      upload: true,
      ref: true,
      platform: true,
      size: true,
      purpose: true,
      sellingPoints: true,
      output: true,
      promptBoost: false,
    })

    const boostProduct = ref('')
    const boostMaterial = ref('')
    const boostCamera = ref('')
    const boostProductRef = ref(null)
    const boostMaterialRef = ref(null)
    const boostCameraRef = ref(null)

    // Platform & Language
    const platforms = ['亚马逊', 'Shopee', 'Lazada', '速卖通', '淘宝', '京东', '独立站', '其他']
    const activePlatform = ref('亚马逊')
    const languages = [
      { label: '英语（美国）', value: 'en-US' },
      { label: '英语（英国）', value: 'en-GB' },
      { label: '中文（简体）', value: 'zh-CN' },
      { label: '日语', value: 'ja-JP' },
      { label: '德语', value: 'de-DE' },
      { label: '法语', value: 'fr-FR' },
      { label: '西班牙语', value: 'es-ES' },
    ]
    const language = ref('en-US')

    // Size options
    const sizeOptions = [
      { label: '1:1', value: '1:1' },
      { label: '3:4', value: '3:4' },
      { label: '4:3', value: '4:3' },
      { label: '16:9', value: '16:9' },
      { label: '9:16', value: '9:16' },
      { label: '2:3', value: '2:3' },
    ]
    const selectedSize = ref('1:1')

    // Purpose
    const purposes = ['新品上市', '提升转化', '季节/节日', '促销活动', '品牌宣传', '其他用途']
    const activePurpose = ref('新品上市')

    // Selling points
    const sellingPoints = [
      '高品质材料', '耐用性强', '舒适体验', '易于安装',
      '多功能', '大容量收纳', '环保健康', '节省空间',
      '防水防污', '安全可靠', '轻便便携', '设计感强',
    ]
    const activeSellingPoints = ref(['高品质材料', '耐用性强', '舒适体验'])

    function toggleSellingPoint(sp) {
      const idx = activeSellingPoints.value.indexOf(sp)
      if (idx > -1) {
        activeSellingPoints.value.splice(idx, 1)
      } else if (activeSellingPoints.value.length < 3) {
        activeSellingPoints.value.push(sp)
      }
    }

    // Generate count
    const generateCount = ref(3)

    // Prompt
    const chatPrompt = ref('')
    const chatMessages = ref([])

    // ---- Computed ----
    const allExpanded = computed(() => {
      return Object.values(sections).every(v => v)
    })

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
        const url = URL.createObjectURL(f)
        uploadedFiles.value.push(url)
        productFiles.value.push(f)
        if (!originalImage.value) originalImage.value = url
      }
    }
    function clearImage() {
      originalImage.value = null
      resultImages.value = []
      activeResult.value = 0
      productFiles.value = []
      uploadedFiles.value = []
    }
    function removeProductFile(index) {
      const removed = uploadedFiles.value[index]
      uploadedFiles.value.splice(index, 1)
      productFiles.value.splice(index, 1)
      if (originalImage.value === removed) {
        originalImage.value = uploadedFiles.value[0] || null
      }
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
      chatPrompt.value = text
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
        chatMessages.value.push({ role: 'bot', text: res?.data?.reply || '已为您生成高转化主图设计。' })
        if (productFiles.value.length && !resultImages.value.length) {
          if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
          try {
            const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText, boostCameraRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
            const fullPrompt = boostText ? `${text}。约束：${boostText}。` : text
            await gen.fullGenerate(productFiles.value, fullPrompt, { consumePoints: 2, featureName: 'main_image', title: '主图设计', n: 1 })
            if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
          } catch (e) { console.error('主图生成失败:', e) }
        }
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
      originalImage, refImage, productFiles, uploadedFiles,
      generating, zoom, resultImages, activeResult,
      sections,
      platforms, activePlatform, languages, language,
      sizeOptions, selectedSize,
      purposes, activePurpose,
      sellingPoints, activeSellingPoints, toggleSellingPoint,
      generateCount,
      chatPrompt, chatMessages,
      allExpanded,
      canvasFlex, configFlex, aiFlex,
      configCollapsed,
      aiPanel,
      fileInput, refFileInput,
      workflowSteps, getStepClass, isStepLineDone,
      boostProduct, boostMaterial, boostCamera, boostProductRef, boostMaterialRef, boostCameraRef,
      triggerUpload, triggerRefUpload, handleFileSelect, handleRefFileSelect,
      handleDrop, handleRefDrop, clearImage, removeProductFile,
      undo, redo, clearCanvas, fitToScreen, toggleFullscreen,
      toggleAllSections, toggleSection,
      useSuggestion, sendMessage, clearChat,
      startColResize, startAiResize,
      // canvasUI, handleCanvasExport,
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
.prompt-boost-row .boost-label { display: block; font-size: 12px; color: #6B7280; margin-bottom: 4px; }

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
  z-index: 6;
}
.col-divider-wrapper .col-divider {
  width: 6px;
  background: transparent;
  cursor: col-resize;
  flex-shrink: 0;
  position: relative;
  z-index: 5;
  transition: background 0.2s;
  height: 100%;
}
.col-divider-wrapper .col-divider:hover,
.col-divider-wrapper .col-divider:active { background: #2563FF; }
.config-toggle-btn {
  position: absolute;
  top: 50%;
  right: -12px;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #D1D5DB;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  color: #6B7280;
  transition: all .2s;
}
.config-toggle-btn:hover { border-color: var(--gh-primary, #2563FF); color: var(--gh-primary, #2563FF); }

/* ============================================================
   Config Column collapsed
   ============================================================ */
.config-col.collapsed {
  flex: 0 0 0 !important;
  min-width: 0 !important;
  width: 0 !important;
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
  background: #F7F9FC;
  min-width: 0;
}

.canvas-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E8EDF5;
  flex-shrink: 0;
  gap: 8px;
}
.toolbar-left, .toolbar-center, .toolbar-right {
  display: flex; align-items: center; gap: 4px;
}
.toolbar-center { flex: 1; justify-content: center; }
.tb-btn {
  border: 1px solid #E8EDF5; background: #fff; padding: 5px 10px;
  border-radius: 6px; font-size: 12px; color: #1F2937; cursor: pointer;
  display: flex; align-items: center; gap: 4px;
}
.tb-btn:hover { background: #F0F4FF; border-color: #2563FF; }
.tb-text-btn {
  border: none; background: none; padding: 5px 8px; font-size: 12px;
  color: #6B7280; cursor: pointer; display: flex; align-items: center; gap: 3px;
  border-radius: 4px;
}
.tb-text-btn:hover { color: #2563FF; background: #F0F4FF; }
.tb-icon-btn {
  width: 24px; height: 24px; border: 1px solid #E8EDF5; border-radius: 6px;
  background: #fff; cursor: pointer; display: flex; align-items: center;
  justify-content: center; font-size: 12px; color: #6B7280; padding: 0;
}
.tb-icon-btn:hover { border-color: #2563FF; color: #2563FF; }
.tb-sep { width: 1px; height: 16px; background: #E8EDF5; }
.zoom-val { font-size: 12px; color: #1F2937; padding: 0 6px; min-width: 35px; text-align: center; }

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
  cursor: pointer;
}

.canvas-placeholder {
  text-align: center;
  color: #9CA3AF;
  padding: 20px;
}
.canvas-placeholder svg { width: 48px; height: 48px; margin-bottom: 12px; opacity: 0.4; }
.canvas-placeholder h3 { font-size: 14px; color: #6B7280; margin-bottom: 6px; font-weight: 500; }
.canvas-placeholder p { font-size: 12px; color: #9CA3AF; }

.canvas-result {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.result-img {
  max-width: 100%; max-height: 100%; object-fit: contain; border-radius: 8px;
}
.result-nav {
  position: absolute; bottom: 10px;
  display: flex; align-items: center; gap: 8px;
  background: rgba(0,0,0,0.5); color: #fff; padding: 4px 10px;
  border-radius: 20px; font-size: 12px;
}
.result-nav button {
  background: none; border: none; color: #fff; cursor: pointer;
  padding: 2px; display: flex; align-items: center; justify-content: center;
  opacity: 0.7; transition: opacity 0.2s;
}
.result-nav button:hover:not(:disabled) { opacity: 1; }
.result-nav button:disabled { opacity: 0.3; cursor: default; }

.upload-preview {
  position: relative;
  max-width: 80%;
  max-height: 80%;
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

.canvas-bottom-bar {
  padding: 6px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
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
  color: var(--gh-primary, #2563FF);
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
  padding: 10px 16px;
  transition: opacity 0.2s;
}
.section-header:hover { opacity: 0.75; }
.section-label { font-size: 13px; font-weight: 600; color: #1F2937; }
.section-sub { font-size: 11px; color: #6B7280; font-weight: 400; }
.expand-text {
  font-size: 11px; color: #2563FF; display: flex; align-items: center; gap: 2px;
}
.expand-arrow { transition: transform 0.25s; }
.expand-arrow.expanded { transform: rotate(180deg); }

.section-body { padding: 0 16px 10px; }

.config-label {
  display: block;
  font-size: 12px; color: #6B7280; margin-bottom: 4px;
}

/* Panel upload zone */
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

/* Platform grid */
.platform-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px;
  margin-top: 4px;
}
.platform-btn {
  display: flex; align-items: center; justify-content: center;
  padding: 7px 6px; border: 1px solid #E8EDF5; border-radius: 8px;
  font-size: 12px; color: #4B5563; cursor: pointer; background: #fff;
  text-align: center; transition: all 0.15s;
}
.platform-btn:hover { border-color: #2563FF; }
.platform-btn.active { border-color: #2563FF; background: #EEF2FF; color: #2563FF; font-weight: 600; }

/* Form select */
.form-select {
  width: 100%;
  padding: 7px 10px; border: 1px solid #E8EDF5; border-radius: 8px;
  font-size: 13px; outline: none; background: #fff; color: #1F2937;
  cursor: pointer; margin-top: 2px;
}
.form-select:focus { border-color: #2563FF; }

.select-hint { font-size: 11px; color: #9CA3AF; margin-top: 6px; }

/* Purpose grid */
.purpose-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;
}
.purpose-btn {
  border: 1.5px solid #E8EDF5; border-radius: 8px;
  padding: 10px 6px; text-align: center; font-size: 12px;
  color: #1F2937; cursor: pointer; background: #fff;
  transition: all 0.15s; display: flex; flex-direction: column; align-items: center; gap: 2px;
}
.purpose-btn:hover { border-color: #2563FF; }
.purpose-btn.active { border-color: #2563FF; background: #EFF4FF; color: #2563FF; font-weight: 600; }

/* Selling points */
.sp-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 6px;
}
.add-link { font-size: 12px; color: #2563FF; cursor: pointer; font-weight: 400; }
.sp-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.sp-tag {
  border: 1px solid #E8EDF5; border-radius: 6px;
  padding: 6px 4px; text-align: center; font-size: 12px;
  color: #1F2937; cursor: pointer; background: #fff;
  transition: all 0.15s;
}
.sp-tag:hover { border-color: #2563FF; color: #2563FF; }
.sp-tag.active {
  border-color: #2563FF; background: #EFF4FF; color: #2563FF; font-weight: 500;
}

/* Generate settings */
.gen-row { display: flex; align-items: center; gap: 6px; margin-top: 4px; flex-wrap: wrap; }
.gen-btn {
  border: 1px solid #E8EDF5; border-radius: 6px;
  padding: 5px 12px; font-size: 12px; color: #1F2937;
  cursor: pointer; background: #fff; transition: all 0.15s;
}
.gen-btn:hover { border-color: #2563FF; }
.gen-btn.active { border-color: #2563FF; background: #EFF4FF; color: #2563FF; font-weight: 600; }

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
  padding-bottom: 10px;
  border-bottom: 1px solid #F3F4F6;
}
.ai-header h3 { font-size: 14px; font-weight: 600; color: #1F2937; margin: 0; }
.ai-clear-btn {
  font-size: 12px; color: #2563FF; background: none; border: none;
  cursor: pointer; padding: 2px 6px; border-radius: 4px;
}
.ai-clear-btn:hover { background: #F0F4FF; }

.ai-suggestions {
  display: flex; flex-wrap: wrap; gap: 6px;
  padding: 8px 0 12px;
}
.ai-sug-tag {
  background: #EFF4FF; color: #2563FF; font-size: 11px;
  padding: 4px 10px; border-radius: 12px; cursor: pointer;
  white-space: nowrap; transition: background 0.15s;
}
.ai-sug-tag:hover { background: #DCE6FF; }

.ai-chat {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
  min-height: 100px;
}
.chat-msg {
  display: flex; gap: 8px; margin-bottom: 10px;
}
.chat-msg.user { flex-direction: row-reverse; }
.chat-msg.user .chat-bubble { background: #2563FF; color: #fff; }
.chat-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  background: #2563FF; color: #fff; display: flex; align-items: center;
  justify-content: center; font-size: 10px; font-weight: 700; flex-shrink: 0;
}
.chat-bubble {
  background: #F7F9FC; border-radius: 10px; padding: 8px 12px;
  font-size: 13px; color: #1F2937; line-height: 1.5; max-width: 85%;
  word-break: break-word;
}

.chat-input-area { margin-top: auto; }
.chat-input {
  width: 100%;
  min-height: 56px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
  color: #1F2937;
  resize: none;
  font-family: inherit;
  outline: none;
  background: #FAFBFC;
}
.chat-input:focus { border-color: #2563FF; }
.chat-input::placeholder { color: #9CA3AF; }

.chat-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 8px;
}
.chat-counter { font-size: 11px; color: #9CA3AF; }
.chat-cost { font-size: 11px; color: #9CA3AF; }
.chat-send {
  display: flex; align-items: center; gap: 4px;
  background: #2563FF; color: #fff; border: none;
  padding: 6px 14px; border-radius: 6px; font-size: 13px;
  cursor: pointer; font-weight: 500;
  transition: opacity 0.2s;
}
.chat-send:hover:not(:disabled) { opacity: 0.9; }
.chat-send:disabled { opacity: 0.5; cursor: not-allowed; }

/* ============================================================
   Responsive
   ============================================================ */
@media (max-width: 1024px) {
  .three-col {
    flex-wrap: wrap;
  }
  .canvas-col { flex: 0 0 100% !important; height: 40%; }
  .config-col { flex: 0 0 50% !important; height: 30%; }
  .ai-col { flex: 0 0 50% !important; height: 30%; }
  .col-divider { display: none; }
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 1 !important; height: auto; min-height: 300px; }
  .config-col { flex: none !important; height: auto; max-height: 40vh; }
  .ai-col { flex: none !important; height: auto; max-height: 40vh; min-width: 0; }
  .col-divider { display: none; }
  .ai-resize-handle { display: none; }
}
</style>