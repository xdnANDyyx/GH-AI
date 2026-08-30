<template>
  <div class="workspace-page">
    <div class="main-row">
      <!-- ===== 画布区域 ===== -->
      <div class="canvas-area">
        <div class="step-bar">
          <div v-for="(step, idx) in steps" :key="idx" class="step-item"
            :class="{ active: currentStep === idx + 1, done: currentStep > idx + 1 }">
            <div class="step-dot"><span>{{ idx + 1 }}</span></div>
            <span class="step-label">{{ step }}</span>
            <div class="step-line" v-if="idx < steps.length - 1"></div>
          </div>
        </div>

        <div class="canvas-wrapper"
        >
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div class="upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
            <!-- 无图片时显示上传占位 -->
            <div class="upload-placeholder" v-if="!originalImage">
              <el-icon :size="48" color="#9CA3AF"><Picture /></el-icon>
              <p class="upload-main-text">请在右侧配置生成参数并点击发送</p>
              <p class="upload-sub-text">支持 JPG / PNG 格式，单张最大 7MB</p>
            </div>
            <!-- 有原图但还没生成结果 -->
            <div class="upload-preview" v-else-if="!resultImages.length">
              <img :src="originalImage" class="preview-img" />
            </div>
            <!-- 生成结果展示 -->
            <div class="result-preview" v-else>
              <img :src="resultImages[activeResult]?.url || resultImages[activeResult]" class="preview-img" />
              <div class="result-nav" v-if="resultImages.length > 1">
                <button @click.stop="activeResult = Math.max(0, activeResult - 1)" :disabled="activeResult === 0">‹</button>
                <span>{{ activeResult + 1 }} / {{ resultImages.length }}</span>
                <button @click.stop="activeResult = Math.min(resultImages.length - 1, activeResult + 1)" :disabled="activeResult === resultImages.length - 1">›</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部工具栏 -->
        <div class="bottom-toolbar">
          <div class="toolbar-box">
            <!-- 暂时注释画布上方中间位置小图标
            <button class="tb-btn" @click.stop>
              <el-icon :size="16"><Monitor /></el-icon>
              <span>适应屏幕</span>
            </button>
            <div class="tb-divider"></div>
            -->
            <div class="tb-zoom">
              <button class="tb-icon-btn" @click.stop="zoomOut">
                <el-icon :size="14"><Minus /></el-icon>
              </button>
              <span class="zoom-val">{{ zoomLevel }}%</span>
              <button class="tb-icon-btn" @click.stop="zoomIn">
                <el-icon :size="14"><Plus /></el-icon>
              </button>
            </div>

          </div>
        </div>
      </div>

      <!-- ===== 右侧配置面板 ===== -->
      <div class="config-panel">
        <el-scrollbar>
          <div class="config-inner">
            <div class="panel-header">{{ panelTitle }}</div>

            <!-- 上传产品图 -->
            <div class="config-section">
              <div class="section-label"><span class="required-mark">*</span>产品图<span class="required-mark">（必填）</span></div>
              <div class="section-body">
                <div class="panel-upload-zone" @click.stop="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                  <el-icon :size="32" color="#2563FF"><UploadFilled /></el-icon>
                  <p class="panel-upload-text">点击或拖拽图片到此处上传</p>
                  <p class="panel-upload-hint">支持 JPG/PNG，单张最大 7MB</p>
                </div>
              </div>
            </div>

            <!-- 参考图（可选） -->
            <div class="config-section collapsible" v-if="showReference">
              <div class="section-header collapsible" @click="toggleSection('ref')">
                <span class="section-label no-margin">参考图（可选）</span>
                <span class="expand-text">{{ sections.ref ? '收起' : '展开' }}
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

            <!-- 提示词 -->
            <div class="config-section">
              <div class="section-label">提示词</div>
              <div class="section-body">
                <div class="prompt-tags" v-if="promptTags.length">
                  <span v-for="(tag, i) in promptTags" :key="i" class="prompt-tag" @click="appendToPrompt(tag)">
                    {{ tag }}
                  </span>
                </div>
                <el-input v-model="prompt" type="textarea" :rows="3" :placeholder="promptPlaceholder" maxlength="2000" show-word-limit />
              </div>
            </div>

            <!-- 尺寸选择 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('size')">
                <span class="section-label no-margin">尺寸选择</span>
                <span class="expand-text">{{ sections.size ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.size }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.size" class="section-body">
                <div class="size-grid">
                  <div v-for="s in sizeOptions" :key="s.value" class="size-option"
                    :class="{ active: selectedSize === s.value }" @click="selectedSize = s.value">
                    <span class="size-label">{{ s.label }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 生成数量 -->
            <div class="config-section">
              <div class="section-label">生成数量</div>
              <div class="section-body">
                <el-input-number v-model="generateCount" :min="1" :max="4" size="small" />
              </div>
            </div>

            <div class="config-divider"></div>

            <!-- 生成按钮 -->
            <div class="generate-area">
              <el-button type="primary" size="large" class="generate-btn" :loading="gen.generating.value"
                :disabled="!canGenerate" @click="handleGenerate">
                {{ gen.generating.value ? gen.statusText.value : '开始生成' }}
              </el-button>
              <div class="gen-progress" v-if="gen.generating.value">
                <el-progress :percentage="gen.progress.value" :stroke-width="6" :show-text="false" />
                <span class="gen-status">{{ gen.statusText.value }}</span>
              </div>
              <div class="gen-error" v-if="gen.error.value">
                <el-icon color="#EF4444"><WarningFilled /></el-icon>
                {{ gen.error.value }}
              </div>
            </div>

            <!-- AI 助手 -->
            <div class="config-section ai-section">
              <div class="section-header">
                <span class="section-label ai-label">
                  <el-icon :size="16" color="#2563FF"><ChatDotRound /></el-icon> AI 助手
                </span>
                <span class="ai-clear" @click="clearChat">清空对话</span>
              </div>
              <div class="section-body">
                <div class="ai-chat">
                  <div class="ai-bubble bot">
                    <div class="bubble-avatar">AI</div>
                    <div class="bubble-content">
                      <p>{{ aiGreeting }}</p>
                    </div>
                  </div>
                  <template v-for="(msg, i) in aiMessages" :key="i">
                    <div class="ai-bubble user">
                      <div class="bubble-content user-content"><p>{{ msg.text }}</p></div>
                    </div>
                    <div class="ai-bubble bot" v-if="msg.reply">
                      <div class="bubble-avatar">AI</div>
                      <div class="bubble-content"><p>{{ msg.reply }}</p></div>
                    </div>
                  </template>
                </div>
                <div class="ai-input-wrap">
                  <textarea v-model="aiInput" class="ai-textarea" placeholder="请输入您的需求..." rows="3" maxlength="2000"></textarea>
                  <div class="ai-input-footer">
                    <span class="ai-counter">{{ aiInput.length }}/2000</span>
                    <button class="ai-send-btn" @click="sendAiMessage" :disabled="!aiInput.trim() || !originalImage">发送</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="status-bar">
      <div class="status-left"></div>
      <div class="status-right">
        <span class="status-remain">今日剩余生成次数：<strong>--/--</strong></span>
        <!-- <span class="status-cost">本次生成预计消耗：<strong>{{ generateCount }}</strong> 积分</span> -->
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFileSelect" />
    <input type="file" ref="refFileInput" accept="image/*" style="display:none" @change="handleRefFileSelect" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useImageGeneration } from '@/composables/useImageGeneration'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { aiDialogue } from '@/api/customer'
import { ElMessage } from 'element-plus'

// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   canvasSelector: '.canvas-wrapper',
//   defaultName: 'workspace',
// })

const props = defineProps({
  sessionType: { type: String, default: '' },
  panelTitle: { type: String, default: '创作配置' },
  steps: { type: Array, default: () => ['上传产品图', '设置参数', 'AI 生成', '查看结果'] },
  promptPlaceholder: { type: String, default: '请描述您想要的效果...' },
  promptTags: { type: Array, default: () => [] },
  aiGreeting: { type: String, default: '您好！我是光合AI助手，有什么可以帮您？' },
  showReference: { type: Boolean, default: false },
  sizeOptions: {
    type: Array,
    default: () => [
      { label: '1:1', value: '1:1' },
      { label: '3:4', value: '3:4' },
      { label: '4:3', value: '4:3' },
      { label: '16:9', value: '16:9' },
      { label: '9:16', value: '9:16' }
    ]
  }
})

const route = useRoute()
const sessionType = props.sessionType || route.meta?.sessionType || 'render'
const gen = useImageGeneration(sessionType)

// 状态
const fileInput = ref(null)
const refFileInput = ref(null)
const originalImage = ref('')
const originalFile = ref(null)
const refImage = ref('')
const refFile = ref(null)
const prompt = ref('')
const selectedSize = ref('1:1')
const generateCount = ref(1)
const currentStep = ref(1)
const zoomLevel = ref(100)
const activeResult = ref(0)

const aiInput = ref('')
const aiMessages = ref([])

const sections = reactive({ ref: false, size: true })

const resultImages = computed(() => gen.resultImages.value)
const canGenerate = computed(() => !!originalImage.value && !!prompt.value.trim() && !gen.generating.value)

function toggleSection(key) { sections[key] = !sections[key] }

// 文件上传
function triggerUpload() { fileInput.value?.click() }
function triggerRefUpload() { refFileInput.value?.click() }

function handleDrop(e) {
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) readFile(file)
}

