<template>
  <!----><div class="workspace-page">


    <!-- Step progress bar (8 steps) -->
    <div class="step-bar">
      <div
        v-for="(s, idx) in workflowSteps"
        :key="idx"
        class="step-item"
        :class="getStepClass(idx + 1, 4)"
      >
        <div class="step-dot">
          <el-icon v-if="isStepDone(s.key)" :size="12"><Check /></el-icon>
          <span v-else>{{ idx + 1 }}</span>
        </div>
        <span class="step-label">{{ s.label }}</span>
        <div class="step-line" v-if="idx < workflowSteps.length - 1"></div>
      </div>
    </div>

    <!-- Three-column layout -->
    <div class="three-col">
      <!-- ===== LEFT: Canvas / Result ===== -->
      <div class="canvas-col" :style="{ flex: canvasFlex }">
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
         
        <!-- Central canvas / upload zone -->
        <div class="canvas-box" v-if="!generated"
        >
          <!-- <CanvasOverlay :overlay="canvasUI" @export="handleCanvasExport" /> -->
          <div class="upload-zone" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
            <div class="upload-placeholder" v-if="!productImage">
              <el-icon :size="48" color="#9CA3AF"><PictureFilled /></el-icon>
              <p class="upload-main-text">拖拽图片到画布，或从右侧配置生成</p>
              <p class="upload-sub-text">生成的模特效果将呈现在画布中</p>
            </div>
            <div class="upload-preview" v-else>
              <img :src="productImage" class="preview-img" :style="{ transform: `scale(${zoom / 100})` }" />
              <div class="preview-overlay">
                <el-button type="danger" size="small" circle @click.stop="productImage = ''">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <div class="canvas-zoom-controls" v-if="productImage">
            <button class="zoom-btn" @click="zoomOut">−</button>
            <span class="zoom-value">{{ zoom }}%</span>
            <button class="zoom-btn" @click="zoomIn">+</button>
          </div>
        </div>

        <!-- Result grid -->
        <div class="result-area" v-else>
          <div class="model-grid">
            <div v-for="i in genCount" :key="i" class="model-card">
              <div
                class="model-thumb"
                :style="{ background: `linear-gradient(135deg, hsl(${340 + i * 20}, 50%, 90%), hsl(${200 + i * 30}, 60%, 88%))` }"
              >
                <el-icon :size="44" style="opacity: 0.35"><User /></el-icon>
                <span>模特方案 {{ i }}</span>
              </div>
              <div class="model-card-actions">
                <el-button text size="small"><el-icon><Star /></el-icon></el-button>
                <el-button text size="small" type="primary"><el-icon><Download /></el-icon></el-button>
              </div>
            </div>
          </div>
          <div class="result-bottom-actions">
            <el-button @click="generated = false"><el-icon><RefreshLeft /></el-icon> 重新生成</el-button>
            <el-button type="primary" @click="$router.push('/hero-image')">
              下一步：主图设计 <el-icon><Right /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="canvas-bottom-bar">
          <span>提示：在右侧上传商品图并配置参数，点击生成按钮即可生成AI模特效果。</span>
        </div>
      </div>

      <!-- Divider + Toggle: canvas ⇔ config -->
      <div class="col-divider-wrapper">
        <div class="col-divider" @mousedown="startColResize($event, 'config')"></div>
        <div class="config-toggle-btn" @click="configCollapsed = !configCollapsed" :title="configCollapsed ? '展开创作配置' : '折叠创作配置'">
          <el-icon :size="14"><ArrowLeft v-if="!configCollapsed" /><ArrowRight v-else /></el-icon>
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

            <!-- Section: Upload product image -->
            <div class="config-section collapsible">
              <div class="section-header collapsible" @click="toggleSection('upload')">
                <span class="section-label">上传商品图</span>
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
              <div class="section-header collapsible" @click="toggleSection('modelSelect')">
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
              </div>
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
                <span class="section-label">输出设置</span>
                <span class="expand-text">
                  {{ sections.output ? '收起' : '展开' }}
                  <el-icon :size="12" class="expand-arrow" :class="{ expanded: sections.output }"><ArrowDown /></el-icon>
                </span>
              </div>
              <div v-show="sections.output" class="section-body">
                <div class="output-row">
                  <span class="output-label">输出尺寸</span>
                  <el-select v-model="outputSize" style="width: 140px" size="small">
                    <el-option label="2000 x 2000" value="2000" />
                    <el-option label="1500 x 1500" value="1500" />
                    <el-option label="1000 x 1000" value="1000" />
                  </el-select>
                </div>
                <div class="output-row">
                  <span class="output-label">生成数量</span>
                  <div class="count-group compact">
                    <div
                      v-for="n in 5"
                      :key="n"
                      class="count-btn"
                      :class="{ active: !useCustomCount && presetCount === n }"
                      @click="selectPresetCount(n)"
                    >{{ n }}</div>
                    <div
                      class="count-btn custom-btn"
                      :class="{ active: useCustomCount }"
                      @click="useCustomCount = true"
                    >自定义</div>
                  </div>
                </div>
                <div class="output-row custom-count-row" v-if="useCustomCount">
                  <span class="output-label">自定义数量</span>
                  <el-input-number v-model="customCount" :min="1" :max="20" size="small" controls-position="right" style="width: 120px" />
                </div>
              </div>
            </div>

            <!-- 提示词增强 -->
            <div class="config-section collapsible">
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
            </div>

            <!-- Generate button -->
            <el-button type="primary" size="large" class="generate-btn" :loading="generating" @click="handleGenerate">
              <el-icon><MagicStick /></el-icon>
              生成AI模特
              <span class="btn-points">-{{ totalPoints }} 积分</span>
            </el-button>
          </div>
        </el-scrollbar>
      </div>

      <!-- Divider handle: config ⇔ AI -->
      <div class="col-divider" @mousedown="startColResize($event, 'ai')"></div>

      <!-- ===== RIGHT: AI Panel ===== -->
      <div class="ai-col" :style="{ flex: aiFlex }" ref="aiPanel">
        <div class="ai-header">
          <h3>AI 助手</h3>
          <button class="ai-clear-btn" @click="clearChat">清空对话</button>
        </div>

        <!-- Chat messages -->
        <div class="ai-chat" ref="chatContainer">
          <!-- Bot greeting -->
          <div class="chat-msg bot">
            <div class="chat-avatar">AI</div>
            <div class="chat-bubble">您好！我是光合AI助手，有什么可以帮您？</div>
          </div>
          <!-- Dynamic messages -->
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

        <!-- Chat input -->
        <div class="chat-input-area">
          <textarea
            v-model="aiInput"
            class="chat-input"
            placeholder="请输入您的需求..."
            rows="4"
            maxlength="2000"
            @keydown.enter.exact.prevent="sendAiMessage"
          ></textarea>
        </div>
        <div class="chat-footer">
          <div>
            <span class="chat-counter">{{ aiInput.length }}/2000</span>
            <br />
            <!-- <span class="chat-cost">本次对话免费</span> -->
          </div>
          <button class="chat-send" @click="sendAiMessage" :disabled="!aiInput.trim()">发送</button>
        </div>
      </div>
    </div>

    <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFile" />
  </div>
