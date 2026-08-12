<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">创作者中心</h2>
        <p class="gh-page-subtitle">管理发布的素材与收益</p>
      </div>
      <el-button type="primary" round>
        <el-icon><Upload /></el-icon>
        发布新素材
      </el-button>
    </div>

    <!-- Stats Cards Row -->
    <div class="stats-row">
      <div class="stat-card gh-card">
        <div class="stat-icon blue">
          <el-icon :size="20"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">48</div>
          <div class="stat-label">发布素材</div>
        </div>
      </div>
      <div class="stat-card gh-card">
        <div class="stat-icon green">
          <el-icon :size="20"><Download /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">2.3k</div>
          <div class="stat-label">总下载量</div>
        </div>
      </div>
      <div class="stat-card gh-card">
        <div class="stat-icon orange">
          <el-icon :size="20"><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">12,560<span class="stat-unit">积分</span></div>
          <div class="stat-label">总收益</div>
        </div>
      </div>
      <div class="stat-card gh-card">
        <div class="stat-icon purple">
          <el-icon :size="20"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">4.8</div>
          <div class="stat-label">平均评分</div>
        </div>
      </div>
    </div>

    <!-- Main Layout -->
    <div class="main-layout">
      <!-- Left Content Area -->
      <div class="content-area">
        <!-- Tab Filter + Sort -->
        <div class="content-toolbar">
          <div class="content-tabs">
            <div
              v-for="tab in tabs"
              :key="tab.value"
              class="content-tab"
              :class="{ active: activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              {{ tab.label }}
              <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
            </div>
          </div>
          <div class="sort-control">
            <el-select v-model="sortBy" class="sort-select" placeholder="排序方式">
              <el-option label="发布时间" value="date" />
              <el-option label="下载量" value="downloads" />
              <el-option label="收益" value="revenue" />
            </el-select>
          </div>
        </div>

        <!-- Asset Grid -->
        <div class="assets-grid">
          <div v-for="(item, idx) in filteredAssets" :key="idx" class="asset-card gh-card">
            <div class="asset-thumb" :style="{ background: item.thumbBg }">
              <div class="thumb-icon-wrap">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <span class="asset-badge" :class="item.badgeClass">{{ item.badge }}</span>
            </div>
            <div class="asset-body">
              <div class="asset-title">{{ item.title }}</div>
              <div class="asset-stats-row">
                <span class="stat-chip">
                  <el-icon :size="12"><Download /></el-icon>
                  {{ item.downloads }}
                </span>
                <span class="stat-chip">
                  <el-icon :size="12"><View /></el-icon>
                  {{ item.views }}
                </span>
                <span class="stat-chip rating">
                  <el-icon :size="12"><Star /></el-icon>
                  {{ item.rating }}
                </span>
              </div>
              <div class="asset-footer">
                <span class="asset-price" :class="{ free: item.price === '免费' }">{{ item.price }}</span>
                <div class="asset-actions">
                  <el-button text size="small" @click.stop="editAsset(item)">编辑</el-button>
                  <el-button
                    text
                    size="small"
                    :type="item.badge === '已下架' ? 'primary' : ''"
                    @click.stop="togglePublish(item)"
                  >
                    {{ item.badge === '已下架' ? '上架' : '下架' }}
                  </el-button>
                  <el-button text size="small" type="primary" @click.stop="viewData(item)">查看数据</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Dashboard Panel -->
      <div class="dashboard-panel">
        <div class="dashboard-inner">
          <!-- 数据概览 -->
          <div class="dash-section">
            <div class="dash-section-title">数据概览</div>
            <div class="dash-stats-grid">
              <div class="dash-stat-item">
                <span class="dash-stat-label">本周下载</span>
                <div class="dash-stat-val-row">
                  <span class="dash-stat-value">156</span>
                  <span class="dash-stat-trend up">+12%</span>
                </div>
              </div>
              <div class="dash-stat-item">
                <span class="dash-stat-label">本周收益</span>
                <div class="dash-stat-val-row">
                  <span class="dash-stat-value">780<span class="dash-unit">积分</span></span>
                  <span class="dash-stat-trend up">+8%</span>
                </div>
              </div>
              <div class="dash-stat-item">
                <span class="dash-stat-label">本月下载</span>
                <div class="dash-stat-val-row">
                  <span class="dash-stat-value">628</span>
                </div>
              </div>
              <div class="dash-stat-item">
                <span class="dash-stat-label">本月收益</span>
                <div class="dash-stat-val-row">
                  <span class="dash-stat-value">3,140<span class="dash-unit">积分</span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 收益趋势 -->
          <div class="dash-section">
            <div class="dash-section-header">
              <span class="dash-section-title">收益趋势</span>
              <div class="chart-tabs">
                <span
                  v-for="tab in chartTabs"
                  :key="tab"
                  class="chart-tab"
                  :class="{ active: activeChartTab === tab }"
                  @click="activeChartTab = tab"
                >{{ tab }}</span>
              </div>
            </div>
            <div class="chart-area">
              <div class="chart-bars">
                <div v-for="(bar, i) in currentChartData" :key="i" class="chart-bar-wrap">
                  <div class="chart-bar" :style="{ height: bar.value + 'px' }"></div>
                  <span class="chart-label">{{ bar.label }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 热门素材 TOP5 -->
          <div class="dash-section">
            <div class="dash-section-title">热门素材 TOP5</div>
            <div class="top-list">
              <div v-for="(item, idx) in topMaterials" :key="idx" class="top-item">
                <span class="top-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                <span class="top-name">{{ item.name }}</span>
                <span class="top-downloads">{{ item.downloads }} 次</span>
              </div>
            </div>
          </div>

          <!-- 创作建议 -->
          <div class="dash-section tips-section">
            <div class="dash-section-title">创作建议</div>
            <div class="tips-content">
              <p class="tip-text">当前场景图类素材下载量最高，建议多产出生活场景类内容，搭配高清晰度和自然光影效果可获得更多曝光。</p>
              <a class="tips-link" @click="viewGuide">
                查看创作指南
                <el-icon :size="12"><ArrowRight /></el-icon>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref('all')
const sortBy = ref('date')
const activeChartTab = ref('7天')

const tabs = [
  { label: '全部素材', value: 'all', count: 48 },
  { label: '已通过', value: 'approved', count: 42 },
  { label: '审核中', value: 'reviewing', count: 4 },
  { label: '已下架', value: 'offline', count: 2 },
]

const chartTabs = ['7天', '30天', '全部']

const chartDataMap = {
  '7天': [
    { label: '周一', value: 45 },
    { label: '周二', value: 72 },
    { label: '周三', value: 38 },
    { label: '周四', value: 96 },
    { label: '周五', value: 68 },
    { label: '周六', value: 110 },
    { label: '周日', value: 85 },
  ],
  '30天': [
    { label: '第1周', value: 280 },
    { label: '第2周', value: 350 },
    { label: '第3周', value: 310 },
    { label: '第4周', value: 420 },
  ],
  '全部': [
    { label: '1月', value: 85 },
    { label: '2月', value: 92 },
    { label: '3月', value: 110 },
    { label: '4月', value: 98 },
    { label: '5月', value: 120 },
    { label: '6月', value: 140 },
  ],
}

const currentChartData = computed(() => chartDataMap[activeChartTab.value] || chartDataMap['7天'])

const allAssets = ref([
  {
    title: '北欧风客厅沙发场景',
    icon: 'PictureFilled',
    thumbBg: 'linear-gradient(135deg, #DBEAFE, #93C5FD)',
    badge: '已发布',
    badgeClass: 'badge-published',
    downloads: 456,
    views: 2340,
    rating: 4.9,
    price: '5积分',
    tab: 'approved',
  },
  {
    title: '现代简约卧室场景',
    icon: 'PictureFilled',
    thumbBg: 'linear-gradient(135deg, #D1FAE5, #A7F3D0)',
    badge: '已发布',
    badgeClass: 'badge-published',
    downloads: 324,
    views: 1876,
    rating: 4.7,
    price: '5积分',
    tab: 'approved',
  },
  {
    title: '日式原木餐桌布置',
    icon: 'PictureFilled',
    thumbBg: 'linear-gradient(135deg, #FEF3C7, #FDE68A)',
    badge: '已发布',
    badgeClass: 'badge-published',
    downloads: 876,
    views: 3456,
    rating: 4.8,
    price: '免费',
    tab: 'approved',
  },
  {
    title: '夏季促销Banner模板',
    icon: 'Postcard',
    thumbBg: 'linear-gradient(135deg, #FCE7F3, #FBCFE8)',
    badge: '审核中',
    badgeClass: 'badge-reviewing',
    downloads: 0,
    views: 0,
    rating: 0,
    price: '3积分',
    tab: 'reviewing',
  },
  {
    title: '智能手表产品展示',
    icon: 'Monitor',
    thumbBg: 'linear-gradient(135deg, #E0E7FF, #C7D2FE)',
    badge: '已发布',
    badgeClass: 'badge-published',
    downloads: 210,
    views: 980,
    rating: 4.6,
    price: '5积分',
    tab: 'approved',
  },
  {
    title: '儿童家具套图素材',
    icon: 'PictureFilled',
    thumbBg: 'linear-gradient(135deg, #F3E8FF, #DDD6FE)',
    badge: '已下架',
    badgeClass: 'badge-offline',
    downloads: 156,
    views: 820,
    rating: 4.5,
    price: '免费',
    tab: 'offline',
  },
])

const filteredAssets = computed(() => {
  if (activeTab.value === 'all') return allAssets.value
  return allAssets.value.filter(a => a.tab === activeTab.value)
})

const topMaterials = ref([
  { name: '日式原木餐桌布置', downloads: 876 },
  { name: '北欧风客厅沙发场景', downloads: 456 },
  { name: '现代简约卧室场景', downloads: 324 },
  { name: '智能手表产品展示', downloads: 210 },
  { name: '夏季促销Banner模板', downloads: 180 },
])

function editAsset(item) {
  console.log('Edit asset:', item.title)
}

function togglePublish(item) {
  console.log('Toggle publish:', item.title)
}

function viewData(item) {
  console.log('View data:', item.title)
}

function viewGuide() {
  console.log('View creation guide')
}
</script>

<style lang="scss" scoped>
.page-container {
  padding: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gh-space-20);
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gh-space-16);
  margin-bottom: var(--gh-space-20);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--gh-space-16);
  padding: var(--gh-space-20) !important;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--gh-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.blue {
    background: rgba(37, 99, 255, 0.1);
    color: var(--gh-primary);
  }

  &.green {
    background: rgba(34, 197, 94, 0.1);
    color: var(--gh-success);
  }

  &.orange {
    background: rgba(245, 158, 11, 0.1);
    color: var(--gh-warning);
  }

  &.purple {
    background: rgba(124, 58, 237, 0.1);
    color: #7C3AED;
  }
}

