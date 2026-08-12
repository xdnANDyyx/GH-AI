<template>
  <div class="workspace-page">
    <!-- Left Canvas Area -->
    <div class="canvas-area">
      <!-- Step Bar -->
      <div class="step-bar">
        <div
          v-for="(s, i) in detailSteps"
          :key="i"
          class="step-item"
          :class="{ active: currentStep === i, done: currentStep > i }"
        >
          <div class="step-dot">
            <el-icon v-if="currentStep > i"><Check /></el-icon>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s.label }}</span>
          <div class="step-line" v-if="i < detailSteps.length - 1"></div>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar-row">
        <div class="toolbar-left">
          <el-button text size="small" @click="undo" :disabled="!canUndo">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
          <el-button text size="small" @click="redo" :disabled="!canRedo">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
          <el-divider direction="vertical" />
        </div>
        <div class="toolbar-center">
          <el-button text size="small" @click="zoomOut">
            <el-icon><Minus /></el-icon>
          </el-button>
          <span class="zoom-level" @click="zoomReset">{{ zoomValue }}%</span>
          <el-button text size="small" @click="zoomIn">
            <el-icon><Plus /></el-icon>
          </el-button>
          <el-divider direction="vertical" />
        </div>
        <div class="toolbar-right">
          <el-button text size="small" @click="toggleFullscreen">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- Canvas Content -->
      <div class="canvas-content">
        <!-- Upload Zone -->
        <div class="canvas-upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
          <div class="upload-placeholder">
            <div class="upload-icon-gray">
              <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
                <rect x="8" y="12" width="40" height="32" rx="4" stroke="#C0C4CC" stroke-width="2" fill="none"/>
                <circle cx="22" cy="24" r="5" stroke="#C0C4CC" stroke-width="2" fill="none"/>
                <path d="M8 36 L18 26 L28 34 L36 24 L48 34 L48 40 C48 42.2 46.2 44 44 44 L12 44 C9.8 44 8 42.2 8 40 Z" fill="#E5E7EB" opacity="0.5"/>
              </svg>
            </div>
            <p class="upload-main-text">拖拽图片到画布，或<span class="highlight">从右侧上传素材</span></p>
            <p class="upload-sub-text">生成高转化的详情图 / A+ 页面</p>
            <p class="upload-hint-text">支持多页设计，突出卖点，提升转化率</p>
          </div>
        </div>

        <!-- Page Management Section -->
        <div class="page-mgmt-section">
          <div class="page-mgmt-header">
            <span class="page-mgmt-title">页面管理（可拖拽排序）</span>
            <div class="page-mgmt-actions">
              <span class="page-count-label">共 {{ pages.length }} 页</span>
              <el-button size="small" round @click="addPage">
                <el-icon><Plus /></el-icon> 添加页面
              </el-button>
            </div>
          </div>
          <div class="page-thumbs-scroll">
            <div
              v-for="(page, i) in pages"
              :key="i"
              class="page-thumb-item"
              :class="{ active: activePage === i }"
              @click="activePage = i"
            >
              <div class="thumb-preview" :style="{ background: page.bg }">
                <span class="thumb-label">{{ page.label }}</span>
              </div>
              <div class="thumb-index">{{ i + 1 }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Config Panel -->
    <div class="config-panel">
      <el-scrollbar>
        <div class="config-inner">
          <!-- Panel header (collapsible) -->
          <div class="panel-header" @click="panelCollapsed = !panelCollapsed">
            <span>创作配置</span>
            <span class="expand-text">
              {{ panelCollapsed ? '展开' : '收起' }}
              <el-icon :size="12" class="expand-arrow" :class="{ expanded: !panelCollapsed }"><ArrowDown /></el-icon>
            </span>
          </div>

          <template v-show="!panelCollapsed">

          <!-- Section 1: 页面尺寸 -->
          <div class="gh-config-section">
            <div class="config-label">页面尺寸</div>
            <div class="page-size-display">
              <div class="size-value-row">
                <span class="size-number">970</span>
                <span class="size-unit">宽度（亚马逊A+ 推荐）</span>
              </div>
              <p class="section-helper">高度将根据内容自动拓展</p>
            </div>
          </div>

          <!-- Section 2: 目标平台 -->
          <div class="gh-config-section">
            <div class="config-label">目标平台</div>
            <div class="platform-grid">
              <div
                v-for="p in platforms"
                :key="p"
                class="platform-btn"
                :class="{ active: selectedPlatform === p }"
                @click="selectedPlatform = p"
              >
                {{ p }}
              </div>
            </div>
            <div class="lang-field">
              <span class="field-label">语言</span>
              <el-select v-model="language" size="default" style="width: 100%">
                <el-option label="英语（美国）" value="en-us" />
                <el-option label="英语（英国）" value="en-gb" />
                <el-option label="中文" value="zh" />
                <el-option label="日本語" value="ja" />
                <el-option label="한국어" value="ko" />
                <el-option label="Español" value="es" />
              </el-select>
            </div>
          </div>

          <!-- Section 3: 核心卖点 -->
          <div class="gh-config-section">
            <div class="config-label">核心卖点</div>
            <div class="tag-chip-group">
              <div
                v-for="t in sellingPointOptions"
                :key="t"
                class="tag-chip"
                :class="{ active: selectedSellingPoints.includes(t) }"
                @click="toggleSellingPoint(t)"
              >
                {{ t }}
              </div>
              <div class="tag-chip add-chip" @click="showCustomSP = true">
                <el-icon><Plus /></el-icon> 自定义卖点
              </div>
            </div>
          </div>

          <!-- Section 4: 画布尺寸 -->
          <div class="gh-config-section">
            <div class="config-label">画布尺寸</div>
            <div class="ratio-presets">
              <div
                v-for="r in aspectRatios"
                :key="r.value"
                class="ratio-btn"
                :class="{ active: selectedRatio === r.value }"
                @click="selectedRatio = r.value"
              >
                {{ r.label }}
              </div>
            </div>
            <div class="custom-size-row" v-if="selectedRatio === 'custom'">
              <div class="size-input-field">
                <el-input v-model="aplusWidth" size="default">
                  <template #append>px</template>
                </el-input>
              </div>
              <span class="size-sep">×</span>
              <div class="size-input-field">
                <el-input v-model="aplusHeight" size="default">
                  <template #append>px</template>
                </el-input>
              </div>
            </div>
          </div>

          <!-- Section 5: 上传参考图 (collapsible) -->
          <div class="gh-config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('upload')">
              <span class="section-label no-margin">上传参考图</span>
              <span class="expand-text">
                {{ sections.upload ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.upload" class="section-body">
              <div class="ref-upload-cards">
                <div class="ref-upload-card required" @click="triggerUpload">
                  <div class="ref-card-icon">
                    <el-icon :size="20"><Plus /></el-icon>
                  </div>
                  <span class="ref-card-title">产品图片</span>
                  <span class="ref-card-badge required">必传</span>
                </div>
                <div class="ref-upload-card" @click="triggerRefUpload">
                  <div class="ref-card-icon">
                    <el-icon :size="20"><Plus /></el-icon>
                  </div>
                  <span class="ref-card-title">参考图</span>
                  <span class="ref-card-badge optional">可选</span>
                </div>
                <div class="ref-upload-card" @click="triggerLogoUpload">
                  <div class="ref-card-icon">
                    <el-icon :size="20"><Plus /></el-icon>
                  </div>
                  <span class="ref-card-title">品牌素材/LOGO</span>
                  <span class="ref-card-badge optional">可选</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Section 6: 内容结构 -->
          <div class="gh-config-section">
            <div class="config-label">内容结构</div>
            <div class="structure-list">
              <div v-for="(s, i) in structureItems" :key="i" class="structure-row">
                <el-switch v-model="s.enabled" size="small" />
                <div class="structure-text">
                  <span class="structure-name">{{ s.name }}</span>
                  <span class="structure-desc">{{ s.desc }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Section 7: 生成数量 -->
          <div class="gh-config-section">
            <div class="config-label">生成数量</div>
            <div class="gen-count-row">
              <div
                v-for="n in [1, 2, 3, 4, 5]"
                :key="n"
                class="gen-count-item"
                :class="{ active: genCount === n }"
                @click="genCount = n"
              >
                {{ n }}张
              </div>
              <el-dropdown trigger="click" @command="(v) => genCount = v">
                <div class="gen-count-item custom-btn">
                  自定义 <el-icon :size="12"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="n in [6, 7, 8, 9, 10]" :key="n" :command="n">{{ n }}张</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          </template>

          <!-- Generate Button -->
          <el-button type="primary" size="large" class="generate-btn" :loading="generating" @click="handleGenerate">
            <el-icon><MagicStick /></el-icon>
            生成详情页（下一步）
          </el-button>

          <!-- Divider -->
          <div class="config-divider"></div>

          <!-- AI 助手 -->
          <div class="config-section ai-section">
            <div class="section-header">
              <span class="section-label ai-label">
                <el-icon :size="16" color="#2563FF"><ChatDotRound /></el-icon>
                AI 助手
              </span>
              <span class="clear-chat" @click="clearChat">清空对话</span>
            </div>
            <div class="section-body">
              <div class="ai-chat">
                <div class="ai-bubble bot">
                  <div class="bubble-avatar">AI</div>
                  <div class="bubble-content">
                    <p>您好！我是光合AI助手，有什么可以帮您？</p>
                  </div>
                </div>
                <template v-for="(msg, i) in aiMessages" :key="i">
                  <div class="ai-bubble user">
                    <div class="bubble-content user-content">
                      <p>{{ msg.text }}</p>
                    </div>
                  </div>
                  <div class="ai-bubble bot" v-if="msg.reply">
                    <div class="bubble-avatar">AI</div>
                    <div class="bubble-content">
                      <p>{{ msg.reply }}</p>
                    </div>
                  </div>
                </template>
              </div>
              <div class="ai-input-wrap">
                <textarea
                  v-model="aiInput"
                  class="ai-textarea"
                  placeholder="请输入您的需求，描述越详细，效果越好..."
                  rows="4"
                  maxlength="2000"
                ></textarea>
                <div class="ai-input-footer">
                  <div class="ai-footer-left">
                    <span class="ai-cost-text">本次操作将消耗 2 积分</span>
                    <span class="ai-counter">{{ aiInput.length }}/2000</span>
                  </div>
                  <button class="ai-send-btn" @click="sendAiMessage" :disabled="!aiInput.trim()">
                    <el-icon :size="14"><Promotion /></el-icon>
                    发送
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFile" />
    <input type="file" ref="refFileInput" accept="image/*" style="display:none" />
    <input type="file" ref="logoFileInput" accept="image/*" style="display:none" />

    <!-- Custom Selling Point Dialog -->
    <el-dialog v-model="showCustomSP" title="自定义卖点" width="360px" :append-to-body="true">
      <el-input v-model="customSPText" placeholder="输入自定义卖点" maxlength="20" />
      <template #footer>
        <el-button @click="showCustomSP = false">取消</el-button>
        <el-button type="primary" @click="addCustomSP">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'

const fileInput = ref(null)
const refFileInput = ref(null)
const logoFileInput = ref(null)
const messagesRef = ref(null)
const generating = ref(false)
const currentStep = ref(0)
const panelCollapsed = ref(false)
const activePage = ref(0)
const genCount = ref(3)
const selectedPlatform = ref('Amazon')
const language = ref('en-us')
const selectedRatio = ref('1:2')
const aplusWidth = ref('1080')
const aplusHeight = ref('2160')
const showCustomSP = ref(false)
const customSPText = ref('')
const zoomValue = ref(100)
const canUndo = ref(false)
const canRedo = ref(false)

const aiInput = ref('')
const aiMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessages = ref([])

const sections = reactive({
  upload: true
})

function toggleSection(key) {
  sections[key] = !sections[key]
}

function sendAiMessage() {
  if (!aiInput.value.trim()) return
  aiMessages.value.push({
    text: aiInput.value,
    reply: '好的，正在为您生成中...'
  })
  aiInput.value = ''
}

function clearChat() {
  aiMessages.value = []
  aiInput.value = ''
}

const detailSteps = [
  { label: '选择模板' },
  { label: '上传素材' },
  { label: '页面编辑' },
  { label: '生成导出' }
]

const platforms = ['Amazon', 'Shopee', 'Lazada', '速卖通', '淘宝', '京东', '独立站', '其他']

const sellingPointOptions = ref([
  '高品质材料', '耐用结实', '多功能使用', '智能设计', '抗菌防霉', '静音减震',
  '时尚设计', '性价比高', '节能设计', '防滑防水', '安全环保', '静音降噪'
])
const selectedSellingPoints = ref(['高品质材料', '耐用结实', '多功能使用'])

const aspectRatios = [
  { label: '1:1', value: '1:1' },
  { label: '3:4', value: '3:4' },
  { label: '1:2', value: '1:2' },
  { label: '9:16', value: '9:16' },
  { label: '自定义', value: 'custom' }
]

const pages = ref([
  { label: '封面图', bg: 'linear-gradient(135deg, #2563FF, #60A5FA)' },
  { label: '核心卖点', bg: 'linear-gradient(135deg, #F59E0B, #FBBF24)' },
  { label: '功能展示', bg: 'linear-gradient(135deg, #059669, #34D399)' },
  { label: '细节展示', bg: 'linear-gradient(135deg, #8B5CF6, #A78BFA)' },
  { label: '场景应用', bg: 'linear-gradient(135deg, #EC4899, #F472B6)' },
  { label: '尺寸参数', bg: 'linear-gradient(135deg, #0EA5E9, #38BDF8)' },
  { label: '售后保障', bg: 'linear-gradient(135deg, #6366F1, #818CF8)' }
])

const structureItems = ref([
  { name: '封面', desc: '吸引注意力', enabled: true },
  { name: '核心卖点', desc: '解决用户需求', enabled: true },
  { name: '产品展示', desc: '多角度展示', enabled: true },
  { name: '对比优势', desc: '突出差异化', enabled: true },
  { name: '使用场景', desc: '增强代入感', enabled: true },
  { name: '产品参数', desc: '规格说明', enabled: true },
  { name: '售后保障', desc: '消除顾虑', enabled: true },
  { name: '品牌故事', desc: '建立信任', enabled: true }
])

const quickQuestions = [
  '如何优化我的详情页？',
  '如何卖出产品卖点？',
  '设计技巧建议'
]

function triggerUpload() { fileInput.value?.click() }
function triggerRefUpload() { refFileInput.value?.click() }
function triggerLogoUpload() { logoFileInput.value?.click() }
function handleDrop() { currentStep.value = 1 }
function handleFile() { currentStep.value = 1 }

function toggleSellingPoint(t) {
  const idx = selectedSellingPoints.value.indexOf(t)
  if (idx >= 0) {
    selectedSellingPoints.value.splice(idx, 1)
  } else {
    selectedSellingPoints.value.push(t)
  }
}

function addCustomSP() {
  if (customSPText.value.trim()) {
    sellingPointOptions.value.push(customSPText.value.trim())
    selectedSellingPoints.value.push(customSPText.value.trim())
    customSPText.value = ''
    showCustomSP.value = false
  }
}

function addPage() {
  const labels = ['FAQ', '对比图', '用户评价', '包装清单', '品牌故事']
  const colors = ['#EF4444', '#10B981', '#3B82F6', '#F59E0B', '#8B5CF6']
  const label = labels.find(l => !pages.value.find(p => p.label === l)) || `页面${pages.value.length + 1}`
  const idx = pages.value.length % colors.length
  pages.value.push({ label, bg: `linear-gradient(135deg, ${colors[idx]}, ${colors[(idx + 1) % colors.length]})` })
}

function zoomIn() { zoomValue.value = Math.min(zoomValue.value + 10, 200) }
function zoomOut() { zoomValue.value = Math.max(zoomValue.value - 10, 50) }
function zoomReset() { zoomValue.value = 100 }
function undo() { /* placeholder */ }
function redo() { /* placeholder */ }
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
      '优化详情页建议：封面突出品牌+产品、卖点用图标化展示、参数用对比表格、场景图展示真实使用效果。',
      '卖出产品卖点的关键：用数据支撑、对比竞品、用户证言、场景化展示，让客户感受到价值。',
      '设计技巧：保持视觉一致性、使用对比色突出CTA、留白要充分、字体层级清晰、移动端优先适配。'
    ]
    chatMessages.value.push({ role: 'assistant', content: replies[Math.floor(Math.random() * replies.length)] })
    chatLoading.value = false
    scrollToBottom()
  }, 1000)
}

