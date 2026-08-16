<template>
  <div class="workspace-page">
    <!-- Steps bar -->
    <div class="steps-bar">
      <template v-for="(s, i) in workflowSteps" :key="i">
        <div class="step-item" :class="getStepClass(i + 1, 5)"><div class="step-num">{{ i + 1 }}</div> {{ s.label }}</div>
        <div v-if="i < workflowSteps.length - 1" class="step-line" :class="{ done: isStepLineDone(i + 1) }"></div>
      </template>
    </div>

    <!-- Three-column -->
    <div class="three-col">
      <!-- ===== Canvas ===== -->
        <div class="canvas-col" :style="{ flex: canvasFlex }">

        <div class="canvas-dropzone"
          @drop.prevent="handleDrop"
        >
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div v-if="!originalImage" class="dropzone-placeholder" @click="triggerUpload">
            <svg class="dropzone-icon" viewBox="0 0 48 48" fill="none"><rect x="4" y="8" width="40" height="50" rx="4" stroke="#D1D5DB" stroke-width="2"/><circle cx="16" cy="20" r="4" stroke="#D1D5DB" stroke-width="2"/><polyline points="44,34 34,24 20,38" stroke="#D1D5DB" stroke-width="2" fill="none"/><polyline points="28,30 22,24 4,38" stroke="#D1D5DB" stroke-width="2" fill="none"/></svg>
            <div class="dropzone-title">拖拽图片到画布，或从右侧上传素材</div>
            <div class="dropzone-desc">支持 JPG / PNG / WebP，建议宽度 ≥ 1200px</div>
          </div>
          <div v-else class="dropzone-preview">
            <img :src="originalImage" class="preview-img" :style="{ transform: `scale(${zoom / 100})` }" />
            <div class="preview-overlay"><button class="preview-del-btn" @click.stop="clearImage">✕</button></div>
          </div>
          <div class="canvas-zoom-controls" v-if="originalImage">
            <button class="zoom-btn" @click="zoomOut">−</button>
            <span class="zoom-value">{{ zoom }}%</span>
            <button class="zoom-btn" @click="zoomIn">+</button>
          </div>
        </div>
        <div class="canvas-tip">建议使用高质量素材，获得更佳效果</div>

        <!-- Templates -->
        <div class="templates-section">
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
        </div>
        <div class="templates-suggest">建议尺寸：1200×300px、1920×600px、1920×1080px 等主流尺寸<span @click="refreshTemplates">🔄 换一换</span></div>
      </div>

      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowLeft v-if="!configCollapsed" /><ArrowRight v-else /></el-icon>
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
                  <div class="upload-card" @click.stop="triggerUpload"><div class="upload-card-label">产品图<span class="req">（必传）</span></div><div class="upload-card-icon"><el-icon :size="20" color="#9CA3AF"><UploadFilled /></el-icon></div><div class="upload-card-action">↑ 上传产品图</div><div class="upload-card-hint">支持 JPG/PNG，最多10张</div></div>
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
                <div class="count-row">
                  <span v-for="n in [1,2,3,4,5]" :key="n" class="count-btn" :class="{ active: generateCount === n }" @click="generateCount = n">{{ n }}张</span>
                  <span class="count-btn" :class="{ active: generateCount > 5 }" @click="generateCount = customCount || 6">自定义</span>
                  <input class="count-input" type="number" v-model.number="customCount" @change="generateCount = customCount" min="1" max="10">
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

            <!-- 提示词增强 -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('promptBoost')">
                <span class="section-label">提示词增强</span>
                <span class="expand-text">{{ sections.promptBoost ? '收起' : '展开' }}<el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.promptBoost }"><ArrowDown /></el-icon></span>
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

            <div class="generate-area">
              <button class="generate-btn" :disabled="!canGenerate" @click="handleGenerate">{{ gen.generating.value ? '生成中...' : '生成Banner（下一步）' }}</button>
              <div class="gen-progress" v-if="gen.generating.value">
                <el-progress :percentage="gen.progress.value" :stroke-width="6" :show-text="false" />
                <span class="gen-status">{{ gen.statusText.value }}</span>
              </div>
              <div class="gen-error" v-if="gen.error.value"><el-icon color="#EF4444"><WarningFilled /></el-icon>{{ gen.error.value }}</div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        <div class="ai-resize-handle" @mousedown="startAiResize"></div>
        <div class="ai-header"><h3>AI 助手</h3><button class="ai-clear-btn" @click="clearChat">清空对话</button></div>
        <div class="ai-suggestions"><span v-for="sug in aiSuggestions" :key="sug" class="ai-sug-tag" @click="appendToAiInput(sug)">{{ sug }}</span></div>
        <div class="ai-chat" ref="chatContainer">
          <div class="chat-msg bot"><div class="chat-avatar">AI</div><div class="chat-bubble">您好！我是光合AI助手，有什么可以帮您？</div></div>
          <div v-for="(msg, i) in aiMessages" :key="i" class="chat-msg" :class="msg.role">
            <div v-if="msg.role === 'bot'" class="chat-avatar">AI</div>
            <div class="chat-bubble">{{ msg.text }}</div>
          </div>
          <div v-if="aiLoading" class="chat-msg bot"><div class="chat-avatar">AI</div><div class="chat-bubble">正在为您生成中...</div></div>
        </div>
        <div class="ai-input-area">
          <textarea class="ai-textarea" v-model="aiInput" placeholder="请输入您的需求，描述越详细，效果越好..." @keydown.enter.exact.prevent="sendAiMessage" maxlength="2000"></textarea>
          <div class="ai-input-footer">
            <span class="ai-char-count">{{ aiInput.length }}/2000</span>
            <button class="ai-send-btn" @click="sendAiMessage" :disabled="!aiInput.trim() || aiLoading">发送<el-icon :size="14"><Promotion /></el-icon></button>
          </div>
        </div>
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" multiple hidden @change="handleFileSelect" />
    <input type="file" ref="bgFileInput" accept="image/*" multiple hidden @change="handleBgFileSelect" />
    <input type="file" ref="logoFileInput" accept="image/*" hidden @change="handleLogoFileSelect" />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import { aiDialogue } from '@/api/customer'
