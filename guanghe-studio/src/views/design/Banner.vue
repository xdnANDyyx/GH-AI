<template>
  <div class="workspace-page">
    <!-- Left Canvas Area -->
    <div class="canvas-area">
      <!-- Step Bar -->
      <div class="step-bar">
        <div
          v-for="(s, i) in bannerSteps"
          :key="i"
          class="step-item"
          :class="{ active: currentStep === i, done: currentStep > i }"
        >
          <div class="step-dot">
            <el-icon v-if="currentStep > i"><Check /></el-icon>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s.label }}</span>
          <div class="step-line" v-if="i < bannerSteps.length - 1"></div>
        </div>
      </div>

      <!-- Canvas Content -->
      <div class="canvas-content">
        <!-- Upload Zone (flex:1) -->
        <div class="canvas-upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
          <div class="upload-placeholder">
            <div class="upload-icon-gray">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect x="6" y="10" width="36" height="28" rx="4" stroke="#C0C4CC" stroke-width="2" fill="none"/>
                <circle cx="18" cy="22" r="4" stroke="#C0C4CC" stroke-width="2" fill="none"/>
                <path d="M6 32 L16 24 L24 30 L32 22 L42 30 L42 34 C42 36.2 40.2 38 38 38 L10 38 C7.8 38 6 36.2 6 34 Z" fill="#E5E7EB" opacity="0.5"/>
              </svg>
            </div>
            <p class="upload-main-text">拖拽图片到画布，或<span class="highlight">从右侧上传素材</span></p>
            <p class="upload-sub-text">支持 JPG / PNG / WebP，建议宽度 ≥ 1200px</p>
          </div>
          <div class="upload-tip-bar">
            <el-icon :size="14"><InfoFilled /></el-icon>
            <span>建议使用高质量素材，获得更佳效果</span>
          </div>
        </div>

        <!-- Bottom Section: 热门模板 -->
        <div class="hot-templates-section">
          <div class="hot-templates-header">
            <span class="section-title">热门模板</span>
          </div>

          <div class="hot-templates-row">
            <div
              v-for="tpl in hotTemplates"
              :key="tpl.id"
              class="hot-tpl-card"
              :class="{ active: selectedTemplate === tpl.id }"
              @click="selectedTemplate = tpl.id"
            >
              <div class="hot-tpl-preview" :style="{ background: tpl.bg }">
                <div class="hot-tpl-text">
                  <div class="hot-tpl-main">{{ tpl.mainText }}</div>
                  <div class="hot-tpl-sub">{{ tpl.subText }}</div>
                </div>
              </div>
              <div class="hot-tpl-name">{{ tpl.name }}</div>
            </div>
          </div>

          <div class="hot-templates-footer">
            <el-button round size="small" @click="refreshTemplates">
              <el-icon><Refresh /></el-icon> 换一换
            </el-button>
            <span class="size-hint-text">建议尺寸：1200×300px、1920×600px、1920×1080px 等主流尺寸</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Config Panel -->
    <div class="config-panel">
      <el-scrollbar>
        <div class="config-inner">
          <!-- Panel header -->
          <div class="panel-header">创作配置</div>

          <!-- Section 1: 画布尺寸 -->
          <div class="gh-config-section">
            <div class="config-label">画布尺寸</div>
            <div class="size-preset-cards">
              <div
                v-for="s in sizePresets"
                :key="s.value"
                class="size-preset-card"
                :class="{ active: selectedSize === s.value }"
                @click="selectedSize = s.value"
              >
                <div class="size-preset-dim">{{ s.dim }}</div>
                <div class="size-preset-tag">{{ s.tag }}</div>
              </div>
            </div>
            <div class="custom-size-row">
              <div class="size-input-field">
                <span class="field-label">宽度</span>
                <el-input v-model="customWidth" size="default">
                  <template #append>px</template>
                </el-input>
              </div>
              <span class="size-sep">×</span>
              <div class="size-input-field">
                <span class="field-label">高度</span>
                <el-input v-model="customHeight" size="default">
                  <template #append>px</template>
                </el-input>
              </div>
            </div>
          </div>

          <!-- Section 2: 上传素材 -->
          <div class="gh-config-section">
            <div class="section-label">上传素材</div>
            <div class="section-body">
              <div class="upload-cards-row">
                <div class="upload-card required" @click="triggerUpload">
                  <el-icon :size="20"><Plus /></el-icon>
                  <span class="upload-card-title">上传产品图（必传）</span>
                  <span class="upload-card-hint">支持 JPG/PNG，最多 10 张</span>
                </div>
                <div class="upload-card" @click="triggerBgUpload">
                  <el-icon :size="20"><Plus /></el-icon>
                  <span class="upload-card-title">上传背景图（可选）</span>
                </div>
                <div class="upload-card" @click="triggerLogoUpload">
                  <el-icon :size="20"><Plus /></el-icon>
                  <span class="upload-card-title">上传 LOGO（可选）</span>
                </div>
              </div>
              <p class="section-helper">建议使用高清素材，支持拖拽上传</p>
            </div>
          </div>

          <!-- Section 3: Banner 类型 -->
          <div class="gh-config-section">
            <div class="config-label">Banner 类型</div>
            <div class="banner-type-grid">
              <div
                v-for="t in bannerTypes"
                :key="t.name"
                class="banner-type-card"
                :class="{ active: selectedBannerType === t.name }"
                @click="selectedBannerType = t.name"
              >
                <div class="banner-type-name">{{ t.name }}</div>
                <div class="banner-type-desc">{{ t.desc }}</div>
              </div>
            </div>
          </div>

          <!-- Section 4: 核心目的 -->
          <div class="gh-config-section">
            <div class="config-label">核心目的</div>
            <div class="purpose-checkbox-group">
              <el-checkbox
                v-for="p in purposeOptions"
                :key="p.value"
                v-model="p.checked"
                class="purpose-checkbox"
              >
                {{ p.label }}
              </el-checkbox>
            </div>
          </div>

          <!-- Section 5: 关键信息 -->
          <div class="gh-config-section">
            <div class="config-label">关键信息</div>
            <div class="key-info-fields">
              <div class="info-field">
                <div class="info-field-header">
                  <span class="info-field-label">主标题</span>
                  <span class="info-field-counter">{{ mainTitle.length }}/30</span>
                </div>
                <el-input
                  v-model="mainTitle"
                  placeholder="输入主标题，如：SUMMER SALE"
                  maxlength="30"
                  size="default"
                />
              </div>
              <div class="info-field">
                <div class="info-field-header">
                  <span class="info-field-label">副标题</span>
                  <span class="info-field-counter">{{ subTitle.length }}/50</span>
                </div>
                <el-input
                  v-model="subTitle"
                  placeholder="输入副标题，如：UP TO 50% OFF"
                  maxlength="50"
                  size="default"
                />
              </div>
              <div class="info-field">
                <div class="info-field-header">
                  <span class="info-field-label">按钮文案</span>
                  <span class="info-field-counter">{{ btnText.length }}/20</span>
                </div>
                <el-input
                  v-model="btnText"
                  placeholder="输入按钮文案，如：SHOP NOW"
                  maxlength="20"
                  size="default"
                />
              </div>
            </div>
          </div>

          <!-- Section 6: 生成数量 -->
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

          <!-- Generate Button -->
          <el-button type="primary" size="large" class="generate-btn" :loading="generating" @click="handleGenerate">
            <el-icon><MagicStick /></el-icon>
            生成 Banner（下一步）
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
                  placeholder="请输入您的需求..."
                  rows="4"
                  maxlength="2000"
                ></textarea>
                <div class="ai-input-footer">
                  <span class="ai-counter">{{ aiInput.length }}/2000</span>
                  <button class="ai-send-btn" @click="sendAiMessage" :disabled="!aiInput.trim()">发送</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <input type="file" ref="fileInput" accept="image/*" multiple style="display:none" @change="handleFile" />
    <input type="file" ref="bgFileInput" accept="image/*" style="display:none" />
    <input type="file" ref="logoFileInput" accept="image/*" style="display:none" />
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'