function handleGenerate() {
  generating.value = true
  currentStep.value = 3
  setTimeout(() => {
    generating.value = false
  }, 2000)
}
</script>

<style lang="scss" scoped>
.workspace-page {
  display: flex;
  gap: 0;
  flex: 1;
  margin: -24px;
}

// ===== Canvas Area =====
.canvas-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.step-bar {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 16px 24px;
  background: var(--gh-bg-card);
  border-bottom: 1px solid var(--gh-border-light);
  flex-shrink: 0;
}

.step-item {
  display: flex;
  align-items: center;
  flex-shrink: 0;

  .step-dot {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    background: var(--gh-border-light);
    color: var(--gh-text-placeholder);
    transition: all 0.3s;
  }

  .step-label {
    font-size: 14px;
    color: var(--gh-text-placeholder);
    margin-left: 8px;
    white-space: nowrap;
    transition: color 0.3s;
  }

  .step-line {
    width: 40px;
    height: 2px;
    background: var(--gh-border-light);
    margin: 0 12px;
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

// ===== Toolbar Row =====
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  background: var(--gh-bg-card);
  border-bottom: 1px solid var(--gh-border-light);
  flex-shrink: 0;

  .toolbar-left,
  .toolbar-center,
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
}

.canvas-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

// ===== Upload Zone =====
.canvas-upload-zone {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed var(--gh-border);
  border-radius: var(--gh-radius-card);
  margin: 20px 24px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #FAFBFC;
  min-height: 200px;

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.upload-icon-gray {
  margin-bottom: 12px;
  color: var(--gh-text-placeholder);
  opacity: 0.6;
}

.upload-main-text {
  font-size: 15px;
  font-weight: 400;
  color: var(--gh-text-secondary);

  .highlight {
    color: var(--gh-primary);
    font-weight: 500;
  }
}

.upload-sub-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--gh-text-primary);
  margin-top: 4px;
}

