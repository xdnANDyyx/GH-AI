<template>
  <div class="workspace-page">
    <!-- Left Canvas Area -->
    <div class="canvas-area">
      <!-- Step Bar (6 steps) -->
      <div class="step-bar">
        <div
          v-for="(s, i) in sizeSteps"
          :key="i"
          class="step-item"
          :class="{ active: currentStep === i, done: currentStep > i }"
        >
          <div class="step-dot">
            <el-icon v-if="currentStep > i"><Check /></el-icon>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s.label }}</span>
          <div class="step-line" v-if="i < sizeSteps.length - 1"></div>
        </div>
      </div>

      <!-- Canvas Content -->
      <div class="canvas-content">
        <div class="canvas-workspace" v-if="!generated">
          <div class="gh-upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
            <div class="upload-icon">
              <el-icon :size="56"><UploadFilled /></el-icon>
            </div>
            <p class="upload-text">拖拽产品图片到此处，或<span class="highlight">点击上传</span></p>
            <p class="upload-hint">支持 JPG/PNG/WebP，建议上传白底图</p>
          </div>
        </div>

        <!-- Generated Results -->
        <div class="result-area" v-else>
          <div class="size-result gh-card">
            <div class="size-diagram">
              <div class="product-silhouette">
                <div class="dimension-line horizontal">
                  <span class="dim-label">宽度: {{ dimensions.width }}{{ unitLabel }}</span>
                </div>
                <div class="product-box" :style="{ borderColor: accentColor }">
                  <div class="product-placeholder">
                    <el-icon :size="32"><Box /></el-icon>
                    <span>产品图</span>
                  </div>
                </div>
                <div class="dimension-line vertical">
                  <span class="dim-label">高度: {{ dimensions.height }}{{ unitLabel }}</span>
                </div>
                <div class="dimension-line depth">
                  <span class="dim-label">深度: {{ dimensions.depth }}{{ unitLabel }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="result-actions">
            <el-button @click="generated = false"><el-icon><RefreshLeft /></el-icon> 重新标注</el-button>
            <el-button type="primary"><el-icon><Download /></el-icon> 下载尺寸图</el-button>
          </div>
        </div>

        <!-- Bottom Toolbar -->
        <div class="bottom-toolbar">
          <div class="toolbar-left">
            <el-button text size="small" @click="fitScreen">
              <el-icon><Monitor /></el-icon> 适应屏幕
            </el-button>
            <el-divider direction="vertical" />
            <el-button text size="small" @click="undo" :disabled="!canUndo">
              <el-icon><RefreshLeft /></el-icon> 撤销
            </el-button>
            <el-button text size="small" @click="redo" :disabled="!canRedo">
              <el-icon><RefreshRight /></el-icon> 恢复
            </el-button>
            <el-button text size="small" @click="clearCanvas">
              <el-icon><Delete /></el-icon> 清空
            </el-button>
          </div>
          <div class="toolbar-right">
            <el-button text size="small" @click="zoomOut">
              <el-icon><Minus /></el-icon>
            </el-button>
            <span class="zoom-level" @click="zoomReset">{{ zoomValue }}%</span>
            <el-button text size="small" @click="zoomIn">
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-divider direction="vertical" />
            <el-button text size="small" @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Config Panel -->
    <div class="config-panel">
      <el-scrollbar>
        <div class="config-inner">
          <div class="config-title">尺寸标记配置</div>

          <el-collapse v-model="activeSections">
            <!-- 1. 上传素材 -->
            <el-collapse-item title="上传素材" name="upload">
              <div class="upload-buttons">
                <el-button class="upload-btn" @click="triggerUpload">
                  <el-icon><Upload /></el-icon>
                  上传产品图（必传）
                </el-button>
                <el-button class="upload-btn secondary" @click="triggerRefUpload">
                  <el-icon><Upload /></el-icon>
                  上传参考图（可选）
                </el-button>
              </div>
              <p class="section-hint">建议上传白底产品图，标注效果更佳</p>
            </el-collapse-item>

            <!-- 2. 标注方式 -->
            <el-collapse-item title="标注方式" name="markMode">
              <el-radio-group v-model="markMode" class="mark-mode-group">
                <el-radio value="auto" size="default">
                  <div class="radio-card">
                    <span class="radio-title">自动标注</span>
                    <span class="radio-desc">AI智能识别并标注</span>
                  </div>
                </el-radio>
                <el-radio value="manual" size="default">
                  <div class="radio-card">
                    <span class="radio-title">手动标注</span>
                    <span class="radio-desc">手动绘制标注线</span>
                  </div>
                </el-radio>
                <el-radio value="mixed" size="default">
                  <div class="radio-card">
                    <span class="radio-title">混合模式</span>
                    <span class="radio-desc">AI标注+手动微调</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-collapse-item>

            <!-- 3. 单位选择 -->
            <el-collapse-item title="单位选择" name="unit">
              <el-select v-model="unit" size="default" style="width: 100%">
                <el-option label="厘米（cm）" value="cm" />
                <el-option label="毫米（mm）" value="mm" />
                <el-option label="英寸（inch）" value="inch" />
                <el-option label="像素（px）" value="px" />
              </el-select>
            </el-collapse-item>

            <!-- 4. 标注样式 -->
            <el-collapse-item title="标注样式" name="lineStyle">
              <div class="line-style-group">
                <div
                  v-for="ls in lineStyles"
                  :key="ls.value"
                  class="line-style-card"
                  :class="{ active: selectedLineStyle === ls.value }"
                  @click="selectedLineStyle = ls.value"
                >
                  <div class="line-preview">
                    <div class="line-demo" :class="ls.value"></div>
                  </div>
                  <span class="line-name">{{ ls.label }}</span>
                </div>
              </div>
            </el-collapse-item>

            <!-- 5. 字体设置 -->
            <el-collapse-item title="字体设置" name="fontSettings">
              <div class="font-settings-row">
                <div class="font-field">
                  <span class="field-label">字号</span>
                  <el-select v-model="fontSize" size="default" style="width: 100%">
                    <el-option label="12px" value="12" />
                    <el-option label="14px" value="14" />
                    <el-option label="16px" value="16" />
                    <el-option label="18px" value="18" />
                    <el-option label="20px" value="20" />
                    <el-option label="24px" value="24" />
                  </el-select>
                </div>
                <div class="font-field">
                  <span class="field-label">颜色</span>
                  <div class="color-picker-row">
                    <div
                      v-for="c in fontColors"
                      :key="c"
                      class="color-pick"
                      :class="{ active: fontColor === c }"
                      :style="{ background: c }"
                      @click="fontColor = c"
                    ></div>
                  </div>
                </div>
              </div>
            </el-collapse-item>

            <!-- 6. 输出设置 -->
            <el-collapse-item title="输出设置" name="output">
              <div class="output-settings">
                <div class="output-row">
                  <span class="output-label">输出格式</span>
                  <el-select v-model="outputFormat" size="default" style="width: 140px">
                    <el-option label="PNG" value="png" />
                    <el-option label="SVG" value="svg" />
                    <el-option label="PDF" value="pdf" />
                  </el-select>
                </div>
                <div class="output-row">
                  <span class="output-label">分辨率</span>
                  <el-select v-model="outputDPI" size="default" style="width: 140px">
                    <el-option label="72 DPI" value="72" />
                    <el-option label="150 DPI" value="150" />
                    <el-option label="300 DPI（推荐）" value="300" />
                  </el-select>
                </div>
              </div>
            </el-collapse-item>

            <!-- 7. 生成数量 -->
            <el-collapse-item title="生成数量" name="genCount">
              <div class="gen-count-group">
                <div
                  v-for="n in [1, 2, 3, 4, 5]"
                  :key="n"
                  class="count-item"
                  :class="{ active: genCount === n }"
                  @click="genCount = n"
                >
                  {{ n }}
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>

          <!-- 8. AI助手 -->
          <div class="gh-config-section ai-chat-section">
            <div class="config-label">
              <el-icon class="ai-label-icon"><ChatDotRound /></el-icon>
              AI 助手
            </div>
            <div class="ai-chat-box">
              <div class="ai-messages" ref="messagesRef">
                <div v-if="chatMessages.length === 0" class="ai-quick">
                  <div class="ai-bubble-welcome">
                    <div class="ai-avatar-sm">
                      <el-icon><ChatDotRound /></el-icon>
                    </div>
                    <span>有什么可以帮您？</span>
                  </div>
                  <div
                    v-for="(q, i) in quickQuestions"
                    :key="i"
                    class="quick-item"
                    @click="sendQuick(q)"
                  >
                    {{ q }}
                  </div>
                </div>
                <div v-for="(msg, idx) in chatMessages" :key="idx" class="msg-item" :class="msg.role">
                  <div class="msg-bubble">
                    <div class="msg-avatar" v-if="msg.role === 'assistant'">
                      <el-icon><ChatDotRound /></el-icon>
                    </div>
                    <div class="msg-content">{{ msg.content }}</div>
                  </div>
                </div>
                <div v-if="chatLoading" class="msg-item assistant">
                  <div class="msg-bubble">
                    <div class="msg-avatar"><el-icon><ChatDotRound /></el-icon></div>
                    <div class="msg-content typing"><span></span><span></span><span></span></div>
                  </div>
                </div>
              </div>
              <div class="ai-input-row">
                <el-input
                  v-model="chatInput"
                  placeholder="向AI助手提问..."
                  size="default"
                  maxlength="2000"
                  show-word-limit
                  @keydown.enter.prevent="sendChat"
                >
                  <template #append>
                    <el-button :disabled="!chatInput.trim() || chatLoading" @click="sendChat">
                      发送（-2积分）
                    </el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </div>

          <!-- Generate Button -->
          <el-button type="primary" size="large" class="generate-btn" :loading="generating" @click="handleGenerate">
            <el-icon><MagicStick /></el-icon>
            生成尺寸图
            <span class="points-cost">-2积分</span>
          </el-button>
        </div>
      </el-scrollbar>
    </div>

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFile" />
    <input type="file" ref="refFileInput" accept="image/*" style="display:none" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'

const fileInput = ref(null)
const refFileInput = ref(null)
const messagesRef = ref(null)
const generated = ref(false)
const generating = ref(false)
const currentStep = ref(5) // last step in 6-step flow
const genCount = ref(3)
const markMode = ref('auto')
const unit = ref('cm')
const selectedLineStyle = ref('solid')
const fontSize = ref('14')
const fontColor = ref('#1F1F1F')
const outputFormat = ref('png')
const outputDPI = ref('300')
const zoomValue = ref(100)
const canUndo = ref(false)
const canRedo = ref(false)

const activeSections = ref(['upload', 'markMode', 'unit', 'lineStyle', 'fontSettings', 'output', 'genCount'])

const dimensions = reactive({ width: 50, height: 80, depth: 30 })

const chatInput = ref('')
const chatLoading = ref(false)
const chatMessages = ref([])

const sizeSteps = [
  { label: '白底图' },
  { label: '生成背景图' },
  { label: '产品精修' },
  { label: 'AI模特' },
  { label: '主图/A+' },
  { label: '尺寸标记' }
]

const unitLabel = computed(() => unit.value)
const accentColor = computed(() => '#2563FF')

const lineStyles = [
  { value: 'solid', label: '实线' },
  { value: 'dashed', label: '虚线' },
  { value: 'arrow', label: '箭头' }
]

const fontColors = ['#1F1F1F', '#2563FF', '#EF4444', '#22C55E', '#FFFFFF']

const quickQuestions = [
  '尺寸标注有哪些规范？',
  '如何标注不规则产品？',
  '推荐适合Amazon的尺寸图样式'
]

function triggerUpload() { fileInput.value?.click() }
function triggerRefUpload() { refFileInput.value?.click() }
function handleDrop() { handleGenerate() }
function handleFile() { handleGenerate() }

function zoomIn() { zoomValue.value = Math.min(zoomValue.value + 10, 200) }
function zoomOut() { zoomValue.value = Math.max(zoomValue.value - 10, 50) }
function zoomReset() { zoomValue.value = 100 }
function fitScreen() { zoomValue.value = 100 }
function undo() { /* placeholder */ }
function redo() { /* placeholder */ }
function clearCanvas() { /* placeholder */ }
function toggleFullscreen() { /* placeholder */ }

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

function sendQuick(text) {
  chatInput.value = text
  sendChat()
}

function sendChat() {
  const text = chatInput.value.trim()
  if (!text || chatLoading.value) return
  chatMessages.value.push({ role: 'user', content: text })
  chatInput.value = ''
  chatLoading.value = true
  scrollToBottom()
  setTimeout(() => {
    const replies = [
      '尺寸标注建议：使用清晰的引线标注、数字字号不小于14pt、标注线用细实线(1-2px)、单位统一标注在一侧。',
      '不规则产品建议使用多点标注，分别标注最宽、最高、最深的部位，并配上俯视/侧视图辅助说明。',
      'Amazon平台推荐使用白底+黑色标注线的"标准"样式，清晰专业，符合平台A+页面规范。'
    ]
    chatMessages.value.push({ role: 'assistant', content: replies[Math.floor(Math.random() * replies.length)] })
    chatLoading.value = false
    scrollToBottom()
  }, 1000)
}

function handleGenerate() {
  generating.value = true
  setTimeout(() => {
    generated.value = true
    generating.value = false
  }, 1500)
}
</script>

<style lang="scss" scoped>
.workspace-page {
  display: flex;
  gap: 0;
  height: calc(100vh - var(--gh-topbar-height) - 48px);
  margin: -24px;
}

// ===== Canvas Area =====
.canvas-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-left: 1px solid var(--gh-border-light);
}