.stat-value {
  font-size: 22px;
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
  line-height: 1.2;
}

.stat-unit {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  margin-left: 2px;
}

.stat-label {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  margin-top: 2px;
}

/* Main Layout */
.main-layout {
  display: flex;
  align-items: flex-start;
}

.content-area {
  flex: 1;
  min-width: 0;
  padding-right: var(--gh-space-24);
}

/* Content Toolbar */
.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gh-space-16);
  flex-wrap: wrap;
  gap: var(--gh-space-12);
}

.content-tabs {
  display: flex;
  gap: var(--gh-space-4);
  border-bottom: 1px solid var(--gh-border-light);
  flex: 1;
}

.content-tab {
  padding: var(--gh-space-8) var(--gh-space-16);
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  margin-bottom: -1px;
  display: flex;
  align-items: center;
  gap: var(--gh-space-4);
  white-space: nowrap;

  &:hover {
    color: var(--gh-primary);
  }

  &.active {
    color: var(--gh-primary);
    border-bottom-color: var(--gh-primary);
  }
}

.tab-count {
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  background: var(--gh-border-light);
  padding: 0 6px;
  border-radius: 10px;
  line-height: 18px;
}

.sort-control {
  flex-shrink: 0;
}

.sort-select {
  width: 130px;
}

