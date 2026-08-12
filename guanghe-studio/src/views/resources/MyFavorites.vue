<template>
  <div class="favorites-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="24" color="#EF4444"><StarFilled /></el-icon>
        <div>
          <h2 class="gh-page-title">我的收藏</h2>
          <p class="gh-page-subtitle">管理您收藏的素材和作品</p>
        </div>
      </div>
      <div class="header-right">
        <div class="search-box">
          <el-icon :size="16" color="#9CA3AF"><Search /></el-icon>
          <input type="text" v-model="searchKeyword" placeholder="搜索收藏素材" />
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <div
        v-for="tab in tabs"
        :key="tab"
        class="tab"
        :class="{ active: activeTab === tab }"
        @click="activeTab = tab"
      >
        {{ tab }}
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <label class="select-all">
          <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
          全选
        </label>
        <span class="selected-count">已选择 {{ selectedItems.length }} 项</span>
        <button class="action-btn" @click="batchManage">批量管理</button>
      </div>
      <div class="action-right">
        <select class="sort-select" v-model="sortBy">
          <option value="time">收藏时间</option>
          <option value="downloads">下载量</option>
          <option value="views">浏览数</option>
        </select>
        <div class="view-toggle">
          <div class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
            <svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="1" width="6" height="6" rx="1" :stroke="viewMode === 'grid' ? '#2563FF' : '#6B7280'" stroke-width="1.5" fill="none"/><rect x="9" y="1" width="6" height="6" rx="1" :stroke="viewMode === 'grid' ? '#2563FF' : '#6B7280'" stroke-width="1.5" fill="none"/><rect x="1" y="9" width="6" height="6" rx="1" :stroke="viewMode === 'grid' ? '#2563FF' : '#6B7280'" stroke-width="1.5" fill="none"/><rect x="9" y="9" width="6" height="6" rx="1" :stroke="viewMode === 'grid' ? '#2563FF' : '#6B7280'" stroke-width="1.5" fill="none"/></svg>
          </div>
          <div class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            <svg width="16" height="16" viewBox="0 0 16 16"><path d="M1 3h14M1 8h14M1 13h14" :stroke="viewMode === 'list' ? '#2563FF' : '#6B7280'" stroke-width="1.5" stroke-linecap="round" fill="none"/></svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content-wrap">
      <div class="grid-area">
        <!-- 卡片网格 -->
        <div class="card-grid" v-if="filteredCards.length">
          <div
            v-for="card in filteredCards"
            :key="card.id"
            class="collect-card"
            :class="{ selected: selectedItems.includes(card.id) }"
          >
            <div class="card-img">
              <div class="card-checkbox">
                <input type="checkbox" :checked="selectedItems.includes(card.id)" @change="toggleSelect(card.id)" />
              </div>
              <button class="card-heart" @click="removeFavorite(card.id)">
                <el-icon :size="16" color="#EF4444"><StarFilled /></el-icon>
              </button>
              <img :src="card.image" :alt="card.title" />
            </div>
            <div class="card-body">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-tags">
                <span v-for="tag in card.tags" :key="tag">{{ tag }}</span>
              </div>
              <div class="card-creator">
                <div class="cavatar">{{ card.creator.charAt(0) }}</div>
                {{ card.creator }}
                <el-icon :size="12" color="#2563FF"><CircleCheckFilled /></el-icon>
              </div>
              <div class="card-stats">
                <span><el-icon :size="12"><View /></el-icon> {{ card.views }}</span>
                <span><el-icon :size="12"><StarFilled /></el-icon> {{ card.likes }}</span>
                <span><el-icon :size="12"><Download /></el-icon> {{ card.downloads }}</span>
              </div>
              <div class="card-actions">
                <button class="card-btn" @click="setAsReference(card)">设为参考图</button>
                <button class="card-btn" @click="moveToFolder(card)">文件夹</button>
                <button class="card-menu-btn" @click="showCardMenu(card)">
                  <el-icon :size="14"><MoreFilled /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-else description="暂无收藏素材" style="padding: 60px 0" />

        <!-- 分页 -->
        <div class="pagination" v-if="filteredCards.length">
          <span>共 {{ totalCount }} 个收藏</span>
          <div class="page-btns">
            <div class="page-btn" @click="prevPage">‹</div>
            <div
              v-for="p in totalPages"
              :key="p"
              class="page-btn"
              :class="{ active: currentPage === p }"
              @click="currentPage = p"
            >
              {{ p }}
            </div>
            <div class="page-btn" @click="nextPage">›</div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 收藏统计 -->
        <div class="panel-card">
          <div class="panel-title">收藏统计</div>
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-label">收藏总数</div>
              <div class="stat-value">{{ totalCount }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">文件夹</div>
              <div class="stat-value">{{ folders.length }}</div>
            </div>
          </div>
        </div>

        <!-- 文件夹 -->
        <div class="panel-card">
          <div class="panel-title">
            文件夹
            <span class="panel-link" @click="manageFolders">管理 ›</span>
          </div>
          <div class="folder-list">
            <div
              v-for="folder in folders"
              :key="folder.name"
              class="folder-item"
              @click="filterByFolder(folder.name)"
            >
              <div class="folder-left">
                <el-icon :size="16" color="#F59E0B"><Folder /></el-icon>
                {{ folder.name }}
              </div>
              <span class="count">{{ folder.count }}</span>
            </div>
          </div>
        </div>

        <!-- 标签云 -->
        <div class="panel-card">
          <div class="panel-title">标签云</div>
          <div class="tag-cloud">
            <span
              v-for="tag in tagCloud"
              :key="tag"
              class="tag-item"
              @click="filterByTag(tag)"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 小贴士 -->
        <div class="panel-card tips-card">
          <h4><el-icon :size="16" color="#D97706"><InfoFilled /></el-icon> 小贴士</h4>
          <p>收藏您喜欢的素材，方便随时查阅和使用。您可以将素材分类到不同文件夹，添加标签以便快速查找。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, Search, View, Download, CircleCheckFilled, MoreFilled, Folder, InfoFilled } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const activeTab = ref('全部收藏')