.step-bar {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 12px 24px;
  background: var(--gh-bg-card);
  border-bottom: 1px solid var(--gh-border-light);
  flex-shrink: 0;
  overflow-x: auto;

  &::-webkit-scrollbar { height: 0; }
}

.step-item {
  display: flex;
  align-items: center;
  flex-shrink: 0;

  .step-dot {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    background: var(--gh-border-light);
    color: var(--gh-text-placeholder);
    transition: all 0.3s;
  }

  .step-label {
    font-size: 13px;
    color: var(--gh-text-placeholder);
    margin-left: 6px;
    white-space: nowrap;
    transition: color 0.3s;
  }

  .step-line {
    width: 28px;
    height: 2px;
    background: var(--gh-border-light);
    margin: 0 8px;
    flex-shrink: 0;
    transition: background 0.3s;
  }

  &.active {
    .step-dot {
      background: var(--gh-primary);
      color: #fff;
      box-shadow: 0 0 0 4px rgba(37, 99, 255, 0.15);
    }
    .step-label {
      color: var(--gh-primary);
      font-weight: 500;
    }
  }

  &.done {
    .step-dot {
      background: var(--gh-success);
      color: #fff;
    }
    .step-label {
      color: var(--gh-text-secondary);
    }
    .step-line {
      background: var(--gh-success);
    }
  }
}

