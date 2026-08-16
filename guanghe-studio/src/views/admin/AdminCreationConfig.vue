<template>
  <div class="admin-creation-config">
    <!-- 功能开关 -->
    <div class="section-header">
      <div>
        <h3 class="section-title">创作功能管理</h3>
        <p class="section-desc">管理 AI 功能模块的开关状态、背景生成与产品精修的配置参数。</p>
      </div>
    </div>

    <!-- ===== 功能开关面板 ===== -->
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">全局功能开关</span>
          <span class="card-header-desc">独立控制各 AI 功能模块在前台的显示/隐藏</span>
        </div>
      </template>

      <div class="toggle-grid" v-loading="toggleLoading">
        <div v-for="item in toggleList" :key="item.toggleKey" class="toggle-item">
          <div class="toggle-info">
            <div class="toggle-label">
              <el-icon :size="18" class="toggle-icon">
                <component :is="toggleIcon(item.toggleKey)" />
              </el-icon>
              {{ item.toggleName }}
            </div>
            <div class="toggle-desc">{{ item.description || item.remark || toggleDesc(item.toggleKey) }}</div>
          </div>
          <div class="toggle-right">
            <el-switch
              v-model="item._enabled"
              :loading="item._saving"
              :active-value="'1'"
              :inactive-value="'0'"
              inline-prompt
              active-text="开"
              inactive-text="关"
              @change="(val) => handleToggleChange(item, val)"
            />
          </div>
        </div>
      </div>

      <div class="card-footer-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>关闭某功能后，前台对应入口将会隐藏。正在创作中的用户不受影响，已生成的内容可正常查看。</span>
      </div>
    </el-card>

    <!-- ===== 背景生成配置 ===== -->
    <!-- <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">背景生成配置</span>
          <span class="card-header-desc">配置"白底生成背景"功能的可选参数</span>
        </div>
      </template>

      <el-form label-width="110px" label-position="right">
        <el-form-item label="场景列表">
          <div class="tag-input-wrap">
            <el-tag
              v-for="(tag, idx) in sceneList"
              :key="idx"
              closable
              :disable-transitions="false"
              @close="removeScene(idx)"
              size="default"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="sceneInputVisible"
              ref="sceneInputRef"
              v-model="sceneInputValue"
              size="small"
              style="width: 120px"
              @keyup.enter="addScene"
              @blur="addScene"
            />
            <el-button v-else size="small" @click="showSceneInput">+ 添加场景</el-button>
          </div>
        </el-form-item>

        <el-form-item label="光线选项">
          <div class="tag-input-wrap">
            <el-tag
              v-for="(tag, idx) in lightOptions"
              :key="idx"
              closable
              @close="removeLight(idx)"
              size="default"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="lightInputVisible"
              ref="lightInputRef"
              v-model="lightInputValue"
              size="small"
              style="width: 120px"
              @keyup.enter="addLight"
              @blur="addLight"
            />
            <el-button v-else size="small" @click="showLightInput">+ 添加光线</el-button>
          </div>
        </el-form-item>

        <el-form-item label="风格预设">
          <div class="tag-input-wrap">
            <el-tag
              v-for="(tag, idx) in stylePresets"
              :key="idx"
              closable
              @close="removeStyle(idx)"
              size="default"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="styleInputVisible"
              ref="styleInputRef"
              v-model="styleInputValue"
              size="small"
              style="width: 120px"
              @keyup.enter="addStyle"
              @blur="addStyle"
            />
            <el-button v-else size="small" @click="showStyleInput">+ 添加风格</el-button>
          </div>
        </el-form-item>

        <el-form-item label="生图数量上限">
          <el-input-number v-model="bgGenMaxCount" :min="1" :max="20" :step="1" style="width: 180px" />
          <span class="form-item-hint">单次生成的最大数量</span>
        </el-form-item>
      </el-form>

      <div style="margin-top: 16px">
        <el-button type="primary" :loading="bgSaving" @click="saveBgConfig">保存背景生成配置</el-button>
      </div>
    </el-card> -->

    <!-- ===== 产品精修配置 ===== -->
    <!-- <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">产品精修配置</span>
          <span class="card-header-desc">配置"产品精修"功能的工具列表与参数</span>
        </div>
      </template>

      <el-form label-width="130px" label-position="right">
        <el-form-item label="可用工具列表">
          <el-checkbox-group v-model="retouchTools" size="default">
            <el-checkbox label="one-click-repair">一键修复</el-checkbox>
            <el-checkbox label="smart-optimize">智能优化</el-checkbox>
            <el-checkbox label="defect-remove">去瑕疵</el-checkbox>
            <el-checkbox label="texture-enhance">纹理增强</el-checkbox>
            <el-checkbox label="color-adjust">色彩调整</el-checkbox>
            <el-checkbox label="light-optimize">光影优化</el-checkbox>
            <el-checkbox label="bg-process">背景处理</el-checkbox>
            <el-checkbox label="sharpen">锐化增强</el-checkbox>
            <el-checkbox label="denoise">降噪处理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="默认精修强度">
          <el-slider v-model="retouchStrength" :min="10" :max="100" :step="10" show-input show-stops style="max-width: 400px" />
        </el-form-item>

        <el-form-item label="工具参数范围">
          <div class="param-range-grid">
            <div class="param-range-item">
              <span class="param-label">色彩饱和度</span>
              <span class="param-range">{{ retouchColorSatMin }}% ~ {{ retouchColorSatMax }}%</span>
            </div>
            <div class="param-range-item">
              <span class="param-label">锐化强度</span>
              <span class="param-range">{{ retouchSharpenMin }}% ~ {{ retouchSharpenMax }}%</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="生成数量上限">
          <el-input-number v-model="retouchMaxCount" :min="1" :max="20" :step="1" style="width: 180px" />
          <span class="form-item-hint">单次精修生成的最大数量</span>
        </el-form-item>
      </el-form>

      <div style="margin-top: 16px">
        <el-button type="primary" :loading="retouchSaving" @click="saveRetouchConfig">保存产品精修配置</el-button>
      </div>
    </el-card> -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { getFeatureToggleList, updateToggleStatusBatch } from '@/api/operation'