const tabs = ['全部收藏', '文件夹', '标签管理']
const selectAll = ref(false)
const sortBy = ref('time')
const viewMode = ref('grid')
const currentPage = ref(1)
const pageSize = ref(8)
const selectedItems = ref([])

// 收藏卡片数据（使用项目内家具场景图片）
const allCards = ref([
  { id: 1, title: '奶油风客厅沙发场景', tags: ['沙发', '客厅', '奶油风'], creator: '光合设计', views: '1.2k', likes: 356, downloads: 98, image: '/images/cream-livingroom.png', folder: '沙发场景' },
  { id: 2, title: '原木风餐厅场景', tags: ['餐桌', '餐厅', '原木风'], creator: '空间美学研究所', views: '856', likes: 243, downloads: 65, image: '/images/kitchen-dining.png', folder: '餐厅场景' },
  { id: 3, title: '简约风卧室床场景', tags: ['床', '卧室', '现代简约'], creator: '北欧生活美学', views: '432', likes: 128, downloads: 32, image: '/images/modern-bedroom.png', folder: '卧室场景' },
  { id: 4, title: '北欧风客厅电视柜场景', tags: ['电视柜', '客厅', '北欧风'], creator: '家庭灵感库', views: '1.5k', likes: 420, downloads: 112, image: '/images/nordic-office.png', folder: '沙发场景' },
  { id: 5, title: '日式原木客厅场景', tags: ['沙发', '客厅', '日式原木'], creator: 'LUXE Home', views: '983', likes: 276, downloads: 68, image: '/images/japanese-room.png', folder: '沙发场景' },
  { id: 6, title: '现代简约卧室场景', tags: ['床', '卧室', '现代简约'], creator: '设计师阿木', views: '756', likes: 210, downloads: 48, image: '/images/modern-bedroom.png', folder: '卧室场景' },
  { id: 7, title: '工业风书房场景', tags: ['书桌', '书房', '工业风'], creator: '光合家居研究所', views: '432', likes: 128, downloads: 35, image: '/images/industrial-livingroom.png', folder: '办公场景' },
  { id: 8, title: '户外阳台场景', tags: ['户外', '阳台', '休闲'], creator: '光合设计', views: '654', likes: 189, downloads: 45, image: '/images/outdoor-patio.png', folder: '户外的场景' },
])

const folders = ref([
  { name: '沙发场景', count: 32 },
  { name: '餐厅场景', count: 24 },
  { name: '卧室场景', count: 28 },
  { name: '办公场景', count: 18 },
  { name: '户外的场景', count: 12 },
  { name: '儿童房', count: 14 },
])

const tagCloud = ref(['奶油风', '原木风', '北欧风', '现代简约', '日式', '工业风', '轻奢', '美式', '法式', '中式'])

const totalCount = computed(() => allCards.value.length)

const filteredCards = computed(() => {
  let list = allCards.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(c => c.title.toLowerCase().includes(kw) || c.tags.some(t => t.toLowerCase().includes(kw)))
  }
  if (sortBy.value === 'downloads') {
    list = [...list].sort((a, b) => b.downloads - a.downloads)
  } else if (sortBy.value === 'views') {
    list = [...list].sort((a, b) => parseInt(b.views) - parseInt(a.views))
  }
  const start = (currentPage.value - 1) * pageSize.value
  return list.slice(start, start + pageSize.value)
})

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize.value)))

function toggleSelectAll() {
  if (selectAll.value) {
    selectedItems.value = filteredCards.value.map(c => c.id)
  } else {
    selectedItems.value = []
  }
}

function toggleSelect(id) {
  const idx = selectedItems.value.indexOf(id)
  if (idx >= 0) {
    selectedItems.value.splice(idx, 1)
  } else {
    selectedItems.value.push(id)
  }
}