const fileInput = ref(null)
const bgFileInput = ref(null)
const logoFileInput = ref(null)
const messagesRef = ref(null)
const generating = ref(false)
const currentStep = ref(0)
const panelCollapsed = ref(false)
const selectedTemplate = ref(1)
const selectedSize = ref('1200x300')
const customWidth = ref('1200')
const customHeight = ref('300')
const genCount = ref(3)
const selectedBannerType = ref('促销活动')
const mainTitle = ref('')
const subTitle = ref('')
const btnText = ref('')

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

const bannerSteps = [
  { label: '选择模板' },
  { label: '编辑内容' },
  { label: '样式设置' },
  { label: '生成导出' }
]

const sizePresets = [
  { value: '1200x300', dim: '1200×300', tag: '编辑' },
  { value: '1920x600', dim: '1920×600', tag: '适配' },
  { value: '1920x1080', dim: '1920×1080', tag: '大屏' }
]

const hotTemplates = ref([
  { id: 1, name: '夏季促销', bg: 'linear-gradient(135deg, #2563FF, #60A5FA)', mainText: 'SUMMER SALE', subText: 'UP TO 50% OFF' },
  { id: 2, name: '大促活动', bg: 'linear-gradient(135deg, #DC2626, #F97316)', mainText: 'MEGA SALE', subText: 'Discount up to 70%' },
  { id: 3, name: '新品上市', bg: 'linear-gradient(135deg, #1F2937, #4B5563)', mainText: 'NEW ARRIVAL', subText: 'SHOP NOW' },
  { id: 4, name: '季节上新', bg: 'linear-gradient(135deg, #D97706, #F59E0B)', mainText: 'AUTUMN COLLECTION', subText: 'SHOP NOW' },
  { id: 5, name: '包邮活动', bg: 'linear-gradient(135deg, #059669, #34D399)', mainText: 'FREE SHIPPING', subText: 'ON ORDERS OVER $50' }
])