.canvas-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.canvas-workspace {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 24px;
  min-height: 0;
}

.gh-upload-zone {
  border: 2px dashed var(--gh-border);
  border-radius: var(--gh-radius-card);
  padding: 48px 48px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #FAFBFC;
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);
  }

  .upload-icon {
    font-size: 40px;
    color: var(--gh-primary);
    margin-bottom: 16px;
    opacity: 0.7;
  }

  .upload-text {
    font-size: 15px;
    color: var(--gh-text-secondary);
    margin-bottom: 8px;

    .highlight {
      color: var(--gh-primary);
      font-weight: 500;
    }
  }

  .upload-hint {
    font-size: 12px;
    color: var(--gh-text-placeholder);
  }
}

// ===== Bottom Toolbar =====
.bottom-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--gh-bg-card);
  border-top: 1px solid var(--gh-border-light);
  flex-shrink: 0;

  .toolbar-left,
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .zoom-level {
    font-size: 12px;
    color: var(--gh-text-secondary);
    min-width: 40px;
    text-align: center;
    cursor: pointer;
    user-select: none;
  }

  :deep(.el-divider--vertical) {
    margin: 0 4px;
  }
}

// ===== Results =====
.result-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
}

.size-result {
  padding: 32px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.size-diagram {
  width: 100%;
  max-width: 420px;
}

.product-silhouette {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px;
}

.product-box {
  width: 180px;
  height: 220px;
  background: linear-gradient(135deg, #E2E8F0, #CBD5E1);
  border-radius: var(--gh-radius-btn);
  border: 2px dashed var(--gh-primary);
  display: flex;
  align-items: center;
  justify-content: center;

  .product-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: var(--gh-text-placeholder);

    span {
      font-size: 12px;
    }
  }
}

.dimension-line {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;

  &.horizontal {
    top: 24px;
    left: 50%;
    transform: translateX(-50%);
    width: 180px;
    height: 20px;
    border-bottom: 2px solid var(--gh-primary);

    &::before, &::after {
      content: '';
      position: absolute;
      width: 2px;
      height: 14px;
      background: var(--gh-primary);
      top: -5px;
    }
    &::before { left: 0; }
    &::after { right: 0; }
  }

  &.vertical {
    right: 24px;
    top: 50%;
    transform: translateY(-50%);
    height: 220px;
    width: 20px;
    border-left: 2px solid var(--gh-primary);

    &::before, &::after {
      content: '';
      position: absolute;
      height: 2px;
      width: 14px;
      background: var(--gh-primary);
      left: -5px;
    }
    &::before { top: 0; }
    &::after { bottom: 0; }
  }

  &.depth {
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    width: 140px;
    border-top: 2px solid var(--gh-primary);

    &::before, &::after {
      content: '';
      position: absolute;
      width: 2px;
      height: 14px;
      background: var(--gh-primary);
      bottom: -5px;
    }
    &::before { left: 0; }
    &::after { right: 0; }
  }
}

.dim-label {
  font-size: 12px;
  color: var(--gh-primary);
  font-weight: 600;
  background: #fff;
  padding: 2px 8px;
  border-radius: 6px;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

// ===== Config Panel =====
.config-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--gh-bg-card);
  border-left: 1px solid var(--gh-border-light);
  overflow: hidden;
}