import { getAdminCreationConfigByGroup, addAdminCreationConfig, updateAdminCreationConfig } from '@/api/customer'

// ========== 功能开关 ==========
const toggleLoading = ref(false)
const toggleList = ref([])

const toggleIconMap = {
  white_bg: 'Picture',
  white_to_bg: 'PictureFilled',
  refine: 'MagicStick',
  ai_model: 'User',
  main_image: 'Postcard',
  detail_img: 'Tickets',
  banner: 'Flag',
  size_mark: 'Ruler',
  batch_process: 'Operation'
}

function toggleIcon(key) {
  const k = key.replace(/^feature\./, '')
  return toggleIconMap[k] || 'Setting'
}

function toggleDesc(key) {
  const k = key.replace(/^feature\./, '')
  const map = {
    white_bg: 'AI 白底图 — 智能识别商品主体生成白底图',
    white_to_bg: '白底图生成背景 — 为白底图智能生成场景背景',
    refine: '产品精修 — AI 智能精修商品图片',
    ai_model: 'AI 模特 — 为商品匹配模特场景',
    main_image: '主图设计 — 高转化电商主图生成',
    detail_img: '详情图/A+ 设计 — 电商详情页与 A+ 设计',
    banner: 'Banner 设计 — 电商 Banner 生成',
    size_mark: '尺寸标记 — 自动标注商品尺寸',
    batch_process: '批量生成 — 批量生成多套设计方案'
  }
  return map[k] || ''
}

async function loadToggles() {
  toggleLoading.value = true
  try {
    const res = await getFeatureToggleList()
    if (res.code === 200) {
      toggleList.value = (res.rows || res.data || []).map(item => ({
        ...item,
        _enabled: item.enabled || '1',
        _saving: false
      }))
    } else {
      // Fallback: use default toggles if API not ready
      toggleList.value = defaultToggles()
    }
  } catch {
    toggleList.value = defaultToggles()
  } finally {
    toggleLoading.value = false
  }
}

function defaultToggles() {
  return [
    { toggleKey: 'white_bg', toggleName: 'AI白底图', enabled: '1', _enabled: '1', _saving: false, remark: 'AI 白底图 — 智能识别商品主体生成白底图' },
    { toggleKey: 'white_to_bg', toggleName: '白底生成背景', enabled: '1', _enabled: '1', _saving: false, remark: '白底图生成背景 — 为白底图智能生成场景背景' },
    { toggleKey: 'refine', toggleName: '产品精修', enabled: '1', _enabled: '1', _saving: false, remark: '产品精修 — AI 智能精修商品图片' },
    { toggleKey: 'ai_model', toggleName: 'AI模特', enabled: '1', _enabled: '1', _saving: false, remark: 'AI 模特 — 为商品匹配模特场景' },
    { toggleKey: 'main_image', toggleName: '主图设计', enabled: '1', _enabled: '1', _saving: false, remark: '主图设计 — 高转化电商主图生成' },
    { toggleKey: 'detail_img', toggleName: '详情图/A+', enabled: '1', _enabled: '1', _saving: false, remark: '详情图/A+ 设计 — 电商详情页与 A+ 设计' },
    { toggleKey: 'banner', toggleName: 'Banner设计', enabled: '1', _enabled: '1', _saving: false, remark: 'Banner 设计 — 电商 Banner 生成' },
    { toggleKey: 'size_mark', toggleName: '尺寸标记', enabled: '1', _enabled: '1', _saving: false, remark: '尺寸标记 — 自动标注商品尺寸' },
    { toggleKey: 'batch_process', toggleName: '批量生成', enabled: '1', _enabled: '1', _saving: false, remark: '批量生成 — 批量生成多套设计方案' }
  ]
}