.upload-hint-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--gh-text-placeholder);
}

// ===== Page Management =====
.page-mgmt-section {
  flex-shrink: 0;
  padding: 12px 24px 16px;
  background: var(--gh-bg-card);
  border-top: 1px solid var(--gh-border-light);
}

.page-mgmt-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;

  .page-mgmt-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--gh-text-primary);
  }

  .page-mgmt-actions {
    display: flex;
    align-items: center;
    gap: 10px;

    .page-count-label {
      font-size: 12px;
      color: var(--gh-text-placeholder);
    }
  }
}

.page-thumbs-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 2px; }
}

.page-thumb-item {
  flex-shrink: 0;
  width: 90px;
  cursor: pointer;
  transition: all 0.2s;

  .thumb-preview {
    width: 90px;
    height: 60px;
    border-radius: var(--gh-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid transparent;
    transition: border-color 0.2s;

    .thumb-label {
      font-size: 10px;
      color: #fff;
      font-weight: 500;
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
    }
  }

  .thumb-index {
    text-align: center;
    font-size: 11px;
    color: var(--gh-text-placeholder);
    margin-top: 4px;
  }

  &.active .thumb-preview {
    border-color: var(--gh-primary);
  }

  &:hover .thumb-preview {
    border-color: #93B4FF;
  }
}

// ===== Config Panel =====
.config-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--gh-bg-card);
  border-left: 1px solid var(--gh-border);
  overflow: hidden;
}