function handleRefDrop(e) {
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) readRefFile(file)
}

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) readFile(file)
}

function handleRefFileSelect(e) {
  const file = e.target.files[0]
  if (file) readRefFile(file)
}

function readFile(file) {
  originalFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => { originalImage.value = ev.target.result }
  reader.readAsDataURL(file)
  if (currentStep.value < 2) currentStep.value = 2
}

function readRefFile(file) {
  refFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => { refImage.value = ev.target.result }
  reader.readAsDataURL(file)
}

// 提示词
function appendToPrompt(tag) {
  prompt.value = prompt.value ? `${prompt.value}，${tag}` : tag
}

// 生成
async function handleGenerate() {
  if (!canGenerate.value) return
  if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
  currentStep.value = 3
  activeResult.value = 0

  try {
    const productFiles = [originalFile.value]
    const extraParams = { n: generateCount.value, consumePoints: 2, featureName: 'ai_assistant', title: 'AI生成' }

    // 如果有参考图，上传并传入
    if (refFile.value) {
      const refUrl = await gen.uploadImage(refFile.value)
      extraParams.referenceImages = [refUrl]
    }

    // 加入尺寸参数
    extraParams.extraOptions = { aspect_ratio: selectedSize.value }

    await gen.fullGenerate(productFiles, prompt.value, extraParams)
    currentStep.value = 4
} catch (e) {
if (e?.message?.includes('已取消')) return
console.error('生成失败:', e)
  }
}

