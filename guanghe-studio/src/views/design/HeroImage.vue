<template>
  <div class="workspace-page">
    <!-- Left Canvas Area -->
    <div class="canvas-area">
      <!-- Step Bar (3 steps) -->
      <div class="step-bar">
        <div
          v-for="(s, i) in heroSteps"
          :key="i"
          class="step-item"
          :class="{ active: currentStep === i, done: currentStep > i }"
        >
          <div class="step-dot">
            <el-icon v-if="currentStep > i"><Check /></el-icon>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s.label }}</span>
          <div class="step-line" v-if="i < heroSteps.length - 1"></div>
        </div>
      </div>

      <!-- Toolbar row -->
      <div class="toolbar-row">
        <div class="toolbar-box">
          <button class="tb-btn" @click.stop="fitScreen">
            <el-icon :size="16"><Monitor /></el-icon>
            <span>适应屏幕</span>
          </button>
          <div class="tb-divider"></div>
          <button class="tb-icon-btn" title="撤销" @click.stop>
            <el-icon :size="16"><RefreshLeft /></el-icon>
          </button>
          <button class="tb-icon-btn" title="恢复" @click.stop>
            <el-icon :size="16"><RefreshRight /></el-icon>
          </button>
          <div class="tb-divider"></div>
          <button class="tb-btn" title="清空" @click.stop="clearCanvas">
            <el-icon :size="16"><Delete /></el-icon>
            <span>清空</span>
          </button>
          <div class="tb-divider"></div>
          <div class="tb-zoom">
            <button class="tb-icon-btn" @click.stop="zoomOut">
              <el-icon :size="14"><Minus /></el-icon>
            </button>
            <span class="zoom-val">{{ zoomValue }}%</span>
            <button class="tb-icon-btn" @click.stop="zoomIn">
              <el-icon :size="14"><Plus /></el-icon>
            </button>
          </div>
          <div class="tb-divider"></div>
          <button class="tb-icon-btn" title="全屏" @click.stop="toggleFullscreen">
            <el-icon :size="16"><FullScreen /></el-icon>
          </button>
        </div>
      </div>

      <!-- Canvas Content -->
      <div class="canvas-content">
        <div class="canvas-workspace" v-if="!generated">
          <div class="gh-upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
            <div v-if="productImage" class="upload-preview-hero">
              <img :src="productImage" class="hero-preview-img" />
            </div>
            <template v-else>
              <div class="upload-icon">
                <el-icon :size="48" color="#9CA3AF"><PictureFilled /></el-icon>
              </div>
              <p class="upload-text">拖拽图片到画布，或点击右侧上传</p>
              <p class="upload-hint">支持 JPG / PNG，建议尺寸 &ge; 2000px</p>
            </template>
          </div>
          <div class="canvas-tip">
            <el-icon :size="14"><InfoFilled /></el-icon>
            <span>提示：建议上传高质量的产品图片，以获得更好的生成效果。</span>
          </div>
        </div>

        <!-- Generated Results -->
        <div class="result-area" v-else>
          <div class="hero-grid">
            <div v-for="i in genCount" :key="i" class="hero-card">
              <div
                class="hero-thumb"
                :style="{ background: `linear-gradient(135deg, hsl(${200 + i * 15}, 60%, 88%), hsl(${280 + i * 20}, 50%, 85%))` }"
              >
                <span>主图方案 {{ i }}</span>
              </div>
              <div class="hero-card-actions">
                <el-button text size="small"><el-icon><Star /></el-icon></el-button>
                <el-button text size="small" type="primary"><el-icon><Download /></el-icon></el-button>
              </div>
            </div>
          </div>
          <div class="result-actions">
            <el-button @click="generated = false"><el-icon><RefreshLeft /></el-icon> 重新生成</el-button>
            <el-button type="primary" @click="$router.push('/detail-page')">下一步：详情页 <el-icon><Right /></el-icon></el-button>
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

          <!-- Section 1: Upload materials -->
          <div class="config-section">
            <div class="section-label">上传素材</div>
            <div class="section-body">
              <div class="upload-field">
                <div class="upload-field-header">
                  <span class="upload-field-label">商品图（可选）</span>
                </div>
                <el-button class="upload-btn" @click="triggerUpload">
                  <el-icon><UploadFilled /></el-icon> 上传商品图
                </el-button>
                <p class="upload-hint-text">支持 JPG/PNG，最多 10 张</p>
              </div>
              <div class="upload-field">
                <div class="upload-field-header">
                  <span class="upload-field-label">参考图（可选）</span>
                </div>
                <el-button class="upload-btn secondary" @click="triggerRefUpload">
                  <el-icon><UploadFilled /></el-icon> 上传参考图
                </el-button>
                <p class="upload-hint-text">支持 JPG/PNG，最多 5 张</p>
              </div>
              <p class="section-note">参考图用于提供风格、构图或场景参考（可选）</p>
            </div>
          </div>

          <!-- Section 2: Platform & Language -->
          <div class="config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('platform')">
              <span class="section-label no-margin">平台与语言</span>
              <span class="expand-text">
                {{ sections.platform ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.platform }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.platform" class="section-body">
              <div class="form-field">
                <span class="field-label">目标平台</span>
                <el-select v-model="platform" size="default" style="width: 100%">
                  <el-option label="亚马逊 Amazon" value="amazon" />
                  <el-option label="Shopee" value="shopee" />
                  <el-option label="Lazada" value="lazada" />
                  <el-option label="速卖通" value="aliexpress" />
                  <el-option label="淘宝" value="taobao" />
                  <el-option label="京东" value="jd" />
                </el-select>
              </div>
              <div class="form-field">
                <span class="field-label">语言</span>
                <el-select v-model="language" size="default" style="width: 100%">
                  <el-option label="英语（美国）" value="en-us" />
                  <el-option label="英语（英国）" value="en-gb" />
                  <el-option label="中文" value="zh" />
                  <el-option label="日本語" value="ja" />
                  <el-option label="한국어" value="ko" />
                </el-select>
              </div>
            </div>
          </div>

          <!-- Section 3: Canvas Size -->
          <div class="config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('canvasSize')">
              <span class="section-label no-margin">画布尺寸</span>
              <span class="expand-text">
                {{ sections.canvasSize ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.canvasSize }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.canvasSize" class="section-body">
              <div class="form-field">
                <span class="field-label">选择尺寸</span>
                <el-select v-model="canvasSize" size="default" style="width: 100%">
                  <el-option label="2000 x 2000 (1:1)" value="2000x2000" />
                  <el-option label="2000 x 2667 (3:4)" value="2000x2667" />
                  <el-option label="1500 x 1500 (1:1)" value="1500x1500" />
                </el-select>
              </div>
              <p class="section-note">亚马逊主图建议：2000px x 2000px 以上效果更佳</p>
            </div>
          </div>

          <!-- Section 4: Purpose -->
          <div class="config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('purpose')">
              <span class="section-label no-margin">主图用途</span>
              <span class="expand-text">
                {{ sections.purpose ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.purpose }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.purpose" class="section-body">
              <p class="section-desc">本主图的主要用途：</p>
              <div class="purpose-grid">
                <div
                  v-for="p in purposeCards"
                  :key="p.name"
                  class="purpose-card"
                  :class="{ active: selectedPurposes.includes(p.name) }"
                  @click="togglePurpose(p.name)"
                >
                  <el-icon :size="20" class="purpose-icon"><component :is="p.icon" /></el-icon>
                  <div class="purpose-info">
                    <span class="purpose-title">{{ p.name }}</span>
                    <span class="purpose-desc">{{ p.desc }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Section 5: Selling Points -->
          <div class="config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('sellingPoints')">
              <span class="section-label no-margin">核心卖点</span>
              <span class="expand-text">
                {{ sections.sellingPoints ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.sellingPoints }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.sellingPoints" class="section-body">
              <p class="section-desc">选择 1-3 个关键卖点突出展示：</p>
              <div class="tag-group">
                <div
                  v-for="t in sellingPointTags"
                  :key="t"
                  class="tag-item"
                  :class="{ active: selectedSellingPoints.includes(t) }"
                  @click="toggleSellingPoint(t)"
                >
                  {{ t }}
                </div>
                <div class="tag-item add-tag" @click="showCustomSP = true">
                  <el-icon><Plus /></el-icon> 自定义卖点
                </div>
              </div>
            </div>
          </div>

          <!-- Section 6: Generate Count -->
          <div class="config-section collapsible">
            <div class="section-header collapsible" @click="toggleSection('genCount')">
              <span class="section-label no-margin">生成数量</span>
              <span class="expand-text">
                {{ sections.genCount ? '收起' : '展开' }}
                <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.genCount }"><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-show="sections.genCount" class="section-body">
              <p class="section-desc">生成张数：</p>
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
            </div>
          </div>

          <!-- Generate button -->
          <el-button type="primary" size="large" class="generate-btn" :loading="generating" @click="handleGenerate">
            <el-icon><MagicStick /></el-icon>
            生成主图
            <span class="btn-points">-6 积分</span>
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

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFile" />
    <input type="file" ref="refFileInput" accept="image/*" style="display:none" />

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
import { ref, reactive, markRaw } from 'vue'
import {
  Goods, TrendCharts, Calendar, Present, Trophy, MoreFilled,
  UploadFilled, Star, Download, MagicStick, Right, Delete,
  RefreshLeft, RefreshRight, ArrowDown, FullScreen,
  Minus, Plus, ChatDotRound, Monitor, Check, PictureFilled,
  InfoFilled
} from '@element-plus/icons-vue'

const fileInput = ref(null)
const refFileInput = ref(null)
const productImage = ref('')
const generated = ref(false)
const generating = ref(false)
const currentStep = ref(0)
const platform = ref('amazon')
const language = ref('en-us')
const canvasSize = ref('2000x2000')
const genCount = ref(3)
const zoomValue = ref(100)
const showCustomSP = ref(false)
const customSPText = ref('')

const aiInput = ref('')
const aiMessages = ref([])

const sections = reactive({
  platform: true,
  canvasSize: true,
  purpose: true,
  sellingPoints: true,
  genCount: true,
  ai: true
})

const heroSteps = [
  { label: '上传素材' },
  { label: '选择配置' },
  { label: '生成主图' }
]

const purposeCards = [
  { name: '新品上市', desc: '突出新品，吸引关注', icon: markRaw(Goods) },
  { name: '提升转化', desc: '突出产品卖点', icon: markRaw(TrendCharts) },
  { name: '季节/节日', desc: '结合节日或季节', icon: markRaw(Calendar) },
  { name: '促销活动', desc: '展示折扣或促销', icon: markRaw(Present) },
  { name: '品牌宣传', desc: '品牌形象宣传', icon: markRaw(Trophy) },
  { name: '其他用途', desc: '品牌标识使用', icon: markRaw(MoreFilled) }
]
const selectedPurposes = ref(['新品上市'])

const sellingPointTags = [
  '高品质材料', '耐用结实', '舒适体验', '易于安装', '多功能使用', '大容量收纳',
  '环保健康', '节省空间', '防水防滑', '安全可靠', '轻便便携', '设计感强'
]
const selectedSellingPoints = ref([])

const quickQuestions = [
  '如何提升主图点击率？',
  '亚马逊主图规范是什么？',
  '推荐更适合的卖点'
]

function toggleSection(key) {
  sections[key] = !sections[key]
}

function triggerUpload() { fileInput.value?.click() }
function triggerRefUpload() { refFileInput.value?.click() }

function togglePurpose(name) {
  const idx = selectedPurposes.value.indexOf(name)
  if (idx >= 0) {
    selectedPurposes.value.splice(idx, 1)
  } else if (selectedPurposes.value.length < 3) {
    selectedPurposes.value.push(name)
  }
}

function toggleSellingPoint(t) {
  const idx = selectedSellingPoints.value.indexOf(t)
  if (idx >= 0) {
    selectedSellingPoints.value.splice(idx, 1)
  } else if (selectedSellingPoints.value.length < 3) {
    selectedSellingPoints.value.push(t)
  }
}

function addCustomSP() {
  if (customSPText.value.trim()) {
    sellingPointTags.push(customSPText.value.trim())
    selectedSellingPoints.value.push(customSPText.value.trim())
    customSPText.value = ''
    showCustomSP.value = false
  }
}

function handleDrop() { currentStep.value = 1 }
function handleFile(e) {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = ev => { productImage.value = ev.target.result }
    reader.readAsDataURL(file)
    currentStep.value = 1
  }
}

function sendAiMessage() {
  if (!aiInput.value.trim()) return
  aiMessages.value.push({
    text: aiInput.value,
    reply: '好的，正在为您生成中...'
  })
  aiInput.value = ''
}

function zoomIn() { zoomValue.value = Math.min(zoomValue.value + 10, 200) }
function zoomOut() { zoomValue.value = Math.max(zoomValue.value - 10, 50) }
function fitScreen() { zoomValue.value = 100 }
function toggleFullscreen() { /* placeholder */ }
function clearCanvas() { productImage.value = ''; generated.value = false; currentStep.value = 0 }

function handleGenerate() {
  generating.value = true
  currentStep.value = 2
  setTimeout(() => {
    generated.value = true
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
  overflow: hidden;
}

// ===== Step Bar =====
.step-bar {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid var(--gh-border, #EBEDF5);
  flex-shrink: 0;
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
    font-size: var(--gh-font-helper, 12px);
    font-weight: var(--gh-weight-semibold, 600);
    background: var(--gh-border-light, #F3F4F6);
    color: var(--gh-text-placeholder, #9CA3AF);
    transition: all 0.3s;
  }

  .step-label {
    font-size: var(--gh-font-body, 14px);
    font-weight: var(--gh-weight-regular, 400);
    color: var(--gh-text-placeholder, #9CA3AF);
    margin-left: 8px;
    white-space: nowrap;
    transition: color 0.3s;
  }

  .step-line {
    width: 48px;
    height: 2px;
    background: var(--gh-border-light, #F3F4F6);
    margin: 0 12px;
    flex-shrink: 0;
    transition: background 0.3s;
  }

  &.active {
    .step-dot {
      background: var(--gh-primary, #2563FF);
      color: #fff;
      box-shadow: 0 0 0 4px rgba(37, 99, 255, 0.15);
    }
    .step-label {
      color: var(--gh-primary, #2563FF);
      font-weight: var(--gh-weight-medium, 500);
    }
  }

  &.done {
    .step-dot {
      background: var(--gh-success, #22C55E);
      color: #fff;
    }
    .step-label {
      color: var(--gh-text-secondary, #6B6B6B);
    }
    .step-line {
      background: var(--gh-success, #22C55E);
    }
  }
}

// ===== Toolbar Row =====
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 24px;
  border-bottom: 1px solid var(--gh-border, #EBEDF5);
  flex-shrink: 0;
  background: #fff;
}

.toolbar-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-btn, 10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tb-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-medium, 500);
  color: var(--gh-text-primary, #1F1F1F);
  border-radius: 6px;
  transition: background 0.2s;

  &:hover { background: var(--gh-border-light, #F3F4F6); }
  span { white-space: nowrap; }
}

.tb-icon-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--gh-text-secondary, #6B6B6B);
  border-radius: 6px;
  transition: all 0.2s;

  &:hover { background: var(--gh-border-light, #F3F4F6); color: var(--gh-text-primary, #1F1F1F); }
}

.tb-divider {
  width: 1px;
  height: 20px;
  background: var(--gh-border, #EBEDF5);
  flex-shrink: 0;
}

.tb-zoom {
  display: flex;
  align-items: center;
  gap: 4px;
}

.zoom-val {
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-medium, 500);
  color: var(--gh-text-primary, #1F1F1F);
  min-width: 36px;
  text-align: center;
  user-select: none;
  cursor: pointer;
}

// ===== Canvas Content =====
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
  justify-content: center;
  padding: 24px;
  gap: 12px;
  min-height: 0;
}

.gh-upload-zone {
  border: 2px dashed var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-card, 16px);
  padding: 48px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--gh-bg-page, #F7F9FC);
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.02);
  }

  .upload-icon {
    margin-bottom: 16px;
    opacity: 0.7;
  }

  .upload-text {
    font-size: var(--gh-font-body, 14px);
    font-weight: var(--gh-weight-regular, 400);
    color: var(--gh-text-secondary, #6B6B6B);
    margin-bottom: 4px;
  }

  .upload-hint {
    font-size: var(--gh-font-helper, 12px);
    font-weight: var(--gh-weight-regular, 400);
    color: var(--gh-text-placeholder, #9CA3AF);
  }
}

.upload-preview-hero {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;

  .hero-preview-img {
    max-width: 60%;
    max-height: 300px;
    object-fit: contain;
    border-radius: var(--gh-radius-sm, 8px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.canvas-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-regular, 400);
  color: var(--gh-text-placeholder, #9CA3AF);
  padding: 8px 16px;
  background: var(--gh-bg-page, #F7F9FC);
  border-radius: var(--gh-radius-sm, 8px);
  flex-shrink: 0;

  .el-icon {
    flex-shrink: 0;
    color: var(--gh-warning, #F59E0B);
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

.hero-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.hero-card {
  border-radius: var(--gh-radius-card, 16px);
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--gh-border, #EBEDF5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s;

  &:hover { box-shadow: var(--gh-shadow-card, 0 8px 24px rgba(0, 0, 0, 0.06)); }
}

.hero-thumb {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--gh-font-body, 14px);
  color: var(--gh-text-secondary, #6B6B6B);
  border-radius: var(--gh-radius-card, 16px) var(--gh-radius-card, 16px) 0 0;

  span { font-weight: var(--gh-weight-medium, 500); }
}

.hero-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding: 8px 12px;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;

  .el-button {
    height: var(--gh-btn-height, 40px);
    border-radius: var(--gh-radius-btn, 10px);
    font-weight: var(--gh-weight-medium, 500);
  }
}

// ===== Config Panel =====
.config-panel {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-left: 1px solid var(--gh-border, #EBEDF5);
  overflow-y: auto;
}

.config-inner {
  padding: 20px;
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

.config-divider {
  height: 1px;
  background: var(--gh-border, #EBEDF5);
  margin: 4px 0 16px;
}

.config-section {
  margin-bottom: 16px;
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

.expand-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--gh-text-placeholder, #9CA3AF);
  display: flex;
  align-items: center;
  gap: 2px;
}

.expand-arrow {
  transition: transform 0.25s;

  &.expanded { transform: rotate(180deg); }
  &:not(.expanded) { transform: rotate(0deg); }
}

// -- Collapsible sections --
.config-section.collapsible {
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

.section-body {
  padding: 8px 0 4px;
}

.section-desc {
  font-size: 13px;
  font-weight: var(--gh-weight-regular, 400);
  color: var(--gh-text-secondary, #6B6B6B);
  margin-bottom: 10px;
}

.section-note {
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-regular, 400);
  color: var(--gh-text-placeholder, #9CA3AF);
  margin-top: 8px;
  line-height: 1.5;
}

// Upload fields
.upload-field {
  margin-bottom: 12px;

  &:last-of-type { margin-bottom: 0; }
}

.upload-field-header {
  margin-bottom: 6px;
}

.upload-field-label {
  font-size: 13px;
  font-weight: var(--gh-weight-medium, 500);
  color: var(--gh-text-primary, #1F1F1F);
}

.upload-btn {
  width: 100%;
  height: var(--gh-btn-height, 40px);
  border-radius: var(--gh-radius-btn, 10px) !important;
  font-size: var(--gh-font-body, 14px);
  font-weight: var(--gh-weight-medium, 500);
  border: 1.5px solid var(--gh-primary, #2563FF) !important;
  color: var(--gh-primary, #2563FF) !important;
  background: rgba(37, 99, 255, 0.04) !important;
  transition: all 0.2s;

  &:hover {
    background: rgba(37, 99, 255, 0.08) !important;
  }

  &.secondary {
    border-color: var(--gh-border, #EBEDF5) !important;
    color: var(--gh-text-secondary, #6B6B6B) !important;
    background: transparent !important;

    &:hover {
      border-color: var(--gh-primary, #2563FF) !important;
      color: var(--gh-primary, #2563FF) !important;
    }
  }
}

.upload-hint-text {
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-regular, 400);
  color: var(--gh-text-placeholder, #9CA3AF);
  margin: 6px 0 0;
}

// Form fields
.form-field {
  margin-bottom: 12px;

  &:last-child { margin-bottom: 0; }

  .field-label {
    font-size: 13px;
    font-weight: var(--gh-weight-medium, 500);
    color: var(--gh-text-primary, #1F1F1F);
    display: block;
    margin-bottom: 6px;
  }
}

// Purpose cards
.purpose-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.purpose-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border: 1.5px solid var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-btn, 10px);
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;

  .purpose-icon {
    color: var(--gh-text-placeholder, #9CA3AF);
    flex-shrink: 0;
    margin-top: 2px;
  }

  .purpose-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;

    .purpose-title {
      font-size: 13px;
      font-weight: var(--gh-weight-medium, 500);
      color: var(--gh-text-primary, #1F1F1F);
    }

    .purpose-desc {
      font-size: 11px;
      font-weight: var(--gh-weight-regular, 400);
      color: var(--gh-text-placeholder, #9CA3AF);
      line-height: 1.3;
    }
  }

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.02);
  }

  &.active {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.04);

    .purpose-icon {
      color: var(--gh-primary, #2563FF);
    }

    .purpose-title {
      color: var(--gh-primary, #2563FF);
    }
  }
}

// Tag group (selling points)
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: var(--gh-font-tag, 14px);
  font-weight: var(--gh-weight-regular, 400);
  border: 1px solid var(--gh-border, #EBEDF5);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--gh-text-secondary, #6B6B6B);
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    color: var(--gh-primary, #2563FF);
  }

  &.active {
    background: var(--gh-primary, #2563FF);
    border-color: var(--gh-primary, #2563FF);
    color: #fff;
  }

  &.add-tag {
    border-style: dashed;
    color: var(--gh-primary, #2563FF);
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.04);

    &:hover {
      background: rgba(37, 99, 255, 0.08);
    }
  }
}

// Generate count
.gen-count-group {
  display: flex;
  gap: 8px;
}

.count-item {
  flex: 1;
  height: var(--gh-btn-height, 40px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--gh-font-body, 14px);
  font-weight: var(--gh-weight-medium, 500);
  border: 1.5px solid var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-btn, 10px);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--gh-text-secondary, #6B6B6B);

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    color: var(--gh-primary, #2563FF);
  }

  &.active {
    background: var(--gh-primary, #2563FF);
    border-color: var(--gh-primary, #2563FF);
    color: #fff;
  }
}

// Generate button
.generate-btn {
  width: 100%;
  height: var(--gh-btn-height, 40px);
  font-size: var(--gh-font-body, 14px);
  font-weight: var(--gh-weight-semibold, 600);
  border-radius: var(--gh-radius-btn, 10px);
  margin-bottom: 16px;
}

.btn-points {
  margin-left: 8px;
  font-size: var(--gh-font-helper, 12px);
  opacity: 0.85;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: var(--gh-radius-xs, 4px);
}

// ===== AI Assistant =====
.ai-section {
  margin-top: 4px;
}

.ai-label {
  font-weight: var(--gh-weight-semibold, 600);
  color: var(--gh-text-primary, #1F1F1F);
}

.ai-chat {
  max-height: 160px;
  overflow-y: auto;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-semibold, 600);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bubble-content {
  padding: 8px 12px;
  border-radius: var(--gh-radius-btn, 10px);
  font-size: var(--gh-font-body, 14px);
  line-height: 1.5;
  font-weight: var(--gh-weight-regular, 400);
  max-width: 85%;

  .ai-bubble.bot & {
    background: var(--gh-border-light, #F3F4F6);
    color: var(--gh-text-primary, #1F1F1F);
  }

  .ai-bubble.user & {
    background: var(--gh-primary, #2563FF);
    color: #fff;
  }

  p { margin: 0; }
}

.ai-quick-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.quick-action {
  display: block;
  padding: 8px 12px;
  font-size: var(--gh-font-helper, 12px);
  font-weight: var(--gh-weight-regular, 400);
  color: var(--gh-text-secondary, #6B6B6B);
  background: #fff;
  border: 1px solid var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-sm, 8px);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.03);
  }
}

.ai-input-wrap {
  border: 1.5px solid var(--gh-border, #EBEDF5);
  border-radius: var(--gh-radius-btn, 10px);
  overflow: hidden;
  transition: border-color 0.2s;

  &:focus-within { border-color: var(--gh-primary, #2563FF); }
}

.ai-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: 8px 12px;
  font-size: var(--gh-font-body, 14px);
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

.ai-counter {
  font-size: 12px;
  font-weight: 400;
  color: var(--gh-text-placeholder, #9CA3AF);
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
</style>