/* Assets Grid */
.assets-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--gh-space-16);
}

.asset-card {
  padding: 0 !important;
  overflow: hidden;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.asset-thumb {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: var(--gh-radius-card) var(--gh-radius-card) 0 0;
}

.thumb-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--gh-radius-sm);
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gh-primary);
  backdrop-filter: blur(4px);
}

.asset-badge {
  position: absolute;
  top: var(--gh-space-8);
  right: var(--gh-space-8);
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-medium);
  padding: 2px 8px;
  border-radius: var(--gh-radius-xs);

  &.badge-published {
    background: var(--gh-success);
    color: #fff;
  }

  &.badge-reviewing {
    background: var(--gh-warning);
    color: #fff;
  }

  &.badge-offline {
    background: var(--gh-text-secondary);
    color: #fff;
  }
}

.asset-body {
  padding: var(--gh-space-12) var(--gh-space-16);
}

.asset-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.asset-stats-row {
  display: flex;
  gap: var(--gh-space-12);
  margin-bottom: var(--gh-space-12);
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);

  &.rating {
    color: var(--gh-warning);
    font-weight: var(--gh-weight-medium);
  }
}

.asset-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--gh-space-8);
  border-top: 1px solid var(--gh-border-light);
}

.asset-price {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);

  &.free {
    color: var(--gh-success);
  }
}