.config-inner {
  padding: 24px;
}

.panel-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--gh-text-primary, #1F1F1F);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
}

.expand-text {
  font-size: 12px;
  color: var(--gh-text-placeholder, #9CA3AF);
  font-weight: 400;
  display: flex;
  align-items: center;
  gap: 2px;
}

.expand-arrow {
  transition: transform 0.25s;

  &.expanded { transform: rotate(180deg); }
  &:not(.expanded) { transform: rotate(0deg); }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--gh-text-primary, #1F1F1F);
  display: flex;
  align-items: center;
  gap: 6px;

  &.no-margin {
    margin-bottom: 0;
  }
}

.section-body {
  padding: 4px 0;
}

// -- Clear chat link --
.clear-chat {
  font-size: 12px;
  color: var(--gh-primary, #2563FF);
  cursor: pointer;
  font-weight: 400;
  transition: opacity 0.2s;

  &:hover { opacity: 0.7; }
}

// -- Collapsible sections --
.gh-config-section.collapsible {
  margin-bottom: 0;
  border-bottom: 1px solid var(--gh-border-light, #F3F4F6);
}

.section-header.collapsible {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 12px 0;
  margin-bottom: 0;
  transition: opacity 0.2s;

  &:hover { opacity: 0.75; }
}

.config-divider {
  height: 1px;
  background: var(--gh-border, #EBEDF5);
  margin: 4px 0 16px;
}

// ========== AI Assistant ==========
.ai-section {
  border-top: none;
  padding-top: 0;
}

.ai-label {
  font-weight: 600;
  color: var(--gh-text-primary, #1F1F1F);
}

.ai-chat {
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-bubble {
  display: flex;
  gap: 8px;

  &.bot { align-items: flex-start; }
  &.user { justify-content: flex-end; }
}

.bubble-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563FF, #4F83FF);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bubble-content {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 400;
  max-width: 85%;

  .ai-bubble.bot & {
    background: #F3F4F6;
    color: var(--gh-text-primary, #1F1F1F);
    border-top-left-radius: 4px;
  }

  .ai-bubble.user & {
    background: var(--gh-primary, #2563FF);
    color: #fff;
    border-top-right-radius: 4px;
  }

  p { margin: 0; }
}

.ai-input-wrap {
  border: 1.5px solid var(--gh-border, #EBEDF5);
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.2s;

  &:focus-within { border-color: var(--gh-primary, #2563FF); }
}

.ai-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: 10px 12px;
  font-size: 14px;
  font-family: inherit;
  color: var(--gh-text-primary, #1F1F1F);
  line-height: 1.5;
  background: transparent;

  &::placeholder { color: var(--gh-text-placeholder, #9CA3AF); }
}

.ai-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #FAFBFC;
}

.ai-footer-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ai-cost-text {
  font-size: 12px;
  color: var(--gh-success, #22C55E);
  font-weight: 400;
}

.ai-counter {
  font-size: 12px;
  color: var(--gh-text-placeholder, #9CA3AF);
  font-weight: 400;
}

.ai-send-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: 8px;
  background: var(--gh-primary, #2563FF);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: opacity 0.2s;

  &:disabled { opacity: 0.4; cursor: not-allowed; }
  &:not(:disabled):hover { opacity: 0.85; }
}

// ===== Section: 页面尺寸 =====
.page-size-display {
  .size-value-row {
    display: flex;
    align-items: baseline;
    gap: 8px;
    padding: 10px 14px;
    background: var(--gh-bg-page);
    border-radius: var(--gh-radius-sm);
    border: 1px solid var(--gh-border-light);

    .size-number {
      font-size: 20px;
      font-weight: 600;
      color: var(--gh-primary);
    }

    .size-unit {
      font-size: 12px;
      color: var(--gh-text-secondary);
    }
  }
}

.section-helper {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-top: 6px;
}

// ===== Section: 目标平台 =====
.platform-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;

  .platform-btn {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 13px;
    border: 1px solid var(--gh-border);
    background: var(--gh-bg-card);
    cursor: pointer;
    transition: all 0.2s;
    color: var(--gh-text-secondary);
    white-space: nowrap;

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

.lang-field {
  .field-label {
    font-size: 13px;
    font-weight: 500;
    color: var(--gh-text-primary);
    display: block;
    margin-bottom: 6px;
  }
}

// ===== Section: 核心卖点 =====
.tag-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .tag-chip {
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 13px;
    border: 1px solid var(--gh-border);
    background: var(--gh-bg-card);
    cursor: pointer;
    transition: all 0.2s;
    color: var(--gh-text-secondary);
    display: flex;
    align-items: center;
    gap: 4px;

    &:hover {
      border-color: var(--gh-primary);
      color: var(--gh-primary);
    }

    &.active {
      background: var(--gh-primary);
      border-color: var(--gh-primary);
      color: #fff;
    }

    &.add-chip {
      border-style: dashed;
      color: var(--gh-primary);
      border-color: var(--gh-primary);
      background: rgba(37, 99, 255, 0.04);

      &:hover {
        background: rgba(37, 99, 255, 0.08);
      }
    }
  }
}

// ===== Section: 画布尺寸 =====
.ratio-presets {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;

  .ratio-btn {
    flex: 1;
    padding: 8px 4px;
    border-radius: var(--gh-radius-sm);
    font-size: 13px;
    font-weight: 500;
    border: 1.5px solid var(--gh-border);
    background: var(--gh-bg-card);
    cursor: pointer;
    text-align: center;
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

.custom-size-row {
  display: flex;
  align-items: center;
  gap: 8px;

  .size-input-field {
    flex: 1;
  }

  .size-sep {
    color: var(--gh-text-placeholder);
    font-size: 14px;
  }
}

// ===== Section: 上传参考图 =====
.ref-upload-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ref-upload-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1.5px dashed var(--gh-border);
  border-radius: var(--gh-radius-btn);
  cursor: pointer;
  transition: all 0.2s;
  background: #FAFBFC;

  .ref-card-icon {
    color: var(--gh-text-placeholder);
    display: flex;
    align-items: center;
  }

  .ref-card-title {
    font-size: 13px;
    color: var(--gh-text-secondary);
    flex: 1;
  }

  .ref-card-badge {
    font-size: 11px;
    font-weight: 500;

    &.required {
      color: var(--gh-danger);
    }

    &.optional {
      color: var(--gh-text-placeholder);
      font-weight: 400;
    }
  }

  &.required {
    border-color: var(--gh-primary);

    .ref-card-title {
      color: var(--gh-primary);
    }
  }

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);

    .ref-card-icon, .ref-card-title {
      color: var(--gh-primary);
    }
  }
}

// ===== Section: 内容结构 =====
.structure-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.structure-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: var(--gh-bg-page);
  border-radius: var(--gh-radius-sm);

  .structure-text {
    display: flex;
    flex-direction: column;
    gap: 1px;

    .structure-name {
      font-size: 13px;
      font-weight: 500;
      color: var(--gh-text-primary);
    }

    .structure-desc {
      font-size: 11px;
      color: var(--gh-text-placeholder);
    }
  }
}

// ===== Section: 生成数量 =====
.gen-count-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.gen-count-item {
  padding: 8px 12px;
  border-radius: var(--gh-radius-btn);
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--gh-border);
  background: var(--gh-bg-card);
  cursor: pointer;
  transition: all 0.2s;
  color: var(--gh-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;

  &:hover {
    border-color: var(--gh-primary);
    color: var(--gh-primary);
  }

  &.active {
    background: var(--gh-primary);
    border-color: var(--gh-primary);
    color: #fff;
  }

  &.custom-btn {
    border-style: dashed;
  }
}

// ===== Generate Button =====
.generate-btn {
  width: 100%;
  height: var(--gh-btn-height);
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--gh-radius-btn);
  margin-top: 8px;
  margin-bottom: 20px;
}

// ===== AI Chat Section =====
.ai-chat-section {
  margin-top: 0;

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
</style>
