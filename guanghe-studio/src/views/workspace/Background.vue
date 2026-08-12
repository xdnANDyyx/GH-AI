<template>
  <div class="workspace-page">
    <!-- Step Bar -->
    <div class="steps-bar">
      <template v-for="(s, i) in workflowSteps" :key="i">
        <div
          class="step-item"
          :class="getStepClass(i + 1, 2)"
        >
          <div class="step-num">{{ i + 1 }}</div>
          <span class="step-label">{{ s.label }}</span>
        </div>
        <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
      </template>
    </div>

    <!-- Three-column layout -->
    <div class="three-col">
      <!-- Canvas column -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <div class="canvas-box"
          @drop="handleDrop"
        >
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div v-if="productImages.length === 0" class="canvas-placeholder" @click="triggerUpload">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>拖拽图片到画布，或从右侧上传</h3>
            <p>支持 JPG / PNG / WebP 格式，最多 10 张</p>
          </div>
          <!-- 有结果图时：2×2 网格展示 -->
          <div v-else-if="resultImages.length > 0" class="result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, idx) in resultImages" :key="'r'+idx" class="result-card">
              <img :src="img.url || img" class="result-img" />
              <div class="result-actions">
                <button
                  class="result-action-btn favorite-btn"
                  :class="{ active: favoritedResults[idx] }"
                  @click.stop="toggleFavorite(idx, img)"
                  :title="favoritedResults[idx] ? '取消收藏' : '收藏'"
                >
                  <svg viewBox="0 0 24 24" :fill="favoritedResults[idx] ? '#FF4D4F' : 'none'" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                </button>
                <button
                  class="result-action-btn download-btn"
                  @click.stop="downloadResult(img, idx)"
                  title="下载"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                </button>
              </div>
            </div>
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>

          <!-- 仅上传商品图，无结果时 -->
          <div v-else class="canvas-result" :class="{ generating: isGenerating }">
            <img v-for="(img, idx) in productImages" :key="idx" :src="img" class="uploaded-img" />
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>
        </div>

        <!-- <div class="canvas-toolbar-bottom">
          <button class="tool-btn" @click="handleFit">适应屏幕</button>
          <button class="tool-btn">↩ 撤销</button>
          <button class="tool-btn">↪ 恢复</button>
          <button class="tool-btn" @click="zoomOut">−</button>
          <span class="zoom-label">{{ zoomLevel }}%</span>
          <button class="tool-btn" @click="zoomIn">+</button>
          <button class="tool-btn" @click="toggleFullscreen">⛶ 全屏</button>
        </div> -->

        <div class="canvas-bottom-bar">AI生成的内容仅供参考，请注意核对细节与版权信息。</div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ right panel -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'right')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowLeft v-if="!configCollapsed" /><ArrowRight v-else /></el-icon>
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

              <!-- 上传商品图 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('upload')">
                  <span class="section-label">上传商品图（最多10张）</span>
                  <span class="expand-text">
                    {{ sections.upload ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.upload" class="section-body">
                  <div class="upload-box" @click.stop="triggerUpload" @dragover.prevent @drop="handleDrop">
                    <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                    <p>点击或拖拽图片到此处上传</p>
                    <p class="hint">支持 JPG / PNG / WebP，最多 10 张</p>
                  </div>
                  <div class="uploaded-files">
                    <div v-for="(img, idx) in productImages" :key="'p'+idx" class="uploaded-thumb">
                      <div class="thumb">
                        <img :src="img" class="thumb-img" />
                        <span class="remove-btn" @click.stop="removeProductImage(idx)">✕</span>
                      </div>
                    </div>
                    <div v-if="productImages.length < 10" class="uploaded-thumb" @click.stop="triggerUpload">
                      <div class="add-btn">+</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 上传参考图 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('refUpload')">
                  <span class="section-label">上传参考图（可选）</span>
                  <span class="expand-text">
                    {{ sections.refUpload ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.refUpload }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.refUpload" class="section-body">
                  <div class="upload-box" @click.stop="triggerRefUpload" @dragover.prevent @drop="handleRefDrop">
                    <el-icon :size="28" color="#2563FF"><UploadFilled /></el-icon>
                    <p>点击或拖拽图片到此处上传</p>
                    <p class="hint">支持 JPG / PNG / WebP，最多 10 张</p>
                  </div>
                  <div class="uploaded-files">
                    <div v-for="(img, idx) in referenceImages" :key="'r'+idx" class="uploaded-thumb">
                      <div class="thumb">
                        <img :src="img" class="thumb-img" />
                        <span class="remove-btn" @click.stop="removeRefImage(idx)">✕</span>
                      </div>
                    </div>
                    <div v-if="referenceImages.length < 10" class="uploaded-thumb" @click.stop="triggerRefUpload">
                      <div class="add-btn">+</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 使用平台 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('platform')">
                  <span class="section-label">使用平台</span>
                  <span class="expand-text">
                    {{ sections.platform ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.platform }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.platform" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="p in platformOptions"
                      :key="p.value"
                      class="option-tag"
                      :class="{ active: selectedPlatform === p.value }"
                      @click="selectedPlatform = p.value"
                    >
                      {{ p.label }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 场景分类 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('scene')">
                  <span class="section-label">场景分类</span>
                  <span class="expand-text">
                    {{ sections.scene ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.scene }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.scene" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="sc in sceneOptions"
                      :key="sc.value"
                      class="option-tag"
                      :class="{ active: selectedScene === sc.value }"
                      @click="selectedScene = sc.value"
                    >
                      {{ sc.label }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 光线选择 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('light')">
                  <span class="section-label">光线选择</span>
                  <span class="expand-text">
                    {{ sections.light ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.light }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.light" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="l in lightOptions"
                      :key="l.value"
                      class="option-tag"
                      :class="{ active: selectedLight === l.value }"
                      @click="selectedLight = l.value"
                    >
                      {{ l.label }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 风格选择 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('style')">
                  <span class="section-label">风格选择</span>
                  <span class="expand-text">
                    {{ sections.style ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.style }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.style" class="section-body">
                  <div class="option-tags">
                    <div
                      v-for="st in styleOptions"
                      :key="st.value"
                      class="option-tag"
                      :class="{ active: selectedStyle === st.value }"
                      @click="selectedStyle = st.value"
                    >
                      {{ st.label }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 输出尺寸 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('size')">
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
                </div>
              </div>

              <!-- 提示词增强 -->
              <div class="config-section collapsible">
                <div class="panel-header collapsible" @click="toggleSection('promptBoost')">
                  <span class="section-label">提示词增强</span>
                  <span class="expand-text">
                    {{ sections.promptBoost ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.promptBoost }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div v-show="sections.promptBoost" class="section-body">
                  <div class="prompt-boost-row">
                    <label class="boost-label">产品类别</label>
                    <PromptLibrarySelect ref="boostProductRef" category="product" v-model="boostProduct" placeholder="选择产品类别" />
                  </div>
                  <div class="prompt-boost-row">
                    <label class="boost-label">材质</label>
                    <PromptLibrarySelect ref="boostMaterialRef" category="material" v-model="boostMaterial" placeholder="选择材质" />
                  </div>
                  <p class="section-helper">选用的约束词会自动拼接到生图提示词中</p>
                </div>
              </div>

              <!-- Generate button -->
              <div class="generate-section">
                <button class="generate-btn" @click="handleGenerate" :disabled="isGenerating || productImages.length === 0">
                  {{ isGenerating ? '生成中...' : '开始生成' }}
                </button>
                <!-- <p class="generate-cost">本次生成预计消耗：2 积分</p> -->
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- Divider inside right panel: config ⇔ AI -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'ai')"></div>

        <!-- AI Assistant column -->
        <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
          <div class="ai-header">
            <h3>AI 助手</h3>
            <a href="#" @click.prevent="clearChat">清空对话</a>
          </div>
          <div class="ai-chat" ref="chatBox">
            <div class="chat-msg bot">
              <div class="chat-avatar">AI</div>
              <div class="chat-bubble">
                您好！我是光合AI助手，有什么可以帮您？
              </div>
            </div>
            <template v-for="(msg, i) in aiMessages" :key="i">
              <div class="chat-msg user">
                <div class="chat-bubble">{{ msg.text }}</div>
              </div>
              <div class="chat-msg bot" v-if="msg.reply">
                <div class="chat-avatar">AI</div>
                <div class="chat-bubble">{{ msg.reply }}</div>
              </div>
            </template>
          </div>
          <div class="chat-input-area">
            <textarea
              v-model="aiInput"
              class="chat-input"
              placeholder="请输入您的需求，描述越详细，效果越好..."
              maxlength="2000"
              @keydown.enter.exact.prevent="sendAiMessage"
            ></textarea>
          </div>
          <div class="chat-footer">
            <div>
              <span class="char-count">{{ aiInput.length }}/2000</span>
              <br>
              <!-- <span class="chat-cost">本次操作将消耗 2 积分</span> -->
            </div>
            <button class="chat-send" @click="sendAiMessage" :disabled="!aiInput.trim()">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" multiple style="display:none" @change="handleFiles" />
    <input type="file" ref="refFileInput" accept="image/*" multiple style="display:none" @change="handleRefFiles" />

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

<script setup>
defineOptions({ name: 'BackgroundView' })
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import { aiDialogue, favoriteMaterial, cancelFavoriteMaterial, getPublicCreationConfigByGroup, reversePrompt } from '@/api/customer'
import { ArrowLeft, ArrowRight, ArrowDown, UploadFilled, Promotion, MagicStick, DocumentCopy } from '@element-plus/icons-vue'

const gen = useImageGeneration('render')
const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   canvasSelector: '.canvas-box',
//   getImage: () => resultImages.value[0]?.url || resultImages.value[0] || '',
//   defaultName: 'background',
// })

const fileInput = ref(null)
const refFileInput = ref(null)

// ===== 反推提示词 =====
const reverseDialogVisible = ref(false)
const reverseImageFile = ref(null)
const reverseImagePreview = ref('')
const reversePromptInput = ref('')
const reverseResult = ref('')
const reverseLoading = ref(false)
const REVERSE_DEFAULT_PROMPT = '请详细描述这张图片的画面，包括主体、材质、光影、背景、构图、视角和风格，输出一段可直接用于 AI 生图的提示词。'
const productImages = ref([])
const referenceImages = ref([])
const currentStep = ref(2)
const zoomLevel = ref(100)
const chatBox = ref(null)

// Panel widths - 画布自动占满剩余空间，右侧栏宽度=配置栏+AI栏+分隔线
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
const configCollapsed = ref(false)
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

const productFiles = ref([])
const referenceFiles = ref([])

const aiInput = ref('')
const aiMessages = ref([])

const selectedPlatform = ref('taobao')
const selectedScene = ref('home')
const selectedLight = ref('natural')
const selectedStyle = ref('minimal')
const outputSize = ref('800:800')

// ===== 提示词增强（从 gh_prompt_library 拉取） =====
const boostProduct = ref('')
const boostMaterial = ref('')
const boostProductRef = ref(null)
const boostMaterialRef = ref(null)

// 收藏状态
const favoritedResults = reactive({})

const sections = reactive({
  upload: true,
  refUpload: true,
  platform: false,
  scene: false,
  light: false,
  style: false,
  size: false,
  promptBoost: false
})


const platformOptions = [
  { label: '淘宝/天猫', value: 'taobao' },
  { label: '京东', value: 'jd' },
  { label: '拼多多', value: 'pdd' },
  { label: '抖音', value: 'douyin' },
  { label: '小红书', value: 'xhs' },
  { label: '亚马逊', value: 'amazon' }
]

const sceneOptions = ref([
  { label: '居家', value: 'home' },
  { label: '户外', value: 'outdoor' },
  { label: '工作室', value: 'studio' },
  { label: '节日主题', value: 'festival' },
  { label: '极简', value: 'minimal' },
  { label: '自然', value: 'nature' },
  { label: '都市', value: 'urban' }
])

const lightOptions = ref([
  { label: '自然光', value: 'natural' },
  { label: '柔光', value: 'soft' },
  { label: '硬光', value: 'hard' },
  { label: '逆光', value: 'backlight' },
  { label: '暖光', value: 'warm' },
  { label: '冷光', value: 'cool' }
])

const styleOptions = ref([
  { label: '简约', value: 'minimal' },
  { label: '轻奢', value: 'luxury' },
  { label: '活力', value: 'vibrant' },
  { label: '复古', value: 'retro' },
  { label: '科技感', value: 'tech' },
  { label: 'ins风', value: 'ins' }
])

const sizeOptions = [
  { label: '1:1（800×800）', value: '800:800' },
  { label: '3:4（800×1067）', value: '800:1067' },
  { label: '4:3（1067×800）', value: '1067:800' },
  { label: '自定义', value: 'custom' }
]

const resultImages = computed(() => gen.resultImages.value)
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)

// All expanded state
const allExpanded = computed(() => {
  return sections.upload && sections.refUpload && sections.platform && sections.scene && sections.light && sections.style && sections.size && sections.promptBoost
})

function toggleAllSections() {
  const newState = !allExpanded.value
  Object.keys(sections).forEach(k => sections[k] = newState)
}

function toggleSection(key) {
  sections[key] = !sections[key]
}

function triggerUpload() { fileInput.value?.click() }
function triggerRefUpload() { refFileInput.value?.click() }

function removeProductImage(idx) {
  productImages.value.splice(idx, 1)
  productFiles.value.splice(idx, 1)
}

function removeRefImage(idx) {
  referenceImages.value.splice(idx, 1)
  referenceFiles.value.splice(idx, 1)
}

function handleDrop(e) {
  const files = Array.from(e.dataTransfer?.files || []).filter(f => f.type.startsWith('image/')).slice(0, 10 - productImages.value.length)
  addProductFiles(files)
}

function addProductFiles(files) {
  files.forEach(f => {
    productFiles.value.push(f)
    const reader = new FileReader()
    reader.onload = ev => productImages.value.push(ev.target.result)
    reader.readAsDataURL(f)
  })
}

function handleRefDrop(e) {
  const files = Array.from(e.dataTransfer?.files || []).filter(f => f.type.startsWith('image/')).slice(0, 10 - referenceImages.value.length)
  files.forEach(f => {
    referenceFiles.value.push(f)
    const reader = new FileReader()
    reader.onload = ev => referenceImages.value.push(ev.target.result)
    reader.readAsDataURL(f)
  })
}

function handleFiles(e) {
  const files = Array.from(e.target.files).slice(0, 10 - productImages.value.length)
  addProductFiles(files)
}

function handleRefFiles(e) {
  const files = Array.from(e.target.files).slice(0, 10 - referenceImages.value.length)
  files.forEach(f => {
    referenceFiles.value.push(f)
    const reader = new FileReader()
    reader.onload = ev => referenceImages.value.push(ev.target.result)
    reader.readAsDataURL(f)
  })
}

async function sendAiMessage() {
  if (!aiInput.value.trim()) return
  const text = aiInput.value
  aiMessages.value.push({ text, reply: '' })
  aiInput.value = ''
  await nextTick()
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  try {
    const res = await aiDialogue({ content: text, sessionType: 'render' })
    const lastMsg = aiMessages.value[aiMessages.value.length - 1]
    lastMsg.reply = res.data?.content || res.data?.reply || res.msg || '已收到您的需求'
    await nextTick()
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  } catch (e) {
    const lastMsg = aiMessages.value[aiMessages.value.length - 1]
    lastMsg.reply = '抱歉，AI 助手暂时无法响应，请稍后再试。'
  }
}

async function handleGenerate() {
  const _basePrompt = aiMessages.value.map(m => m.text).join('\n') || ''
  const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
  const prompt = boostText ? `${_basePrompt}；约束：${boostText}。` : _basePrompt
  const extraParams = {
    platform: selectedPlatform.value,
    scene: selectedScene.value,
    light: selectedLight.value,
    style: selectedStyle.value,
    outputSize: outputSize.value,
    n: 4
  }
  if (referenceFiles.value.length) {
    extraParams.referenceImages = await gen.uploadImages(referenceFiles.value)
  }
  if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
  extraParams.consumePoints = 2
  extraParams.featureName = 'background'
  extraParams.title = '白底生成背景'
  await gen.fullGenerate(productFiles.value, prompt, extraParams)
}

function clearChat() {
  aiMessages.value = []
  aiInput.value = ''
}

// ===== 收藏切换 =====
async function toggleFavorite(idx, img) {
  try {
    const imgUrl = img.url || img
    if (favoritedResults[idx]) {
      // 取消收藏
      await cancelFavoriteMaterial({ materialUrl: imgUrl, sessionType: 'render' })
      favoritedResults[idx] = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await favoriteMaterial({ materialUrl: imgUrl, sessionType: 'render', source: 'background' })
      favoritedResults[idx] = true
      ElMessage.success('已收藏')
    }
  } catch (e) {
    console.error('收藏操作失败', e)
    ElMessage.error('收藏操作失败，请重试')
  }
}

// ===== 下载结果 =====
async function downloadResult(img, idx) {
  const url = img.url || img
  if (!url) return
  try {
    const res = await fetch(url)
    const blob = await res.blob()
    const objectUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = objectUrl
    a.download = `背景图_${idx + 1}_${Date.now()}.png`
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

// function zoomIn() { if (zoomLevel.value < 200) zoomLevel.value += 10 }
// function zoomOut() { if (zoomLevel.value > 25) zoomLevel.value -= 10 }
function handleFit() { zoomLevel.value = 100 }
function toggleFullscreen() {
  const el = document.querySelector('.canvas-box')
  if (el) {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      el.requestFullscreen()
    }
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

onMounted(() => {
  gen.loadPromptInfo()
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  nextTick(() => {
    const rightCol = document.querySelector('.right-col')
    if (rightCol) {
      const w = rightCol.getBoundingClientRect().width
      _configWidthPx.value = Math.round(w * 0.35)
      _aiWidthPx.value = Math.round(w * 0.55)
    }
  })
  loadBgConfig()
})

async function loadBgConfig() {
  try {
    const res = await getPublicCreationConfigByGroup('bg_generation')
    const list = res.data || res.rows || []
    const cfg = list.find(c => c.configKey === 'config')
    if (cfg && cfg.configValue) {
      const c = JSON.parse(cfg.configValue)
      if (c.sceneList) sceneOptions.value = c.sceneList.map(s => ({ label: s, value: s }))
      if (c.lightOptions) lightOptions.value = c.lightOptions.map(s => ({ label: s, value: s }))
      if (c.stylePresets) styleOptions.value = c.stylePresets.map(s => ({ label: s, value: s }))
    }
  } catch { /* use defaults */ }
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

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
})
</script>

<style scoped lang="scss">
// ========== Page Layout ==========
.workspace-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  overflow: hidden;
}

// ========== Steps Bar ==========
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

  &.active {
    color: #2563FF;
    font-weight: 600;

    .step-num {
      background: #2563FF;
      color: #fff;
      border-color: #2563FF;
    }
  }

  &.done {
    .step-num {
      background: #22C55E;
      color: #fff;
      border-color: #22C55E;
    }
  }
}

.step-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  border: 2px solid #E8EDF5;
  transition: all 0.25s;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #E8EDF5;
  margin: 0 6px;
  min-width: 8px;

  &.done {
    background: #22C55E;
  }
}

// ========== Three Column Layout ==========
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
  color: #6B7280;
  box-shadow: -2px 0 8px rgba(0,0,0,0.06);
  transition: all 0.2s ease;
}
.col-divider-wrapper .config-toggle-btn:hover {
  background: #F0F4FF;
  color: #2563FF;
  border-color: #2563FF;
}

// ========== Right Panel Divider ==========
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

// ========== Canvas Column ==========
.canvas-col {
  flex: 1 1 0%;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  background: var(--gh-bg-page, #F7F9FC);
  padding: 16px;
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
  text-align: center;
  color: #9CA3AF;
  padding: 20px;
  cursor: pointer;

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: .4;
  }

  h3 {
    font-size: 14px;
    color: #6B7280;
    margin-bottom: 6px;
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

  .result-actions {
    position: absolute;
    bottom: 8px;
    right: 8px;
    display: flex;
    gap: 6px;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .result-actions {
    opacity: 1;
  }
}

.result-action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #6B7280;

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: #fff;
    transform: scale(1.1);
  }

  &.favorite-btn.active {
    color: #FF4D4F;
  }

  &.download-btn:hover {
    color: #2563FF;
  }
}

.generating-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,.85);

  .progress-ring {
    font-size: 24px;
    font-weight: 700;
    color: #2563FF;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
    color: #6B7280;
  }
}

.canvas-toolbar-bottom {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 12px 0;
  flex-shrink: 0;
}

.tool-btn {
  padding: 6px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  color: #6B7280;

  &:hover {
    background: #F7F9FC;
    border-color: #2563FF;
    color: #2563FF;
  }
}

.zoom-label {
  font-size: 12px;
  color: #6B7280;
  padding: 6px 4px;
}

.canvas-bottom-bar {
  padding: 6px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
}

// ========== Right Column (Config + AI) ==========
.right-col {
  display: flex;
  background: #fff;
  min-width: 0;
  overflow: hidden;
}

// ========== Config Column ==========
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

.prompt-boost-row {
  margin-bottom: 10px;
}
.prompt-boost-row .boost-label {
  display: block;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
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

  &:hover .collapse-btn {
    color: #2563FF;
  }
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
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

.panel-header.collapsible {
  padding: 10px 16px;
  transition: opacity 0.2s;
}
.panel-header:hover { opacity: 0.75; }

.section-label {
  font-size: 13px;
  font-weight: 500;
  color: #1F2937;
}

.expand-text {
  font-size: 11px;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  gap: 2px;
}

.expand-arrow {
  transition: transform 0.25s;
}
.expand-arrow.expanded { transform: rotate(180deg); }

.section-body {
  padding: 4px 16px 8px;
}

// Upload
.upload-box {
  border: 2px dashed #E8EDF5;
  border-radius: 10px;
  padding: 14px;
  text-align: center;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.15s;

  &:hover {
    border-color: #2563FF;
    background: #FAFBFF;
  }

  .el-icon {
    font-size: 24px;
    margin-bottom: 4px;
  }

  p {
    font-size: 12px;
    color: #6B7280;
  }

  .hint {
    font-size: 10px;
    color: #9CA3AF;
    margin-top: 2px;
  }
}

.uploaded-files {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 0 16px;
  margin-top: 8px;
}

.uploaded-thumb {
  width: 56px;
  cursor: pointer;

  .thumb {
    width: 56px;
    height: 42px;
    border-radius: 6px;
    overflow: hidden;
    position: relative;
  }

  .thumb-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .remove-btn {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgba(0,0,0,.5);
    color: #fff;
    font-size: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.15s;
  }

  &:hover .remove-btn {
    opacity: 1;
  }

  .add-btn {
    width: 56px;
    height: 42px;
    border: 2px dashed #E8EDF5;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #9CA3AF;
    cursor: pointer;
    transition: all 0.15s;

    &:hover {
      border-color: #2563FF;
      color: #2563FF;
    }
  }
}

// ========== Option Tags (横向排版) ==========
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

// ========== Size Select ==========
.size-select {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}

.config-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0 4px;

  .radio-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: #6B7280;
    cursor: pointer;
    padding: 4px 0;

    input[type="radio"] {
      accent-color: #2563FF;
      width: 14px;
      height: 14px;
    }

    &:hover {
      color: #1F2937;
    }
  }
}

// Generate section
.generate-section {
  padding: 16px 0 0;
  border-top: 1px solid #E8EDF5;
  text-align: center;
}

.generate-btn {
  width: 100%;
  padding: 10px 20px;
  background: #2563FF;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;

  &:hover:not(:disabled) {
    background: #1D4ED8;
  }

  &:disabled {
    opacity: .6;
    cursor: not-allowed;
  }
}

.generate-cost {
  font-size: 10px;
  color: #9CA3AF;
  margin-top: 6px;
}

// ========== AI Column ==========
.ai-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  padding: 16px;
  overflow: hidden;
  min-width: 240px;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.ai-header h3 { font-size: 14px; font-weight: 600; margin: 0; }
.ai-header a { font-size: 11px; color: #2563FF; text-decoration: none; &:hover { text-decoration: underline; } }

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
.char-count { font-size: 10px; color: #9CA3AF; }
.chat-cost { font-size: 10px; color: #22C55E; }
.chat-send {
  padding: 8px 16px; background: #2563FF; color: #fff;
  border: none; border-radius: 8px; font-size: 12px; cursor: pointer; font-weight: 500;
  white-space: nowrap;
}
.chat-send:hover { opacity: 0.9; }
.chat-send:disabled { opacity: 0.4; cursor: not-allowed; }

// ========== Responsive ==========
@media (max-width: 1024px) {
  .steps-bar { padding: 10px 16px; gap: 4px; }
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