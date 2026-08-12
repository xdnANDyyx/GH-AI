<template>
  <div class="material-plaza">
    <div class="plaza-body">
      <!-- ===== 左侧主区域 ===== -->
      <div class="plaza-main">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchQuery"
            placeholder="搜索素材名称、标签、作者..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <el-tooltip content="以图搜图" placement="top">
            <el-icon class="camera-btn" @click="handleImageSearch"><Camera /></el-icon>
          </el-tooltip>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-row">
          <el-select
            v-model="filters.productType"
            placeholder="产品分类"
            clearable
            class="filter-select"
            @change="handleFilterChange"
          >
            <el-option v-for="o in productTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-select
            v-model="filters.space"
            placeholder="空间分类"
            clearable
            class="filter-select"
            @change="handleFilterChange"
          >
            <el-option v-for="o in spaceOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-select
            v-model="filters.style"
            placeholder="风格标签"
            clearable
            class="filter-select"
            @change="handleFilterChange"
          >
            <el-option v-for="o in styleOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-select
            v-model="filters.platform"
            placeholder="平台适配"
            clearable
            class="filter-select"
            @change="handleFilterChange"
          >
            <el-option v-for="o in platformOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </div>

        <!-- 排序栏 -->
        <div class="sort-bar">
          <div class="sort-left">
            <span
              class="sort-item"
              :class="{ active: sortBy === 'recommend' }"
              @click="changeSort('recommend')"
            >推荐</span>
            <span
              class="sort-item"
              :class="{ active: sortBy === 'newest' }"
              @click="changeSort('newest')"
            >最新</span>
            <span class="sort-count">共 {{ totalCount }} 个素材</span>
          </div>
          <div class="view-toggle">
            <button
              class="view-btn"
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
              title="网格视图"
            >
              <el-icon><Grid /></el-icon>
            </button>
            <button
              class="view-btn"
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
              title="列表视图"
            >
              <el-icon><List /></el-icon>
            </button>
          </div>
        </div>

        <!-- 素材网格 -->
        <div class="card-grid" :class="{ 'list-mode': viewMode === 'list' }">
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
            <p class="empty-desc">素材广场暂无公开素材，请稍后再来</p>
          </div>

          <!-- 素材卡片 -->
          <div
            v-for="m in materials"
            :key="m.id"
            class="card"
            :class="{ selected: selectedMaterial?.id === m.id }"
            @click="selectMaterial(m)"
          >
            <div class="card-thumb">
              <el-image
                v-if="m.thumbnailUrl || m.materialUrl"
                :src="m.thumbnailUrl || m.materialUrl"
                fit="cover"
                loading="lazy"
                class="thumb-img"
              />
              <el-icon v-else class="thumb-placeholder"><PictureFilled /></el-icon>
              <!-- 标签 -->
              <span v-if="m.tagType === 'recommend'" class="card-tag recommend">推荐</span>
              <span v-else-if="m.tagType === 'premium'" class="card-tag premium">优选</span>
              <span v-else-if="m.tagType === 'hot'" class="card-tag hot">热门</span>
              <span v-else-if="m.tagType === 'new'" class="card-tag new-tag">新品</span>
              <!-- 收藏 -->
              <div class="card-fav" @click.stop="toggleFavorite(m)">
                <el-icon :class="{ 'is-fav': m.isFavorited }">
                  <StarFilled v-if="m.isFavorited" />
                  <Star v-else />
                </el-icon>
              </div>
            </div>
            <div class="card-body">
              <div class="card-title">{{ m.materialName }}</div>
              <div class="card-author">
                <el-avatar :size="18" :src="m.authorAvatar" class="author-avatar">
                  {{ (m.authorName || '匿名')[0] }}
                </el-avatar>
                <span class="author-name">{{ m.authorName || '官方素材' }}</span>
                <el-icon v-if="m.verified" class="author-badge"><CircleCheckFilled /></el-icon>
              </div>
              <div class="card-tags-row">
                <span v-for="tag in (m.tags || []).slice(0, 3)" :key="tag.id || tag" class="card-tag-item">
                  {{ tag.tagName || tag }}
                </span>
              </div>
              <div class="card-meta">
                <div class="card-match">
                  <span class="match-pill">匹配 <strong>{{ m.matchScore || 95 }}%</strong></span>
                </div>
                <div class="card-likes">
                  <el-icon><Pointer /></el-icon>
                  {{ m.favoriteCount || 0 }}
                </div>
                <div class="card-price" :class="m.pointsCost > 0 ? 'paid' : 'free'">
                  {{ m.pointsCost > 0 ? m.pointsCost + '积分' : '免费' }}
                </div>
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

      <!-- ===== 右侧预览面板 ===== -->
      <div class="preview-panel">
        <template v-if="selectedMaterial">
          <!-- 主图区 -->
          <div class="preview-top">
            <div class="preview-main-img">
              <el-image
                v-if="selectedMaterial.materialUrl || selectedMaterial.thumbnailUrl"
                :src="selectedMaterial.materialUrl || selectedMaterial.thumbnailUrl"
                fit="contain"
                class="main-img"
                :preview-src-list="[selectedMaterial.materialUrl || selectedMaterial.thumbnailUrl]"
                :preview-teleported="true"
              />
              <el-icon v-else class="preview-placeholder"><PictureFilled /></el-icon>
            </div>
            <!-- 缩略图列表 -->
            <div class="preview-thumbs" v-if="previewImages.length > 1">
              <div
                v-for="(img, idx) in previewImages"
                :key="idx"
                class="preview-thumb"
                :class="{ active: idx === activePreviewIdx }"
                @click="activePreviewIdx = idx"
              >
                <el-image :src="img" fit="cover" />
              </div>
            </div>
          </div>

          <!-- 信息区 -->
          <div class="preview-info">
            <div class="preview-title-row">
              <h3 class="preview-title">{{ selectedMaterial.materialName }}</h3>
              <el-icon class="preview-fav" @click="toggleFavorite(selectedMaterial)">
                <StarFilled v-if="selectedMaterial.isFavorited" />
                <Star v-else />
              </el-icon>
            </div>
            <div class="preview-author">
              <el-avatar :size="24" :src="selectedMaterial.authorAvatar">
                {{ (selectedMaterial.authorName || '匿名')[0] }}
              </el-avatar>
              <span class="preview-author-name">{{ selectedMaterial.authorName || '官方素材' }}</span>
              <el-icon v-if="selectedMaterial.verified" class="preview-author-badge"><CircleCheckFilled /></el-icon>
            </div>
            <div class="preview-date">{{ formatDate(selectedMaterial.createTime) }}</div>
            <div class="preview-actions">
              <button class="btn-download" @click="handleDownload(selectedMaterial)">
                <el-icon><Download /></el-icon> 下载
              </button>
              <button class="btn-add-project" @click="handleAddToProject(selectedMaterial)">
                加入项目
              </button>
            </div>
          </div>

          <!-- AI智能识别 -->
          <div class="ai-section">
            <div class="ai-section-title">
              <el-icon><MagicStick /></el-icon> AI智能识别
            </div>
            <div class="ai-list">
              <div v-for="ai in aiRecognitionList" :key="ai.label" class="ai-item">
                <div class="ai-item-left">
                  <div class="ai-item-icon" :style="{ background: ai.bg }">
                    <el-icon><component :is="ai.icon" /></el-icon>
                  </div>
                  <span class="ai-item-label">{{ ai.label }}</span>
                </div>
                <span class="ai-item-value">{{ ai.value }}</span>
              </div>
            </div>
          </div>

          <!-- 匹配度分析 -->
          <div class="ai-section">
            <div class="ai-section-title">
              <el-icon><DataAnalysis /></el-icon> 匹配度分析
            </div>
            <div class="match-cards">
              <div class="match-card">
                <div class="match-card-num">{{ selectedMaterial.styleMatch || 92 }}%</div>
                <div class="match-card-label">风格匹配</div>
              </div>
              <div class="match-card">
                <div class="match-card-num">{{ selectedMaterial.sceneMatch || 88 }}%</div>
                <div class="match-card-label">场景匹配</div>
              </div>
              <div class="match-card">
                <div class="match-card-num">{{ selectedMaterial.qualityMatch || 96 }}%</div>
                <div class="match-card-label">质量评分</div>
              </div>
            </div>
          </div>

          <!-- AI推荐理由 -->
          <div class="ai-recommend">
            <div class="ai-recommend-title">
              <el-icon><ChatDotRound /></el-icon> AI推荐理由
            </div>
            <div class="ai-recommend-text">
              {{ selectedMaterial.aiRecommend || '该素材风格精致，场景搭配协调，适合用于电商主图和详情页设计，能有效提升商品点击率。' }}
            </div>
          </div>

          <!-- 举报 -->
          <div class="preview-report">
            <a @click="handleReport(selectedMaterial)">举报该素材</a>
          </div>
        </template>

        <!-- 未选择素材时的占位 -->
        <div v-else class="preview-empty">
          <el-icon class="preview-empty-icon"><PictureFilled /></el-icon>
          <p>点击左侧素材卡片查看详情</p>
          <p class="preview-empty-desc">AI智能识别 + 匹配度分析</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Grid, List, PictureFilled, Download, Star, StarFilled,
  Camera, CircleCheckFilled, Pointer, MagicStick, DataAnalysis,
  ChatDotRound, Picture, Monitor, House, Brush
} from '@element-plus/icons-vue'
import { listOfficialMaterials, favoriteMaterial, cancelFavoriteMaterial } from '@/api/customer'
import { useUserStore } from '@/store'