// AI 对话
function clearChat() {
  aiMessages.value = []
  aiInput.value = ''
  originalImage.value = ''
  gen.reset()
  currentStep.value = 1
  activeResult.value = 0
}

async function sendAiMessage() {
  if (!aiInput.value.trim()) return
  const text = aiInput.value.trim()
  aiMessages.value.push({ text, reply: '' })
  aiInput.value = ''

  try {
    const res = await aiDialogue({ content: text, sessionType })
    const reply = res.data?.content || res.data?.reply || '好的，正在为您处理中...'
    aiMessages.value[aiMessages.value.length - 1].reply = reply
  } catch {
    aiMessages.value[aiMessages.value.length - 1].reply = '抱歉，暂时无法回复，请稍后重试。'
  }
}

// function zoomIn() { if (zoomLevel.value < 200) zoomLevel.value += 10 }
// function zoomOut() { if (zoomLevel.value > 25) zoomLevel.value -= 10 }

// 加载配置
onMounted(async () => {
  await Promise.allSettled([
    gen.loadPromptInfo(),
    gen.loadPixelConfigs(),
    gen.loadDeductTypes()
  ])
})
</script>

<style lang="scss" scoped>
.workspace-page { display: flex; flex-direction: column; gap: 0; flex: 1; margin: -24px; }
.main-row { display: flex; flex: 1; min-height: 0; }
.canvas-area { flex: 1; display: flex; flex-direction: column; min-width: 0; }