.config-inner {
  padding: 24px;
}

.config-title {
  font-size: var(--gh-font-module);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--gh-border-light);
}

// Collapse styling
:deep(.el-collapse) {
  border: none;

  .el-collapse-item {
    margin-bottom: 4px;

    .el-collapse-item__header {
      font-size: 14px;
      font-weight: 500;
      color: var(--gh-text-primary);
      height: 40px;
      line-height: 40px;
      background: transparent;
      border: none;
      padding: 0;
    }

    .el-collapse-item__wrap {
      border: none;
      padding-bottom: 8px;
    }

    .el-collapse-item__content {
      padding: 0;
    }
  }
}

.section-hint {
  font-size: var(--gh-font-helper);
  color: var(--gh-text-placeholder);
  margin-top: 6px;
}

// Upload buttons
.upload-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-btn {
  width: 100%;
  height: var(--gh-btn-height);
  border-radius: var(--gh-radius-btn);
  font-size: 14px;
  font-weight: 500;
  border: 1.5px solid var(--gh-primary);
  color: var(--gh-primary);
  background: rgba(37, 99, 255, 0.04);
  transition: all 0.2s;

  &:hover {
    background: rgba(37, 99, 255, 0.08);
  }

  &.secondary {
    border-color: var(--gh-border);
    color: var(--gh-text-secondary);
    background: transparent;

    &:hover {
      border-color: var(--gh-primary);
      color: var(--gh-primary);
    }
  }
}