.asset-actions {
  display: flex;
  gap: var(--gh-space-4);
}

/* Right Dashboard Panel */
.dashboard-panel {
  width: 320px;
  flex-shrink: 0;
  border-left: 1px solid var(--gh-border);
  padding-left: var(--gh-space-24);
  position: sticky;
  top: var(--gh-space-20);
}

.dashboard-inner {
  display: flex;
  flex-direction: column;
}

.dash-section {
  margin-bottom: var(--gh-space-24);
}

.dash-section-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-12);
}

.dash-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gh-space-12);
}

.chart-tabs {
  display: flex;
  gap: var(--gh-space-4);
}

.chart-tab {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-secondary);
  padding: 2px 8px;
  border-radius: var(--gh-radius-xs);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: var(--gh-primary);
  }

  &.active {
    background: var(--gh-primary-bg);
    color: var(--gh-primary);
  }
}

/* Dashboard Stats Grid */
.dash-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--gh-space-12);
}

.dash-stat-item {
  padding: var(--gh-space-12);
  background: var(--gh-bg-page);
  border-radius: var(--gh-radius-sm);
}

.dash-stat-label {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  display: block;
  margin-bottom: var(--gh-space-4);
}

.dash-stat-val-row {
  display: flex;
  align-items: baseline;
  gap: var(--gh-space-4);
}

.dash-stat-value {
  font-size: var(--gh-font-module);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
}

.dash-unit {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
}

.dash-stat-trend {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-medium);

  &.up {
    color: var(--gh-success);
  }

  &.down {
    color: var(--gh-danger);
  }
}

/* Chart Area */
.chart-area {
  height: 120px;
  padding-bottom: var(--gh-space-20);
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: var(--gh-space-8);
  height: 100%;
}

.chart-bar-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--gh-space-4);
  height: 100%;
  justify-content: flex-end;
}

.chart-bar {
  width: 100%;
  max-width: 24px;
  background: linear-gradient(180deg, var(--gh-primary), var(--gh-primary-light));
  border-radius: var(--gh-radius-xs) var(--gh-radius-xs) 0 0;
  transition: height 0.5s ease;
  min-height: 6px;
}

.chart-label {
  font-size: 10px;
  color: var(--gh-text-secondary);
}

/* Top Materials List */
.top-list {
  display: flex;
  flex-direction: column;
  gap: var(--gh-space-8);
}

.top-item {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
  padding: var(--gh-space-8) 0;
}

.top-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: var(--gh-weight-semibold);
  background: var(--gh-border-light);
  color: var(--gh-text-secondary);
  flex-shrink: 0;

  &.rank-1 {
    background: var(--gh-primary-bg);
    color: var(--gh-primary);
  }

  &.rank-2 {
    background: rgba(34, 197, 94, 0.1);
    color: var(--gh-success);
  }

  &.rank-3 {
    background: rgba(245, 158, 11, 0.1);
    color: var(--gh-warning);
  }
}

.top-name {
  flex: 1;
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-downloads {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  flex-shrink: 0;
}

/* Tips Section */
.tips-section {
  padding: var(--gh-space-16);
  background: var(--gh-bg-page);
  border-radius: var(--gh-radius-sm);
}

.tip-text {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--gh-space-8);
}

.tips-link {
  display: inline-flex;
  align-items: center;
  gap: var(--gh-space-4);
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-primary);
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.8;
  }
}
</style>