async function handleToggleChange(item, val) {
  item._saving = true
  try {
    const payload = [{ toggleKey: item.toggleKey, enabled: val }]
    const res = await updateToggleStatusBatch(payload)
    if (res.code === 200) {
      item.enabled = val
      ElMessage.success(`${item.toggleName} ${val === '1' ? '已开启' : '已关闭'}`)
    } else {
      // Rollback
      item._enabled = item.enabled
      ElMessage.error(res.msg || '更新失败')
    }
  } catch {
    item._enabled = item.status
    ElMessage.error('操作异常')
  } finally {
    item._saving = false
  }
}

// ========== 背景生成配置 ==========
const bgSaving = ref(false)
const sceneList = ref(['客厅', '卧室', '餐厅', '厨房', '书房', '户外花园', '阳台', '酒店', '办公室', '商场'])
const sceneInputVisible = ref(false)
const sceneInputValue = ref('')
const sceneInputRef = ref(null)

const lightOptions = ref(['自然光', '暖光', '冷光', '柔光', '强光', '侧光', '逆光', '氛围灯'])
const lightInputVisible = ref(false)
const lightInputValue = ref('')
const lightInputRef = ref(null)

const stylePresets = ref(['现代简约', '北欧风', '日式', '工业风', '轻奢', '中式古典', '美式乡村', '地中海', '极简', '复古'])
const styleInputVisible = ref(false)
const styleInputValue = ref('')
const styleInputRef = ref(null)

const bgGenMaxCount = ref(4)
const bgConfigId = ref(null)

function showSceneInput() { sceneInputVisible.value = true; nextTick(() => sceneInputRef.value?.focus()) }
function showLightInput() { lightInputVisible.value = true; nextTick(() => lightInputRef.value?.focus()) }
function showStyleInput() { styleInputVisible.value = true; nextTick(() => styleInputRef.value?.focus()) }

function addScene() {
  const v = sceneInputValue.value.trim()
  if (v && !sceneList.value.includes(v)) sceneList.value.push(v)
  sceneInputVisible.value = false
  sceneInputValue.value = ''
}
function addLight() {
  const v = lightInputValue.value.trim()
  if (v && !lightOptions.value.includes(v)) lightOptions.value.push(v)
  lightInputVisible.value = false
  lightInputValue.value = ''
}
function addStyle() {
  const v = styleInputValue.value.trim()
  if (v && !stylePresets.value.includes(v)) stylePresets.value.push(v)
  styleInputVisible.value = false
  styleInputValue.value = ''
}

function removeScene(idx) { sceneList.value.splice(idx, 1) }
function removeLight(idx) { lightOptions.value.splice(idx, 1) }
function removeStyle(idx) { stylePresets.value.splice(idx, 1) }

async function saveBgConfig() {
  bgSaving.value = true
  try {
    const config = {
      sceneList: sceneList.value,
      lightOptions: lightOptions.value,
      stylePresets: stylePresets.value,
      maxCount: bgGenMaxCount.value
    }
    const payload = {
      configGroup: 'bg_generation',
      configKey: 'config',
      configValue: JSON.stringify(config),
      configName: '背景生成配置',
      sort: 0,
      status: '0'
    }
    if (!bgConfigId.value) {
      try {
        const res = await getAdminCreationConfigByGroup('bg_generation')
        const list = res.data || res.rows || []
        const cfg = list.find(c => c.configKey === 'config')
        if (cfg) bgConfigId.value = cfg.id
      } catch { /* ignore */ }
    }
    if (bgConfigId.value) {
      payload.id = bgConfigId.value
      await updateAdminCreationConfig(payload)
    } else {
      await addAdminCreationConfig(payload)
    }
    ElMessage.success('背景生成配置已保存')
    loadBgConfig()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    bgSaving.value = false
  }
}

// ========== 产品精修配置 ==========
const retouchSaving = ref(false)
const retouchTools = ref(['one-click-repair', 'smart-optimize', 'defect-remove', 'texture-enhance', 'color-adjust', 'light-optimize'])
const retouchStrength = ref(70)
const retouchColorSatMin = ref(0)
const retouchColorSatMax = ref(200)
const retouchSharpenMin = ref(0)
const retouchSharpenMax = ref(150)
const retouchMaxCount = ref(4)
const retouchConfigId = ref(null)

