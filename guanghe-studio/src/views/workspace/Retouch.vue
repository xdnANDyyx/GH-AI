

<template>
  <div class="workspace-page">
    <!-- Three-column layout -->
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
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <!-- 未生成：显示空状态 -->
          <div v-if="!processedImage" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>AI 精修结果生成后将显示在此处</h3>
            <p>请在右侧上传图片并点击发送</p>
          </div>
          <!-- 已生成：显示结果图 -->
          <div v-else class="result-view">
            <img :src="processedImage" class="result-img" alt="精修结果" @contextmenu.prevent="openHandoffMenu($event, processedImage)" />
          </div>
        </div>

        <div class="canvas-bottom-bar">
          <span>提示：在左侧上传需要精修的图片，选择右侧精修功能，或在AI助手中输入您的需求。</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ config -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowLeft v-if="!configCollapsed" /><ArrowRight v-else /></el-icon>
        </div>
      </div>

      <!-- ===== CENTER: Config Panel (25%) ===== -->
      <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
        <el-scrollbar v-show="!configCollapsed">
          <div class="config-inner">
            <!-- 创作配置 标题 (带全部展开/折叠) -->
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

            <!-- Section: 上传图片 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label">上传图片</span>
                <span class="expand-text">
                  {{ sections.upload ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.upload">
                <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                  <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                  <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                  <p class="panel-upload-hint">支持 JPG / PNG / WebP，最多 10 张</p>
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

            <!-- Section: 精修工具 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('retouch')">
                <span class="section-label">精修工具</span>
                <span class="expand-text">
                  {{ sections.retouch ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.retouch }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.retouch">
                <div class="tool-grid">
                  <div
                    v-for="tool in retouchTools"
                    :key="tool.key"
                    class="tool-card"
                    :class="{ active: activeTool === tool.key }"
                    @click="activeTool = tool.key"
                  >
                    <div class="tool-icon-svg" v-html="tool.svgIcon"></div>
                    <div class="tool-name">{{ tool.name }}</div>
                    <div class="tool-desc">{{ tool.desc }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Section: 输出设置 -->
            <div class="panel-section" :class="{ collapsed: !sections.output }">
              <div class="section-header" @click="toggleOutputSection">
                <span class="section-label">输出设置</span>
                <span class="expand-text">
                  {{ sections.output ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.output }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div class="section-body" v-show="sections.output">
                <div class="output-settings">
                  <div class="output-row">
                    <span class="output-label">画质</span>
                    <select v-model="outputQuality" class="form-select">
                      <option v-for="q in qualityOptions" :key="q.value" :value="q.value">{{ q.label }}</option>
                    </select>
                  </div>
                  <div class="output-row">
                    <span class="output-label">输出格式</span>
                    <select v-model="outputFormat" class="form-select">
                      <option v-for="f in formatOptions" :key="f.value" :value="f.value">{{ f.label }}</option>
                    </select>
                  </div>
                  <div class="output-row">
                    <span class="output-label">尺寸</span>
                    <select v-model="selectedSize" class="form-select">
                      <option v-for="s in sizeOptions" :key="s.value" :value="s.value">{{ s.label }} ({{ s.value }})</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- Divider handle: config ⇔ AI -->
      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== RIGHT: AI Panel (flex:1) ===== -->
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

            <!-- Section: 提示词增强（已隐藏） -->
            <!--
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
            -->
          </div>

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
            <p class="rp-upload-hint">支持 JPG/PNG/WebP，最多 20MB</p>
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

    <!-- Hidden file input -->
    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />

    <!-- 右键接力菜单：将精修结果发送到下一站（占位，预留扩展） -->
    <div
      v-if="handoffMenu.show"
      class="context-menu"
      :style="{ left: handoffMenu.x + 'px', top: handoffMenu.y + 'px' }"
      @click.stop
    >
      <div class="context-menu-item context-menu-cancel" @click="hideHandoffMenu">关闭菜单</div>
    </div>
  </div>
</template>

<script>
import { UploadFilled, ArrowDown, ArrowLeft, ArrowRight, MagicStick, DocumentCopy } from '@element-plus/icons-vue'
import { ref, reactive, computed, onMounted, onActivated, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicCreationConfigByGroup, reversePrompt, listPromptLibraryBatch } from '@/api/customer'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import { useImageHandoffStore } from '@/store'
import { urlToFile } from '@/utils/image'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'RetouchView',
  components: { PromptLibrarySelect, AiAssistant },
  setup() {
    const router = useRouter()
    // ---- Canvas Interactions ----
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-box',
    //   getImage: () => processedImage.value || originalImage.value || '',
    //   defaultName: 'retouch',
    // })
    const gen = useImageGeneration('render')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()
    const handoffStore = useImageHandoffStore()

    // ---- State ----
    const configCollapsed = ref(false)
    const processed = ref(false)
    const originalImage = ref(null)
    const processedImage = ref(null)
    const productFiles = ref([])
    const uploadedFiles = ref([])
    const generating = ref(false)
    const compareMode = ref(true)
    const zoom = ref(100)

    // Tool selection
    const activeTool = ref('smart-optimize')
    const allRetouchTools = [
      { key: 'one-click-repair', name: '一键修复', desc: '智能一键修复', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M4 20l7-9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><path d="M11 3l4 4-4 4-4-4 4-4z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M19 13l1 2 2 1-2 1-1 2-1-2-2-1 2-1 1-2z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round" fill="currentColor" opacity="0.2"/></svg>' },
      { key: 'smart-optimize', name: '智能优化', desc: '一键提升画质', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M12 3l2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5L12 3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>' },
      { key: 'defect-remove', name: '去瑕疵', desc: '去除划痕、污渍', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M12 2l2.5 6.5L21 11l-6.5 2.5L12 20l-2.5-6.5L3 11l6.5-2.5L12 2z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M8 8l8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
      { key: 'texture-enhance', name: '纹理增强', desc: '增强材质细节', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="13" y="3" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="3" y="13" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="13" y="13" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 2"/></svg>' },
      { key: 'color-adjust', name: '色彩调整', desc: '亮度、对比度', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/><path d="M12 3a9 9 0 010 18V3z" fill="currentColor" opacity="0.25"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.2"/></svg>' },
      { key: 'light-optimize', name: '光影优化', desc: '优化光影层次', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.5"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
      { key: 'bg-process', name: '背景处理', desc: '纯色 / 自定义', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M6 12l3-3 3 3 5-6 5 7H2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>' },
      { key: 'sharpen', name: '锐化增强', desc: '提升清晰度', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M12 2l10 10-10 10L2 12 12 2z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.2" fill="currentColor" opacity="0.1"/></svg>' },
      { key: 'denoise', name: '降噪处理', desc: '减少噪点', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/><path d="M8 8l8 8M16 8l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
    ]
    // 精修工具列表（从后台加载）
    const retouchTools = ref([...allRetouchTools])
    // 提示词映射：tool key → promptText
    const promptMap = ref({})

    // Config sections
    const sections = reactive({
      upload: true,
      retouch: true,
      output: true,
      promptBoost: false,
    })
    // 输出设置（从后台加载）
    const outputQuality = ref('high')
    const outputFormat = ref('png')
    const qualityOptions = ref([
      { label: '标准', value: 'standard' },
      { label: '高清', value: 'high' },
      { label: '超清', value: 'ultra' },
    ])
    const formatOptions = ref([
      { label: 'PNG', value: 'png' },
      { label: 'JPG', value: 'jpg' },
      { label: 'WebP', value: 'webp' },
    ])
    const defaultIntensity = ref(50)
    const maxGenerateCount = ref(5)
    const boostProduct = ref('')
    const boostMaterial = ref('')
    const boostProductRef = ref(null)
    const boostMaterialRef = ref(null)

    const sizeOptions = ref([
      { label: '1:1', value: '1:1', w: 20, h: 20 },
      { label: '4:3', value: '4:3', w: 24, h: 18 },
      { label: '3:4', value: '3:4', w: 18, h: 24 },
      { label: '16:9', value: '16:9', w: 28, h: 16 },
      { label: '9:16', value: '9:16', w: 16, h: 28 },
      { label: '3:2', value: '3:2', w: 24, h: 16 },
      { label: '2:3', value: '2:3', w: 16, h: 24 },
      { label: '自定义', value: 'custom', w: 18, h: 18 },
    ])
    const selectedSize = ref('1:1')

    // Prompt
    const promptTags = ref([])
    const showPromptInput = ref(false)
    const newPromptTag = ref('')

    // Chat
    const chatPrompt = ref('')
    const chatMessages = ref([])

    // ---- 跨页面图片接力（右键菜单） ----
    const handoffMenu = reactive({ show: false, x: 0, y: 0, imageUrl: '' })

    function openHandoffMenu(e, img) {
      const url = typeof img === 'string' ? img : (img?.url || '')
      handoffMenu.imageUrl = url
      // 防止菜单超出视口右侧
      const menuW = 200
      const maxLeft = window.innerWidth - menuW - 8
      handoffMenu.x = Math.min(e.clientX, maxLeft)
      handoffMenu.y = e.clientY
      handoffMenu.show = true
    }
    function hideHandoffMenu() {
      handoffMenu.show = false
    }
    function handleClickOutside(e) {
      if (handoffMenu.show && !e.target.closest('.context-menu')) {
        hideHandoffMenu()
      }
    }

    /**
     * 消费接力图片（来自 白底图 / 背景生成 的右键“放入产品精修”）
     * - 用 urlToFile 重建可上传的 File
     * - 用 FileReader 转 dataURL 作为展示原图（避免 blob: URL 在 keep-alive 重激活后失效）
     */
    async function consumeHandoffImage() {
      const pending = handoffStore.consume()
      if (!pending) return
      const url = pending.url
      if (!url) return
      try {
        const file = await urlToFile(url, 'retouch-source')
        productFiles.value.push(file)
        const reader = new FileReader()
        reader.onload = ev => {
          uploadedFiles.value.push(ev.target.result)
          // 若当前没有展示原图，则把接力图片作为主展示图
          if (!originalImage.value) originalImage.value = ev.target.result
          ElMessage.success('已载入接力图片，可直接进行精修')
        }
        reader.readAsDataURL(file)
      } catch (e) {
        console.warn('载入接力图片失败:', e)
        ElMessage.error('图片载入失败，请手动上传')
      }
    }

    // ---- Computed ----
    const allExpanded = computed(() => {
      return Object.values(sections).every(v => v)
    })

    // ---- Layout resize ----
    const _configWidthPx = ref(280)
    const _aiWidthPx = ref(360)
    const aiPanel = ref(null)
        const canvas3Flex = computed(() => '1 1 0%')
    const canvasFlex = canvas3Flex
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
        // 画布/配置栏分隔线：按比例缩放配置栏和AI栏
        const rightWidth = rect.right - e.clientX - 24
        const totalCurrent = _configWidthPx.value + _aiWidthPx.value + 12
        if (totalCurrent > 0 && rightWidth > 200) {
          const ratio = rightWidth / totalCurrent
          _configWidthPx.value = Math.max(150, Math.min(600, Math.round(_configWidthPx.value * ratio)))
          _aiWidthPx.value = Math.max(200, Math.min(800, Math.round(_aiWidthPx.value * ratio)))
        }
      } else if (resizeTarget === 'ai') {
        // 配置栏/AI栏分隔线：只调整AI栏宽度，配置栏不变
        const configEl = document.querySelector('.config-col')
        if (!configEl) return
        const configRect = configEl.getBoundingClientRect()
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

    // AI panel independent resize (right edge)
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
      document.addEventListener('click', handleClickOutside)
      loadCreationConfig()
      consumeHandoffImage()
    })

    onActivated(() => {
      consumeHandoffImage()
    })

    // ===== 从后台创作配置读取产品精修配置 =====
    async function loadCreationConfig() {
      try {
        const res = await getPublicCreationConfigByGroup('retouch')
        const list = res.data || res.rows || []
        const map = {}
        list.forEach(c => { map[c.configKey] = c })

        // ---- 精修工具 ----
        const toolsCfg = map.tools
        if (toolsCfg && toolsCfg.configValue) {
          const arr = JSON.parse(toolsCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            // 从后台工具列表过滤出已有的工具（保留 SVG 图标）
            const keySet = new Set(arr.map(t => typeof t === 'string' ? t : t.value))
            retouchTools.value = allRetouchTools.filter(t => keySet.has(t.key))
          }
        }

        // ---- 画质选项 ----
        const qualityCfg = map.quality_options
        if (qualityCfg && qualityCfg.configValue) {
          const arr = JSON.parse(qualityCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            qualityOptions.value = arr.map(s => ({ label: s.label || s.value, value: s.value }))
          }
        }

        // ---- 输出格式 ----
        const formatCfg = map.format_options
        if (formatCfg && formatCfg.configValue) {
          const arr = JSON.parse(formatCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            formatOptions.value = arr.map(s => ({ label: s.label || s.value, value: (s.value || '').toLowerCase() }))
          }
        }

        // ---- 默认精修强度 ----
        const intensityCfg = map.default_intensity
        if (intensityCfg && intensityCfg.configValue) {
          const n = Number(JSON.parse(intensityCfg.configValue))
          if (n > 0) defaultIntensity.value = n
        }

        // ---- 生成数量上限 ----
        const maxCountCfg = map.max_generate_count
        if (maxCountCfg && maxCountCfg.configValue) {
          const n = Number(JSON.parse(maxCountCfg.configValue))
          if (n > 0) maxGenerateCount.value = n
        }

        // 加载提示词映射
        await loadPromptMap()
      } catch { /* use defaults */ }
    }

    // 加载提示词库映射：将工具/尺寸的 value 映射到对应的提示词内容
    async function loadPromptMap() {
      try {
        const res = await listPromptLibraryBatch('opt_tool,opt_size', 'retouch')
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

    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
      document.removeEventListener('mousemove', onAiMouseMove)
      document.removeEventListener('mouseup', onAiMouseUp)
      document.removeEventListener('click', handleClickOutside)
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
        productFiles.value.push(f)
        if (!originalImage.value) originalImage.value = url
      }
    }
    function clearImage() {
      originalImage.value = null
      processed.value = false
      processedImage.value = null
      productFiles.value = []
      uploadedFiles.value = []
    }
    function removeProductFile(index) {
      uploadedFiles.value.splice(index, 1)
      productFiles.value.splice(index, 1)
    }

    function undo() { /* placeholder */ }
    function redo() { /* placeholder */ }
    function reset() { processed.value = false; processedImage.value = null }
    // function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
    // function zoomOut() { zoom.value = Math.max(10, zoom.value - 10) }
    function toggleFullscreen() { /* placeholder */ }

    function toggleAllSections() {
      const val = !allExpanded.value
      Object.keys(sections).forEach(k => sections[k] = val)
    }

    function toggleSection(key) {
      if (sections.hasOwnProperty(key)) sections[key] = !sections[key]
    }
    function toggleOutputSection() {
      sections.output = !sections.output
    }

    // ---- 生成状态（供 AiAssistant 组件使用） ----
    const isGenerating = computed(() => gen.generating.value)
    const genProgress = computed(() => gen.progress.value)
    const genStatus = computed(() => gen.statusText.value)
    const genError = computed(() => gen.error.value)
    const aiAssistantRef = ref(null)

    async function handleGenerate() {
      const text = aiAssistantRef.value?.inputText?.trim() || ''
      if (!productFiles.value.length) {
        ElMessage.warning('请先上传需要精修的图片')
        return
      }
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      try {
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const fullPrompt = boostText ? `${text}。约束：${boostText}。` : text
        await gen.fullGenerate(productFiles.value, fullPrompt, { consumePoints: 2, featureName: 'retouch', title: '产品精修', n: 1 })
        if (gen.resultImages.value.length > 0) {
          processed.value = true
          processedImage.value = gen.resultImages.value[0].url || gen.resultImages.value[0]
        }
      } catch (e) {
        console.error('精修生成失败:', e)
        const isTimeout = e?.code === 'ECONNABORTED'
          || /timeout|超时|人数过多|繁忙|busy/i.test(e?.message || '')
        ElMessage.error(isTimeout
          ? '当前模型使用人数过多，可选用其他模型生图或稍后再试'
          : '生成失败，请稍后重试')
      }
    }

    // ---- 反推提示词 ----
    const reverseDialogVisible = ref(false)
    const reverseImageFile = ref(null)
    const reverseImagePreview = ref('')
    const reverseResult = ref('')
    const reverseLoading = ref(false)
    const REVERSE_DEFAULT_PROMPT = `请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。 输出结果应具有高度可复用性，能直接用于引导图像生成。`
    const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)
    const REVERSE_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
    const REVERSE_MAX_SIZE = 20 * 1024 * 1024

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

    function clearWorkspaceImages() {
      originalImage.value = null
      processedImage.value = null
      productFiles.value = []
      uploadedFiles.value = []
      gen.reset()
    }

    return {
      configCollapsed,
      processed, originalImage, processedImage, productFiles, uploadedFiles,
      generating, compareMode, zoom,
      activeTool, retouchTools,
      sections,
      sizeOptions, selectedSize,
      promptTags, showPromptInput, newPromptTag,
      outputQuality, outputFormat, qualityOptions, formatOptions,
      defaultIntensity, maxGenerateCount, promptMap,
      toggleOutputSection,
      allExpanded,
      canvasFlex, configFlex, aiFlex,
      aiPanel,
      fileInput,
      workflowSteps, getStepClass, isStepLineDone,
      boostProduct, boostMaterial, boostProductRef, boostMaterialRef,
      triggerUpload, handleFileSelect, handleDrop, clearImage, removeProductFile,
      undo, redo, reset, toggleFullscreen,
      toggleAllSections, toggleSection,
      startColResize, startAiResize,
      // canvasUI, handleCanvasExport,
      // ---- 图片接力右键菜单 ----
      handoffMenu, openHandoffMenu, hideHandoffMenu,
      // ---- 反推提示词 ----
      reverseDialogVisible, reverseImageFile, reverseImagePreview, reverseResult,
      reverseLoading, reversePromptInput,
      openReversePromptDialog, triggerReverseUpload, handleReverseDrop,
      clearReverseImage, submitReversePrompt, copyResult,
      // ---- AiAssistant ----
      isGenerating, genProgress, genStatus, genError, aiAssistantRef, handleGenerate,
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
  flex-shrink: 0;
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
  flex: 1; height: 2px; background: #E8EDF5; min-width: 20px; margin: 0 8px;
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

/* ---- Column Divider + Toggle Wrapper ---- */
.col-divider-wrapper {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
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

.compare-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
  gap: 12px;
}
.compare-label {
  font-size: 12px; font-weight: 500;
  display: flex; align-items: center; gap: 6px;
}
.compare-label input[type="checkbox"] { cursor: pointer; transform: scale(1.1); }
.compare-btns { display: flex; gap: 6px; }
.compare-btns button {
  padding: 6px 12px; border: 1px solid #E8EDF5; border-radius: 8px;
  background: #fff; font-size: 12px; cursor: pointer; color: #6B7280;
  transition: all 0.15s;
}
.compare-btns button:hover { border-color: #2563FF; color: #2563FF; }

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

.result-view {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.result-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.upload-zone {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
}

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

/* Compare container */
.compare-container {
  display: flex;
  width: 100%; height: 100%;
  background: #fff;
}
.compare-side {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
}
.compare-side .label {
  position: absolute; top: 10px; left: 10px;
  background: rgba(0,0,0,0.5); color: #fff; font-size: 11px;
  padding: 3px 8px; border-radius: 6px; z-index: 2;
}
.compare-img-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8edf5, #f0f4fa);
  color: #9CA3AF; font-size: 12px;
}
.compare-img-placeholder img {
  max-width: 100%; max-height: 100%; object-fit: contain;
}
.compare-divider-bar {
  width: 4px; background: #fff; cursor: col-resize; flex-shrink: 0;
  position: relative; z-index: 3;
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
  position: relative;
}

/* Config toggle button */
.config-toggle-btn {
  width: 26px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #E8EDF5;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  color: #6B7280;
  box-shadow: 2px 0 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease;
  flex-shrink: 0;
  z-index: 10;
}
.config-toggle-btn:hover {
  background: #F0F4FF;
  color: #2563FF;
  border-color: #2563FF;
}

/* Collapsed state */
.config-col.collapsed {
  flex: 0 0 0 !important;
  min-width: 0;
  padding: 0;
  border: none;
  overflow: hidden;
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

.panel-section {
  margin-bottom: 4px;
  border-bottom: 1px solid #F3F4F6;
  padding-bottom: 12px;
}
.panel-section.collapsed { padding-bottom: 4px; }

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
.section-arrow {
  font-size: 14px; color: #9CA3AF; transition: transform 0.25s;
  flex-shrink: 0;
}
.section-arrow.expanded { transform: rotate(180deg); }

.section-body { padding: 0 16px 8px; }

/* Output settings */
.output-settings { padding: 0; }
.output-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 0; border-bottom: 1px solid #F7F9FC;
}
.output-row:last-child { border-bottom: none; }
.output-label { font-size: 12px; color: #6B7280; font-weight: 500; }
.form-select {
  padding: 6px 10px; border: 1px solid #E8EDF5; border-radius: 6px;
  font-size: 12px; outline: none; background: #fff; color: #1F2937;
  cursor: pointer;
}
.form-select:focus { border-color: #2563FF; }

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

/* Tool grid (in config panel) */
.tool-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 0 16px;
}
.tool-card {
  border: 1.5px solid #E8EDF5; border-radius: 10px;
  padding: 12px 8px 10px; text-align: center; cursor: pointer;
  transition: all 0.2s; background: #fff;
}
.tool-card:hover {
  border-color: #2563FF; background: #FAFBFF;
  transform: translateY(-1px); box-shadow: 0 2px 8px rgba(37,99,255,0.06);
}
.tool-card.active {
  border-color: #2563FF; background: #EEF2FF;
  box-shadow: 0 2px 8px rgba(37,99,255,0.1);
}
.tool-card.active .tool-icon-svg :deep(svg) { color: #2563FF; }
.tool-icon-svg {
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 6px;
}
.tool-icon-svg :deep(svg) {
  color: #6B7280; transition: color 0.2s; width: 22px; height: 22px;
}
.tool-card:hover .tool-icon-svg :deep(svg) { color: #2563FF; }
.tool-name {
  font-size: 12px; font-weight: 600; margin-bottom: 2px; color: #1F2937;
}
.tool-desc { font-size: 10px; color: #9CA3AF; line-height: 1.3; }

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

/* ============================================================
   Responsive
   ============================================================ */
@media (max-width: 1024px) {
  .steps-bar { padding: 0 0 8px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 12px; margin: 0 6px; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .config-col { flex: 0 0 50% !important; }
  .ai-col { flex: 0 0 50% !important; }
  .col-divider { display: none; }
  .tool-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .config-col { flex: 0 0 auto !important; max-height: 200px; overflow-y: auto; }
  .ai-col { flex: 1 1 auto !important; min-height: 250px; }
  .ai-resize-handle { display: none; }
  .tool-grid { grid-template-columns: repeat(3, 1fr); }
}

/* ============================================================
   右键接力菜单（跨页面图片传递）
   ============================================================ */
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 180px;
  max-width: 220px;
  background: #FFFFFF;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  padding: 4px;
  overflow: hidden;
}
.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  font-size: 13px;
  color: #1F2937;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.context-menu-item:hover {
  background: #EEF2FF;
  color: #4F46E5;
}
.context-menu-item .el-icon {
  font-size: 16px;
}
.context-menu-cancel {
  justify-content: center;
  color: #6B7280;
  border-top: 1px solid #F1F5F9;
  margin-top: 4px;
  border-radius: 6px 6px 0 0;
}
.context-menu-cancel:hover {
  background: #F8FAFC;
  color: #6B7280;
}

/* ============================================================
   反推提示词
   ============================================================ */
/* 入口按钮 */
.reverse-prompt-entry {
  margin: 0 0 12px 0;
  padding: 0 16px;
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

/* 模态框 */
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