const userStore = useUserStore()

/* ===== 状态 ===== */
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const totalCount = ref(0)
const loading = ref(false)
const viewMode = ref('grid')
const sortBy = ref('recommend')
const selectedMaterial = ref(null)
const activePreviewIdx = ref(0)

const filters = reactive({
  productType: '',
  space: '',
  style: '',
  platform: ''
})

const materials = ref([])

/* ===== 筛选选项 ===== */
const productTypeOptions = [
  { label: '家居家具', value: 'furniture' },
  { label: '服装鞋帽', value: 'clothing' },
  { label: '美妆个护', value: 'beauty' },
  { label: '食品饮料', value: 'food' },
  { label: '数码电器', value: 'digital' }
]
const spaceOptions = [
  { label: '客厅', value: 'living' },
  { label: '卧室', value: 'bedroom' },
  { label: '厨房', value: 'kitchen' },
  { label: '办公室', value: 'office' },
  { label: '户外', value: 'outdoor' }
]
const styleOptions = [
  { label: '简约现代', value: 'modern' },
  { label: '北欧风', value: 'nordic' },
  { label: '中式', value: 'chinese' },
  { label: '日式', value: 'japanese' },
  { label: '工业风', value: 'industrial' }
]
const platformOptions = [
  { label: '淘宝天猫', value: 'taobao' },
  { label: '京东', value: 'jd' },
  { label: '拼多多', value: 'pdd' },
  { label: '抖音', value: 'douyin' },
  { label: '小红书', value: 'xhs' }
]