.step-bar { display: flex; align-items: center; padding: 12px 24px; background: #fff; border-bottom: 1px solid var(--gh-border, #EBEDF5); flex-shrink: 0; }
.step-item { display: flex; align-items: center; flex: 1; min-width: 0;
  .step-dot { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; background: #F3F4F6; color: #9CA3AF; flex-shrink: 0; transition: all 0.3s; }
  .step-label { font-size: 12px; color: #9CA3AF; margin-left: 6px; white-space: nowrap; }
  .step-line { flex: 1; height: 2px; background: #F3F4F6; margin: 0 8px; min-width: 12px; }
  &.active { .step-dot { background: var(--gh-primary, #2563FF); color: #fff; box-shadow: 0 0 0 4px rgba(37,99,255,0.15); } .step-label { color: var(--gh-primary, #2563FF); font-weight: 500; } }
  &.done { .step-dot { background: var(--gh-success, #22C55E); color: #fff; } .step-line { background: var(--gh-success, #22C55E); } }
}

.canvas-wrapper { flex: 1; display: flex; align-items: stretch; padding: 24px; min-height: 0; }
.upload-zone { flex: 1; display: flex; align-items: center; justify-content: center; border: 2px dashed var(--gh-border, #EBEDF5); cursor: pointer; transition: border-color 0.25s; background: var(--gh-bg-page, #F7F9FC);
  &:hover { border-color: var(--gh-primary, #2563FF); }
}
.upload-placeholder { text-align: center; }
.upload-main-text { font-size: 14px; color: #6B6B6B; margin: 16px 0 4px; }
.upload-sub-text { font-size: 12px; color: #9CA3AF; margin: 0; }
.upload-preview, .result-preview { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 100%; height: 100%;
  .preview-img { max-width: 80%; max-height: 80%; object-fit: contain; }
}
.result-nav { display: flex; align-items: center; gap: 12px; margin-top: 12px;
  button { width: 32px; height: 32px; border: 1px solid #E5E7EB; border-radius: 8px; background: #fff; cursor: pointer; font-size: 18px;
    &:disabled { opacity: 0.4; cursor: default; }
  }
  span { font-size: 14px; color: #6B7280; }
}

.bottom-toolbar { display: flex; align-items: center; justify-content: center; padding: 10px 24px; border-top: 1px solid var(--gh-border, #EBEDF5); flex-shrink: 0; }
.toolbar-box { display: flex; align-items: center; gap: 8px; padding: 6px 12px; background: #fff; border: 1px solid var(--gh-border, #EBEDF5); border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.tb-btn { display: flex; align-items: center; gap: 4px; padding: 4px 8px; border: none; background: transparent; cursor: pointer; font-size: 12px; color: #1F1F1F; border-radius: 6px; &:hover { background: #F3F4F6; } span { white-space: nowrap; } }
.tb-icon-btn { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border: none; background: transparent; cursor: pointer; color: #6B6B6B; border-radius: 6px; &:hover { background: #F3F4F6; } }
.tb-divider { width: 1px; height: 20px; background: #F3F4F6; }
.tb-zoom { display: flex; align-items: center; gap: 6px; }
.zoom-val { font-size: 12px; color: #6B6B6B; min-width: 36px; text-align: center; }

// 配置面板
.config-panel { width: 380px; background: #fff; border-left: 1px solid var(--gh-border, #EBEDF5); flex-shrink: 0; display: flex; flex-direction: column; }
.config-inner { padding: 20px; }
.panel-header { font-size: 16px; font-weight: 600; color: #111827; margin-bottom: 20px; }

.config-section { margin-bottom: 20px;
  &.collapsible { margin-bottom: 16px; }
}
.section-label { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px;
  &.no-margin { margin-bottom: 0; }
  &.ai-label { display: flex; align-items: center; gap: 6px; }
}
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }
.ai-clear { font-size: 12px; color: var(--gh-text-placeholder); cursor: pointer; }
.section-header.collapsible { display: flex; align-items: center; justify-content: space-between; cursor: pointer; padding: 4px 0; }
.expand-text { font-size: 12px; color: #9CA3AF; display: flex; align-items: center; gap: 2px; }
.expand-arrow { transition: transform 0.2s; &.expanded { transform: rotate(180deg); } }
.section-body { margin-top: 10px; }
.section-helper { font-size: 12px; color: #9CA3AF; margin-top: 8px; }
.config-divider { height: 1px; background: #F3F4F6; margin: 16px 0; }

.panel-upload-zone { border: 2px dashed #E5E7EB; border-radius: 12px; padding: 24px 16px; text-align: center; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #2563FF; background: rgba(37,99,255,0.02); }
  &.small { padding: 16px 12px; }
}
.panel-upload-text { font-size: 13px; color: #6B7280; margin: 8px 0 4px; }
.panel-upload-hint { font-size: 12px; color: #9CA3AF; margin: 0; }

.ref-preview-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.ref-thumb { width: 48px; height: 48px; border-radius: 8px; object-fit: cover; }
.ref-remove { border: none; background: transparent; color: #EF4444; font-size: 18px; cursor: pointer; }

.prompt-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
.prompt-tag { padding: 4px 10px; border-radius: 6px; background: #EFF6FF; color: #2563FF; font-size: 12px; cursor: pointer; transition: background 0.2s;
  &:hover { background: #DBEAFE; }
}

.size-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.size-option { padding: 10px; border: 1px solid #E5E7EB; border-radius: 8px; text-align: center; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #2563FF; }
  &.active { border-color: #2563FF; background: #EFF6FF; color: #2563FF; }
  .size-label { font-size: 13px; font-weight: 500; }
}

// 生成区域
.generate-area { margin-bottom: 20px; }
.generate-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 600; border-radius: 10px; letter-spacing: 2px; }
.gen-progress { margin-top: 10px; text-align: center; }
.gen-status { font-size: 12px; color: #6B7280; display: block; margin-top: 4px; }
.gen-error { margin-top: 8px; font-size: 13px; color: #EF4444; display: flex; align-items: center; gap: 4px; }

// AI 助手
.ai-section { border-top: 1px solid #F3F4F6; padding-top: 16px; }
.ai-chat { max-height: 200px; overflow-y: auto; margin-bottom: 12px; }
.ai-bubble { display: flex; gap: 8px; margin-bottom: 12px;
  &.user { justify-content: flex-end; .bubble-content { background: #2563FF; color: #fff; border-radius: 12px 12px 2px 12px; } }
  &.bot { .bubble-content { background: #F3F4F6; color: #374151; border-radius: 12px 12px 12px 2px; } }
}
.bubble-avatar { width: 28px; height: 28px; border-radius: 50%; background: #2563FF; color: #fff; font-size: 11px; font-weight: 600; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.bubble-content { max-width: 80%; padding: 8px 12px; font-size: 13px; line-height: 1.5; p { margin: 0; } }
.user-content { background: #2563FF; color: #fff; }

.ai-input-wrap { border: 1px solid #E5E7EB; border-radius: 10px; overflow: hidden; }
.ai-textarea { width: 100%; border: none; outline: none; resize: none; padding: 10px 12px; font-size: 13px; font-family: inherit; }
.ai-input-footer { display: flex; align-items: center; justify-content: space-between; padding: 6px 12px; border-top: 1px solid #F3F4F6; }
.ai-counter { font-size: 12px; color: #9CA3AF; }
.ai-send-btn { border: none; background: #2563FF; color: #fff; padding: 4px 14px; border-radius: 6px; font-size: 13px; cursor: pointer;
  &:disabled { opacity: 0.5; cursor: default; }
}

// 状态栏
.status-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 24px; border-top: 1px solid var(--gh-border, #EBEDF5); background: #fff; flex-shrink: 0; }
.status-right { font-size: 12px; color: #6B7280; display: flex; gap: 20px; strong { color: #111827; } }
</style>