const bannerTypes = [
  { name: '促销活动', desc: '打折促销、限时优惠' },
  { name: '新品上市', desc: '当季新品展示' },
  { name: '品牌宣传', desc: '提升品牌认知' },
  { name: '节日季节', desc: '结合节日主题' },
  { name: '信息通知', desc: '公告或信息展示' },
  { name: '店铺装修', desc: '店铺形象展示' }
]

const purposeOptions = ref([
  { value: 'sales', label: '提升销量', checked: true },
  { value: 'brand', label: '品牌宣传', checked: false },
  { value: 'clearance', label: '清仓', checked: false },
  { value: 'newproduct', label: '新品推广', checked: false },
  { value: 'event', label: '活动宣传', checked: false },
  { value: 'holiday', label: '节日营销', checked: false },
  { value: 'other', label: '其他', checked: false }
])

const quickQuestions = [
  '如何提高Banner点击率？',
  '推荐的Banner尺寸有哪些？',
  '怎样搭配Banner配色？'
]

function triggerUpload() { fileInput.value?.click() }
function triggerBgUpload() { bgFileInput.value?.click() }
function triggerLogoUpload() { logoFileInput.value?.click() }
function handleDrop() { currentStep.value = 1 }
function handleFile() { currentStep.value = 1 }

function refreshTemplates() {
  const arr = [...hotTemplates.value]
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]]
  }
  hotTemplates.value = arr
}

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
      '提高Banner点击率的关键：使用高对比配色、突出核心卖点文字、添加清晰的CTA按钮、产品图占比30-40%。',
      '推荐尺寸：首页轮播 1920×600px、商品详情 1200×300px、社交媒体 1200×628px、广告投放 1920×1080px。',
      'Banner配色建议：主色不超过3种，使用品牌色为主色调，辅以对比色突出文字，白色/浅色文字在深色背景上效果最佳。'
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
  min-height: 180px;
  position: relative;

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon-gray {
  margin-bottom: 8px;
  color: var(--gh-text-placeholder);
  opacity: 0.6;
}

.upload-main-text {
  font-size: 14px;
  font-weight: 400;
  color: var(--gh-text-secondary);

  .highlight {
    color: var(--gh-primary);
    font-weight: 500;
  }
}

.upload-sub-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--gh-text-placeholder);
}

.upload-tip-bar {
  position: absolute;
  bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--gh-text-placeholder);
  background: var(--gh-bg-page);
  padding: 6px 14px;
  border-radius: 20px;

  .el-icon {
    color: var(--gh-warning);
  }
}

// ===== Hot Templates Section =====
.hot-templates-section {
  flex-shrink: 0;
  padding: 16px 24px 20px;
  border-top: 1px solid var(--gh-border-light);
  background: var(--gh-bg-card);
}

