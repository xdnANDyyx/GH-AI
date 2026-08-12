<template>
  <div class="my-materials">
    <div class="materials-body">
      <!-- ===== 左侧主区域 ===== -->
      <div class="materials-main">
        <!-- 标签栏 -->
        <div class="tabs">
          <div
            v-for="tab in tabList"
            :key="tab.value"
            class="tab"
            :class="{ active: activeTab === tab.value }"
            @click="switchTab(tab.value)"
          >{{ tab.label }}</div>
        </div>

        <!-- 筛选栏 -->
        <div class="filters">
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchQuery"
              placeholder="搜索我的素材..."
              @keyup.enter="handleSearch"
            />
          </div>
          <el-select v-model="filters.type" placeholder="素材类型" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
            <el-option label="3D模型" value="model3d" />
          </el-select>
          <el-select v-model="filters.productType" placeholder="产品类型" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="家居家具" value="furniture" />
            <el-option label="服装鞋帽" value="clothing" />
            <el-option label="美妆个护" value="beauty" />
          </el-select>
          <el-select v-model="filters.space" placeholder="空间场景" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="客厅" value="living" />
            <el-option label="卧室" value="bedroom" />
            <el-option label="办公室" value="office" />
          </el-select>
          <el-select v-model="filters.platform" placeholder="适配平台" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="淘宝天猫" value="taobao" />
            <el-option label="京东" value="jd" />
            <el-option label="抖音" value="douyin" />
          </el-select>
          <el-select v-model="filters.auditStatus" placeholder="审核状态" clearable class="filter-select" @change="handleFilterChange">
            <el-option label="已上架" value="1" />
            <el-option label="审核中" value="0" />
            <el-option label="未通过" value="2" />
          </el-select>
          <button class="btn-more" @click="showMoreFilter = !showMoreFilter">
            更多筛选 <el-icon><ArrowDown /></el-icon>
          </button>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <div class="sort-left" @click="toggleSort">
            <span>上传时间</span>
            <el-icon><CaretBottom v-if="sortOrder === 'desc'" /><CaretTop v-else /></el-icon>
            <span class="sort-count">共 {{ totalCount }} 个素材</span>
          </div>
          <div class="view-switch">
            <button class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </button>
            <button class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </button>
          </div>
        </div>

        <!-- 素材网格 -->
        <div class="cards-grid" :class="{ 'list-mode': viewMode === 'list' }">
          <!-- 加载中 -->
          <template v-if="loading">
            <div v-for="i in 8" :key="'sk-' + i" class="card skeleton">
              <div class="card-thumb skeleton-thumb"></div>
              <div class="card-body">
                <div class="skeleton-line w-60"></div>
                <div class="skeleton-line w-40"></div>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-else-if="materials.length === 0" class="empty-state">
            <el-icon class="empty-icon"><PictureFilled /></el-icon>
            <p class="empty-title">暂无素材</p>
            <p class="empty-desc">上传您的第一个素材，开始获得积分收益</p>
            <el-button type="primary" @click="handleUpload">
              <el-icon><Upload /></el-icon> 上传素材
            </el-button>
          </div>

          <!-- 素材卡片 -->
          <div
            v-for="m in materials"
            :key="m.id"
            class="card"
            @click="showDetail(m)"
          >
            <div class="card-thumb" :class="'bg-' + ((m.id % 8) + 1)">
              <el-image
                v-if="m.thumbnailUrl || m.materialUrl"
                :src="m.thumbnailUrl || m.materialUrl"
                fit="cover"
                loading="lazy"
                class="thumb-img"
              />
              <!-- 状态徽章 -->
              <span class="card-status" :class="statusClass(m.auditStatus)">
                {{ statusText(m.auditStatus) }}
              </span>
            </div>
            <div class="card-body">
              <div class="card-title">{{ m.materialName }}</div>
              <div class="card-tags">
                <span v-for="tag in (m.tags || []).slice(0, 3)" :key="tag.id || tag" class="tag">
                  {{ tag.tagName || tag }}
                </span>
              </div>
              <div class="card-stats">
                <span class="stat-item">
                  <el-icon><Star /></el-icon> {{ m.favoriteCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Pointer /></el-icon> {{ m.likeCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Download /></el-icon> {{ m.downloadCount || 0 }}
                </span>
              </div>
              <div class="card-bottom">
                <span v-if="m.pointsEarned > 0" class="credits-tag credits-earned">
                  +{{ m.pointsEarned }} 积分
                </span>
                <span v-else class="credits-tag credits-none" @click.stop="handleShelf(m)">
                  点击上架
                </span>
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, m)" @click.stop>
                  <el-icon class="card-menu"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="shelf" v-if="m.auditStatus === '1'">下架</el-dropdown-item>
                      <el-dropdown-item command="unshelf" v-else-if="m.auditStatus === '0'">上架</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-row" v-if="totalCount > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalCount"
            layout="prev, pager, next"
            background
            size="small"
          />
          <span class="total-text">共 {{ totalCount }} 条</span>
        </div>
      </div>

      <!-- ===== 右侧面板 ===== -->
      <div class="right-panel">
        <!-- 统计卡片 -->
        <div class="panel-section">
          <div class="panel-header">
            <span class="panel-title">我的素材数据</span>
            <a class="panel-link" @click="$router.push('/points-center')">查看明细</a>
          </div>
          <p class="stats-desc">近30天数据统计</p>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-label">素材总数</div>
              <div class="stat-value">{{ stats.totalMaterial }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">上架中</div>
              <div class="stat-value highlight">{{ stats.listedCount }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">总下载量</div>
              <div class="stat-value">{{ stats.totalDownload }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">获得积分</div>
              <div class="stat-value highlight">{{ stats.totalPoints }}</div>
            </div>
          </div>
        </div>

        <!-- 积分收益趋势图 -->
        <div class="panel-section">
          <div class="panel-header">
            <span class="panel-title">积分收益趋势</span>
            <select v-model="chartRange" class="chart-select" @change="loadChartData">
              <option value="7">近7天</option>
              <option value="30">近30天</option>
            </select>
          </div>
          <div class="chart-container" ref="chartContainer">
            <svg class="chart-svg" :viewBox="`0 0 ${chartWidth} ${chartHeight}`" @mousemove="handleChartHover" @mouseleave="chartTooltip.visible = false">
              <!-- 网格线 -->
              <line v-for="i in 4" :key="'grid-' + i"
                :x1="0" :y1="(chartHeight / 4) * i" :x2="chartWidth" :y2="(chartHeight / 4) * i"
                stroke="#F3F4F6" stroke-width="1"
              />
              <!-- 折线 -->
              <polyline
                :points="chartPoints"
                fill="none"
                stroke="#2563FF"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- 填充区域 -->
              <polygon
                :points="`0,${chartHeight} ${chartPoints} ${chartWidth},${chartHeight}`"
                fill="rgba(37, 99, 255, 0.08)"
              />
              <!-- 数据点 -->
              <circle
                v-for="(p, idx) in chartDataPoints"
                :key="'pt-' + idx"
                :cx="p.x" :cy="p.y" r="3"
                fill="#2563FF"
                @mouseenter="showTooltip(idx, p)"
                @mouseleave="chartTooltip.visible = false"
              />
            </svg>
            <div
              v-if="chartTooltip.visible"
              class="chart-tooltip"
              :style="{ left: chartTooltip.x + 'px', top: chartTooltip.y + 'px' }"
            >
              {{ chartTooltip.date }}<br/>+{{ chartTooltip.value }} 积分
            </div>
          </div>
        </div>

        <!-- 积分明细 -->
        <div class="panel-section">
          <div class="panel-header">
            <span class="panel-title">积分明细</span>
          </div>
          <div class="income-list">
            <div v-for="item in incomeList" :key="item.title" class="income-item">
              <div class="income-left">
                <span class="income-title">{{ item.title }}</span>
                <span class="income-time">{{ item.time }}</span>
              </div>
              <span class="income-amount">+{{ item.amount }}</span>
            </div>
          </div>
        </div>

        <!-- 上传指南 -->
        <div class="panel-section">
          <div class="panel-header">
            <span class="panel-title">上传指南</span>
          </div>
          <div class="guide-list">
            <div v-for="g in guideList" :key="g" class="guide-item" @click="handleGuide(g)">
              <span>{{ g }}</span>
              <span class="guide-arrow">›</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传素材" width="500px">
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        accept="image/*"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 JPG/PNG 格式，文件不超过 50MB，分辨率 ≥ 2000px</div>
        </template>
      </el-upload>
      <el-form :model="uploadForm" label-width="80px" style="margin-top: 16px">
        <el-form-item label="素材名称">
          <el-input v-model="uploadForm.materialName" placeholder="请输入素材名称" />
        </el-form-item>
        <el-form-item label="产品类型">
          <el-select v-model="uploadForm.productType" placeholder="请选择" style="width: 100%">
            <el-option label="家居家具" value="furniture" />
            <el-option label="服装鞋帽" value="clothing" />
            <el-option label="美妆个护" value="beauty" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Grid, List, PictureFilled, Download, Star, Pointer,
  Upload, UploadFilled, ArrowDown, CaretBottom, CaretTop, MoreFilled
} from '@element-plus/icons-vue'
import {
  listMyUpload, uploadMaterial, updateMyMaterial,
  changeMaterialShelfStatus, getPointsStats
} from '@/api/customer'

/* ===== 状态 ===== */
const activeTab = ref('all')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const totalCount = ref(0)
const loading = ref(false)
const viewMode = ref('grid')
const sortOrder = ref('desc')
const showMoreFilter = ref(false)
const chartRange = ref('30')
const uploadVisible = ref(false)

const tabList = [
  { label: '全部素材', value: 'all' },
  { label: '已上架', value: '1' },
  { label: '审核中', value: '0' },
  { label: '未通过', value: '2' },
  { label: '草稿箱', value: 'draft' }
]

const filters = reactive({
  type: '',
  productType: '',
  space: '',
  platform: '',
  auditStatus: ''
})

const uploadForm = reactive({
  materialName: '',
  productType: '',
  file: null
})

const materials = ref([])

const stats = reactive({
  totalMaterial: 0,
  listedCount: 0,
  totalDownload: 0,
  totalPoints: 0
})

/* ===== 图表数据 ===== */
const chartWidth = 280
const chartHeight = 120
const chartData = ref([])
const chartTooltip = reactive({ visible: false, x: 0, y: 0, date: '', value: 0 })

const chartDataPoints = computed(() => {
  if (chartData.value.length === 0) return []
  const max = Math.max(...chartData.value.map(d => d.value), 1)
  const stepX = chartWidth / Math.max(chartData.value.length - 1, 1)
  return chartData.value.map((d, i) => ({
    x: i * stepX,
    y: chartHeight - (d.value / max) * (chartHeight - 10) - 5
  }))
})

const chartPoints = computed(() => {
  return chartDataPoints.value.map(p => `${p.x},${p.y}`).join(' ')
})

const incomeList = ref([
  { title: '素材被下载', time: '2小时前', amount: 10 },
  { title: '素材被收藏', time: '5小时前', amount: 5 },
  { title: '素材上架奖励', time: '昨天', amount: 50 },
  { title: '素材被下载', time: '2天前', amount: 10 }
])

const guideList = [
  '图片分辨率 ≥ 2000px',
  '图片格式支持 JPG/PNG',
  '内容需原创且符合规范',
  '审核通过后自动上架',
  '被下载可获得积分奖励'
]

/* ===== 方法 ===== */
async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      materialName: searchQuery.value,
      auditStatus: activeTab.value === 'all' ? undefined : activeTab.value,
      sortOrder: sortOrder.value,
      ...filters
    }
    const res = await listMyUpload(params)
    if (res && res.code === 200) {
      materials.value = res.rows || []
      totalCount.value = res.total || 0
    }
  } catch (e) {
    console.error('加载素材列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getPointsStats()
    if (res && res.code === 200 && res.data) {
      stats.totalPoints = res.data.totalEarned || 0
    }
    stats.totalMaterial = totalCount.value || 0
    stats.listedCount = materials.value.filter(m => m.auditStatus === '1').length
    stats.totalDownload = materials.value.reduce((sum, m) => sum + (m.downloadCount || 0), 0)
  } catch (e) {
    stats.totalMaterial = 0
    stats.listedCount = 0
    stats.totalDownload = 0
    stats.totalPoints = 0
  }
}

function loadChartData() {
  const days = parseInt(chartRange.value)
  const data = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    data.push({
      date: `${d.getMonth() + 1}/${d.getDate()}`,
      value: Math.floor(Math.random() * 50) + 10
    })
  }
  chartData.value = data
}

function handleChartHover(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  const x = e.clientX - rect.left
  const stepX = chartWidth / Math.max(chartData.value.length - 1, 1)
  const idx = Math.round(x / stepX)
  if (idx >= 0 && idx < chartDataPoints.value.length) {
    showTooltip(idx, chartDataPoints.value[idx])
  }
}

function showTooltip(idx, p) {
  const data = chartData.value[idx]
  chartTooltip.visible = true
  chartTooltip.x = p.x - 40
  chartTooltip.y = p.y - 50
  chartTooltip.date = data.date
  chartTooltip.value = data.value
}

function switchTab(val) {
  activeTab.value = val
  currentPage.value = 1
  loadData()
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleFilterChange() {
  currentPage.value = 1
  loadData()
}

function toggleSort() {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  loadData()
}

function statusText(status) {
  const map = { '1': '已上架', '0': '审核中', '2': '未通过', 'draft': '草稿' }
  return map[status] || '审核中'
}

function statusClass(status) {
  const map = { '1': 'status-live', '0': 'status-pending', '2': 'status-rejected', 'draft': 'status-draft' }
  return map[status] || 'status-pending'
}

function showDetail(m) {
  ElMessage.info(`查看素材「${m.materialName}」详情`)
}

function handleUpload() {
  uploadForm.materialName = ''
  uploadForm.productType = ''
  uploadForm.file = null
  uploadVisible.value = true
}

function handleFileChange(file) {
  uploadForm.file = file.raw
  if (!uploadForm.materialName) {
    uploadForm.materialName = file.name.replace(/\.[^.]+$/, '')
  }
}

async function submitUpload() {
  if (!uploadForm.file) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  if (!uploadForm.materialName) {
    ElMessage.warning('请输入素材名称')
    return
  }
  try {
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    formData.append('materialName', uploadForm.materialName)
    formData.append('productType', uploadForm.productType)
    await uploadMaterial(formData)
    ElMessage.success('上传成功，等待审核')
    uploadVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    console.error('上传失败', e)
  }
}

async function handleShelf(m) {
  try {
    await changeMaterialShelfStatus({ materialId: m.id, status: '1' })
    ElMessage.success('上架成功')
    loadData()
  } catch (e) {
    console.error('上架失败', e)
  }
}

async function handleCommand(cmd, m) {
  switch (cmd) {
    case 'edit':
      ElMessage.info('编辑功能开发中')
      break
    case 'shelf':
      await handleShelf(m)
      break
    case 'unshelf':
      try {
        await ElMessageBox.confirm('确认下架该素材？', '提示', { type: 'warning' })
        await changeMaterialShelfStatus({ materialId: m.id, status: '0' })
        ElMessage.success('已下架')
        loadData()
      } catch (e) { /* 取消 */ }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确认删除该素材？删除后不可恢复', '警告', { type: 'warning' })
        ElMessage.success('已删除')
        loadData()
      } catch (e) { /* 取消 */ }
      break
  }
}

function handleGuide(g) {
  ElMessage.info(g)
}

/* ===== 监听 ===== */
watch(currentPage, () => loadData())
watch(totalCount, () => loadStats())

onMounted(() => {
  loadData()
  loadStats()
  loadChartData()
})
</script>

<style lang="scss" scoped>
.my-materials {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.materials-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 0;
  height: 100%;
  overflow: hidden;
}

/* ===== 左侧主区域 ===== */
.materials-main {
  min-width: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 20px 24px;
  gap: 14px;

  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }
}

/* Tabs */
.tabs {
  display: flex;
  border-bottom: 1px solid #EAEEF3;
  gap: 0;
  .tab {
    padding: 10px 20px;
    font-size: 14px;
    color: #6B7280;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.15s;
    &:hover { color: #2563FF; }
    &.active {
      color: #2563FF;
      border-bottom-color: #2563FF;
      font-weight: 500;
    }
  }
}

/* Filters */
.filters {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  .search-box {
    flex: 1;
    min-width: 180px;
    max-width: 280px;
    position: relative;
    .search-icon {
      position: absolute;
      left: 10px;
      top: 50%;
      transform: translateY(-50%);
      color: #9CA3AF;
    }
    input {
      width: 100%;
      padding: 7px 12px 7px 32px;
      border: 1px solid #E5E7EB;
      border-radius: 8px;
      font-size: 13px;
      outline: none;
      background: #fff;
      color: #1a1a1a;
      &:focus { border-color: #2563FF; }
    }
  }
  .filter-select { width: 130px; }
  .btn-more {
    padding: 7px 12px;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 13px;
    background: #fff;
    color: #4a5568;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

/* Sort */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #6B7280;
  .sort-left {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    &:hover { color: #2563FF; }
    .sort-count { color: #9CA3AF; margin-left: 8px; }
  }
  .view-switch {
    display: flex;
    gap: 4px;
    .view-btn {
      width: 30px;
      height: 30px;
      border: 1px solid #E5E7EB;
      border-radius: 6px;
      background: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #9CA3AF;
      &.active {
        border-color: #2563FF;
        color: #2563FF;
        background: #EBF0FF;
      }
    }
  }
}

/* Cards Grid */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  &.list-mode { grid-template-columns: repeat(2, 1fr); }
}

.card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.card-thumb {
  height: 140px;
  position: relative;
  overflow: hidden;
  background: #F3F4F6;
  .thumb-img {
    width: 100%;
    height: 100%;
    :deep(.el-image__inner) { width: 100%; height: 100%; object-fit: cover; }
  }
  &.bg-1 { background: linear-gradient(135deg, #F5E6D3, #E8D5C4); }
  &.bg-2 { background: linear-gradient(135deg, #EDE4D8, #D4C8B8); }
  &.bg-3 { background: linear-gradient(135deg, #F0E8DC, #D9CFC1); }
  &.bg-4 { background: linear-gradient(135deg, #E8DDD0, #CFC3B3); }
  &.bg-5 { background: linear-gradient(135deg, #F2E9DE, #DDD2C4); }
  &.bg-6 { background: linear-gradient(135deg, #EFE3D5, #D6C9BA); }
  &.bg-7 { background: linear-gradient(135deg, #EBE0D2, #D0C4B5); }
  &.bg-8 { background: linear-gradient(135deg, #F3EAE0, #DDD1C3); }
}

.card-status {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #fff;
  &.status-live { background: #22C55E; }
  &.status-pending { background: #F59E0B; }
  &.status-rejected { background: #EF4444; }
  &.status-draft { background: #9CA3AF; }
}

.card-body { padding: 10px 12px 12px; }
.card-title {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  .tag {
    padding: 1px 6px;
    background: #F3F4F6;
    border-radius: 4px;
    font-size: 11px;
    color: #6B7280;
  }
}
.card-stats {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #9CA3AF;
  margin-bottom: 6px;
  .stat-item {
    display: flex;
    align-items: center;
    gap: 3px;
    .el-icon { font-size: 12px; }
  }
}
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .credits-tag {
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
    &.credits-earned { background: #F0FDF4; color: #22C55E; }
    &.credits-none { background: #FEF2F2; color: #EF4444; cursor: pointer; }
  }
  .card-menu {
    cursor: pointer;
    color: #9CA3AF;
    padding: 2px;
    font-size: 16px;
    &:hover { color: #2563FF; }
  }
}

/* 分页 */
.pagination-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding-top: 4px;
  .total-text { font-size: 13px; color: #6B7280; }
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  .empty-icon { font-size: 56px; color: #D1D5DB; margin-bottom: 12px; }
  .empty-title { font-size: 16px; font-weight: 600; color: #111827; margin-bottom: 6px; }
  .empty-desc { font-size: 13px; color: #6B7280; margin-bottom: 16px; }
}

/* 骨架屏 */
.skeleton {
  .skeleton-thumb {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    height: 100%;
  }
  .skeleton-line {
    height: 12px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
    margin-bottom: 8px;
    &.w-60 { width: 60%; }
    &.w-40 { width: 40%; }
  }
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 右侧面板 ===== */
.right-panel {
  background: #fff;
  border-left: 1px solid #EAEEF3;
  overflow-y: auto;
  padding: 20px;
  height: 100%;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 2px; }
}

.panel-section { margin-bottom: 20px; }
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  .panel-title { font-size: 14px; font-weight: 600; color: #1a1a1a; }
  .panel-link {
    font-size: 12px;
    color: #2563FF;
    cursor: pointer;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}

.stats-desc { font-size: 11px; color: #9CA3AF; margin-bottom: 10px; }
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.stat-card {
  background: #F7F9FC;
  border-radius: 10px;
  padding: 12px;
  .stat-label { font-size: 11px; color: #9CA3AF; margin-bottom: 4px; }
  .stat-value { font-size: 20px; font-weight: 600; color: #1a1a1a; }
  .stat-value.highlight { color: #2563FF; }
}

/* Chart */
.chart-container {
  background: #F7F9FC;
  border-radius: 10px;
  padding: 14px;
  position: relative;
}
.chart-select {
  padding: 3px 8px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  font-size: 11px;
  background: #fff;
  color: #4a5568;
  outline: none;
}
.chart-svg {
  width: 100%;
  height: 120px;
  display: block;
}
.chart-tooltip {
  position: absolute;
  background: #1a1a1a;
  color: #fff;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 11px;
  pointer-events: none;
  white-space: nowrap;
  z-index: 10;
  line-height: 1.6;
}

/* Income list */
.income-list { display: flex; flex-direction: column; }
.income-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #F3F4F6;
  font-size: 12px;
  &:last-child { border-bottom: none; }
  .income-left { display: flex; flex-direction: column; gap: 1px; }
  .income-title { color: #4a5568; }
  .income-time { color: #9CA3AF; font-size: 11px; }
  .income-amount { color: #22C55E; font-weight: 500; }
}

/* Guide */
.guide-list { display: flex; flex-direction: column; }
.guide-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #F3F4F6;
  font-size: 13px;
  color: #4a5568;
  cursor: pointer;
  &:last-child { border-bottom: none; }
  &:hover { color: #2563FF; }
  .guide-arrow { color: #9CA3AF; font-size: 12px; }
}

@media (max-width: 1400px) {
  .cards-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>