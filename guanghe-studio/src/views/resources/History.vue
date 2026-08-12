<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">历史记录</h2>
        <p class="gh-page-subtitle">查看您生成的所有内容，支持预览、编辑、下载和再次使用</p>
      </div>
    </div>

    <!-- Horizontal Filter Bar -->
    <div class="filter-bar">
      <div class="filter-bar-left">
        <el-select
          v-model="filters.timeRange"
          placeholder="时间范围"
          clearable
          class="filter-dropdown"

        >
          <el-option v-for="opt in timeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>

        <el-select
          :model-value="filters.featureType"
          @update:model-value="setFilter('featureType', $event)"
          placeholder="功能类型"
          clearable
          class="filter-dropdown"
        >
          <el-option v-for="(cfg, key) in featureTypeOptions" :key="key" :label="cfg.label" :value="key" />
        </el-select>

        <el-select
          :model-value="filters.status"
          @update:model-value="setFilter('status', $event)"
          placeholder="操作状态"
          clearable
          class="filter-dropdown"
        >
          <el-option label="全部" value="" />
          <el-option label="已完成" value="success" />
          <el-option label="处理中" value="processing" />
          <el-option label="失败" value="failed" />
        </el-select>

        <template v-if="filters.timeRange === 'custom'">
          <el-date-picker
            v-model="filters.customStart"
            type="date"
            size="default"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            class="filter-date-picker"
          />
          <span class="filter-date-sep">~</span>
          <el-date-picker
            v-model="filters.customEnd"
            type="date"
            size="default"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="filter-date-picker"
          />
        </template>
      </div>

      <div class="filter-bar-right">
        <span class="filter-stats">
          共 <span class="stats-count">{{ totalCount }}</span> 条记录
        </span>
        <el-button @click="resetFilters">
          <el-icon><RefreshLeft /></el-icon>
          重置筛选
        </el-button>
        <el-button type="danger" plain round @click="clearAllHistory">
          <el-icon><Delete /></el-icon>
          清空历史
        </el-button>
      </div>
    </div>

    <!-- Timeline Grid -->
    <div class="timeline-area">
      <el-empty v-if="!loading && timelineGroups.length === 0" description="暂无历史记录，去工作台生成作品吧～" />

        <div v-for="(group, gIdx) in timelineGroups" :key="gIdx" class="timeline-group">
          <div class="date-header">
            <span class="date-text">{{ group.label }}</span>
            <span class="date-count">{{ group.items.length }} 条记录</span>
          </div>
          <div class="timeline-list">
            <div v-for="(item, idx) in group.items" :key="item.id" class="history-card gh-card">
              <div class="card-left">
                <div class="card-icon" :style="{ background: featureBg(item.featureType) }">
                  <el-icon :size="20" :color="featureColor(item.featureType)">
                    <component :is="featureIcon(item.featureType)" />
                  </el-icon>
                </div>
                <div class="timeline-line" v-if="idx < group.items.length - 1"></div>
              </div>
              <div class="card-content">
                <div class="card-header">
                  <div class="card-title-row">
                    <span class="card-title">{{ item.title || '未命名作品' }}</span>
                    <span class="card-status" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
                  </div>
                  <span class="card-time">{{ formatTime(item.timestamp) }}</span>
                </div>
                <div class="card-desc">{{ item.description || item.prompt || '—' }}</div>
                <div class="card-footer">
                  <div class="card-thumb-row">
                    <div class="card-thumb" v-if="item.resultImages && item.resultImages[0]">
                      <img :src="item.resultImages[0]" alt="缩略图" />
                    </div>
                    <div class="card-thumb placeholder" v-else>
                      <el-icon :size="14" color="#9CA3AF"><PictureFilled /></el-icon>
                    </div>
                    <span class="card-type-label">{{ featureLabel(item.featureType) }}</span>
                    <span class="card-points" v-if="item.consumePoints > 0">-{{ item.consumePoints }} 积分</span>
                  </div>
                  <div class="card-actions">
                    <el-button text size="small" type="primary" @click.stop="viewDetail(item)">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-button>
                    <el-button text size="small" @click.stop="regenerate(item)">
                      <el-icon><RefreshRight /></el-icon>
                      再次生成
                    </el-button>
                    <el-button text size="small" type="danger" @click.stop="deleteRecord(item)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>


    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="记录详情" width="720px" :destroy-on-close="true">
      <div v-if="detailRecord" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">标题：</span>
          <span class="detail-value">{{ detailRecord.title || '未命名' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">功能类型：</span>
          <span class="detail-value">{{ featureLabel(detailRecord.featureType) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态：</span>
          <span class="detail-value" :class="statusClass(detailRecord.status)">{{ statusLabel(detailRecord.status) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">生成时间：</span>
          <span class="detail-value">{{ formatFullTime(detailRecord.timestamp) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">消耗积分：</span>
          <span class="detail-value">{{ detailRecord.consumePoints || 0 }}</span>
        </div>
        <div class="detail-row" v-if="detailRecord.prompt">
          <span class="detail-label">提示词：</span>
          <span class="detail-value prompt-text">{{ detailRecord.prompt }}</span>
        </div>
        <div class="detail-row" v-if="detailRecord.description">
          <span class="detail-label">描述：</span>
          <span class="detail-value">{{ detailRecord.description }}</span>
        </div>
        <div class="detail-images" v-if="detailRecord.resultImages && detailRecord.resultImages.length">
          <div class="detail-label">生成结果：</div>
          <div class="image-grid">
            <div v-for="(img, i) in detailRecord.resultImages" :key="i" class="image-item">
              <img :src="img" :alt="`结果${i+1}`" @click="previewImage(img)" />
              <el-button text size="small" @click="downloadImage(img, i)">
                <el-icon><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[previewUrl]"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View, RefreshRight, Delete, RefreshLeft, Download, PictureFilled,
  MagicStick, Picture, User, Edit, Upload, Files, Histogram,
} from '@element-plus/icons-vue'
import { useHistory, FEATURE_TYPES } from '@/composables/useHistory'

const router = useRouter()
const {
  records, filters, filteredRecords, timelineGroups, totalCount,
  deleteHistoryRecord, clearHistory, setFilter, resetFilters,
} = useHistory()

const loading = ref(false)
const detailVisible = ref(false)
const detailRecord = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')

const timeOptions = [
  { label: '今天', value: 'today' },
  { label: '近7天', value: '7days' },
  { label: '近30天', value: '30days' },
  { label: '自定义', value: 'custom' },
  { label: '全部', value: 'all' },
]

const featureTypeOptions = FEATURE_TYPES

const FEATURE_ROUTE_MAP = {
  white_bg: '/whiteBg',
  background: '/whiteToBg',
  retouch: '/refine',
  ai_model: '/aiModel',
  main_image: '/mainImage',
  detail_img: '/detailImg',
  banner: '/banner',
  size_mark: '/size',
  ai_assistant: '',
}

const FEATURE_ICONS = {
  white_bg: Picture,
  background: Picture,
  retouch: MagicStick,
  ai_model: User,
  main_image: Picture,
  detail_img: Edit,
  banner: Histogram,
  size_mark: Edit,
  ai_assistant: MagicStick,
}

function featureLabel(type) {
  return FEATURE_TYPES[type]?.label || type || '未知'
}
function featureColor(type) {
  return FEATURE_TYPES[type]?.color || '#6B7280'
}
function featureBg(type) {
  return FEATURE_TYPES[type]?.bg || '#F3F4F6'
}
function featureIcon(type) {
  return FEATURE_ICONS[type] || PictureFilled
}

function statusLabel(s) {
  return { success: '已完成', processing: '处理中', failed: '失败' }[s] || s || '未知'
}
function statusClass(s) {
  return { success: 'status-done', processing: 'status-processing', failed: 'status-failed' }[s] || ''
}

function formatTime(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) {
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  }
  return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatFullTime(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

function viewDetail(item) {
  detailRecord.value = item
  detailVisible.value = true
}

function regenerate(item) {
  const path = FEATURE_ROUTE_MAP[item.featureType]
  if (path) {
    router.push(path)
    ElMessage.success(`已跳转到${featureLabel(item.featureType)}工作台`)
  } else {
    ElMessage.info('该记录暂不支持再次生成')
  }
}

function deleteRecord(item) {
  ElMessageBox.confirm(`确定删除这条记录吗？`, '提示', { type: 'warning' })
    .then(() => {
      deleteHistoryRecord(item.id)
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

function clearAllHistory() {
  if (records.value.length === 0) {
    ElMessage.info('暂无记录可清空')
    return
  }
  ElMessageBox.confirm(`确定清空全部 ${records.value.length} 条历史记录吗？此操作不可恢复。`, '警告', { type: 'warning' })
    .then(() => {
      clearHistory()
      ElMessage.success('已清空全部历史记录')
    })
    .catch(() => {})
}

function previewImage(url) {
  previewUrl.value = url
  previewVisible.value = true
}

async function downloadImage(url, index) {
  try {
    const res = await fetch(url)
    const blob = await res.blob()
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `history_${Date.now()}_${index + 1}.png`
    link.click()
    URL.revokeObjectURL(link.href)
  } catch {
    ElMessage.warning('下载失败，请稍后重试')
  }
}

onMounted(() => {
  loading.value = true
  setTimeout(() => { loading.value = false }, 200)
})
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

/* Horizontal Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--gh-space-16);
  margin-bottom: var(--gh-space-20);
  padding: var(--gh-space-12) var(--gh-space-16);
  background: var(--gh-bg-card);
  border: 1px solid var(--gh-border);
  border-radius: var(--gh-radius-card);
  flex-wrap: wrap;
}

.filter-bar-left {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
  flex-wrap: wrap;
}

.filter-bar-right {
  display: flex;
  align-items: center;
  gap: var(--gh-space-12);
  flex-shrink: 0;
}

.filter-dropdown {
  width: 150px;
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-btn);
  }
}

.filter-date-picker {
  width: 150px !important;
}

.filter-date-sep {
  color: var(--gh-text-secondary);
  font-size: var(--gh-font-body);
}

.filter-stats {
  font-size: var(--gh-font-body);
  color: var(--gh-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--gh-space-4);
}

/* Timeline Area */
.timeline-area {
  flex: 1;
  min-width: 0;
}

/* Timeline Group */
.timeline-group {
  margin-bottom: var(--gh-space-32);
}

.date-header {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
  margin-bottom: var(--gh-space-16);
  padding-bottom: var(--gh-space-12);
  border-bottom: 1px solid var(--gh-border-light);
}

.date-text {
  font-size: var(--gh-font-module);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
}

.date-count {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
}

/* Timeline List */
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: var(--gh-space-12);
}

/* History Card */
.history-card {
  display: flex;
  gap: var(--gh-space-16);
  padding: var(--gh-space-20) !important;
  border-radius: var(--gh-radius-card);
  transition: transform 0.2s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

.card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--gh-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: var(--gh-border-light);
  margin-top: var(--gh-space-8);
  min-height: 20px;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--gh-space-8);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
}

.card-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-primary);
}

.card-status {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-medium);
  padding: 2px 8px;
  border-radius: var(--gh-radius-xs);

  &.status-done {
    background: rgba(34, 197, 94, 0.1);
    color: var(--gh-success);
  }

  &.status-processing {
    background: var(--gh-primary-bg);
    color: var(--gh-primary);
  }

  &.status-failed {
    background: rgba(239, 68, 68, 0.1);
    color: var(--gh-danger);
  }
}

.card-time {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  flex-shrink: 0;
}

.card-desc {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  line-height: 1.5;
  margin-bottom: var(--gh-space-12);
  word-break: break-word;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-thumb-row {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
}

.card-thumb {
  width: 36px;
  height: 36px;
  border-radius: var(--gh-radius-sm);
  overflow: hidden;
  flex-shrink: 0;

  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--gh-border-light);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-type-label {
  font-size: var(--gh-font-helper);
  font-weight: var(--gh-weight-regular);
  color: var(--gh-text-secondary);
  padding: 2px 8px;
  background: var(--gh-border-light);
  border-radius: var(--gh-radius-xs);
}

.card-points {
  font-size: var(--gh-font-helper);
  color: var(--gh-danger);
  padding: 2px 6px;
  background: rgba(239, 68, 68, 0.08);
  border-radius: var(--gh-radius-xs);
}

.card-actions {
  display: flex;
  gap: var(--gh-space-4);
}


/* 详情弹窗 */
.detail-content {
  .detail-row {
    display: flex;
    margin-bottom: 12px;
    font-size: 14px;

    .detail-label {
      width: 80px;
      color: #6B7280;
      flex-shrink: 0;
    }

    .detail-value {
      color: #1F2937;
      flex: 1;

      &.prompt-text {
        background: #F9FAFB;
        padding: 8px 12px;
        border-radius: 6px;
        word-break: break-word;
        line-height: 1.6;
      }

      &.status-done { color: #22C55E; }
      &.status-processing { color: #2563FF; }
      &.status-failed { color: #EF4444; }
    }
  }

  .detail-images {
    margin-top: 16px;

    .detail-label {
      color: #6B7280;
      margin-bottom: 12px;
      font-size: 14px;
    }

    .image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 12px;
    }

    .image-item {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #E8EDF5;

      img {
        width: 100%;
        height: 140px;
        object-fit: cover;
        cursor: pointer;
        display: block;
      }

      :deep(.el-button) {
        position: absolute;
        right: 4px;
        bottom: 4px;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 4px;
      }
    }
  }
}
</style>