</template>

<script setup>
defineOptions({ name: 'AiModelView' })
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  UploadFilled, Star, Download, MagicStick, Right, Delete,
  RefreshLeft, RefreshRight, ArrowDown, ArrowLeft, ArrowRight,
  ChatDotRound, Monitor, User, Check,
  PictureFilled, House, Sunny, OfficeBuilding
} from '@element-plus/icons-vue'
// import { useCanvasInteractions } from '@/composables/useCanvasInteractions'
// import CanvasOverlay from '@/components/CanvasOverlay.vue'
import { useImageGeneration } from '@/composables/useImageGeneration'
import { useWorkflowProgress } from '@/composables/useWorkflowProgress'
import PromptLibrarySelect from '@/components/PromptLibrarySelect.vue'
import { aiDialogue, getPublicPublishedAiModels } from '@/api/customer'
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
const generated = ref(false)
const generating = ref(false)

const zoom = ref(100)
function zoomIn() { zoom.value = Math.min(200, zoom.value + 10) }
function zoomOut() { zoom.value = Math.max(30, zoom.value - 10) }

const aiInput = ref('')
const aiMessages = ref([])

const gen = useImageGeneration('render')
const { steps: workflowSteps, getStepClass, isStepDone } = useWorkflowProgress()

const gender = ref('女')
const age = ref('青年')
const hairstyle = ref('长发')
const ethnicity = ref('asian')
const pose = ref('站立')
const clothing = ref('商务')
const sceneStyle = ref('pure-bg')
const outputSize = ref('2000')

