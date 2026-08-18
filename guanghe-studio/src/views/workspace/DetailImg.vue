<template>
  <div class="detail-page">
    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== Canvas Area ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Steps bar（显示在画布顶部，不超出画布区域） -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 4)">
              <div class="step-num">{{ i + 1 }}</div>
              <span>{{ s.label }}</span>
            </div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>

        <!-- Canvas Display (no upload) -->
        <div class="canvas-box">
          <div v-if="productFiles.length === 0 && resultImages.length === 0" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>AI 详情图生成后将显示在此处</h3>
            <p>请在右侧配置生成参数并点击发送</p>
          </div>
          <!-- 有结果图时：展示结果 -->
          <div v-else-if="resultImages.length > 0" class="result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, idx) in resultImages" :key="'r'+idx" class="result-card">
              <img :src="img.url || img" class="result-img" />
            </div>
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>
          <!-- 仅上传产品图，无结果时 -->
          <div v-else class="canvas-result" :class="{ generating: isGenerating }">
            <img v-for="(f, idx) in productFiles" :key="idx" :src="getObjectUrl(f)" class="uploaded-img" />
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>
        </div>

        <div class="canvas-bottom-bar">AI生成的内容仅供参考，请注意核对细节与版权信息。</div>

        <!-- Page Management -->
        <div class="page-mgmt">
          <div class="page-mgmt-header">
            <span class="page-mgmt-title">页面管理</span>
            <span class="page-mgmt-subtitle">（可拖拽排序）</span>
          </div>
          <div class="page-list-wrapper">
            <div class="page-list" ref="pageListRef">
              <div class="page-card-add" @click="addPage">
                <div class="page-card-add-icon">+</div>
                <div class="page-card-add-text">添加页面</div>
              </div>
              <div
                v-for="(page, i) in pages"
                :key="i"
                class="page-card"
                :class="{ selected: selectedPage === i }"
                @click="selectedPage = i"
              >
                <div class="page-card-img" :class="page.bgClass">
                  <div class="page-card-img-inner">
                    <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" v-html="page.icon"></svg>
                    <span>{{ page.desc }}</span>
                  </div>
                </div>
                <div class="page-card-label">{{ i + 1 }} {{ page.label }}</div>
              </div>
            </div>
            <div class="page-arrow" @click="scrollPageList(200)">›</div>
          </div>
          <div class="page-mgmt-footer">
            <span class="page-mgmt-hint">建议页数：5~10页，突出核心卖点，提升转化效果</span>
            <span class="page-mgmt-count">共 {{ pages.length }} 页</span>
          </div>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ right panels -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'right')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowRight v-if="!configCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
      </div>

      <!-- ===== RIGHT: Config Panel + AI ===== -->
      <div class="right-col" :style="{ flex: rightFlex }">
        <!-- Right panel divider: config ⇔ AI -->
        <div v-show="!configCollapsed" class="right-panel-divider" @mousedown="startRightPanelResize($event, 'config')"></div>
        
        <!-- ===== Config Panel ===== -->
        <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
          <el-scrollbar v-show="!configCollapsed">
            <div class="config-inner">
              <!-- Header -->
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

              <!-- Section: 页面尺寸 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('pageSize')">
                  <span class="section-label">页面尺寸</span>
                  <span class="expand-text">
                    {{ sections.pageSize ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.pageSize }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.pageSize">
                  <span class="config-label">选择尺寸</span>
                  <select class="form-select block" v-model="pageWidth">
                    <option value="970">970 宽度（亚马逊A+ 推荐）</option>
                    <option value="750">750 宽度（淘宝/京东 推荐）</option>
                    <option value="800">800 宽度（Shopee 推荐）</option>
                    <option value="1200">1200 宽度（自定义）</option>
                  </select>
                  <p class="config-hint">高度将根据内容自动延展</p>
                </div>
              </div>

              <!-- Section: 平台与语言 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('platform')">
                  <span class="section-label">平台与语言</span>
                  <span class="expand-text">
                    {{ sections.platform ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.platform }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.platform">
                  <span class="config-label">目标平台</span>
                  <div class="option-tags">
                    <div
                      v-for="p in platforms"
                      :key="p"
                      class="option-tag"
                      :class="{ active: selectedPlatform === p }"
                      @click="selectedPlatform = (selectedPlatform === p ? '' : p)"
                    >{{ p }}</div>
                  </div>
                  <span class="config-label">语言</span>
                  <select class="form-select block" v-model="language">
                    <option value="en">英语（美国）</option>
                    <option value="zh">中文（简体）</option>
                    <option value="ja">日语</option>
                    <option value="ko">韩语</option>
                  </select>
                </div>
              </div>

              <!-- Section: 核心卖点 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('sellingPoints')">
                  <span class="section-label">核心卖点 <span style="font-weight:400;font-size:11px;color:#9CA3AF;">（建议选择 3~6 项）</span></span>
                  <span class="expand-text">
                    {{ sections.sellingPoints ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.sellingPoints }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.sellingPoints">
                  <div class="tag-grid">
                    <div
                      v-for="p in sellingPoints"
                      :key="p"
                      class="tag-btn"
                      :class="{ active: activePoints.includes(p) }"
                      @click="togglePoint(p)"
                    >{{ p }}</div>
                    <div class="tag-btn add-tag" @click="customPoint = ''; customPointDialog = true">+ 自定义卖点</div>
                  </div>
                </div>
              </div>

              <!-- Section: 画布尺寸 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('canvasSize')">
                  <span class="section-label">画布尺寸</span>
                  <span class="expand-text">
                    {{ sections.canvasSize ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.canvasSize }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.canvasSize">
                  <el-select v-model="outputSize" placeholder="请选择输出尺寸" style="width: 100%">
                    <el-option label="不指定" value="" />
                    <el-option label="1:1  (1080×1080)" value="1:1" />
                    <el-option label="3:4  (1080×1440)" value="3:4" />
                    <el-option label="4:3  (1440×1080)" value="4:3" />
                    <el-option label="自定义" value="custom" />
                  </el-select>
                  <!-- 自定义尺寸 -->
                  <div v-if="outputSize === 'custom'" class="custom-size-row">
                    <div class="custom-size-input">
                      <span>宽</span>
                      <el-input-number v-model="customWidth" :min="100" :max="9999" :step="10" size="small" controls-position="right" />
                    </div>
                    <span class="custom-size-x">×</span>
                    <div class="custom-size-input">
                      <span>高</span>
                      <el-input-number v-model="customHeight" :min="100" :max="9999" :step="10" size="small" controls-position="right" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 上传参考图 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('refUpload')">
                  <span class="section-label">上传参考图</span>
                  <span class="expand-text">
                    {{ sections.refUpload ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.refUpload }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.refUpload">
                  <div class="upload-row">
                    <div class="upload-card" @click="triggerUpload">
                      <div class="upload-card-label">产品图片（必传）</div>
                      <div class="upload-card-action">↑ 上传产品图</div>
                      <div class="upload-card-hint">支持 JPG / PNG，最多 10 张</div>
                    </div>
                    <div class="upload-card" @click="triggerRefUpload">
                      <div class="upload-card-label">参考图（可选）</div>
                      <div class="upload-card-action">↑ 上传参考图</div>
                      <div class="upload-card-hint">支持 JPG / PNG，最多 5 张</div>
                    </div>
                    <div class="upload-card" @click="triggerLogoUpload">
                      <div class="upload-card-label">品牌素材（可选）</div>
                      <div class="upload-card-action">↑ 上传LOGO</div>
                      <div class="upload-card-hint">支持 PNG，透明背景更佳</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 内容结构 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('contentStructure')">
                  <span class="section-label-group">
                    <span class="section-label">内容结构</span>
                    <span class="section-label-hint">（可拖拽调整顺序）</span>
                  </span>
                  <div class="section-header-right">
                    <span class="structure-link">推荐结构</span>
                    <span class="expand-text">
                      {{ sections.contentStructure ? '收起' : '展开' }}
                      <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.contentStructure }"><ArrowDown /></el-icon>
                    </span>
                  </div>
                </div>
                <div class="section-body" v-show="sections.contentStructure">
                  <div class="structure-list">
                    <div
                      v-for="(item, i) in contentStructure"
                      :key="i"
                      class="structure-item"
                    >
                      <div class="structure-icon" v-html="item.icon"></div>
                      <div class="structure-info">
                        <div class="structure-name">{{ item.name }} <span>/ {{ item.desc }}</span></div>
                        <div class="structure-detail">{{ item.detail }}</div>
                      </div>
                      <div class="toggle-switch" :class="{ off: !item.enabled }" @click="item.enabled = !item.enabled">
                        <div class="toggle-knob"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section: 生成数量 -->
              <div class="config-section collapsible">
                <div class="section-header" @click="toggleSection('genCount')">
                  <span class="section-label">生成数量</span>
                  <span class="expand-text">
                    {{ sections.genCount ? '收起' : '展开' }}
                    <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.genCount }"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="section-body" v-show="sections.genCount">
                  <span class="config-label">生成数量</span>
                  <div class="option-tags">
                    <div
                      v-for="n in [1,2,3,4,5]"
                      :key="n"
                      class="option-tag"
                      :class="{ active: genCount === n }"
                      @click="genCount = (genCount === n ? '' : n)"
                    >{{ n }}张</div>
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- Divider inside right panel: config ⇔ AI -->
        <div class="right-panel-divider" @mousedown="startRightPanelResize($event, 'ai')"></div>

        <!-- ===== AI Assistant Panel ===== -->
        <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
          <div class="ai-header">
            <span class="panel-title">AI 助手</span>
            <span class="ai-clear" @click="clearChat">清空对话</span>
          </div>
          <div class="ai-body">
            <div class="ai-chat" ref="chatRef">
              <div v-for="(msg, i) in chatMessages" :key="i" class="ai-bubble" :class="{ user: msg.role === 'user' }">
                <div v-if="msg.role === 'ai'" class="ai-avatar">AI</div>
                <div class="ai-msg">{{ msg.content }}</div>
                <div v-if="msg.role === 'user'" class="user-avatar">我</div>
              </div>
            </div>
            <div class="ai-quick">
              <span class="ai-quick-tag" v-for="t in quickTags" :key="t" @click="sendQuick(t)">{{ t }}</span>
            </div>
            <div class="ai-input-area">
              <textarea
                class="ai-textarea"
                v-model="chatInput"
                placeholder="请输入您的需求，描述越详细，效果越好..."
                @keydown.enter.exact.prevent="sendMessage"
                maxlength="2000"
              ></textarea>
              <div class="chat-footer">
                <div class="chat-footer-left">
                  <span class="char-count">{{ chatInput.length }}/2000</span>
                </div>
                <div class="chat-footer-right">
                  <el-select v-model="selectedModel" size="small" class="model-select" :disabled="isGenerating">
                    <el-option v-for="m in modelOptions" :key="m.value" :label="m.label" :value="m.value" />
                  </el-select>
                  <button class="chat-send" @click="sendMessage" :disabled="!chatInput.trim() || isGenerating">
                    <el-icon><Promotion /></el-icon>
                    {{ isGenerating ? '生成中...' : '发送' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hidden file input -->
    <input type="file" ref="fileInputRef" hidden accept="image/*" multiple @change="onFileChange" />
    <input type="file" ref="refInputRef" hidden accept="image/*" multiple @change="onRefFileChange" />
    <input type="file" ref="logoInputRef" hidden accept="image/*" @change="onLogoFileChange" />

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
defineOptions({ name: 'DetailImgView' })
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ArrowDown, ArrowLeft, ArrowRight, UploadFilled, DocumentCopy, Promotion } from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { aiDialogue, reversePrompt } from '@/api/customer'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import { ElMessage } from 'element-plus'

// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   canvasSelector: '.canvas-dropzone',
//   defaultName: 'detail-img',
// })
const gen = useImageGeneration('render')
const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

const productFiles = ref([])
const originalImage = ref('')
const resultImages = ref([])

// Generation state from composable
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)

// ===== Canvas & Zoom =====
const zoom = ref(100)

function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
function zoomOut() { zoom.value = Math.max(30, zoom.value - 10) }
function undo() { /* TODO */ }
function redo() { /* TODO */ }
function clearCanvas() { /* TODO */ }
function toggleFullscreen() { /* TODO */ }

// ===== File Upload =====
const fileInputRef = ref(null)
const refInputRef = ref(null)
const logoInputRef = ref(null)

function triggerUpload() { fileInputRef.value?.click() }
function triggerRefUpload() { refInputRef.value?.click() }
function triggerLogoUpload() { logoInputRef.value?.click() }

function onFileChange(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  productFiles.value = [...productFiles.value, ...files].slice(0, 10)
  if (!originalImage.value) originalImage.value = URL.createObjectURL(files[0])
  e.target.value = ''
}
function onRefFileChange(e) {
  const files = e.target.files
  console.log('Ref files:', files)
  e.target.value = ''
}
function onLogoFileChange(e) {
  const file = e.target.files?.[0]
  console.log('Logo file:', file)
  e.target.value = ''
}
function handleDrop(e) {
  const files = Array.from(e.dataTransfer?.files || [])
  if (!files.length) return
  const imgFiles = files.filter(f => f.type.startsWith('image/'))
  if (!imgFiles.length) return
  productFiles.value = [...productFiles.value, ...imgFiles].slice(0, 10)
  if (!originalImage.value) originalImage.value = URL.createObjectURL(imgFiles[0])
}

// ===== Pages with Furniture Themes =====
const pages = ref([
  {
    label: '封面图', desc: '吸引注意力', bgClass: 'furniture-icon-sofa',
    icon: '<path d="M6 16h36v16a4 4 0 0 1-4 4H10a4 4 0 0 1-4-4V16z"/><path d="M6 16V12a4 4 0 0 1 4-4h28a4 4 0 0 1 4 4v4"/><path d="M6 24h36"/><rect x="10" y="8" width="5" height="8" rx="1.5"/><rect x="33" y="8" width="5" height="8" rx="1.5"/><circle cx="20" cy="20" r="1.5" fill="currentColor"/><circle cx="28" cy="20" r="1.5" fill="currentColor"/>'
  },
  {
    label: '核心卖点', desc: '解决用户需求', bgClass: 'furniture-icon-bed',
    icon: '<path d="M4 26V14a4 4 0 0 1 4-4h32a4 4 0 0 1 4 4v12"/><rect x="4" y="26" width="40" height="10" rx="2"/><line x1="6" y1="36" x2="6" y2="42"/><line x1="42" y1="36" x2="42" y2="42"/><rect x="8" y="16" width="14" height="8" rx="2"/><rect x="26" y="16" width="14" height="8" rx="2"/>'
  },
  {
    label: '功能展示', desc: '产品功能亮点', bgClass: 'furniture-icon-chair',
    icon: '<rect x="14" y="8" width="20" height="4" rx="2"/><path d="M14 12v14a8 8 0 0 0 8 8h4a8 8 0 0 0 8-8V12"/><rect x="12" y="36" width="5" height="6" rx="1"/><rect x="31" y="36" width="5" height="6" rx="1"/><rect x="20" y="14" width="8" height="2" rx="1"/>'
  },
  {
    label: '细节展示', desc: '产品细节', bgClass: 'furniture-icon-lamp',
    icon: '<path d="M20 40h8"/><path d="M22 8v6l-6 14h16l-6-14V8"/><path d="M18 28h12"/><line x1="16" y1="8" x2="32" y2="8"/><line x1="24" y1="4" x2="24" y2="8"/>'
  },
  {
    label: '场景应用', desc: '使用场景', bgClass: 'furniture-icon-table',
    icon: '<rect x="6" y="8" width="36" height="4" rx="2"/><rect x="10" y="12" width="4" height="26" rx="1"/><rect x="34" y="12" width="4" height="26" rx="1"/><rect x="22" y="12" width="4" height="26" rx="1"/>'
  },
  {
    label: '尺寸参数', desc: '规格参数', bgClass: 'furniture-icon-cabinet',
    icon: '<rect x="6" y="6" width="36" height="34" rx="3"/><line x1="6" y1="22" x2="42" y2="22"/><line x1="6" y1="14" x2="42" y2="14"/><line x1="6" y1="30" x2="42" y2="30"/><circle cx="24" cy="10" r="2"/><circle cx="24" cy="18" r="2"/><circle cx="24" cy="26" r="2"/>'
  },
  {
    label: '售后保障', desc: '售后服务', bgClass: 'furniture-icon-decor',
    icon: '<circle cx="24" cy="24" r="14"/><path d="M24 16v8l6 4"/><path d="M24 10v4"/><path d="M10 24h4"/><path d="M34 24h4"/>'
  },
])
const selectedPage = ref(0)
const pageListRef = ref(null)

function addPage() {
  const i = pages.value.length + 1
  const themes = ['furniture-icon-sofa', 'furniture-icon-bed', 'furniture-icon-chair', 'furniture-icon-lamp', 'furniture-icon-table', 'furniture-icon-cabinet', 'furniture-icon-decor']
  pages.value.push({
    label: `新页面 ${i}`,
    desc: '新页面',
    bgClass: themes[i % themes.length],
    icon: '<rect x="8" y="8" width="32" height="32" rx="4"/>'
  })
}
function scrollPageList(offset) {
  const el = pageListRef.value
  if (el) el.scrollBy({ left: offset, behavior: 'smooth' })
}

// ===== Config collapse =====
const configCollapsed = ref(false)

// ===== Layout Resize =====
const _configWidthPx = ref(280)
const _aiWidthPx = ref(360)
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
const aiPanel = ref(null)

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

// ===== Config Panel Sections =====
const allExpanded = computed(() => Object.values(sections).every(Boolean))

const sections = reactive({
  pageSize: true,
  platform: true,
  sellingPoints: true,
  canvasSize: true,
  refUpload: true,
  contentStructure: true,
  genCount: true,
})

const boostProduct = ref('')
const boostMaterial = ref('')
const boostProductRef = ref(null)
const boostMaterialRef = ref(null)

function toggleSection(key) { sections[key] = !sections[key] }

function toggleAllSections() {
  const expand = !allExpanded.value
  Object.keys(sections).forEach(k => sections[k] = expand)
}

// Platform
const platforms = ['亚马逊', 'Shopee', 'Lazada', '速卖通', '淘宝', '京东', '独立站', '其他']
const selectedPlatform = ref('亚马逊')
const language = ref('en')
const pageWidth = ref('970')

// Selling Points
const sellingPoints = ['高品质材料', '耐用结实', '多功能使用', '易于安装', '防刮涂层', '安全环保', '时尚设计', '性价比高', '智能设计', '抗菌防霉', '防滑防水', '静音减震', '节省空间', '大量收纳', '便捷技巧']
const activePoints = ref(['高品质材料', '多功能使用', '安全环保'])
const customPoint = ref('')
const customPointDialog = ref(false)

function togglePoint(p) {
  const idx = activePoints.value.indexOf(p)
  if (idx >= 0) activePoints.value.splice(idx, 1)
  else activePoints.value.push(p)
}

// Size
const outputSize = ref('')
const customWidth = ref(1080)
const customHeight = ref(2160)
const effectiveOutputSize = computed(() => {
  if (outputSize.value === 'custom') return `${customWidth.value}x${customHeight.value}`
  return outputSize.value
})

// Content Structure
const contentStructure = reactive([
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#2563FF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 11h18"/><circle cx="8" cy="8" r="1" fill="#2563FF"/><circle cx="8" cy="16" r="1" fill="#2563FF"/></svg>',
    name: '封面', desc: '吸引注意力', detail: '展示核心卖点与产品形象', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#E6A817" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>',
    name: '核心卖点', desc: '解决用户需求', detail: '3~5 个核心卖点展示', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#22C55E" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 8h20"/><path d="M2 16h20"/><circle cx="7" cy="12" r="1.5" fill="#22C55E"/><circle cx="12" cy="12" r="1.5" fill="#22C55E"/><circle cx="17" cy="12" r="1.5" fill="#22C55E"/></svg>',
    name: '产品展示', desc: '多角度展示', detail: '全方位展示产品细节', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#8B5CF6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/><line x1="2" y1="20" x2="22" y2="20"/></svg>',
    name: '对比优势', desc: '建立信任', detail: '与竞品对比，突出优势', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>',
    name: '使用场景', desc: '增强代入感', detail: '展示真实使用场景', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#06B6D4" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="21"/></svg>',
    name: '产品参数', desc: '专业信息', detail: '规格参数与产品信息', enabled: true
  },
  {
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M9 12l2 2 4-4"/></svg>',
    name: '售后保障', desc: '打消顾虑', detail: '售后服务与保障说明', enabled: true
  },
])

// Generate Count
const genCount = ref('')

// AI Model
const selectedModel = ref('deepseek')
const modelOptions = [
  { label: 'DeepSeek', value: 'deepseek' },
  { label: '通义千问 Qwen', value: 'qwen-plus' },
  { label: '智谱 GLM-4', value: 'glm-4' },
  { label: '豆包 Doubao', value: 'doubao' }
]

// ===== AI Chat =====
const chatRef = ref(null)
const chatInput = ref('')
const chatMessages = ref([
  { role: 'ai', content: '您好！我是光合AI助手，有什么可以帮您？' }
])
const quickTags = ['如何优化我的详情页？', '如何突出产品卖点？', '设计技巧建议']

async function sendMessage() {
  if (!chatInput.value.trim()) return
  if (isGenerating.value) return
  const text = chatInput.value.trim()
  chatMessages.value.push({ role: 'user', content: text })
  chatInput.value = ''
  nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight })
  try {
    const historyMessages = chatMessages.value.slice(0, -1).map(m => ({ role: m.role === 'ai' ? 'assistant' : 'user', content: m.content }))
    const res = await aiDialogue({ messages: historyMessages, content: text, model: selectedModel.value })
    chatMessages.value.push({ role: 'ai', content: res?.data?.reply || '已收到您的需求，请稍候。' })
    if (productFiles.value.length && !resultImages.value.length) {
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      try {
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const fullPrompt = boostText ? `${text}。约束：${boostText}。` : text
        const extra = { consumePoints: 2, featureName: 'detail_img', title: '详情页生成', model: selectedModel.value }
        if (effectiveOutputSize.value) extra.outputSize = effectiveOutputSize.value
        if (genCount.value) extra.n = Number(genCount.value)
        await gen.fullGenerate(productFiles.value, fullPrompt, extra)
        if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
      } catch (e) { console.error('详情图生成失败:', e) }
    }
  } catch (e) {
    chatMessages.value.push({ role: 'ai', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
  }
  nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight })
}