import { ArrowDown, ArrowLeft, ArrowRight, WarningFilled, FullScreen, RefreshLeft, Delete, UploadFilled, Link, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'

export default {
  name: 'BannerView',
  components: { ArrowDown, ArrowLeft, ArrowRight, WarningFilled, FullScreen, RefreshLeft, Delete, UploadFilled, Link, Promotion, PromptLibrarySelect },
  setup() {
    const gen = useImageGeneration('render')
    const { steps: workflowSteps, getStepClass, isStepLineDone } = useWorkflowProgress()
    // const { canvasUI, handleCanvasExport } = useCanvasInteractions({
    //   canvasSelector: '.canvas-dropzone',
    //   defaultName: 'banner',
    // })
    const fileInput = ref(null); const bgFileInput = ref(null); const logoFileInput = ref(null)
    const originalImage = ref(''); const originalFile = ref(null); const bgImage = ref(''); const bgFile = ref(null); const logoImage = ref(''); const logoFile = ref(null)
    const uploadedFiles = ref([]); const productFiles = ref([])
    const zoom = ref(100)

    // Canvas size
    const canvasPreset = ref('1200x300'); const canvasWidth = ref(1200); const canvasHeight = ref(300); const sizeLinked = ref(true)
    const sizePresets = [
      { label: '1200×300（横幅）', value: '1200x300', w: 1200, h: 300 },
      { label: '1920×600（通栏）', value: '1920x600', w: 1920, h: 600 },
      { label: '1920×1080（大屏）', value: '1920x1080', w: 1920, h: 1080 },
      { label: '自定义', value: 'custom', w: 0, h: 0 },
    ]

    // Sections
    const sections = reactive({ canvasSize: true, upload: true, bannerType: true, purpose: true, keyInfo: true, count: true, language: true, promptBoost: false })
    const allExpanded = computed(() => Object.values(sections).every(v => v))

    // Banner type
    const activeBannerType = ref('promo')
    const bannerTypes = [
      { key: 'promo', name: '促销活动', desc: '打折促销、限时优惠' },
      { key: 'new', name: '新品上市', desc: '新品发布、产品推荐' },
      { key: 'brand', name: '品牌宣传', desc: '品牌故事、品牌形象' },
      { key: 'season', name: '节日季节', desc: '节日活动、季节主题' },
      { key: 'notice', name: '信息通知', desc: '公告通知、店铺信息' },
      { key: 'decorate', name: '店铺装修', desc: '店铺头图、页面装饰' },
    ]

    // Purposes
    const selectedPurposes = ref(['sales'])
    const purposes = [
      { key: 'sales', label: '提升销量/促销转化' }, { key: 'newProduct', label: '新品推广' },
      { key: 'branding', label: '品牌宣传/提升认知' }, { key: 'traffic', label: '活动宣传/引流' },
      { key: 'clearance', label: '清仓/库存处理' }, { key: 'festival', label: '节日营销' },
      { key: 'shopImage', label: '店铺形象展示' }, { key: 'other', label: '其他' },
    ]

    // Key info
    const mainTitle = ref(''); const subTitle = ref(''); const btnText = ref('')
    const generateCount = ref(3); const customCount = ref(6)

    // Language
    const language = ref('zh-CN')
    const languages = [
      { label: '中文（简体）', value: 'zh-CN' },
      { label: '英语（美国）', value: 'en-US' },
      { label: '英语（英国）', value: 'en-GB' },
      { label: '日语', value: 'ja-JP' },
      { label: '韩语', value: 'ko-KR' },
      { label: '德语', value: 'de-DE' },
      { label: '法语', value: 'fr-FR' },
      { label: '西班牙语', value: 'es-ES' },
    ]

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
    const aiPanel = ref(null); const chatContainer = ref(null)
    const aiInput = ref(''); const aiMessages = ref([]); const aiLoading = ref(false)
    const aiSuggestions = ['如何设计高点击Banner？', 'Banner尺寸规范', '配色方案建议']

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
      await Promise.allSettled([gen.loadPromptInfo(), gen.loadPixelConfigs(), gen.loadDeductTypes()])
    })
    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onMouseMove); document.removeEventListener('mouseup', onMouseUp)
      document.removeEventListener('mousemove', onAiMouseMove); document.removeEventListener('mouseup', onAiMouseUp)
    })

    // Methods
    function triggerUpload() { fileInput.value?.click() }
    function triggerBgUpload() { bgFileInput.value?.click() }
    function triggerLogoUpload() { logoFileInput.value?.click() }
    function handleFileSelect(e) { const files = e.target.files; if (files.length) addFiles(files); fileInput.value.value = '' }
    function handleBgFileSelect(e) { const file = e.target.files[0]; if (file) { bgFile.value = file; const r = new FileReader(); r.onload = ev => { bgImage.value = ev.target.result }; r.readAsDataURL(file) }; bgFileInput.value.value = '' }
    function handleLogoFileSelect(e) { const file = e.target.files[0]; if (file) { logoFile.value = file; const r = new FileReader(); r.onload = ev => { logoImage.value = ev.target.result }; r.readAsDataURL(file) }; logoFileInput.value.value = '' }
    function handleDrop(e) { const files = e.dataTransfer.files; if (files.length) addFiles(files) }
    function addFiles(files) { for (const f of files) { const url = URL.createObjectURL(f); uploadedFiles.value.push(url); productFiles.value.push(f); if (!originalImage.value) { originalImage.value = url; originalFile.value = f } } }
    function clearImage() { originalImage.value = ''; originalFile.value = null; uploadedFiles.value = []; productFiles.value = [] }
    function removeProductFile(index) { uploadedFiles.value.splice(index, 1); productFiles.value.splice(index, 1); if (uploadedFiles.value.length === 0) { originalImage.value = ''; originalFile.value = null } else { originalImage.value = uploadedFiles.value[0]; originalFile.value = productFiles.value[0] } }
    function selectCanvasPreset(s) { canvasPreset.value = s.value; canvasWidth.value = s.w; canvasHeight.value = s.h }
    function onCanvasPresetChange(val) {
      const s = sizePresets.find(p => p.value === val)
      if (s && s.value !== 'custom') { canvasWidth.value = s.w; canvasHeight.value = s.h }
    }
    function onCustomSize() { canvasPreset.value = 'custom' }
    function toggleAllSections() { const val = !allExpanded.value; Object.keys(sections).forEach(k => { sections[k] = val }) }
    function toggleSection(key) { if (sections.hasOwnProperty(key)) sections[key] = !sections[key] }
    function togglePurpose(key) { const idx = selectedPurposes.value.indexOf(key); if (idx >= 0) selectedPurposes.value.splice(idx, 1); else selectedPurposes.value.push(key) }
    function selectTemplate(tpl) { selectedTemplate.value = tpl; if (tpl.title) { const parts = tpl.title.split('\n'); mainTitle.value = parts[0] || ''; subTitle.value = parts[1] || '' }; if (tpl.tag) { const m = bannerTypes.find(b => b.name === tpl.tag); if (m) activeBannerType.value = m.key } }
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
      } catch (e) { console.error('生成失败:', e) }
    }

    function appendToAiInput(text) { aiInput.value = aiInput.value ? `${aiInput.value} ${text}` : text }
    async function sendAiMessage() {
      if (!aiInput.value.trim() || aiLoading.value) return
      const text = aiInput.value.trim(); aiMessages.value.push({ role: 'user', text }); aiInput.value = ''; aiLoading.value = true; await nextTick(); scrollChat()
      try { const res = await aiDialogue({ content: text, sessionType: 'render' }); const reply = res.data?.content || res.data?.reply || '好的，正在为您处理中...'; aiMessages.value.push({ role: 'bot', text: reply }) }
      catch { aiMessages.value.push({ role: 'bot', text: '抱歉，暂时无法回复，请稍后重试。' }) }
      finally { aiLoading.value = false; await nextTick(); scrollChat() }
    }
    function clearChat() { aiMessages.value = [] }
    function scrollChat() { const el = chatContainer.value; if (el) el.scrollTop = el.scrollHeight }

    return {
      gen, fileInput, bgFileInput, logoFileInput, originalImage, originalFile, bgImage, bgFile, logoImage, logoFile, uploadedFiles, productFiles, zoom,
      workflowSteps, getStepClass, isStepLineDone,
      canvasPreset, canvasWidth, canvasHeight, sizeLinked, sizePresets, selectCanvasPreset, onCanvasPresetChange, onCustomSize,
      sections, allExpanded, toggleAllSections, toggleSection,
      activeBannerType, bannerTypes, selectedPurposes, purposes, togglePurpose,
      mainTitle, subTitle, btnText, generateCount, customCount,
      language, languages,
      boostProduct, boostMaterial, boostProductRef, boostMaterialRef,
      activeTemplateTab, templateTabs, templates, selectedTemplate, filteredTemplates, selectTemplate, scrollTemplates, refreshTemplates,
      canvasFlex, configFlex, aiFlex, aiPanel, chatContainer, configCollapsed, startColResize, startAiResize,
      triggerUpload, triggerBgUpload, triggerLogoUpload, handleFileSelect, handleBgFileSelect, handleLogoFileSelect, handleDrop, clearImage, removeProductFile,
      undo, redo, reset, zoomIn, zoomOut, toggleFullscreen, handleGenerate, canGenerate,
      aiInput, aiMessages, aiLoading, aiSuggestions, appendToAiInput, sendAiMessage, clearChat,
      // canvasUI, handleCanvasExport,
    }
  }
}
</script>