/* ===== AI识别列表 ===== */
const aiRecognitionList = computed(() => {
  const m = selectedMaterial.value
  if (!m) return []
  return [
    { label: '产品类型', value: m.productType || '家居家具', icon: 'Picture', bg: '#EBF0FF' },
    { label: '空间场景', value: m.spaceScene || '客厅', icon: 'House', bg: '#F0FDF4' },
    { label: '风格标签', value: m.styleTag || '简约现代', icon: 'Brush', bg: '#FEF3C7' },
    { label: '适配平台', value: m.platform || '淘宝天猫', icon: 'Monitor', bg: '#FCE7F3' },
    { label: '分辨率', value: m.resolution || '2000×2000', icon: 'Monitor', bg: '#E0E7FF' },
    { label: '主色调', value: m.mainColor || '暖白色', icon: 'Brush', bg: '#F3F4F6' }
  ]
})

const previewImages = computed(() => {
  const m = selectedMaterial.value
  if (!m) return []
  const imgs = []
  if (m.materialUrl) imgs.push(m.materialUrl)
  if (m.thumbnailUrl && m.thumbnailUrl !== m.materialUrl) imgs.push(m.thumbnailUrl)
  return imgs
})

/* ===== 方法 ===== */
async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      materialName: searchQuery.value,
      status: '0',
      ...filters
    }
    const res = await listOfficialMaterials(params)
    if (res && res.code === 200) {
      materials.value = res.rows || []
      totalCount.value = res.total || 0
      if (materials.value.length > 0 && !selectedMaterial.value) {
        selectMaterial(materials.value[0])
      }
    }
  } catch (e) {
    console.error('加载素材列表失败', e)
  } finally {
    loading.value = false
  }
}