function sendQuick(tag) {
  chatInput.value = tag
  sendMessage()
}

function clearChat() {
  chatMessages.value = [{ role: 'ai', content: '您好！我是光合AI助手，有什么可以帮您？' }]
}

function getObjectUrl(file) {
  return URL.createObjectURL(file)
}

// ===== 反推提示词 =====
const REVERSE_DEFAULT_PROMPT = '请对原图进行逆向视觉解构，推测其生成逻辑与核心构成元素。请以结构化、专业的中文提示词格式输出，需涵盖：结构布局与质感；关键细节；技术参数与视角。输出结果应具有高度可复用性，能直接用于引导图像生成。'
const reverseDialogVisible = ref(false)
const reverseImageFile = ref(null)
const reverseImagePreview = ref('')
const reversePromptInput = ref(REVERSE_DEFAULT_PROMPT)
const reverseResult = ref('')
const reverseLoading = ref(false)

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

onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
})
</script>

<style scoped>
/* ===== Page Layout ===== */
.detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: #F7F9FC;
}

/* ===== Steps Bar ===== */
.steps-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  gap: 0;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #E8EDF5;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.step-item .step-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #E8EDF5;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}
.step-item.active .step-num {
  background: #2563FF;
  color: #fff;
}
.step-item.done .step-num {
  background: #22C55E;
  color: #fff;
}
.step-item span {
  font-size: 12px;
  color: #9CA3AF;
}
.step-item.active span {
  color: #2563FF;
  font-weight: 700;
}
.step-item.done span {
  color: #22C55E;
}
.step-line {
  width: 36px;
  height: 2px;
  background: #E8EDF5;
  margin: 0 6px;
}
.step-line.done { background: #22C55E; }

/* ===== Three Column Layout ===== */
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

/* ===== Right Panel Dividers ===== */
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

/* ===== Canvas Area ===== */
.canvas-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 16px;
  min-width: 300px;
}