// Mark Mode Radio Group
.mark-mode-group {
  display: flex;
  flex-direction: column;
  gap: 8px;

  :deep(.el-radio) {
    margin-right: 0;
    height: auto;
    width: 100%;

    .el-radio__input {
      padding-top: 12px;
    }

    .el-radio__label {
      padding-left: 8px;
      flex: 1;
    }
  }
}

.radio-card {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 0;

  .radio-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--gh-text-primary);
  }

  .radio-desc {
    font-size: 12px;
    color: var(--gh-text-placeholder);
  }
}

// Line Style Group
.line-style-group {
  display: flex;
  gap: 8px;
}

.line-style-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border: 1.5px solid var(--gh-border);
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--gh-bg-card);

  .line-preview {
    width: 100%;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;

    .line-demo {
      width: 80%;
      height: 0;

      &.solid {
        border-top: 2px solid var(--gh-text-primary);
      }

      &.dashed {
        border-top: 2px dashed var(--gh-text-primary);
      }

      &.arrow {
        border-top: 2px solid var(--gh-text-primary);
        position: relative;

        &::after {
          content: '';
          position: absolute;
          right: -2px;
          top: -5px;
          width: 0;
          height: 0;
          border-left: 6px solid var(--gh-text-primary);
          border-top: 4px solid transparent;
          border-bottom: 4px solid transparent;
        }
      }
    }
  }

  .line-name {
    font-size: 12px;
    color: var(--gh-text-secondary);
  }

  &:hover {
    border-color: var(--gh-primary);
  }

  &.active {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.04);

    .line-name {
      color: var(--gh-primary);
      font-weight: 500;
    }

    .line-demo {
      &.solid, &.arrow {
        border-color: var(--gh-primary);

        &::after {
          border-left-color: var(--gh-primary);
        }
      }

      &.dashed {
        border-color: var(--gh-primary);
      }
    }
  }
}

