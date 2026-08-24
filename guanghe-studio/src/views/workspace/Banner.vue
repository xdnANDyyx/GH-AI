<template>
  <div class="workspace-page">

    <!-- Three-column -->
    <div class="three-col">
      <!-- ===== Canvas ===== -->
        <div class="canvas-col" :style="{ flex: canvasFlex }">
        <!-- Steps bar -->
        <div class="steps-bar">
          <template v-for="(s, i) in workflowSteps" :key="i">
            <div class="step-item" :class="getStepClass(i + 1, 5)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
            <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
          </template>
        </div>

        <div class="canvas-box">
          <!-- 未生成：显示空状态 -->
          <div v-if="resultImages.length === 0" class="canvas-placeholder">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="6" y="10" width="36" height="28" rx="3" stroke="#9CA3AF" stroke-width="1.5"/>
              <circle cx="18" cy="22" r="4" stroke="#9CA3AF" stroke-width="1.5"/>
              <path d="M6 32l9-9 6 6 9-12 12 15" stroke="#9CA3AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <h3>Banner 生成后将显示在此处</h3>
            <p>请在右侧上传素材并配置参数</p>
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>
          <!-- 有结果图时：展示结果 -->
          <div v-else class="result-grid" :class="{ generating: isGenerating }">
            <div v-for="(img, idx) in resultImages" :key="'r'+idx" class="result-card">
              <img :src="img.url || img" class="result-img" />
            </div>
            <div v-if="isGenerating" class="generating-overlay">
              <div class="progress-ring">{{ genProgress }}%</div>
              <p>{{ genStatus }}</p>
            </div>
          </div>
        </div>

        <div class="canvas-tip">建议使用高质量素材，获得更佳效果</div>

        <!-- Templates -->
        <!-- <div class="templates-section">
          <div class="templates-header"><span class="templates-title">热门模板</span></div>
          <div class="templates-tabs">
            <span v-for="tab in templateTabs" :key="tab" class="templates-tab" :class="{ active: activeTemplateTab === tab }" @click="activeTemplateTab = tab">{{ tab }}</span>
          </div>
          <div class="templates-grid" ref="templatesGrid">
            <div v-for="tpl in filteredTemplates" :key="tpl.name" class="template-card" :class="{ selected: selectedTemplate?.name === tpl.name }" @click="selectTemplate(tpl)">
              <div class="template-card-preview" :style="{ background: tpl.bg }">
                <div class="tpl-main-text">{{ tpl.title }}</div>
                <div class="tpl-sub-text">{{ tpl.subtitle }}</div>
              </div>
              <div class="template-card-info">
                <div class="template-card-name">{{ tpl.name }}</div>
                <div class="template-card-tag">{{ tpl.tag }}</div>
              </div>
            </div>
          </div>
          <div class="templates-nav">
            <span class="templates-hint">建议尺寸：1200×300px、1920×600px、1920×1080px 等主流尺寸</span>
            <span class="templates-arrow" @click="scrollTemplates('prev')">‹</span>
            <span class="templates-arrow" @click="scrollTemplates('next')">›</span>
          </div>
        </div> -->
        <div class="templates-suggest">建议尺寸：1200×300px、1920×600px、1920×1080px 等主流尺寸<span @click="refreshTemplates">🔄 换一换</span></div>
      </div>

      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowRight v-if="!configCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
      </div>

      <!-- ===== Config ===== -->
      <div class="config-col" :class="{ collapsed: configCollapsed }" :style="{ flex: configFlex }">
        <el-scrollbar>
          <div class="config-inner">
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

            <!-- 画布尺寸 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('canvasSize')">
                <span class="section-label">画布尺寸</span>
                <span class="expand-text">{{ sections.canvasSize ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.canvasSize }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.canvasSize">
                <el-select v-model="canvasPreset" style="width: 100%" @change="onCanvasPresetChange">
                  <el-option v-for="s in sizePresets" :key="s.value" :label="s.label" :value="s.value" />
                </el-select>
                <!-- 自定义尺寸 -->
                <div v-if="canvasPreset === 'custom'" class="custom-size-row">
                  <div class="custom-size-input">
                    <span>宽</span>
                    <el-input-number v-model="canvasWidth" :min="64" :max="4096" :step="100" size="small" controls-position="right" />
                  </div>
                  <span class="custom-size-x">×</span>
                  <div class="custom-size-input">
                    <span>高</span>
                    <el-input-number v-model="canvasHeight" :min="64" :max="4096" :step="100" size="small" controls-position="right" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 上传素材 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label">上传素材</span>
                <span class="expand-text">{{ sections.upload ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.upload }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.upload">
                <div class="upload-row">
                  <div class="upload-card" @click.stop="triggerUpload"><div class="upload-card-label">产品图（可选）</div><div class="upload-card-icon"><el-icon :size="20" color="#9CA3AF"><UploadFilled /></el-icon></div><div class="upload-card-action">↑ 上传产品图</div><div class="upload-card-hint">支持 JPG/PNG，最多10张</div></div>
                  <div class="upload-card" @click.stop="triggerBgUpload"><div class="upload-card-label">背景图（可选）</div><div class="upload-card-icon"><el-icon :size="20" color="#9CA3AF"><UploadFilled /></el-icon></div><div class="upload-card-action">↑ 上传背景图</div><div class="upload-card-hint">支持 JPG/PNG，最多5张</div></div>
                  <div class="upload-card" @click.stop="triggerLogoUpload"><div class="upload-card-label">LOGO（可选）</div><div class="upload-card-icon"><el-icon :size="20" color="#9CA3AF"><UploadFilled /></el-icon></div><div class="upload-card-action">↑ 上传LOGO</div><div class="upload-card-hint">支持PNG，透明背景更佳</div></div>
                </div>
                <div class="upload-note">建议使用高清素材，支持拖拽上传</div>
                <div v-if="uploadedFiles.length" class="uploaded-images-list">
                  <div v-for="(f, i) in uploadedFiles" :key="i" class="uploaded-thumb-wrap">
                    <div class="uploaded-thumb"><img :src="f" v-if="typeof f === 'string'" /><span v-else class="thumb-placeholder">图片</span></div>
                    <div class="uploaded-remove" @click.stop="removeProductFile(i)">✕</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Banner类型 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('bannerType')">
                <span class="section-label">Banner类型</span>
                <span class="expand-text">{{ sections.bannerType ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.bannerType }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.bannerType">
                <div class="type-grid">
                  <div v-for="t in bannerTypes" :key="t.key" class="type-btn" :class="{ active: activeBannerType === t.key }" @click="activeBannerType = t.key"><div class="type-btn-name">{{ t.name }}</div><div class="type-btn-desc">{{ t.desc }}</div></div>
                </div>
              </div>
            </div>

            <!-- 核心目的 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('purpose')">
                <span class="section-label">核心目的<span class="optional-label">（可多选）</span></span>
                <span class="expand-text">{{ sections.purpose ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.purpose }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.purpose">
                <div class="checkbox-grid">
                  <div v-for="p in purposes" :key="p.key" class="checkbox-item" @click="togglePurpose(p.key)">
                    <div class="checkbox-box" :class="{ checked: selectedPurposes.includes(p.key) }">
                      <svg v-if="selectedPurposes.includes(p.key)" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                    </div>{{ p.label }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 关键信息 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('keyInfo')">
                <span class="section-label">关键信息</span>
                <span class="expand-text">{{ sections.keyInfo ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.keyInfo }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.keyInfo">
                <div class="input-group">
                  <div class="input-label">主标题<span class="char-count">{{ mainTitle.length }}/30</span></div>
                  <input class="input-field" v-model="mainTitle" placeholder="输入主标题，如：SUMMER SALE" maxlength="30">
                </div>
                <div class="input-group">
                  <div class="input-label">副标题<span class="char-count">{{ subTitle.length }}/50</span></div>
                  <input class="input-field" v-model="subTitle" placeholder="输入副标题，如：UP TO 50% OFF" maxlength="50">
                </div>
                <div class="input-group">
                  <div class="input-label">按钮文案<span class="char-count">{{ btnText.length }}/20</span></div>
                  <input class="input-field" v-model="btnText" placeholder="输入按钮文案，如：SHOP NOW" maxlength="20">
                </div>
              </div>
            </div>

            <!-- 生成数量 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('count')">
                <span class="section-label">生成数量</span>
                <span class="expand-text">{{ sections.count ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.count }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.count">
                <div class="gen-count-row">
                  <span class="config-label">生成数量</span>
                  <el-input-number v-model="generateCount" :min="1" :max="maxGenerateCount" size="small" controls-position="right" style="width: 120px" />
                </div>
              </div>
            </div>

            <!-- 语言 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('language')">
                <span class="section-label">语言</span>
                <span class="expand-text">{{ sections.language ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.language }"><ArrowDown /></el-icon></span>
              </div>
              <div class="section-body" v-show="sections.language">
                <el-select v-model="language" style="width: 100%">
                  <el-option v-for="l in languages" :key="l.value" :label="l.label" :value="l.value" />
                </el-select>
                <p class="section-helper">选择输出图片上文字的语言，适配跨境电商场景</p>
              </div>
            </div>

          </div>
        </el-scrollbar>
      </div>

      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        <div class="ai-resize-handle" @mousedown="startAiResize"></div>
        
        <AiAssistant
          ref="aiAssistantRef"
          :generate-fn="handleGenerateFromAi"
          :is-generating="isGenerating"
          :gen-status="genStatus"
          :gen-progress="genProgress"
          :gen-error="genError"
          :has-image="!!originalImage"
          :on-clear-images="clearWorkspaceImages"
        />
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />
    <input type="file" ref="bgFileInput" accept="image/*" multiple hidden @change="handleBgFileSelect" />
    <input type="file" ref="logoFileInput" accept="image/*" hidden @change="handleLogoFileSelect" />

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

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import { ArrowDown, ArrowLeft, ArrowRight, WarningFilled, FullScreen, RefreshLeft, Delete, UploadFilled, Link, MagicStick, DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import AiAssistant from '@/components/AiAssistant.vue'
import { reversePrompt, getPublicCreationConfigByGroup, listPromptLibraryBatch } from '@/api/customer'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'

export default {
  name: 'BannerView',
  components: { ArrowDown, ArrowLeft, ArrowRight, WarningFilled, FullScreen, RefreshLeft, Delete, UploadFilled, Link, MagicStick, DocumentCopy, PromptLibrarySelect, AiAssistant },
  setup() {
    const gen = useImageGeneration('render')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-dropzone',
    //   defaultName: 'banner',
    // })
    const fileInput = ref(null); const bgFileInput = ref(null); const logoFileInput = ref(null)
    const originalImage = ref(''); const originalFile = ref(null); const bgImage = ref(''); const bgFile = ref(null); const logoImage = ref(''); const logoFile = ref(null); const resultImages = ref([])
    const uploadedFiles = ref([]); const productFiles = ref([])
    const zoom = ref(100)

    // Canvas size
    const canvasPreset = ref('1200x300'); const canvasWidth = ref(1200); const canvasHeight = ref(300); const sizeLinked = ref(true)
    const sizePresets = ref([
      { label: '1200×300（横幅）', value: '1200x300', w: 1200, h: 300 },
      { label: '1920×600（通栏）', value: '1920x600', w: 1920, h: 600 },
      { label: '1920×1080（大屏）', value: '1920x1080', w: 1920, h: 1080 },
      { label: '自定义', value: 'custom', w: 0, h: 0 },
    ])

    // Sections
    const sections = reactive({ canvasSize: true, upload: true, bannerType: true, purpose: true, keyInfo: true, count: true, language: true, promptBoost: false })
    const allExpanded = computed(() => Object.values(sections).every(v => v))

    // Banner type
    const activeBannerType = ref('promo')
    const bannerTypes = ref([
      { key: 'promo', name: '促销活动', desc: '打折促销、限时优惠' },
      { key: 'new', name: '新品上市', desc: '新品发布、产品推荐' },
      { key: 'brand', name: '品牌宣传', desc: '品牌故事、品牌形象' },
      { key: 'season', name: '节日季节', desc: '节日活动、季节主题' },
      { key: 'notice', name: '信息通知', desc: '公告通知、店铺信息' },
      { key: 'decorate', name: '店铺装修', desc: '店铺头图、页面装饰' },
    ])

    // Purposes
    const selectedPurposes = ref(['sales'])
    const purposes = ref([
      { key: 'sales', label: '提升销量/促销转化' }, { key: 'newProduct', label: '新品推广' },
      { key: 'branding', label: '品牌宣传/提升认知' }, { key: 'traffic', label: '活动宣传/引流' },
      { key: 'clearance', label: '清仓/库存处理' }, { key: 'festival', label: '节日营销' },
      { key: 'shopImage', label: '店铺形象展示' }, { key: 'other', label: '其他' },
    ])

    // 提示词映射
    const promptMap = ref({})
    // 生成数量上限
    const maxGenerateCount = ref(10)

    // Key info
    const mainTitle = ref(''); const subTitle = ref(''); const btnText = ref('')
    const generateCount = ref(3); const customCount = ref(6)

    // Language
    const language = ref('zh-CN')
    const languages = ref([
      { label: '中文（简体）', value: 'zh-CN' },
      { label: '英语（美国）', value: 'en-US' },
      { label: '英语（英国）', value: 'en-GB' },
      { label: '日语', value: 'ja-JP' },
      { label: '韩语', value: 'ko-KR' },
      { label: '德语', value: 'de-DE' },
      { label: '法语', value: 'fr-FR' },
      { label: '西班牙语', value: 'es-ES' },
    ])

    // Prompt boost
    const boostProduct = ref(''); const boostMaterial = ref('')
    const boostProductRef = ref(null); const boostMaterialRef = ref(null)

    // Templates
    const activeTemplateTab = ref('全部'); const selectedTemplate = ref(null)
    const templateTabs = ['全部', '促销活动', '节日季节', '新品上市', '品牌宣传', '信息通知', '店铺装修', '其他']
    const templates = ref([
      { name: '夏季促销', tag: '促销活动', title: 'SUMMER SALE\nUP TO 50% OFF', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #2563FF, #1E40AF)' },
      { name: '大促活动', tag: '促销活动', title: 'MEGA SALE\nDISCOUNT UP TO 70% OFF', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #EF4444, #B91C1C)' },
      { name: '新品上市', tag: '新品上市', title: 'NEW ARRIVAL\nSHOP NOW', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #1F2937, #111827)' },
      { name: '季节上新', tag: '节日季节', title: 'AUTUMN COLLECTION\nSHOP NOW', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #F59E0B, #D97706)' },
      { name: '包邮活动', tag: '促销活动', title: 'FREE SHIPPING\nON ORDERS OVER $50', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #22C55E, #15803D)' },
      { name: '品牌宣传', tag: '品牌宣传', title: 'BRAND\nSTORY', subtitle: '', bg: 'linear-gradient(135deg, #8B5CF6, #6D28D9)' },
      { name: '限时抢购', tag: '促销活动', title: 'FLASH SALE\nLIMITED TIME', subtitle: 'img-placeholder', bg: 'linear-gradient(135deg, #F97316, #EA580C)' },
    ])
    const filteredTemplates = computed(() => activeTemplateTab.value === '全部' ? templates.value : templates.value.filter(t => t.tag === activeTemplateTab.value))

    // AI
const aiPanel = ref(null)
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

// ---- 生成状态（供 AiAssistant 组件使用） ----
const isGenerating = computed(() => gen.generating.value)
const genProgress = computed(() => gen.progress.value)
const genStatus = computed(() => gen.statusText.value)
const genError = computed(() => gen.error.value)

    const canGenerate = computed(() => !!originalImage.value && !gen.generating.value)

    // ========== Config Collapse ==========
    const configCollapsed = ref(false)

    // === Layout resize (参照 Retouch.vue) ===
    const canvasFlex = computed(() => '1 1 0%')
const _configWidthPx = ref(340)
const _aiWidthPx = ref(260)
const configFlex = computed(() => {
  if (configCollapsed.value) return '0 0 40px'
  return `0 0 ${_configWidthPx.value}px`
})
const aiFlex = computed(() => `0 0 ${_aiWidthPx.value}px`)
    let isResizing = false; let resizeTarget = ''

    function startColResize(e, target) { isResizing = true; resizeTarget = target; document.body.style.cursor = 'col-resize'; document.body.style.userSelect = 'none'; e.preventDefault() }
    function onMouseMove(e) {
      if (!isResizing) return
      const threeCol = document.querySelector('.three-col'); if (!threeCol) return
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
    function onMouseUp() { if (isResizing) { isResizing = false; resizeTarget = ''; document.body.style.cursor = ''; document.body.style.userSelect = '' } }

    let isAiResizing = false; let aiStartX = 0; let aiStartWidth = 0
    function startAiResize(e) { isAiResizing = true; aiStartX = e.clientX; const aiEl = aiPanel.value; aiStartWidth = aiEl ? aiEl.getBoundingClientRect().width : 260; document.body.style.cursor = 'col-resize'; document.body.style.userSelect = 'none'; e.preventDefault(); e.stopPropagation() }
    function onAiMouseMove(e) { if (!isAiResizing) return; const delta = aiStartX - e.clientX; let newWidth = aiStartWidth + delta; newWidth = Math.max(200, Math.min(500, newWidth)); _aiWidthPx.value = newWidth }
    function onAiMouseUp() { if (isAiResizing) { isAiResizing = false; document.body.style.cursor = ''; document.body.style.userSelect = '' } }

    onMounted(async () => {
      document.addEventListener('mousemove', onMouseMove); document.addEventListener('mouseup', onMouseUp)
      document.addEventListener('mousemove', onAiMouseMove); document.addEventListener('mouseup', onAiMouseUp)
      loadCreationConfig()
      await Promise.allSettled([gen.loadPromptInfo(), gen.loadPixelConfigs(), gen.loadDeductTypes()])
    })
    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onMouseMove); document.removeEventListener('mouseup', onMouseUp)
      document.removeEventListener('mousemove', onAiMouseMove); document.removeEventListener('mouseup', onAiMouseUp)
    })

    // ===== 从后台创作配置读取Banner设计配置 =====
    async function loadCreationConfig() {
      try {
        const res = await getPublicCreationConfigByGroup('banner')
        const list = res.data || res.rows || []
        const map = {}
        list.forEach(c => { map[c.configKey] = c })

        // ---- 画布尺寸预设 ----
        const sizeCfg = map.size_presets
        if (sizeCfg && sizeCfg.configValue) {
          const arr = JSON.parse(sizeCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            sizePresets.value = arr.map(s => ({ label: s.label || s.value, value: s.value, w: s.w || 0, h: s.h || 0 }))
          }
        }

        // ---- Banner 类型 ----
        const typeCfg = map.banner_types
        if (typeCfg && typeCfg.configValue) {
          const arr = JSON.parse(typeCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            bannerTypes.value = arr.map(s => ({ key: s.key || s.value, name: s.name || s.label, desc: s.desc || '' }))
          }
        }

        // ---- 核心目的 ----
        const purposeCfg = map.purposes
        if (purposeCfg && purposeCfg.configValue) {
          const arr = JSON.parse(purposeCfg.configValue)
          if (Array.isArray(arr) && arr.length) {
            purposes.value = arr.map(s => ({ key: s.key || s.value, label: s.label || s.value }))
          }
        }

        // ---- 生成数量上限 ----
        const maxCountCfg = map.max_generate_count
        if (maxCountCfg && maxCountCfg.configValue) {
          const n = Number(JSON.parse(maxCountCfg.configValue))
          if (n > 0) maxGenerateCount.value = n
        }

        // ---- 语言列表（从通用配置加载） ----
        try {
          const commonRes = await getPublicCreationConfigByGroup('common')
          const commonList = commonRes.data || commonRes.rows || []
          const commonMap = {}
          commonList.forEach(c => { commonMap[c.configKey] = c })
          const langCfg = commonMap.languages
          if (langCfg && langCfg.configValue) {
            const items = JSON.parse(langCfg.configValue)
            if (Array.isArray(items) && items.length) {
              languages.value = items.filter(l => l.value)
            }
          }
        } catch { /* use defaults */ }

        // 加载提示词映射
        await loadPromptMap()
      } catch { /* use defaults */ }
    }

    // 加载提示词库映射
    async function loadPromptMap() {
      try {
        const res = await listPromptLibraryBatch('opt_banner_type,opt_purpose,opt_template', 'banner')
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

    // Methods
    function triggerUpload() { fileInput.value?.click() }
    function triggerBgUpload() { bgFileInput.value?.click() }
    function triggerLogoUpload() { logoFileInput.value?.click() }
    function handleFileSelect(e) { const files = e.target.files; if (files.length) addFiles(files); fileInput.value.value = '' }
    function handleBgFileSelect(e) { const file = e.target.files[0]; if (file) { bgFile.value = file; const r = new FileReader(); r.onload = ev => { bgImage.value = ev.target.result }; r.readAsDataURL(file) }; bgFileInput.value.value = '' }
    function handleLogoFileSelect(e) { const file = e.target.files[0]; if (file) { logoFile.value = file; const r = new FileReader(); r.onload = ev => { logoImage.value = ev.target.result }; r.readAsDataURL(file) }; logoFileInput.value.value = '' }
    function handleDrop(e) { const files = e.dataTransfer.files; if (files.length) addFiles(files) }
    function addFiles(files) { for (const f of files) { const url = URL.createObjectURL(f); uploadedFiles.value.push(url); productFiles.value.push(f); if (!originalImage.value) { originalImage.value = url; originalFile.value = f } } }
    function clearImage() { originalImage.value = ''; originalFile.value = null; uploadedFiles.value = []; productFiles.value = []; resultImages.value = [] }
    function clearWorkspaceImages() { clearImage(); gen.reset() }
    function removeProductFile(index) { uploadedFiles.value.splice(index, 1); productFiles.value.splice(index, 1); if (uploadedFiles.value.length === 0) { originalImage.value = ''; originalFile.value = null } else { originalImage.value = uploadedFiles.value[0]; originalFile.value = productFiles.value[0] } }
    function selectCanvasPreset(s) { canvasPreset.value = s.value; canvasWidth.value = s.w; canvasHeight.value = s.h }
    function onCanvasPresetChange(val) {
      const s = sizePresets.value.find(p => p.value === val)
      if (s && s.value !== 'custom') { canvasWidth.value = s.w; canvasHeight.value = s.h }
    }
    function onCustomSize() { canvasPreset.value = 'custom' }
    function toggleAllSections() { const val = !allExpanded.value; Object.keys(sections).forEach(k => { sections[k] = val }) }
    function toggleSection(key) { if (sections.hasOwnProperty(key)) sections[key] = !sections[key] }
    function togglePurpose(key) { const idx = selectedPurposes.value.indexOf(key); if (idx >= 0) selectedPurposes.value.splice(idx, 1); else selectedPurposes.value.push(key) }
    function selectTemplate(tpl) { selectedTemplate.value = tpl; if (tpl.title) { const parts = tpl.title.split('\n'); mainTitle.value = parts[0] || ''; subTitle.value = parts[1] || '' }; if (tpl.tag) { const m = bannerTypes.value.find(b => b.name === tpl.tag); if (m) activeBannerType.value = m.key } }
    function scrollTemplates(dir) { const grid = document.querySelector('.templates-grid'); if (grid) grid.scrollBy({ left: dir === 'next' ? 240 : -240, behavior: 'smooth' }) }
    function refreshTemplates() { templates.value = [...templates.value].sort(() => Math.random() - 0.5) }
    function undo() {}
    function redo() {}
    function reset() { clearImage(); selectedTemplate.value = null; mainTitle.value = ''; subTitle.value = ''; btnText.value = '' }
    function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
    function zoomOut() { zoom.value = Math.max(10, zoom.value - 10) }
    function toggleFullscreen() {}

    async function handleGenerate() {
      if (!canGenerate.value) return
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      try {
        const extraParams = { n: generateCount.value, extraOptions: { canvasWidth: canvasWidth.value, canvasHeight: canvasHeight.value, bannerType: activeBannerType.value, purposes: [...selectedPurposes.value], mainTitle: mainTitle.value, subTitle: subTitle.value, btnText: btnText.value }, consumePoints: 2, featureName: 'banner', title: 'Banner设计' }
        if (bgFile.value) { const bgUrl = await gen.uploadImage(bgFile.value); extraParams.backgroundImage = bgUrl }
        if (logoFile.value) { const logoUrl = await gen.uploadImage(logoFile.value); extraParams.logoImage = logoUrl }
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const baseText = mainTitle.value + ' ' + subTitle.value
        const fullPrompt = boostText ? `${baseText}。约束：${boostText}。` : baseText
        await gen.fullGenerate([originalFile.value], fullPrompt, extraParams)
        if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
      } catch (e) { console.error('生成失败:', e) }
    }

    async function handleGenerateFromAi() {
      const text = aiAssistantRef.value?.inputText?.trim() || ''
      if (!originalFile.value) { ElMessage.warning('请先上传产品图片'); return }
      if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
      try {
        const extraParams = { n: generateCount.value, extraOptions: { canvasWidth: canvasWidth.value, canvasHeight: canvasHeight.value, bannerType: activeBannerType.value, purposes: [...selectedPurposes.value], mainTitle: mainTitle.value, subTitle: subTitle.value, btnText: btnText.value }, consumePoints: 2, featureName: 'banner', title: 'Banner设计' }
        if (bgFile.value) { const bgUrl = await gen.uploadImage(bgFile.value); extraParams.backgroundImage = bgUrl }
        if (logoFile.value) { const logoUrl = await gen.uploadImage(logoFile.value); extraParams.logoImage = logoUrl }
        const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
        const baseText = mainTitle.value + ' ' + subTitle.value + (text ? ' ' + text : '')
        const fullPrompt = boostText ? `${baseText}。约束：${boostText}。` : baseText
        await gen.fullGenerate([originalFile.value], fullPrompt, extraParams)
        if (gen.resultImages.value.length > 0) resultImages.value = gen.resultImages.value
      } catch (e) {
        console.error('生成失败:', e)
        const isTimeout = e?.code === 'ECONNABORTED'
          || /timeout|超时|人数过多|繁忙|busy/i.test(e?.message || '')
        ElMessage.error(isTimeout
          ? '当前模型使用人数过多，可选用其他模型生图或稍后再试'
          : '生成失败，请稍后重试')
      }
    }

    return {
      gen, fileInput, bgFileInput, logoFileInput, originalImage, originalFile, bgImage, bgFile, logoImage, logoFile, uploadedFiles, productFiles, resultImages, zoom,
      workflowSteps, getStepClass, isStepLineDone,
      canvasPreset, canvasWidth, canvasHeight, sizeLinked, sizePresets, selectCanvasPreset, onCanvasPresetChange, onCustomSize,
      sections, allExpanded, toggleAllSections, toggleSection,
      activeBannerType, bannerTypes, selectedPurposes, purposes, togglePurpose,
      mainTitle, subTitle, btnText, generateCount, maxGenerateCount, promptMap,
      language, languages,
      boostProduct, boostMaterial, boostProductRef, boostMaterialRef,
      activeTemplateTab, templateTabs, templates, selectedTemplate, filteredTemplates, selectTemplate, scrollTemplates, refreshTemplates,
      canvasFlex, configFlex, aiFlex, aiPanel, configCollapsed, startColResize, startAiResize,
      triggerUpload, triggerBgUpload, triggerLogoUpload, handleFileSelect, handleBgFileSelect, handleLogoFileSelect, handleDrop, clearImage, removeProductFile,
      undo, redo, reset, zoomIn, zoomOut, toggleFullscreen, handleGenerate, canGenerate,
      // ---- AiAssistant ----
      isGenerating, genProgress, genStatus, genError, aiAssistantRef, handleGenerateFromAi,
      // ---- 反推提示词 ----
      reverseDialogVisible, reverseImageFile, reverseImagePreview, reverseResult, reverseLoading,
      reversePromptInput, openReversePromptDialog, triggerReverseUpload, handleReverseDrop,
      clearReverseImage, submitReversePrompt, copyResult,
    }
  }
}
</script>

<style scoped>
.workspace-page { display: flex; flex-direction: column; height: 100%; overflow: hidden; }

/* Steps Bar */
.steps-bar { display: flex; align-items: center; padding: 0 0 12px; background: transparent; flex-shrink: 0; overflow-x: auto; gap: 0; }
.step-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #6B7280; white-space: nowrap; cursor: pointer; }
.step-item.active { color: #2563FF; font-weight: 600; }
.step-num { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; border: 2px solid #E8EDF5; flex-shrink: 0; }
.step-item.active .step-num { background: #2563FF; color: #fff; border-color: #2563FF; }
.step-item.done { color: #22C55E; }
.step-item.done .step-num { background: #22C55E; color: #fff; border-color: #22C55E; }
.step-line { flex: 1; height: 2px; background: #E8EDF5; min-width: 12px; margin: 0 6px; }
.step-line.done { background: #22C55E; }

.prompt-boost-row { margin-bottom: 10px; }
.prompt-boost-row .boost-label {
  display: block; font-size: 12px; color: #6B7280; margin-bottom: 4px;
}

/* Three Column */
.three-col { display: flex; flex: 1; overflow: hidden; min-height: 0; }

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

/* ===== Config Toggle Button ===== */
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

/* ===== Config Panel Collapsed ===== */
.config-col.collapsed {
  flex: 0 0 0px !important;
  min-width: 0 !important;
  max-width: 0 !important;
  overflow: hidden;
  padding: 0;
}

/* Canvas */
.canvas-col { display: flex; flex-direction: column; overflow-y: auto; background: #F7F9FC; min-width: 0; padding: 16px; min-height: 0; }
.canvas-col > * { flex-shrink: 0; }
.canvas-col .templates-section { flex-shrink: 0; }
.canvas-toolbar { display: flex; align-items: center; padding: 8px 12px; margin-bottom: 12px; background: #fff; border-radius: 12px; border: 1px solid #E8EDF5; flex-shrink: 0; }
.tb-left, .tb-center, .tb-right { display: flex; align-items: center; gap: 4px; }
.tb-center { flex: 1; justify-content: center; gap: 2px; }
.tb-btn { border: 1px solid #E8EDF5; background: #fff; padding: 5px 10px; border-radius: 6px; font-size: 12px; color: #1F2937; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.tb-btn:hover { background: #F0F4FF; }
.tb-text-btn { border: none; background: none; padding: 5px 8px; font-size: 12px; color: #6B7280; cursor: pointer; display: flex; align-items: center; gap: 3px; border-radius: 4px; }
.tb-text-btn:hover { color: #2563FF; background: #F0F4FF; }
.tb-sep { width: 1px; height: 16px; background: #E8EDF5; margin: 0 4px; }
.tb-zoom { font-size: 12px; color: #1F2937; padding: 0 6px; min-width: 40px; text-align: center; }
.zoom-btn { width: 28px; height: 28px; border: 1px solid #E8EDF5; border-radius: 6px; background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 14px; color: #4B5563; }
.zoom-btn:hover { border-color: #2563FF; color: #2563FF; }

.canvas-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #EBEDF5;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  min-height: 300px;
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

.result-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  align-content: start;
  overflow: auto;
  position: relative;
  padding: 4px;
  width: 100%;

  &.generating { opacity: 0.6; pointer-events: none; }
}

.result-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #EBEDF5;
  aspect-ratio: 16/9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.generating-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 10;
}

.progress-ring {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 4px solid #E5E7EB;
  border-top-color: #2563FF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #2563FF;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.canvas-tip { text-align: center; padding: 4px 0 12px; font-size: 12px; color: #6B7280; flex-shrink: 0; }

/* Templates */
.templates-section { padding-top: 8px; }
.templates-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.templates-title { font-size: 15px; font-weight: 700; color: #1F2937; }
.templates-tabs { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 12px; }
.templates-tab { padding: 4px 12px; border-radius: 16px; font-size: 12px; cursor: pointer; border: 1px solid #E8EDF5; color: #4B5563; background: #fff; transition: all 0.15s; white-space: nowrap; }
.templates-tab:hover { border-color: #2563FF; color: #2563FF; }
.templates-tab.active { background: #2563FF; color: #fff; border-color: #2563FF; font-weight: 500; }
.templates-grid { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; }
.templates-grid::-webkit-scrollbar { height: 4px; }
.templates-grid::-webkit-scrollbar-thumb { background: #E8EDF5; border-radius: 4px; }
.template-card { width: 220px; min-width: 220px; border-radius: 10px; overflow: hidden; border: 1px solid #E8EDF5; cursor: pointer; background: #fff; transition: box-shadow 0.2s, transform 0.15s; }
.template-card:hover { box-shadow: 0 4px 16px rgba(37,99,255,0.12); transform: translateY(-2px); }
.template-card.selected { border-color: #2563FF; box-shadow: 0 0 0 2px rgba(37,99,255,0.3); }
.template-card-preview { height: 100px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 12px; position: relative; overflow: hidden; }
.tpl-main-text { font-size: 14px; font-weight: 800; color: #fff; text-align: center; letter-spacing: 1px; line-height: 1.3; text-shadow: 0 1px 4px rgba(0,0,0,0.2); white-space: pre-line; }
.tpl-sub-text { font-size: 10px; color: rgba(255,255,255,0.85); margin-top: 4px; text-align: center; letter-spacing: 0.5px; }
.template-card-info { padding: 8px 10px; }
.template-card-name { font-size: 12px; font-weight: 600; color: #1F2937; }
.template-card-tag { font-size: 10px; color: #6B7280; margin-top: 2px; }
.templates-nav { display: flex; align-items: center; justify-content: flex-end; margin-top: 8px; gap: 8px; }
.templates-hint { font-size: 11px; color: #9CA3AF; flex: 1; }
.templates-arrow { width: 32px; height: 32px; border-radius: 50%; border: 1px solid #E8EDF5; background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #6B7280; font-size: 16px; transition: all 0.15s; }
.templates-arrow:hover { border-color: #2563FF; color: #2563FF; }
.templates-suggest { text-align: center; padding: 8px 0 4px; font-size: 12px; color: #6B7280; }
.templates-suggest span { cursor: pointer; color: #2563FF; margin-left: 8px; }

/* Config Panel */
.config-col { display: flex; flex-direction: column; overflow: hidden; background: #fff; min-width: 280px; }
.config-inner { padding: 0 0 16px; }
.panel-header { font-size: 15px; font-weight: 600; color: #1F2937; margin-bottom: 12px; display: flex; align-items: center; justify-content: space-between; cursor: pointer; user-select: none; padding: 14px 16px 0; }
.panel-toggle-all { font-size: 12px; color: #2563FF; font-weight: 400; display: flex; align-items: center; gap: 2px; transition: opacity 0.2s; }
.panel-toggle-all:hover { opacity: 0.7; }
.config-section.collapsible { margin-bottom: 0; border-bottom: 1px solid #F3F4F6; }
.section-header { display: flex; align-items: center; justify-content: space-between; cursor: pointer; user-select: none; padding: 8px 16px; transition: opacity 0.2s; }
.section-header:hover { opacity: 0.75; }
.section-label { font-size: 13px; font-weight: 600; color: #1F2937; }
.optional-label { font-weight: 400; font-size: 12px; color: #9CA3AF; }
.expand-text { font-size: 12px; color: #9CA3AF; display: flex; align-items: center; gap: 2px; }
.expand-arrow { transition: transform 0.25s; }
.expand-arrow.expanded { transform: rotate(180deg); }
.section-body { padding: 0 16px 8px; }

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

/* Upload */
.upload-row { display: flex; gap: 8px; margin-bottom: 6px; }
.upload-card { flex: 1; border: 2px dashed #E8EDF5; border-radius: 8px; padding: 12px 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 3px; text-align: center; cursor: pointer; transition: border-color 0.2s; }
.upload-card:hover { border-color: #2563FF; }
.upload-card-label { font-size: 11px; color: #1F2937; font-weight: 600; }
.upload-card-label .req { color: #EF4444; font-size: 10px; margin-left: 2px; }
.upload-card-icon { margin-bottom: 2px; }
.upload-card-action { font-size: 11px; color: #2563FF; font-weight: 500; }
.upload-card-hint { font-size: 10px; color: #9CA3AF; }
.upload-note { font-size: 11px; color: #9CA3AF; margin-bottom: 8px; }
.uploaded-images-list { display: flex; flex-wrap: wrap; gap: 8px; }
.uploaded-thumb-wrap { position: relative; width: 48px; height: 48px; border-radius: 8px; overflow: hidden; border: 1px solid #E8EDF5; }
.uploaded-thumb { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #F7F9FC; }
.uploaded-thumb img { width: 100%; height: 100%; object-fit: cover; }
.thumb-placeholder { font-size: 9px; color: #9CA3AF; }
.uploaded-remove { position: absolute; top: 2px; right: 2px; width: 16px; height: 16px; border-radius: 50%; background: rgba(0,0,0,0.45); color: #fff; font-size: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; opacity: 0; transition: opacity 0.2s; }
.uploaded-thumb-wrap:hover .uploaded-remove { opacity: 1; }

/* Type Grid */
.type-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.type-btn { border: 1.5px solid #E8EDF5; border-radius: 8px; padding: 10px 8px; text-align: left; font-size: 12px; color: #1F2937; cursor: pointer; background: #fff; transition: all 0.15s; }
.type-btn:hover { border-color: #2563FF; }
.type-btn.active { border-color: #2563FF; background: #EFF4FF; }
.type-btn-name { font-weight: 600; margin-bottom: 2px; }
.type-btn.active .type-btn-name { color: #2563FF; }
.type-btn-desc { font-size: 11px; color: #9CA3AF; }

/* Checkbox */
.checkbox-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; }
.checkbox-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #1F2937; cursor: pointer; padding: 4px 0; }
.checkbox-box { width: 16px; height: 16px; border: 1.5px solid #D1D5DB; border-radius: 4px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all 0.15s; }
.checkbox-box.checked { background: #2563FF; border-color: #2563FF; }

/* Input */
.input-group { margin-bottom: 10px; }
.input-label { font-size: 12px; color: #6B7280; margin-bottom: 4px; font-weight: 500; display: flex; align-items: center; justify-content: space-between; }
.char-count { font-size: 11px; color: #9CA3AF; font-weight: 400; }
.input-field { width: 100%; padding: 8px 10px; border: 1px solid #E8EDF5; border-radius: 8px; font-size: 13px; color: #1F2937; font-family: inherit; outline: none; transition: border-color 0.15s; }
.input-field:focus { border-color: #2563FF; }
.input-field::placeholder { color: #9CA3AF; }

/* Count */
.count-row { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
.count-btn { padding: 6px 14px; border: 1px solid #E8EDF5; border-radius: 6px; font-size: 12px; color: #4B5563; cursor: pointer; background: #fff; transition: all 0.15s; }
.count-btn:hover { border-color: #2563FF; }
.count-btn.active { border-color: #2563FF; background: #EEF2FF; color: #2563FF; font-weight: 600; }
.count-input { width: 50px; padding: 6px 8px; border: 1px solid #E8EDF5; border-radius: 6px; font-size: 13px; text-align: center; outline: none; }
.count-input:focus { border-color: #2563FF; }

/* Section helper */
.section-helper {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 6px;
  line-height: 1.5;
}

/* Generate */
.generate-area { padding: 0 16px 12px; }
.generate-btn { width: 100%; padding: 12px 0; background: #2563FF; color: #fff; border: none; border-radius: 10px; font-size: 15px; font-weight: 600; cursor: pointer; transition: background 0.15s; }
.generate-btn:hover { background: #1D4ED8; }
.generate-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.gen-progress { margin-top: 10px; text-align: center; }
.gen-status { font-size: 12px; color: #6B7280; display: block; margin-top: 4px; }
.gen-error { margin-top: 8px; font-size: 13px; color: #EF4444; display: flex; align-items: center; gap: 4px; }

/* AI Panel */
.ai-col { display: flex; flex-direction: column; background: #fff; padding: 16px; overflow: hidden; min-width: 240px; position: relative; }
.ai-resize-handle { position: absolute; left: 0; top: 0; bottom: 0; width: 6px; cursor: col-resize; z-index: 5; background: transparent; transition: background 0.2s; }
.ai-resize-handle:hover { background: #2563FF; }

::-webkit-scrollbar { width: 5px; height: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #9CA3AF; }

@media (max-width: 1280px) {
  .canvas-col { padding: 12px; }
  .config-col { min-width: 260px; }
  .ai-col { min-width: 180px; }
}

@media (max-width: 1024px) {
  .steps-bar { padding: 0 0 8px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 8px; margin: 0 4px; }
  .three-col { flex-wrap: wrap; }
  .canvas-col { flex: 0 0 100% !important; max-height: 50vh; }
  .config-col { flex: 0 0 50% !important; }
  .ai-col { flex: 0 0 50% !important; }
  .col-divider { display: none; }
  .ai-resize-handle { display: none; }
  .canvas-toolbar { flex-wrap: wrap; gap: 4px; }
  .tb-center { order: 3; flex-basis: 100%; justify-content: flex-start; }
}

@media (max-width: 768px) {
  .steps-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .config-col { flex: 0 0 auto !important; max-height: 240px; overflow-y: auto; }
  .ai-col { flex: 1 1 auto !important; min-height: 260px; }
  .templates-grid { padding-bottom: 4px; }
  .template-card { width: 180px; min-width: 180px; }
  .template-card-preview { height: 80px; }
  .tpl-main-text { font-size: 12px; }
  .upload-row { flex-direction: column; }
  .upload-card { flex-direction: row; padding: 8px 10px; gap: 8px; text-align: left; }
  .upload-card-icon { margin-bottom: 0; }
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