.canvas-box {
  flex: 1;
  min-height: 300px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E8EDF5;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.canvas-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #9CA3AF;
  padding: 24px;
  text-align: center;
}
.canvas-placeholder .placeholder-icon {
  width: 48px;
  height: 48px;
  color: #D1D5DB;
}
.canvas-placeholder .placeholder-title {
  font-size: 14px;
  font-weight: 500;
  color: #6B7280;
}
.canvas-placeholder .placeholder-desc {
  font-size: 12px;
  color: #9CA3AF;
}

/* Canvas result (uploaded images) */
.canvas-result {
  flex: 1;
  min-height: 200px;
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-content: flex-start;
  justify-content: center;
  position: relative;
}
.uploaded-img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  object-fit: contain;
}

/* 2x2 result grid */
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

/* Generating overlay */
.generating-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 5;
  border-radius: inherit;
}
.progress-ring {
  width: 64px;
  height: 64px;
}
.progress-ring-text {
  font-size: 14px;
  font-weight: 600;
  color: #2563FF;
}
.generating-hint {
  font-size: 12px;
  color: #6B7280;
}

.canvas-bottom-bar {
  padding: 8px 4px;
  font-size: 11px;
  color: #9CA3AF;
  text-align: center;
  flex-shrink: 0;
}