function selectMaterial(m) {
  selectedMaterial.value = m
  activePreviewIdx.value = 0
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleFilterChange() {
  currentPage.value = 1
  loadData()
}

function changeSort(val) {
  sortBy.value = val
  loadData()
}

function handleImageSearch() {
  ElMessage.info('以图搜图功能开发中')
}

async function toggleFavorite(material) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (material.isFavorited) {
      await cancelFavoriteMaterial({ materialId: material.id })
      material.isFavorited = false
      material.favoriteCount = Math.max(0, (material.favoriteCount || 0) - 1)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteMaterial({ materialId: material.id })
      material.isFavorited = true
      material.favoriteCount = (material.favoriteCount || 0) + 1
      ElMessage.success('已收藏')
    }
  } catch (e) {
    console.error('收藏操作失败', e)
  }
}

function handleDownload(m) {
  if (!m.materialUrl && !m.thumbnailUrl) {
    ElMessage.warning('暂无下载链接')
    return
  }
  const url = m.materialUrl || m.thumbnailUrl
  const link = document.createElement('a')
  link.href = url
  link.download = m.materialName || '素材'
  link.target = '_blank'
  link.click()
}

function handleAddToProject(m) {
  ElMessage.success(`已将「${m.materialName}」加入项目`)
}

function handleReport(m) {
  ElMessage.info(`已提交举报，我们将尽快审核「${m.materialName}」`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/* ===== 监听 ===== */
watch(currentPage, () => loadData())
watch(searchQuery, () => { currentPage.value = 1 })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.material-plaza {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.plaza-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 0;
  height: 100%;
  overflow: hidden;
}

/* ===== 左侧主区域 ===== */
.plaza-main {
  min-width: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px 24px;

  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 0 12px;
  height: 40px;
  gap: 8px;

  .search-icon { color: #9CA3AF; font-size: 18px; }
  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 14px;
    color: #111827;
    background: transparent;
    &::placeholder { color: #9CA3AF; }
  }
  .camera-btn {
    cursor: pointer;
    font-size: 18px;
    color: #9CA3AF;
    &:hover { color: #2563FF; }
  }
}

/* 筛选栏 */
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  .filter-select {
    width: 140px;
  }
}

/* 排序栏 */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sort-left {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #6B7280;
  .sort-item {
    cursor: pointer;
    font-weight: 500;
    &.active { color: #2563FF; font-weight: 600; }
    &:hover { color: #2563FF; }
  }
  .sort-count { color: #9CA3AF; font-size: 12px; }
}
.view-toggle {
  display: flex;
  gap: 4px;
  .view-btn {
    background: none;
    border: 1px solid #E5E7EB;
    border-radius: 6px;
    padding: 5px 7px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    &.active { background: #EBF0FF; border-color: #2563FF; }
    .el-icon { font-size: 16px; color: #6B7280; }
    &.active .el-icon { color: #2563FF; }
  }
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  &.list-mode {
    grid-template-columns: repeat(2, 1fr);
  }
}

.card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #F0F0F0;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
  &.selected {
    border-color: #2563FF;
    box-shadow: 0 0 0 2px rgba(37, 99, 255, 0.15);
  }
}

.card-thumb {
  position: relative;
  height: 140px;
  overflow: hidden;
  background: #F3F4F6;
  .thumb-img {
    width: 100%;
    height: 100%;
    :deep(.el-image__inner) { width: 100%; height: 100%; object-fit: cover; }
  }
  .thumb-placeholder {
    font-size: 36px;
    color: #D1D5DB;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.card-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  &.recommend { background: #22C55E; }
  &.premium { background: #8B5CF6; }
  &.hot { background: #F97316; }
  &.new-tag { background: #F59E0B; }
}

.card-fav {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  .el-icon { font-size: 16px; color: #9CA3AF; }
  .el-icon.is-fav { color: #F59E0B; }
  &:hover { background: rgba(255, 255, 255, 1); }
}

.card-body { padding: 10px 12px 12px; }
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-author {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  .author-avatar { flex-shrink: 0; }
  .author-name {
    font-size: 12px;
    color: #6B7280;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .author-badge { color: #2563FF; font-size: 14px; flex-shrink: 0; }
}
.card-tags-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  .card-tag-item {
    background: #F3F4F6;
    color: #6B7280;
    font-size: 11px;
    padding: 1px 6px;
    border-radius: 4px;
  }
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  .match-pill {
    background: #F3F4F6;
    border-radius: 10px;
    padding: 2px 8px;
    font-size: 11px;
    color: #6B7280;
    strong { color: #22C55E; font-weight: 600; }
  }
  .card-likes {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    color: #9CA3AF;
    .el-icon { font-size: 14px; }
  }
  .card-price {
    font-size: 12px;
    font-weight: 600;
    &.free { color: #22C55E; }
    &.paid { color: #F59E0B; }
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
  .empty-desc { font-size: 13px; color: #6B7280; }
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

/* ===== 右侧预览面板 ===== */
.preview-panel {
  background: #fff;
  border-left: 1px solid #EAEEF3;
  overflow-y: auto;
  height: 100%;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 2px; }
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 8px;
  color: #9CA3AF;
  .preview-empty-icon { font-size: 48px; }
  p { font-size: 14px; }
  .preview-empty-desc { font-size: 12px; }
}

.preview-top { position: relative; }
.preview-main-img {
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F9FAFB;
  .main-img {
    width: 100%;
    height: 100%;
    :deep(.el-image__inner) { width: 100%; height: 100%; object-fit: contain; }
  }
  .preview-placeholder { font-size: 48px; color: #D1D5DB; }
}

.preview-thumbs {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  .preview-thumb {
    width: 64px;
    height: 48px;
    border-radius: 6px;
    cursor: pointer;
    border: 2px solid transparent;
    overflow: hidden;
    &.active { border-color: #2563FF; }
    :deep(.el-image__inner) { width: 100%; height: 100%; object-fit: cover; }
  }
}

.preview-info { padding: 0 16px 16px; }
.preview-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
  .preview-title { font-size: 16px; font-weight: 600; color: #111827; flex: 1; }
  .preview-fav {
    cursor: pointer;
    font-size: 22px;
    color: #9CA3AF;
    flex-shrink: 0;
    &:hover { color: #F59E0B; }
  }
}
.preview-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  .preview-author-name { font-size: 13px; color: #374151; font-weight: 500; }
  .preview-author-badge { color: #2563FF; font-size: 14px; }
}
.preview-date { font-size: 12px; color: #9CA3AF; margin-bottom: 14px; }
.preview-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  .btn-download {
    flex: 1;
    background: #2563FF;
    color: #fff;
    border: none;
    padding: 9px 0;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    &:hover { background: #1D4ED8; }
  }
  .btn-add-project {
    flex: 1;
    background: #fff;
    color: #374151;
    border: 1px solid #D1D5DB;
    padding: 9px 0;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    &:hover { border-color: #2563FF; color: #2563FF; }
  }
}

/* AI识别区 */
.ai-section { padding: 0 16px 16px; }
.ai-section-title {
  font-size: 13px;
  color: #9CA3AF;
  font-weight: 500;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  .el-icon { font-size: 16px; }
}
.ai-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.ai-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: #F9FAFB;
  border-radius: 8px;
  .ai-item-left { display: flex; align-items: center; gap: 8px; }
  .ai-item-icon {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    .el-icon { font-size: 14px; color: #6B7280; }
  }
  .ai-item-label { font-size: 12px; color: #6B7280; }
  .ai-item-value { font-size: 13px; color: #111827; font-weight: 500; }
}

/* 匹配度分析 */
.match-cards {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.match-card {
  flex: 1;
  background: #F9FAFB;
  border-radius: 10px;
  padding: 12px 8px;
  text-align: center;
  .match-card-num { font-size: 22px; font-weight: 700; color: #22C55E; }
  .match-card-label { font-size: 11px; color: #6B7280; margin-top: 2px; }
}

/* AI推荐 */
.ai-recommend { padding: 0 16px 16px; }
.ai-recommend-title {
  font-size: 13px;
  color: #9CA3AF;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.ai-recommend-text {
  font-size: 13px;
  color: #4B5563;
  line-height: 1.6;
  background: #F9FAFB;
  border-radius: 10px;
  padding: 12px;
}

.preview-report {
  padding: 0 16px 24px;
  text-align: center;
  a {
    font-size: 12px;
    color: #9CA3AF;
    cursor: pointer;
    text-decoration: underline;
    &:hover { color: #EF4444; }
  }
}

@media (max-width: 1400px) {
  .card-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