// ===== 提示词增强（从 gh_prompt_library 拉取） =====
const boostProduct = ref('')
const boostMaterial = ref('')
const boostProductRef = ref(null)
const boostMaterialRef = ref(null)
const presetCount = ref(4)
const useCustomCount = ref(false)
const customCount = ref(4)
const genCount = computed(() => useCustomCount.value ? customCount.value : presetCount.value)
const totalPoints = computed(() => genCount.value)

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



const ageRanges = ['青年(18-25)', '轻熟(26-35)', '成熟(36-45)', '中年(46+)']
const hairstyles = ['短发', '长发', '卷发', '直发', '马尾', '丸子头']

const ethnicities = [
  { label: '亚洲', value: 'asian', color: '#F5DEB3' },
  { label: '欧美', value: 'western', color: '#FFDAB9' },
  { label: '非洲', value: 'african', color: '#8B6914' },
  { label: '中东', value: 'middleeast', color: '#D2B48C' },
  { label: '拉丁', value: 'latin', color: '#DEB887' }
]

const poses = ['站立', '坐姿', '行走', '半身', '全身', '回眸', '侧面', '正面']
const clothingOptions = ['商务', '休闲', '运动', '正装', '街拍']

const sceneOptions = [
  { label: '室内白底', value: 'indoor-white', gradient: 'linear-gradient(135deg, #ffffff, #e9ecef)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M3 12h18" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 2"/></svg>' },
  { label: '室内场景', value: 'indoor', gradient: 'linear-gradient(135deg, #e0e7ff, #c7d2fe)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M4 20V8l8-5 8 5v12" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><rect x="9" y="12" width="6" height="8" stroke="currentColor" stroke-width="1.5"/></svg>' },
  { label: '户外自然', value: 'outdoor-nature', gradient: 'linear-gradient(135deg, #d1fae5, #a7f3d0)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M12 3l5 8H7l5-8z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M12 11v9" stroke="currentColor" stroke-width="1.5"/><path d="M9 20h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { label: '街头', value: 'street', gradient: 'linear-gradient(135deg, #fef3c7, #fde68a)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><path d="M5 21V7l5-3v17" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M10 21V11l6 3v7" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/><path d="M3 21h18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>' },
  { label: '商场', value: 'mall', gradient: 'linear-gradient(135deg, #fce7f3, #fbcfe8)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="4" y="8" width="16" height="13" rx="1" stroke="currentColor" stroke-width="1.5"/><path d="M9 8V5a3 3 0 016 0v3" stroke="currentColor" stroke-width="1.5"/><path d="M4 13h16" stroke="currentColor" stroke-width="1.5"/></svg>' },
  { label: '纯色背景', value: 'pure-bg', gradient: 'linear-gradient(135deg, #f1f5f9, #e2e8f0)', svgIcon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.5"/></svg>' }
]

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
  document.removeEventListener('mouseup', onMouseUp)
})

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

function selectPresetCount(n) {
  useCustomCount.value = false
  presetCount.value = n
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

async function sendAiMessage() {
  const text = aiInput.value.trim()
  if (!text || generating.value) return
  aiMessages.value.push({ text, reply: '' })
  aiInput.value = ''
  nextTick(() => scrollChat())

  if (originalFile.value) handleGenerate()

  try {
    const historyMessages = aiMessages.value.slice(0, -1).map(m => ({ role: 'user', content: m.text }))
    const res = await aiDialogue({ messages: historyMessages, content: text, model: 'deepseek' })
    aiMessages.value[aiMessages.value.length - 1].reply = res?.data?.reply || '抱歉，暂时无法获取回复。'
  } catch (e) {
    aiMessages.value[aiMessages.value.length - 1].reply = '抱歉，AI服务暂时不可用。'
  }
  nextTick(() => scrollChat())
}

async function handleGenerate() {
  if (!originalFile.value) { ElMessage.warning('请先上传产品图片'); return }
  if (!(await gen.checkPoints(2))) { ElMessage.warning('积分不足，请先充值'); return }
  generating.value = true
  try {
    const _basePrompt = `生成AI模特图，性别${gender.value}，年龄${age.value}，发型${hairstyle.value}，姿势${pose.value}，服装${clothing.value}，场景${sceneStyle.value}`
    const boostText = [boostProductRef.value?.getSelectedItems()[0]?.promptText, boostMaterialRef.value?.getSelectedItems()[0]?.promptText].filter(Boolean).join('；')
    const prompt = boostText ? `${_basePrompt}；约束：${boostText}。` : _basePrompt
    await gen.fullGenerate([originalFile.value], prompt, { consumePoints: 2, featureName: 'ai_model', title: 'AI模特生成', n: 1 })
    if (gen.resultImages.value.length > 0) {
      generated.value = true
      productImage.value = gen.resultImages.value[0].url || gen.resultImages.value[0]
    }
  } catch (e) {
    console.error('AI模特生成失败:', e)
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

function clearCanvas() {
  productImage.value = ''
  generated.value = false
}

function clearChat() {
  aiMessages.value = []
}

function scrollChat() {
  const el = document.querySelector('.ai-chat')
  if (el) el.scrollTop = el.scrollHeight
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

// ========== Step Bar ==========
.step-bar {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #E8EDF5;
  flex-shrink: 0;
  overflow-x: auto;
}

.step-item {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;

  .step-dot {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    background: #F3F4F6;
    color: #9CA3AF;
    flex-shrink: 0;
    transition: all 0.3s;
  }

  .step-label {
    font-size: 12px;
    font-weight: 400;
    color: #9CA3AF;
    margin-left: 4px;
    white-space: nowrap;
    transition: color 0.3s;
  }

  .step-line {
    flex: 1;
    height: 2px;
    background: #F3F4F6;
    margin: 0 6px;
    min-width: 8px;
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
      font-weight: 500;
    }
  }

  &.done {
    .step-dot {
      background: #22C55E;
      color: #fff;
    }
    .step-label {
      color: #6B6B6B;
    }
    .step-line {
      background: #22C55E;
    }
  }
}

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
  padding: 12px;
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
  display: flex;
  align-items: stretch;
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
  padding: 6px 0 0;
  font-size: 11px;
  color: #9CA3AF;
  flex-shrink: 0;
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
  padding: 14px;
  overflow: hidden;
  min-width: 200px;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.ai-header h3 { font-size: 14px; font-weight: 600; margin: 0; }
.ai-clear-btn {
  font-size: 11px; color: #2563FF; background: none; border: none;
  cursor: pointer; text-decoration: underline;
}
.ai-clear-btn:hover { opacity: 0.7; }

.ai-chat {
  background: #F7F9FC;
  border-radius: 10px;
  padding: 12px;
  overflow-y: auto;
  flex: 1;
  margin-bottom: 8px;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-msg {
  display: flex;
  gap: 8px;
}
.chat-msg.bot { flex-direction: row; }
.chat-msg.user { flex-direction: row-reverse; }
.chat-avatar {
  width: 22px; height: 22px; border-radius: 50%;
  background: linear-gradient(135deg, #2563FF, #4F83FF);
  display: flex; align-items: center; justify-content: center;
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
.chat-counter { font-size: 10px; color: #9CA3AF; }
.chat-cost { font-size: 10px; color: #22C55E; }
.chat-send {
  padding: 8px 16px; background: #2563FF; color: #fff;
  border: none; border-radius: 8px; font-size: 12px; cursor: pointer; font-weight: 500;
  white-space: nowrap;
}
.chat-send:hover { opacity: 0.9; }
.chat-send:disabled { opacity: 0.4; cursor: not-allowed; }

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
  .step-bar { padding: 10px 16px; }
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
  .step-bar { display: none; }
  .three-col { flex-direction: column; }
  .canvas-col { flex: 0 0 45vh !important; max-height: 45vh; }
  .config-col { flex: 0 0 auto !important; max-height: 200px; overflow-y: auto; }
  .ai-col { flex: 1 1 auto !important; min-height: 250px; }
}
</style>