function removeFavorite(id) {
  ElMessage.success('已取消收藏')
  allCards.value = allCards.value.filter(c => c.id !== id)
}

function setAsReference(card) {
  ElMessage.success(`已将"${card.title}"设为参考图`)
}

function moveToFolder(card) {
  ElMessage.info(`移动"${card.title}"到文件夹`)
}

function showCardMenu(card) {
  ElMessage.info(`更多操作：${card.title}`)
}

function batchManage() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要管理的素材')
    return
  }
  ElMessage.info(`正在管理 ${selectedItems.value.length} 项素材`)
}

function manageFolders() {
  ElMessage.info('文件夹管理功能开发中')
}

function filterByFolder(name) {
  ElMessage.info(`筛选文件夹：${name}`)
}

function filterByTag(tag) {
  searchKeyword.value = tag
  ElMessage.info(`筛选标签：${tag}`)
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 4px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #F7F9FC;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  padding: 8px 16px;
  gap: 8px;

  input {
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    flex: 1;
    width: 200px;
    font-family: inherit;
  }
}

/* Tabs */
.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 2px solid #E8EDF5;
}

.tab {
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  color: #6B7280;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.15s;

  &.active {
    color: #2563FF;
    border-bottom-color: #2563FF;
    font-weight: 500;
  }

  &:hover {
    color: #2563FF;
  }
}

/* 操作栏 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.action-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.select-all {
  font-size: 13px;
  color: #6B7280;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;

  input {
    accent-color: #2563FF;
  }
}

.selected-count {
  font-size: 13px;
  color: #6B7280;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  color: #6B7280;
  transition: all 0.15s;

  &:hover {
    border-color: #2563FF;
    color: #2563FF;
  }
}

.action-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  font-size: 13px;
  outline: none;
  background: #fff;
  font-family: inherit;
}

.view-toggle {
  display: flex;
  gap: 4px;
}

.view-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;

  &.active {
    background: #EEF2FF;
    border-color: #2563FF;
  }
}

/* 内容区 */
.content-wrap {
  display: flex;
  gap: 24px;
}

.grid-area {
  flex: 1;
  min-width: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.collect-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #E8EDF5;
  position: relative;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &.selected {
    border-color: #2563FF;
    box-shadow: 0 0 0 2px rgba(37, 99, 255, 0.2);
  }
}

.card-img {
  height: 180px;
  position: relative;
  overflow: hidden;
  background: #F7F9FC;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;

  input {
    width: 18px;
    height: 18px;
    accent-color: #2563FF;
    cursor: pointer;
  }
}

.card-heart {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: none;
  transition: all 0.2s;

  &:hover {
    background: #fff;
    transform: scale(1.1);
  }
}

.card-body {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #1F2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;

  span {
    padding: 3px 8px;
    background: #F7F9FC;
    border-radius: 6px;
    font-size: 11px;
    color: #6B7280;
  }
}

.card-creator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 8px;
}

.cavatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #E8EDF5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #6B7280;
  flex-shrink: 0;
}

.card-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 10px;

  span {
    display: flex;
    align-items: center;
    gap: 3px;
  }
}

.card-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.card-btn {
  flex: 1;
  text-align: center;
  font-size: 12px;
  padding: 6px;
  border-radius: 6px;
  border: 1px solid #E8EDF5;
  background: #fff;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: #2563FF;
    color: #2563FF;
  }
}

.card-menu-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #9CA3AF;
  cursor: pointer;
  border-radius: 6px;

  &:hover {
    background: #F7F9FC;
    color: #2563FF;
  }
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 13px;
  color: #6B7280;
}

.page-btns {
  display: flex;
  gap: 4px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;

  &:hover {
    border-color: #2563FF;
    color: #2563FF;
  }

  &.active {
    background: #2563FF;
    color: #fff;
    border-color: #2563FF;
  }
}

/* 右侧面板 */
.right-panel {
  width: 280px;
  flex-shrink: 0;
}

.panel-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #E8EDF5;
  margin-bottom: 20px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1F2937;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-link {
  font-size: 12px;
  color: #2563FF;
  font-weight: 400;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  padding: 12px;
  background: #F7F9FC;
  border-radius: 10px;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1F2937;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.folder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #F7F9FC;
  }
}

.folder-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #1F2937;
}

.count {
  font-size: 12px;
  color: #9CA3AF;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 4px 12px;
  background: #EEF2FF;
  border-radius: 6px;
  font-size: 12px;
  color: #2563FF;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #DCE6FF;
  }
}

.tips-card {
  background: #FEF3C7;
  border: 1px solid #FDE68A;

  h4 {
    color: #D97706;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  p {
    font-size: 12px;
    color: #92400E;
    line-height: 1.5;
  }
}

/* 响应式 */
@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 960px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-wrap {
    flex-direction: column;
  }

  .right-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>