async function saveRetouchConfig() {
  retouchSaving.value = true
  try {
    const config = {
      tools: retouchTools.value,
      strength: retouchStrength.value,
      colorSatMin: retouchColorSatMin.value,
      colorSatMax: retouchColorSatMax.value,
      sharpenMin: retouchSharpenMin.value,
      sharpenMax: retouchSharpenMax.value,
      maxCount: retouchMaxCount.value
    }
    const payload = {
      configGroup: 'retouch',
      configKey: 'config',
      configValue: JSON.stringify(config),
      configName: '产品精修配置',
      sort: 0,
      status: '0'
    }
    if (!retouchConfigId.value) {
      try {
        const res = await getAdminCreationConfigByGroup('retouch')
        const list = res.data || res.rows || []
        const cfg = list.find(c => c.configKey === 'config')
        if (cfg) retouchConfigId.value = cfg.id
      } catch { /* ignore */ }
    }
    if (retouchConfigId.value) {
      payload.id = retouchConfigId.value
      await updateAdminCreationConfig(payload)
    } else {
      await addAdminCreationConfig(payload)
    }
    ElMessage.success('产品精修配置已保存')
    loadRetouchConfig()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    retouchSaving.value = false
  }
}

// ========== Init ==========
async function loadBgConfig() {
  try {
    const res = await getAdminCreationConfigByGroup('bg_generation')
    const list = res.data || res.rows || []
    const cfg = list.find(c => c.configKey === 'config')
    if (cfg && cfg.configValue) {
      const c = JSON.parse(cfg.configValue)
      bgConfigId.value = cfg.id
      if (c.sceneList) sceneList.value = c.sceneList
      if (c.lightOptions) lightOptions.value = c.lightOptions
      if (c.stylePresets) stylePresets.value = c.stylePresets
      if (c.maxCount) bgGenMaxCount.value = c.maxCount
    }
  } catch { /* use defaults */ }
}

async function loadRetouchConfig() {
  try {
    const res = await getAdminCreationConfigByGroup('retouch')
    const list = res.data || res.rows || []
    const cfg = list.find(c => c.configKey === 'config')
    if (cfg && cfg.configValue) {
      const c = JSON.parse(cfg.configValue)
      retouchConfigId.value = cfg.id
      if (c.tools) retouchTools.value = c.tools
      if (c.strength) retouchStrength.value = c.strength
      if (c.colorSatMin !== undefined) retouchColorSatMin.value = c.colorSatMin
      if (c.colorSatMax) retouchColorSatMax.value = c.colorSatMax
      if (c.sharpenMin !== undefined) retouchSharpenMin.value = c.sharpenMin
      if (c.sharpenMax) retouchSharpenMax.value = c.sharpenMax
      if (c.maxCount) retouchMaxCount.value = c.maxCount
    }
  } catch { /* use defaults */ }
}

onMounted(() => {
  loadToggles()
  loadBgConfig()
  loadRetouchConfig()
})
</script>

<style scoped lang="scss">
.admin-creation-config {
  padding: 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.section-header {
  margin-bottom: 20px;
  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 4px 0;
  }
  .section-desc {
    font-size: 13px;
    color: #6b7280;
    margin: 0;
  }
}

.config-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f3f4f6;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  &-title {
    font-size: 15px;
    font-weight: 600;
    color: #111827;
  }
  &-desc {
    font-size: 12px;
    color: #9ca3af;
  }
}

// Toggle grid
.toggle-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 8px;
}

.toggle-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #f0f1f3;
  transition: background 0.15s;
  &:hover {
    background: #f3f5f7;
  }
}

.toggle-info {
  flex: 1;
  min-width: 0;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 2px;
}

.toggle-icon {
  color: #6b7280;
}

.toggle-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 24px;
}

.toggle-right {
  flex-shrink: 0;
  margin-left: 16px;
}

.card-footer-tip {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  padding: 10px 14px;
  background: #fffbeb;
  border-radius: 8px;
  border: 1px solid #fde68a;
  .el-icon {
    color: #f59e0b;
    flex-shrink: 0;
  }
}

// Tag input
.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.form-item-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.param-range-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.param-range-item {
  display: flex;
  align-items: center;
  gap: 12px;
  .param-label {
    font-size: 13px;
    color: #6b7280;
    width: 100px;
  }
  .param-range {
    font-size: 13px;
    color: #111827;
    font-weight: 500;
    padding: 4px 12px;
    background: #f3f4f6;
    border-radius: 6px;
  }
}
</style>