// Font Settings
.font-settings-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.font-field {
  .field-label {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    display: block;
    margin-bottom: 6px;
  }
}

.color-picker-row {
  display: flex;
  gap: 8px;
}

.color-pick {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--gh-border);
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    border-color: var(--gh-primary);
    box-shadow: 0 0 0 3px rgba(37, 99, 255, 0.15);
  }

  &:hover {
    transform: scale(1.1);
  }
}

// Output Settings
.output-settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.output-row {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .output-label {
    font-size: 14px;
    color: var(--gh-text-secondary);
    flex-shrink: 0;
  }
}

// Generate count
.gen-count-group {
  display: flex;
  gap: 8px;

  .count-item {
    width: var(--gh-btn-height);
    height: var(--gh-btn-height);
    border-radius: var(--gh-radius-btn);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 500;
    border: 1px solid var(--gh-border);
    background: var(--gh-bg-card);
    cursor: pointer;
    transition: all 0.2s;
    color: var(--gh-text-secondary);

    &:hover {
      border-color: var(--gh-primary);
      color: var(--gh-primary);
    }

    &.active {
      background: var(--gh-primary);
      border-color: var(--gh-primary);
      color: #fff;
    }
  }
}

// AI Chat Section
.ai-chat-section {
  margin-top: 8px;

  .config-label {
    font-size: 14px;
    font-weight: 500;
    color: var(--gh-text-primary);
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .ai-label-icon {
    color: var(--gh-primary);
  }
}

.ai-chat-box {
  border: 1px solid var(--gh-border-light);
  border-radius: 12px;
  overflow: hidden;
  background: var(--gh-bg-page);
}

.ai-messages {
  max-height: 200px;
  overflow-y: auto;
  padding: 12px;
}

.ai-bubble-welcome {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--gh-text-secondary);

  .ai-avatar-sm {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: rgba(37, 99, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--gh-primary);
    flex-shrink: 0;
    font-size: 12px;
  }
}

.ai-quick {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-item {
  padding: 8px 12px;
  border-radius: var(--gh-radius-sm);
  border: 1px solid var(--gh-border);
  font-size: 12px;
  color: var(--gh-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--gh-bg-card);

  &:hover {
    border-color: var(--gh-primary);
    color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.03);
  }
}

.msg-item {
  margin-bottom: 10px;

  &.user {
    .msg-bubble { flex-direction: row-reverse; }
    .msg-content {
      background: var(--gh-primary);
      color: #fff;
      border-radius: 14px 4px 14px 14px;
    }
  }

  &.assistant {
    .msg-content {
      background: var(--gh-bg-card);
      color: var(--gh-text-primary);
      border-radius: 4px 14px 14px 14px;
    }
  }
}

.msg-bubble {
  display: flex;
  gap: 6px;
  align-items: flex-start;
}

.msg-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(37, 99, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gh-primary);
  flex-shrink: 0;
  font-size: 12px;
}

.msg-content {
  padding: 8px 12px;
  font-size: 12px;
  line-height: 1.6;
  max-width: 85%;
  word-break: break-word;

  &.typing {
    display: flex;
    gap: 4px;
    padding: 12px 16px;

    span {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: var(--gh-text-placeholder);
      animation: typing 1.4s infinite;

      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-3px); }
}

.ai-input-row {
  padding: 8px;
  border-top: 1px solid var(--gh-border-light);
  background: var(--gh-bg-card);

  :deep(.el-input-group__append) {
    padding: 0 12px;
    .el-button {
      margin: 0;
      font-size: 12px;
    }
  }
}

// Generate button
.generate-btn {
  width: 100%;
  height: var(--gh-btn-height);
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--gh-radius-btn);
  margin-top: 16px;

  .points-cost {
    margin-left: 8px;
    font-size: 12px;
    opacity: 0.8;
  }
}
</style>
