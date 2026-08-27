<template>
  <!----><div class="workspace-page">

    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas / Result ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Step progress bar -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 4)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>
        <!-- Toolbar row -->
        <!-- 暂时注释画布上方中间位置小图标
        <div class="toolbar-row">
          <div class="toolbar-box">
            <button class="tb-btn" @click.stop>
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
          </div>
    
        </div>
        -->
         
        <!-- Canvas Area -->
        <div class="canvas-box">
          <!-- 未生成：显示空状态 -->
          <div v-if="resultImages.length === 0" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>AI 模特生成后将显示在此处</h3>
            <p>请在右侧上传商品图并点击发送</p>
          </div>
          <!-- 有结果图时：展示结果 -->
          <div v-else class="result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, idx) in resultImages" :key="'r'+idx" class="result-card">
              <img :src="img.url || img" class="result-img" />
            </div>
          </div>
        </div>

        <div class="canvas-bottom-bar">
          <span>提示：在右侧上传商品图，在AI助手中输入需求后点击发送即可生成。</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ config -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowRight v-if="!configCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
      </div>

      <!-- ===== CENTER: Config Panel ===== -->
      <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
        <el-scrollbar v-show="!configCollapsed">
          <div class="config-inner">
            <!-- Panel header with expand/collapse all -->
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

            <!-- Section: Upload product image -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label"><span class="required-mark">*</span>上传商品图<span class="required-mark">（必填）</span></span>
                <span class="expand-text">
                  {{ sections.upload ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.upload" class="section-body">
                <el-button size="default" plain class="upload-btn-full" @click="triggerUpload">
                  <el-icon><UploadFilled /></el-icon> 上传
                </el-button>
                <p class="upload-hint-text">支持 JPG/PNG，最多 10 张</p>
              </div>
            </div>

            <!-- Section: Select Model -->
            <div class="config-section collapsible">
              <!-- <div class="section-header collapsible" @click="toggleSection('modelSelect')">
                <span class="section-label">选择模特</span>
                <span class="expand-text">
                  {{ sections.modelSelect ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.modelSelect }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.modelSelect" class="section-body">
                <div v-loading="modelLoading" class="model-select-list">
                  <div v-if="modelList.length === 0 && !modelLoading" class="model-select-empty">
                    暂无可用模特
                  </div>
                  <div
                    v-for="model in modelList"
                    :key="model.id"
                    class="model-select-card"
                    :class="{ active: selectedModelId === model.id }"
                    @click="selectModel(model)"
                  >
                    <div class="model-select-thumb">
                      <img v-if="model.previewUrl" :src="model.previewUrl" :alt="model.name" />
                      <el-icon v-else :size="24"><User /></el-icon>
                    </div>
                    <div class="model-select-info">
                      <div class="model-select-name">{{ model.name }}</div>
                      <div class="model-select-meta">
                        <span v-if="model.gender">{{ model.gender === 'male' ? '男' : '女' }}</span>
                        <span v-if="model.ageGroup">· {{ model.ageGroup === 'youth' ? '青年' : (model.ageGroup === 'middle' ? '中年' : '老年') }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <p class="section-helper" v-if="modelList.length > 0">点击模特可自动填入配置参数，再次点击取消选择</p>
              </div> -->
            </div>

            <!-- 1. Gender -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('gender')">
                <span class="section-label">性别选择</span>
                <span class="expand-text">
                  {{ sections.gender ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.gender }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.gender" class="section-body">
                <el-radio-group v-model="gender" class="gender-radios">
                  <el-radio label="男" border size="small">男</el-radio>
                  <el-radio label="女" border size="small">女</el-radio>
                  <el-radio label="不限" border size="small">不限</el-radio>
                </el-radio-group>
              </div>
            </div>

            <!-- 2. Age range -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('age')">
                <span class="section-label">年龄选择</span>
                <span class="expand-text">
                  {{ sections.age ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.age }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.age" class="section-body">
                <div class="tag-chips">
                  <div
                    v-for="a in ageRanges"
                    :key="a"
                    class="tag-chip"
                    :class="{ active: age === a }"
                    @click="age = a"
                  >{{ a }}</div>
                </div>
              </div>
            </div>

            <!-- 3. Hairstyle -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('hairstyle')">
                <span class="section-label">发型选择</span>
                <span class="expand-text">
                  {{ sections.hairstyle ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.hairstyle }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.hairstyle" class="section-body">
                <div class="tag-chips">
                  <div
                    v-for="h in hairstyles"
                    :key="h"
                    class="tag-chip"
                    :class="{ active: hairstyle === h }"
                    @click="hairstyle = h"
                  >{{ h }}</div>
                </div>
              </div>
            </div>

            <!-- 4. Ethnicity -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('ethnicity')">
                <span class="section-label">人种选择</span>
                <span class="expand-text">
                  {{ sections.ethnicity ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.ethnicity }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.ethnicity" class="section-body">
                <div class="ethnicity-grid">
                  <div
                    v-for="e in ethnicities"
                    :key="e.value"
                    class="ethnicity-card"
                    :class="{ active: ethnicity === e.value }"
                    @click="ethnicity = e.value"
                  >
                    <div class="ethnicity-swatch" :style="{ background: e.color }"></div>
                    <span class="ethnicity-name">{{ e.label }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 5. Pose -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('pose')">
                <span class="section-label">姿势选择</span>
                <span class="expand-text">
                  {{ sections.pose ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.pose }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.pose" class="section-body">
                <div class="tag-chips">
                  <div
                    v-for="p in poses"
                    :key="p"
                    class="tag-chip"
                    :class="{ active: pose === p }"
                    @click="pose = p"
                  >{{ p }}</div>
                </div>
              </div>
            </div>

            <!-- 6. Clothing -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('clothing')">
                <span class="section-label">服装选择</span>
                <span class="expand-text">
                  {{ sections.clothing ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.clothing }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.clothing" class="section-body">
                <div class="tag-chips">
                  <div
                    v-for="c in clothingOptions"
                    :key="c"
                    class="tag-chip"
                    :class="{ active: clothing === c }"
                    @click="clothing = c"
                  >{{ c }}</div>
                </div>
              </div>
            </div>

            <!-- 7. Scene style -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('scene')">
                <span class="section-label">场景风格 <span class="section-optional">(可选)</span></span>
                <span class="expand-text">
                  {{ sections.scene ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.scene }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.scene" class="section-body">
                <div class="scene-grid">
                  <div
                    v-for="s in sceneOptions"
                    :key="s.value"
                    class="scene-card"
                    :class="{ active: sceneStyle === s.value }"
                    @click="sceneStyle = s.value"
                  >
                    <div class="scene-card-thumb" :style="{ background: s.gradient }">
                      <div class="scene-thumb-icon" v-html="s.svgIcon"></div>
                    </div>
                    <span class="scene-card-name">{{ s.label }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 8. Output settings -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('output')">
                <span class="section-label">输出尺寸</span>
                <span class="expand-text">
                  {{ sections.output ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.output }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.output" class="section-body">
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

            <!-- 9. 生成数量 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('genCount')">
                <span class="section-label">生成数量</span>
                <span class="expand-text">
                  {{ sections.genCount ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.genCount }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.genCount" class="section-body">
                <div class="gen-count-row">
                  <el-input-number v-model="generateCount" :min="1" :max="maxGenerateCount" size="small" controls-position="right" style="width: 120px" />
                </div>
              </div>
            </div>

            <!-- 提示词增强 -->
            <!-- <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('promptBoost')">
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
            </div> -->

          </div>
        </el-scrollbar>
      </div>

      <!-- Divider handle: config ⇔ AI -->
      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== RIGHT: AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        
        <AiAssistant
          ref="aiAssistantRef"
          :generate-fn="handleGenerate"
          :is-generating="isGenerating"
          :gen-status="genStatus"
          :gen-progress="genProgress"
          :gen-error="genError"
          :has-image="!!productImage"
          :on-clear-images="clearWorkspaceImages"
        />
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFile" />

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
  </div>
</template>

<script setup>
defineOptions({ name: 'AiModelView' })
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  UploadFilled, Star, Download, MagicStick, Right, Delete,
  RefreshLeft, RefreshRight, ArrowDown, ArrowLeft, ArrowRight,
  ChatDotRound, Monitor, User,
  PictureFilled, House, Sunny, OfficeBuilding, DocumentCopy
} from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { getPublicPublishedAiModels, reversePrompt, getPublicCreationConfigByGroup, listPromptLibraryBatch } from '@/api/customer'
import { ElMessage } from 'element-plus'

// const { canvasUI, handleCanvasExport } = useCanvasInteractions({
//   canvasSelector: '.canvas-box',
//   getImage: () => productImage.value || '',
//   defaultName: 'ai-model',
// })

const configCollapsed = ref(false)

const fileInput = ref(null)
const productImage = ref('')
const originalFile = ref(null)
const resultImages = ref([])
const generating = ref(false)

const zoom = ref(100)
function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
function zoomOut() { zoom.value = Math.max(30, zoom.value - 10) }

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
    ElMessage.error('复制失败')
  }
}

const gen = useImageGeneration('render')
const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()

// ---- 生成状态（供 AiAssistant 组件使用） ----
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)
const genError = computed(() => gen.error.value)

const gender = ref('女')
const age = ref('青年')
const hairstyle = ref('长发')
const ethnicity = ref('asian')
const pose = ref('站立')
const clothing = ref('商务')
const sceneStyle = ref('pure-bg')
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

// ===== 配置选项（从后台加载） =====
const genderOptions = ref([
  { label: '男', value: '男' },
  { label: '女', value: '女' },
  { label: '不限', value: '不限' },
])
const ageRanges = ref(['青年(18-25)', '轻熟(26-35)', '成熟(36-45)', '中年(46+)'])
const hairstyles = ref(['短发', '长发', '卷发', '直发', '马尾', '丸子头'])
const ethnicities = ref([
  { label: '亚洲', value: 'asian', color: '#F5DEB3' },
  { label: '欧美', value: 'western', color: '#FFDAB9' },
  { label: '非洲', value: 'african', color: '#8B6914' },
  { label: '中东', value: 'middleeast', color: '#D2B48C' },
  { label: '拉丁', value: 'latin', color: '#DEB887' }
])
const poses = ref(['站立', '坐姿', '行走', '半身', '全身', '回眸', '侧面', '正面'])
const clothingOptions = ref(['商务', '休闲', '运动', '正装', '街拍'])
const sceneOptions = ref([
  { label: '室内白底', value: 'indoor-white', gradient: 'linear-gradient(135deg, #ffffff, #e9ecef)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M3 12h18" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 2"/></svg>' },
  { label: '室内场景', value: 'indoor', gradient: 'linear-gradient(135deg, #e0e7ff, #c7d2fe)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M4 20V8l8-5 8 5v12" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><rect x="9" y="12" width="6" height="8" stroke="currentColor" stroke-width="1.5"/></svg>' },
  { label: '户外自然', value: 'outdoor-nature', gradient: 'linear-gradient(135deg, #d1fae5, #a7f3d0)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M12 3l5 8H7l5-8z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M12 11v9" stroke="currentColor" stroke-width="1.5"/><path d="M9 20h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { label: '街头', value: 'street', gradient: 'linear-gradient(135deg, #fef3c7, #fde68a)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M5 21V7l5-3v17" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M10 21V11l6 3v7" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M3 21h18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { label: '商场', value: 'mall', gradient: 'linear-gradient(135deg, #fce7f3, #fbcfe8)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="4" y="8" width="16" height="13" rx="1" stroke="currentColor" stroke-width="1.5"/><path d="M9 8V5a3 3 0 016 0v3" stroke="currentColor" stroke-width="1.5"/><path d="M4 13h16" stroke="currentColor" stroke-width="1.5"/></svg>' },
  { label: '纯色背景', value: 'pure-bg', gradient: 'linear-gradient(135deg, #f1f5f9, #e2e8f0)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.5"/></svg>' }
])
const sizeOptions = ref([
  { label: '不指定尺寸', value: '' },
  { label: '1:1（800×800）', value: '800:800' },
  { label: '3:4（800×1067）', value: '800:1067' },
  { label: '4:3（1067×800）', value: '1067:800' },
  { label: '自定义', value: 'custom' }
])
const maxGenerateCount = ref(5)
const generateCount = ref(1)
const promptMap = ref({})

// ===== 提示词增强（从 gh_prompt_library 拉取） =====
const boostProduct = ref('')
const boostMaterial = ref('')
const boostProductRef = ref(null)
const boostMaterialRef = ref(null)

const sections = reactive({
  upload: true,
  modelSelect: true,
  gender: false,
  age: false,
  hairstyle: false,
  ethnicity: false,
  pose: false,
  clothing: false,
  scene: false,
  output: false,
  genCount: false,
  promptBoost: false
})

// ===== 模特列表（从后端加载已发布且已授权的模特） =====
const modelList = ref([])
const modelLoading = ref(false)
const selectedModelId = ref(null)

async function loadModelList() {
  modelLoading.value = true
  try {
    const res = await getPublicPublishedAiModels()
    modelList.value = res.data || []
  } catch {
    modelList.value = []
  } finally {
    modelLoading.value = false
  }
}

function selectModel(model) {
  if (selectedModelId.value === model.id) {
    selectedModelId.value = null
    return
  }
  selectedModelId.value = model.id
  if (model.gender) gender.value = model.gender === 'male' ? '男' : (model.gender === 'female' ? '女' : '不限')
  if (model.ageGroup) {
    const ageMap = { youth: '青年(18-25)', middle: '轻熟(26-35)', elder: '中年(46+)' }
    if (ageMap[model.ageGroup]) age.value = ageMap[model.ageGroup]
  }
  if (model.hairStyle) hairstyle.value = model.hairStyle
  if (model.ethnicity) ethnicity.value = model.ethnicity
  if (model.pose) pose.value = model.pose
  if (model.clothing) clothing.value = model.clothing
  if (model.scene) sceneStyle.value = model.scene
}

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

onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  loadModelList()
  loadCreationConfig()
  nextTick(() => {
    const rightCol = document.querySelector('.right-col')
    if (rightCol) {
      const w = rightCol.getBoundingClientRect().width
      _configWidthPx.value = Math.round(w * 0.35)
      _aiWidthPx.value = Math.round(w * 0.55)
    }
  })
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)})

// ---- Methods ----
function toggleSection(key) {
  if (sections.hasOwnProperty(key)) {
    sections[key] = !sections[key]
  }
}

function toggleAllSections() {
  const val = !allExpanded.value
  Object.keys(sections).forEach(k => sections[k] = val)
}

function triggerUpload() { fileInput.value?.click() }

function handleDrop(e) {
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) {
    readFile(file)
  }
}

function handleFile(e) {
  const file = e.target.files[0]
  if (file) readFile(file)
}

function readFile(file) {
  originalFile.value = file
  const reader = new FileReader()
  reader.onload = ev => { productImage.value = ev.target.result }
  reader.readAsDataURL(file)
}

async function handleGenerate() {
  const text = aiAssistantRef.value?.inputText?.trim() || ''
  if (!originalFile.value) { ElMessage.warning('请先上传产品图片'); return }
  if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
  try {
    const _basePrompt = `生成AI模特图，性别${gender.value}，年龄${age.value}，发型${hairstyle.value}，姿势${pose.value}，服装${clothing.value}，场景${sceneStyle.value}`
    const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
    const sizeText = effectiveOutputSize.value ? `输出图片尺寸为 ${effectiveOutputSize.value}，` : ''
    const prompt = boostText ? `${_basePrompt}；${sizeText}${text ? text + '。' : ''}约束：${boostText}。` : `${_basePrompt}${sizeText ? '。' + sizeText : ''}${text ? '。' + text : ''}`
    const extraOptions = {}
    if (effectiveOutputSize.value) extraOptions.output_size = effectiveOutputSize.value
    if (generateCount.value) extraOptions.n = Number(generateCount.value)
    await gen.fullGenerate([originalFile.value], prompt, { ...extraOptions, consumePoints: 2, featureName: 'ai_model', title: 'AI模特生成' })
    if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
  } catch (e) {
    console.error('AI模特生成失败:', e)
    const isTimeout = e?.code === 'ECONNABORTED'
      || /timeout|超时|人数过多|繁忙|busy/i.test(e?.message || '')
    ElMessage.error(isTimeout
      ? '当前模型使用人数过多，可选用其他模型生图或稍后再试'
      : '生成失败，请稍后重试')
  }
}

function clearCanvas() {
  productImage.value = ''
  resultImages.value = []
}

// ===== 从后台创作配置读取AI模特配置 =====
async function loadCreationConfig() {
  try {
    const res = await getPublicCreationConfigByGroup('ai_model')
    const list = res.data || res.rows || []
    const map = {}
    list.forEach(c => { map[c.configKey] = c })

    // ---- 性别选项 ----
    const genderCfg = map.gender_options
    if (genderCfg && genderCfg.configValue) {
      const arr = JSON.parse(genderCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        genderOptions.value = arr.map(s => ({ label: s.label || s.value, value: s.label || s.value }))
      }
    }

    // ---- 年龄选项 ----
    const ageCfg = map.age_ranges
    if (ageCfg && ageCfg.configValue) {
      const arr = JSON.parse(ageCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        ageRanges.value = arr.map(s => typeof s === 'string' ? s : (s.label || s.value))
      }
    }

    // ---- 发型选项 ----
    const hairCfg = map.hairstyles
    if (hairCfg && hairCfg.configValue) {
      const arr = JSON.parse(hairCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        hairstyles.value = arr.map(s => typeof s === 'string' ? s : (s.label || s.value))
      }
    }

    // ---- 人种选项 ----
    const ethnicityCfg = map.ethnicities
    if (ethnicityCfg && ethnicityCfg.configValue) {
      const arr = JSON.parse(ethnicityCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        const colorMap = { asian: '#F5DEB3', caucasian: '#FFDAB9', western: '#FFDAB9', african: '#8B6914', latino: '#DEB887', middle_east: '#D2B48C', middleeast: '#D2B48C' }
        ethnicities.value = arr.map(s => ({
          label: s.label || s.value,
          value: s.value,
          color: s.color || colorMap[s.value] || '#F5DEB3'
        }))
      }
    }

    // ---- 姿势选项 ----
    const poseCfg = map.poses
    if (poseCfg && poseCfg.configValue) {
      const arr = JSON.parse(poseCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        poses.value = arr.map(s => typeof s === 'string' ? s : (s.label || s.value))
      }
    }

    // ---- 服装选项 ----
    const clothingCfg = map.clothing_options
    if (clothingCfg && clothingCfg.configValue) {
      const arr = JSON.parse(clothingCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        clothingOptions.value = arr.map(s => typeof s === 'string' ? s : (s.label || s.value))
      }
    }

    // ---- 场景选项 ----
    const sceneCfg = map.scene_options
    if (sceneCfg && sceneCfg.configValue) {
      const arr = JSON.parse(sceneCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        // 保留原有 gradient/svgIcon，只更新 label/value
        const existingMap = {}
        sceneOptions.value.forEach(s => { existingMap[s.value] = s })
        sceneOptions.value = arr.map(s => {
          const val = s.value || s.label
          return existingMap[val] || {
            label: s.label || val,
            value: val,
            gradient: 'linear-gradient(135deg, #f1f5f9, #e2e8f0)',
            svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/></svg>'
          }
        })
      }
    }

    // ---- 输出尺寸 ----
    const sizeCfg = map.output_sizes || map.size_options || map.size_presets
    if (sizeCfg && sizeCfg.configValue) {
      const arr = JSON.parse(sizeCfg.configValue)
      if (Array.isArray(arr) && arr.length) {
        const loaded = arr.map(s => (typeof s === 'string' ? { label: s, value: s } : s))
        // Ensure 'custom' option is always present
        const hasCustom = loaded.some(s => s.value === 'custom')
        if (!hasCustom) {
          loaded.push({ label: '自定义', value: 'custom' })
        }
        sizeOptions.value = [{ label: '不指定尺寸', value: '' }, ...loaded]
      }
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

// 加载提示词库映射
async function loadPromptMap() {
  try {
    const res = await listPromptLibraryBatch('opt_age,opt_hairstyle,opt_ethnicity,opt_pose,opt_clothing,opt_scene', 'ai_model')
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

function clearWorkspaceImages() {
  productImage.value = ''
  originalFile.value = null
  resultImages.value = []
  gender.value = '女'
  hairstyle.value = '长发'
  ethnicity.value = 'asian'
  pose.value = '站立'
  clothing.value = '商务'
  outputSize.value = ''
  customWidth.value = 1000
  customHeight.value = 1000
  generateCount.value = 1
  gen.reset()
}
</script>

<style lang="scss" scoped>
// ============================================================
//   Layout
// ============================================================
.workspace-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

// ========== Promotional Banner ==========
.promo-banner {
  flex-shrink: 0;
  background: linear-gradient(135deg, #2563FF 0%, #4F83FF 60%, #6B9FFF 100%);
  padding: 10px 24px;
}

.promo-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.promo-text {
  display: flex;
  align-items: center;
  gap: 16px;
}

.promo-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.promo-subtitle {
  font-size: 13px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.85);
}

.promo-btn {
  height: 32px;
  padding: 0 20px;
  border-radius: var(--gh-radius-btn, 10px);
  background: #fff !important;
  color: var(--gh-primary, #2563FF) !important;
  border: none !important;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover { opacity: 0.9; }
}

// ========== Steps Bar ==========
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

// ---- Three Column ----
.three-col {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

// ---- Column Divider ----
.col-divider-wrapper {
  width: 30px;
  flex-shrink: 0;
  background: transparent;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-divider {
  width: 6px;
  height: 100%;
  background: transparent;
  cursor: col-resize;
  position: absolute;
  left: 12px;
  top: 0;
  bottom: 0;
  z-index: 5;
  transition: background 0.2s;
}
.col-divider:hover,
.col-divider:active { background: #2563FF; }

.config-toggle-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  right: 0;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #EBEDF5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
  color: #6B6B6B;
}
.config-toggle-btn:hover {
  background: #F7F9FC;
  color: #2563FF;
}

// ============================================================
//   Canvas Column
// ============================================================
.canvas-col {
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
  background: #F7F9FC;
  min-width: 0;
}

// ========== Toolbar Row ==========
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 10px;
  flex-shrink: 0;
}

.toolbar-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #EBEDF5;
  border-radius: 10px;
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
  font-size: 12px;
  font-weight: 500;
  color: #1F1F1F;
  border-radius: 6px;
  transition: background 0.2s;

  &:hover { background: #F3F4F6; }
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
  color: #6B6B6B;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover { background: #F3F4F6; color: #1F1F1F; }
}

.tb-divider {
  width: 1px;
  height: 20px;
  background: #EBEDF5;
  flex-shrink: 0;
}

// ========== Canvas Box ==========
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
  position: relative;
}

.canvas-zoom-controls {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 4px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}
.canvas-zoom-controls .zoom-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #E8EDF5;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: #4B5563;
}
.canvas-zoom-controls .zoom-btn:hover {
  border-color: #2563FF;
  color: #2563FF;
}
.canvas-zoom-controls .zoom-value {
  font-size: 12px;
  color: #1F2937;
  min-width: 40px;
  text-align: center;
}

.upload-zone {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #EBEDF5;
  border-radius: 16px;
  cursor: pointer;
  transition: border-color 0.25s, background 0.25s;
  background: #fff;

  &:hover {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.02);
  }
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

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 4px;
    opacity: .4;
  }

  h3 {
    font-size: 14px;
    color: #6B7280;
    margin-bottom: 0;
    font-weight: 500;
  }

  p {
    font-size: 12px;
    color: #9CA3AF;
  }
}

// ========== Result Grid ==========
.result-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  align-content: start;
  overflow: auto;
  position: relative;
  padding: 4px;

  &.generating { opacity: 0.6; pointer-events: none; }
}

.result-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #EBEDF5;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.upload-placeholder {
  text-align: center;
}

.upload-main-text {
  font-size: 14px;
  font-weight: 400;
  color: #6B6B6B;
  margin: 16px 0 4px;
}

.upload-sub-text {
  font-size: 12px;
  font-weight: 400;
  color: #9CA3AF;
  margin: 0;
}

.upload-preview {
  position: relative;
  max-width: 80%;
  max-height: 80%;

  .preview-img {
    max-width: 100%;
    max-height: 400px;
    object-fit: contain;
    border-radius: 8px;
  }

  .preview-overlay {
    position: absolute;
    top: 8px;
    right: 8px;
  }
}

// ========== Result Area ==========
.result-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  overflow: auto;
}

.model-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  align-content: start;
}

.model-card {
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #EBEDF5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06); }
}

.model-thumb {
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;

  span {
    font-size: 14px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.35);
  }
}

.model-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding: 8px 12px;
}

.result-bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;

  .el-button {
    height: 40px;
    border-radius: 10px;
    font-weight: 500;
  }
}

.canvas-bottom-bar {
  padding: 8px 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
}

// ============================================================
//   Config Column
// ============================================================
.config-col {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  min-width: 0;
  transition: flex 0.3s ease;

  &.collapsed {
    flex: 0 0 0 !important;
    min-width: 0 !important;
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
  }
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
  padding: 0;
}

.panel-header {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 14px 16px 6px;
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

// ---- Sections ----
.config-section {
  margin-bottom: 0;
}

.config-section.collapsible {
  border-bottom: 1px solid #F3F4F6;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0;
}

.section-header.collapsible {
  cursor: pointer;
  user-select: none;
  padding: 10px 16px;
  transition: opacity 0.2s;
  margin-bottom: 0;

  &:hover { opacity: 0.75; }
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
  display: flex;
  align-items: center;
  gap: 6px;
}
.required-mark { color: #EF4444; margin-right: 2px; font-weight: 500; }

.section-optional {
  font-size: 11px;
  font-weight: 400;
  color: #9CA3AF;
}

.expand-text {
  font-size: 12px;
  color: #9CA3AF;
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

.section-body {
  padding: 0 16px 10px;
}

// -- Upload --
.upload-btn-full {
  width: 100%;
  border-radius: 10px !important;
}

.upload-hint-text {
  font-size: 12px;
  font-weight: 400;
  color: #9CA3AF;
  margin: 8px 0 0;
}

// -- Gender radios --
.gender-radios {
  display: flex;
  gap: 8px;

  :deep(.el-radio) {
    margin-right: 0;
    flex: 1;
    justify-content: center;
  }
}

// -- Tag chips --
.tag-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 400;
  color: #6B6B6B;
  background: #F7F9FC;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1.5px solid transparent;

  &:hover {
    color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.08);
  }

  &.active {
    color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.06);
    border-color: var(--gh-primary, #2563FF);
    font-weight: 500;
  }
}

// -- Ethnicity grid --
.ethnicity-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.ethnicity-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 4px;
  border: 1.5px solid #EBEDF5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  .ethnicity-swatch {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.08);
  }

  .ethnicity-name {
    font-size: 11px;
    font-weight: 500;
    color: #6B6B6B;
  }

  &:hover { border-color: #93C5FD; }

  &.active {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.08);
    .ethnicity-name { color: var(--gh-primary, #2563FF); font-weight: 600; }
  }
}

// -- Scene grid --
.scene-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.scene-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border: 1.5px solid #EBEDF5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;

  .scene-card-thumb {
    width: 100%;
    height: 50px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    transition: all 0.2s;
  }

  .scene-thumb-icon {
    color: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .scene-card-name {
    font-size: 12px;
    font-weight: 500;
    color: #1F1F1F;
    text-align: center;
  }

  &:hover {
    border-color: #93C5FD;
    .scene-card-thumb { box-shadow: 0 2px 8px rgba(37, 99, 255, 0.12); }
  }

  &.active {
    border-color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.03);
    .scene-card-name { color: var(--gh-primary, #2563FF); font-weight: 600; }
  }
}

// -- Output row --
.output-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
  color: #1F1F1F;

  &:last-child { margin-bottom: 0; }
}

.output-label {
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
}

.count-group {
  display: flex;
  gap: 8px;

  &.compact { gap: 4px; }
}

.count-btn {
  flex: 1;
  height: 32px;
  min-width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #EBEDF5;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #6B6B6B;
  cursor: pointer;
  transition: all 0.2s;

  &:hover { border-color: #93C5FD; color: var(--gh-primary, #2563FF); }

  &.active {
    border-color: var(--gh-primary, #2563FF);
    color: var(--gh-primary, #2563FF);
    background: rgba(37, 99, 255, 0.05);
    font-weight: 600;
  }
}

.custom-btn {
  padding: 0 10px;
  font-size: 12px;
  min-width: 48px;
  flex: 0 0 auto;
}

.custom-count-row {
margin-top: 8px;
}

/* ---- 自定义尺寸（与白底图一致） ---- */
.custom-size-row {
display: flex;
align-items: center;
gap: 8px;
margin-top: 10px;
padding-top: 10px;
border-top: 1px dashed #E8EDF5;
}
.custom-size-input {
flex: 1;
display: flex;
flex-direction: column;
gap: 4px;
}
.custom-size-input span {
font-size: 11px;
color: #6B7280;
}
.custom-size-x {
color: #9CA3AF;
font-size: 13px;
padding-top: 14px;
}

/* ---- 生成数量（与主图设计一致） ---- */
.gen-count-row {
display: flex;
align-items: center;
gap: 8px;
}

// -- Generate button --
.generate-btn {
  width: calc(100% - 32px);
  margin: 12px 16px 16px;
  height: 40px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
}

.btn-points {
  margin-left: 8px;
  font-size: 12px;
  opacity: 0.85;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
}

// ============================================================
//   AI Column
// ============================================================
.ai-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  padding: 16px;
  overflow: hidden;
  min-width: 240px;
}

// ===== Model select =====
.model-select-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 40px;
}
.model-select-empty {
  text-align: center;
  color: #9CA3AF;
  font-size: 12px;
  padding: 16px 0;
}
.model-select-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  &:hover {
    border-color: #2563FF;
    background: #f0f5ff;
  }
  &.active {
    border-color: #2563FF;
    background: #e0e7ff;
    box-shadow: 0 0 0 1px #2563FF;
  }
}
.model-select-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
.model-select-info {
  flex: 1;
  min-width: 0;
}
.model-select-name {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.model-select-meta {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

// ============================================================
//   Responsive
// ============================================================
@media (max-width: 1024px) {
  .promo-banner { padding: 10px 16px; }
  .steps-bar { padding: 0 0 8px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 8px; margin: 0 4px; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .config-col { flex: 0 0 50% !important; }
  .ai-col { flex: 0 0 50% !important; }
  .col-divider-wrapper { display: none; }
  .col-divider { display: none; }
}

@media (max-width: 768px) {
  .promo-banner { display: none; }
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .config-col { flex: 0 0 auto !important; max-height: 200px; overflow-y: auto; }
  .ai-col { flex: 1 1 auto !important; min-height: 250px; }
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