.page-mgmt {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E8EDF5;
  padding: 16px;
  flex-shrink: 0;
}
.page-mgmt-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
}
.page-mgmt-title { font-size: 14px; font-weight: 700; color: #1F2937; }
.page-mgmt-subtitle { font-size: 12px; color: #9CA3AF; }
.page-list-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  flex: 1;
  padding-bottom: 4px;
}
.page-list::-webkit-scrollbar { height: 4px; }
.page-list::-webkit-scrollbar-thumb { background: #E8EDF5; border-radius: 4px; }
.page-card {
  width: 120px;
  flex-shrink: 0;
  border-radius: 8px;
  border: 1px solid #E8EDF5;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.page-card:hover { border-color: #2563FF; box-shadow: 0 2px 8px rgba(37,99,255,0.1); }
.page-card.selected { border-color: #2563FF; }
.page-card-img {
  width: 100%;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
/* Furniture-themed backgrounds */
.page-card-img.furniture-icon-sofa { background: linear-gradient(135deg, #FEF3C7, #FDE68A); }
.page-card-img.furniture-icon-bed { background: linear-gradient(135deg, #DBEAFE, #BFDBFE); }
.page-card-img.furniture-icon-chair { background: linear-gradient(135deg, #D1FAE5, #A7F3D0); }
.page-card-img.furniture-icon-table { background: linear-gradient(135deg, #EDE9FE, #DDD6FE); }
.page-card-img.furniture-icon-cabinet { background: linear-gradient(135deg, #FCE7F3, #FBCFE8); }
.page-card-img.furniture-icon-lamp { background: linear-gradient(135deg, #FEE2E2, #FECACA); }
.page-card-img.furniture-icon-decor { background: linear-gradient(135deg, #E0E7FF, #C7D2FE); }

.page-card-img-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #6B7280;
  font-size: 11px;
  font-weight: 500;
}
.page-card-img-inner svg { width: 32px; height: 32px; opacity: 0.75; }
.page-card-label {
  padding: 6px 8px;
  font-size: 11px;
  color: #4B5563;
  text-align: center;
  background: #FAFBFC;
}
.page-card-add {
  width: 120px;
  flex-shrink: 0;
  border-radius: 8px;
  border: 2px dashed #E8EDF5;
  height: 112px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.page-card-add:hover { border-color: #2563FF; background: #FAFBFF; }
.page-card-add-icon { font-size: 24px; color: #9CA3AF; }
.page-card-add:hover .page-card-add-icon { color: #2563FF; }
.page-card-add-text { font-size: 11px; color: #9CA3AF; }
.page-arrow {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid #E8EDF5;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  color: #6B7280;
  font-size: 16px;
}
.page-arrow:hover { background: #F3F4F6; }
.page-mgmt-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}
.page-mgmt-hint { font-size: 12px; color: #9CA3AF; }
.page-mgmt-count { font-size: 12px; color: #2563FF; font-weight: 500; }

/* ===== Right Column (Config + AI) ===== */
.right-col {
  display: flex;
  background: #fff;
  min-width: 0;
  overflow: hidden;
}

/* ===== Config Panel ===== */
.config-col {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  min-width: 0;
}

.config-col.collapsed {
  flex: 0 0 0px !important;
  min-width: 0 !important;
  max-width: 0 !important;
  overflow: hidden;
  padding: 0;
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

.section-label {
  font-size: 13px;
  font-weight: 500;
  color: #1F2937;
}

.section-label-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-label-hint {
  font-size: 11px;
  color: #9CA3AF;
  font-weight: 400;
}

.section-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.structure-link {
  font-size: 11px;
  color: #2563FF;
  cursor: pointer;
  text-decoration: underline;
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

.config-label {
  font-size: 12px;
  font-weight: 500;
  color: #6B7280;
  display: block;
  margin-bottom: 6px;
}

.config-hint {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 4px;
  margin-bottom: 0;
}

.form-select {
  padding: 6px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  outline: none;
  background: #fff;
  color: #1F2937;
  cursor: pointer;
  width: 100%;
}
.form-select:focus { border-color: #2563FF; }
.form-select.block { display: block; }

.platform-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-bottom: 12px;
}
.platform-btn {
  padding: 8px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
}
.platform-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.platform-btn.active {
  border-color: #2563FF;
  background: #EEF2FF;
  color: #2563FF;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag-btn {
  padding: 6px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  background: #fff;
  font-size: 11px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
}
.tag-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.tag-btn.active {
  border-color: #2563FF;
  background: #EEF2FF;
  color: #2563FF;
}
.tag-btn.add-tag {
  border-style: dashed;
}

.size-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.size-btn {
  flex: 1;
  min-width: 60px;
  padding: 8px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
  text-align: center;
}
.size-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.size-btn.active {
  border-color: #2563FF;
  background: #EEF2FF;
  color: #2563FF;
}
.size-btn-ratio {
  font-size: 13px;
  font-weight: 500;
  display: block;
}
.size-btn-dim {
  font-size: 10px;
  color: #9CA3AF;
  margin-top: 2px;
  display: block;
}
.size-btn-link {
  min-width: 60px;
  padding: 8px 12px;
  color: #2563FF;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
}

.size-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.size-input {
  width: 80px;
  padding: 6px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  text-align: center;
}
.size-input-x { color: #6B7280; }
.size-input-unit { color: #6B7280; }

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

.upload-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.upload-card {
  border: 2px dashed #E8EDF5;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
}
.upload-card:hover {
  border-color: #2563FF;
  background: #FAFBFF;
}
.upload-card-label {
  font-size: 12px;
  font-weight: 500;
  color: #1F2937;
  margin-bottom: 4px;
}
.upload-card-action {
  font-size: 11px;
  color: #2563FF;
  margin-bottom: 2px;
}
.upload-card-hint {
  font-size: 10px;
  color: #9CA3AF;
  margin: 0;
}

.structure-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.structure-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #F3F4F6;
  border-radius: 8px;
}
.structure-icon {
  flex-shrink: 0;
}
.structure-info {
  flex: 1;
  min-width: 0;
}
.structure-name {
  font-size: 12px;
  font-weight: 500;
  color: #1F2937;
  margin-bottom: 2px;
}
.structure-name span {
  font-weight: 400;
  color: #9CA3AF;
  margin-left: 4px;
}
.structure-detail {
  font-size: 10px;
  color: #9CA3AF;
}

.toggle-switch {
  width: 36px;
  height: 20px;
  background: #2563FF;
  border-radius: 10px;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}
.toggle-switch.off {
  background: #E5E7EB;
}
.toggle-knob {
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: left 0.2s;
}
.toggle-switch.off .toggle-knob {
  left: 18px;
}

.count-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.count-btn {
  padding: 8px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
}
.count-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.count-btn.active {
  border-color: #2563FF;
  background: #EEF2FF;
  color: #2563FF;
}
.count-input {
  width: 50px;
  padding: 8px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  text-align: center;
}

/* ===== AI Assistant Panel ===== */
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
.ai-clear {
  font-size: 11px;
  color: #2563FF;
  cursor: pointer;
}
.ai-clear:hover { text-decoration: underline; }

.ai-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-chat {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 8px;
  min-height: 0;
}

.ai-bubble {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}
.ai-bubble.user {
  flex-direction: row-reverse;
}

.ai-avatar, .user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  flex-shrink: 0;
}
.ai-avatar {
  background: #2563FF;
  color: #fff;
}
.user-avatar {
  background: #9CA3AF;
  color: #fff;
}

.ai-msg {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 12px;
  line-height: 1.6;
  max-width: 85%;
  word-wrap: break-word;
}
.ai-bubble:not(.user) .ai-msg {
  background: #F7F9FC;
  color: #1F2937;
}
.ai-bubble.user .ai-msg {
  background: #EEF2FF;
  color: #2563FF;
}

.ai-quick {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
.ai-quick-tag {
  padding: 6px 10px;
  background: #F7F9FC;
  border-radius: 6px;
  font-size: 11px;
  color: #2563FF;
  cursor: pointer;
  transition: background 0.15s;
}
.ai-quick-tag:hover {
  background: #EEF2FF;
}

.ai-input-area {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}
.ai-textarea {
  width: 100%;
  min-height: 60px;
  max-height: 120px;
  padding: 10px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  font-size: 12px;
  font-family: inherit;
  resize: vertical;
}
.ai-textarea:focus {
  outline: none;
  border-color: #2563FF;
}

.ai-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ai-char-count {
  font-size: 10px;
  color: #9CA3AF;
}
.ai-send-btn {
  padding: 8px 14px;
  background: #2563FF;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.ai-send-btn:hover {
  background: #1D4ED8;
}
.ai-send-btn svg {
  width: 14px;
  height: 14px;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .steps-bar { display: none; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .right-col { flex: 0 0 100% !important; max-height: 50vh; }
  .col-divider { display: none; }
}

@media (max-width: 768px) {
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .right-col { flex: 1 1 auto !important; min-height: 250px; }
  .right-panel-divider { display: none; }
  .config-col { max-height: 200px; overflow-y: auto; }
}

/* ===== Option Tags (toggleable config options) ===== */
.option-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.option-tag {
  padding: 6px 14px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  background: #fff;
  font-size: 12px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}
.option-tag:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.option-tag.active {
  border-color: #2563FF;
  background: #EEF2FF;
  color: #2563FF;
}

/* ===== Reverse Prompt Entry ===== */
.reverse-prompt-entry {
  margin-bottom: 10px;
}
.reverse-prompt-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px dashed #E8EDF5;
  border-radius: 8px;
  background: #FAFBFC;
  color: #2563FF;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.reverse-prompt-btn:hover {
  border-color: #2563FF;
  background: #EEF2FF;
}
.entry-helper {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 4px;
}

/* ===== Output Size Row ===== */
.output-size-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.output-size-row .size-input {
  width: 80px;
  padding: 6px 10px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  font-size: 12px;
  text-align: center;
}

/* ===== AI Chat Footer ===== */
.chat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 6px;
}
.chat-footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.char-count {
  font-size: 11px;
  color: #9CA3AF;
}
.chat-footer-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.model-select {
  width: 140px;
}
.chat-send {
  padding: 8px 14px;
  background: #2563FF;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.chat-send:hover {
  background: #1D4ED8;
}
.chat-send:disabled {
  background: #94A3B8;
  cursor: not-allowed;
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