<style scoped>
.workspace-page { display: flex; flex-direction: column; height: 100%; overflow: hidden; }

/* Steps Bar */
.steps-bar { display: flex; align-items: center; padding: 12px 24px; background: #fff; border-bottom: 1px solid #E8EDF5; flex-shrink: 0; gap: 0; }
.step-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #6B7280; white-space: nowrap; }
.step-item.active { color: #2563FF; font-weight: 600; }
.step-num { width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; border: 2px solid #E8EDF5; flex-shrink: 0; }
.step-item.active .step-num { background: #2563FF; color: #fff; border-color: #2563FF; }
.step-item.done { color: #22C55E; }
.step-item.done .step-num { background: #22C55E; color: #fff; border-color: #22C55E; }
.step-line { flex: 1; height: 2px; background: #E8EDF5; min-width: 12px; margin: 0 6px; }
.step-line.active { background: #2563FF; }
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

.canvas-dropzone { min-height: 390px; background: #fff; border: 2px dashed #E8EDF5; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; flex-shrink: 0; cursor: pointer; transition: border-color 0.2s; overflow: hidden; position: relative; }
.canvas-dropzone:hover { border-color: #2563FF; }
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
.canvas-zoom-controls .zoom-value {
  font-size: 12px;
  color: #1F2937;
  min-width: 40px;
  text-align: center;
}
.dropzone-placeholder { text-align: center; padding: 20px; }
.dropzone-icon { width: 48px; height: 48px; color: #D1D5DB; }
.dropzone-title { font-size: 14px; font-weight: 600; color: #1F2937; margin-top: 8px; }
.dropzone-desc { font-size: 12px; color: #6B7280; }
.canvas-tip { text-align: center; padding: 4px 0 12px; font-size: 12px; color: #6B7280; flex-shrink: 0; }
.dropzone-preview { position: relative; max-width: 100%; max-height: 100%; }
.preview-img { max-width: 100%; max-height: 280px; object-fit: contain; border-radius: 8px; }
.preview-overlay { position: absolute; top: 8px; right: 8px; }
.preview-del-btn { width: 28px; height: 28px; border-radius: 50%; background: rgba(0,0,0,0.5); color: #fff; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; justify-content: center; }
.preview-del-btn:hover { background: #EF4444; }

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
.ai-col { display: flex; flex-direction: column; background: #fff; padding: 14px 12px; overflow: hidden; min-width: 200px; position: relative; }
.ai-resize-handle { position: absolute; left: 0; top: 0; bottom: 0; width: 6px; cursor: col-resize; z-index: 5; background: transparent; transition: background 0.2s; }
.ai-resize-handle:hover { background: #2563FF; }
.ai-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; flex-shrink: 0; gap: 8px; min-height: 24px; }
.ai-header h3 { font-size: 14px; font-weight: 600; margin: 0; white-space: nowrap; }
.ai-clear-btn { font-size: 11px; color: #2563FF; background: none; border: none; cursor: pointer; text-decoration: underline; white-space: nowrap; padding: 2px 4px; flex-shrink: 0; }
.ai-clear-btn:hover { opacity: 0.7; }
.ai-suggestions { display: flex; flex-wrap: wrap; gap: 6px; padding-bottom: 10px; flex-shrink: 0; }
.ai-sug-tag { background: #EFF4FF; color: #2563FF; font-size: 11px; padding: 4px 8px; border-radius: 12px; cursor: pointer; white-space: nowrap; }
.ai-sug-tag:hover { background: #DCE6FF; }
.ai-chat { background: #F7F9FC; border-radius: 10px; padding: 12px; overflow-y: auto; flex: 1; margin-bottom: 8px; min-height: 150px; }
.chat-msg { display: flex; gap: 6px; margin-bottom: 10px; }
.chat-msg.bot { flex-direction: row; }
.chat-msg.user { flex-direction: row-reverse; }
.chat-avatar { width: 22px; height: 22px; border-radius: 50%; background: #2563FF; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 10px; flex-shrink: 0; }
.chat-bubble { padding: 8px 12px; border-radius: 10px; font-size: 12px; line-height: 1.5; max-width: 85%; }
.chat-msg.bot .chat-bubble { background: #fff; color: #1F2937; }
.chat-msg.user .chat-bubble { background: #EEF2FF; color: #2563FF; }
.ai-input-area { flex-shrink: 0; }
.ai-textarea { width: 100%; height: 60px; border: 1px solid #E8EDF5; border-radius: 8px; padding: 8px 10px; font-size: 13px; color: #1F2937; resize: none; font-family: inherit; outline: none; }
.ai-textarea:focus { border-color: #2563FF; }
.ai-textarea::placeholder { color: #9CA3AF; }
.ai-input-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.ai-char-count { font-size: 11px; color: #6B7280; }
.ai-send-btn { background: #2563FF; color: #fff; border: none; padding: 6px 14px; border-radius: 6px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 4px; font-weight: 500; }
.ai-send-btn:hover { background: #1D4ED8; }
.ai-send-btn:disabled { opacity: 0.4; cursor: not-allowed; }

::-webkit-scrollbar { width: 5px; height: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #9CA3AF; }

@media (max-width: 1280px) {
  .steps-bar { padding: 10px 16px; gap: 4px; }
  .step-item { font-size: 11px; }
  .step-line { min-width: 8px; margin: 0 4px; }
  .canvas-col { padding: 12px; }
  .config-col { min-width: 260px; }
  .ai-col { min-width: 180px; }
}

@media (max-width: 1024px) {
  .steps-bar { padding: 8px 12px; gap: 2px; }
  .step-item { font-size: 10px; }
  .step-line { min-width: 6px; margin: 0 3px; }
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
  .ai-resize-handle { display: none; }
  .templates-grid { padding-bottom: 4px; }
  .template-card { width: 180px; min-width: 180px; }
  .template-card-preview { height: 80px; }
  .tpl-main-text { font-size: 12px; }
  .upload-row { flex-direction: column; }
  .upload-card { flex-direction: row; padding: 8px 10px; gap: 8px; text-align: left; }
  .upload-card-icon { margin-bottom: 0; }
}
</style>