.hot-templates-header {
  margin-bottom: 12px;

  .section-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--gh-text-primary);
  }
}

.hot-templates-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 2px; }
}

.hot-tpl-card {
  flex-shrink: 0;
  width: 240px;
  cursor: pointer;
  border-radius: var(--gh-radius-sm);
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.2s;
  background: var(--gh-bg-card);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    border-color: #93B4FF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &.active {
    border-color: var(--gh-primary);
    box-shadow: 0 4px 16px rgba(37, 99, 255, 0.15);
  }
}

.hot-tpl-preview {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.hot-tpl-text {
  text-align: center;
  color: #fff;
  z-index: 1;
}

.hot-tpl-main {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  line-height: 1.3;
}

.hot-tpl-sub {
  font-size: 8px;
  opacity: 0.85;
  margin-top: 2px;
  font-weight: 500;
}

.hot-tpl-name {
  padding: 6px 8px;
  font-size: 12px;
  color: var(--gh-text-secondary);
  text-align: center;
  font-weight: 500;
  background: var(--gh-bg-card);
}

.hot-templates-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;

  .size-hint-text {
    font-size: 12px;
    color: var(--gh-text-placeholder);
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

// ===== Section: 画布尺寸 =====
.size-preset-cards {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.size-preset-card {
  flex: 1;
  padding: 10px 8px;
  border: 1.5px solid var(--gh-border);
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  background: var(--gh-bg-card);

  .size-preset-dim {
    font-size: 13px;
    font-weight: 500;
    color: var(--gh-text-primary);
    margin-bottom: 4px;
  }

  .size-preset-tag {
    font-size: 11px;
    color: var(--gh-text-placeholder);
  }

  &:hover {
    border-color: var(--gh-primary);
  }

  &.active {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.04);

    .size-preset-dim {
      color: var(--gh-primary);
    }

    .size-preset-tag {
      color: var(--gh-primary);
    }
  }
}

.custom-size-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;

  .size-input-field {
    flex: 1;

    .field-label {
      font-size: 12px;
      color: var(--gh-text-secondary);
      display: block;
      margin-bottom: 4px;
    }
  }

  .size-sep {
    color: var(--gh-text-placeholder);
    font-size: 14px;
    padding-bottom: 10px;
  }
}

// ===== Section: 上传素材 =====
.upload-cards-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 12px;
  border: 1.5px dashed var(--gh-border);
  border-radius: var(--gh-radius-btn);
  cursor: pointer;
  transition: all 0.2s;
  background: #FAFBFC;
  text-align: center;

  .el-icon {
    color: var(--gh-text-placeholder);
  }

  .upload-card-title {
    font-size: 13px;
    font-weight: 500;
    color: var(--gh-text-secondary);
  }

  .upload-card-hint {
    font-size: 11px;
    color: var(--gh-text-placeholder);
  }

  &.required {
    border-color: var(--gh-primary);
    border-style: dashed;

    .upload-card-title {
      color: var(--gh-primary);
    }
  }

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);

    .el-icon, .upload-card-title {
      color: var(--gh-primary);
    }
  }
}

.section-helper {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-top: 8px;
}

// ===== Section: Banner 类型 =====
.banner-type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.banner-type-card {
  padding: 10px 8px;
  border: 1.5px solid var(--gh-border);
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  background: var(--gh-bg-card);

  .banner-type-name {
    font-size: 13px;
    font-weight: 500;
    color: var(--gh-text-primary);
    margin-bottom: 2px;
  }

  .banner-type-desc {
    font-size: 10px;
    color: var(--gh-text-placeholder);
    line-height: 1.3;
  }

  &:hover {
    border-color: var(--gh-primary);
  }

  &.active {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.04);

    .banner-type-name {
      color: var(--gh-primary);
    }
  }
}

// ===== Section: 核心目的 =====
.purpose-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .purpose-checkbox {
    :deep(.el-checkbox__label) {
      font-size: 13px;
      color: var(--gh-text-secondary);
    }
  }
}

// ===== Section: 关键信息 =====
.key-info-fields {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-field {
  .info-field-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;

    .info-field-label {
      font-size: 13px;
      font-weight: 500;
      color: var(--gh-text-primary);
    }

    .info-field-counter {
      font-size: 12px;
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
