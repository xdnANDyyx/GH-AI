<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">回收站</h2>
        <p class="gh-page-subtitle">已删除的文件将在此保留30天后自动清除</p>
      </div>
      <div class="header-actions">
        <el-button type="danger" plain round :disabled="selected.length === 0" @click="batchDelete">
          <el-icon><Delete /></el-icon>
          彻底删除 ({{ selected.length }})
        </el-button>
        <el-button round @click="restoreAll">
          <el-icon><RefreshLeft /></el-icon>
          全部恢复
        </el-button>
      </div>
    </div>

    <!-- Info Banner -->
    <div class="info-banner">
      <el-icon><InfoFilled /></el-icon>
      <span>回收站中共有 <strong>{{ recycleItems.length }}</strong> 个文件，占存储空间 <strong>{{ totalSize }}</strong></span>
    </div>

    <!-- Table -->
    <div class="table-wrapper gh-card">
      <el-table :data="recycleItems" @selection-change="handleSelectionChange" style="width: 100%" :header-cell-style="{ background: '#F9FAFB', color: '#374151', fontWeight: 600 }">
        <el-table-column type="selection" width="48" />
        <el-table-column label="文件名" min-width="240">
          <template #default="{ row }">
            <div class="file-cell">
              <div class="file-thumb" :style="{ background: row.thumbBg }">
                <el-icon :size="16"><component :is="row.icon" /></el-icon>
              </div>
              <div class="file-info">
                <span class="file-name">{{ row.name }}</span>
                <span class="file-path">{{ row.path }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <span class="type-badge" :class="row.typeClass">{{ row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deleteTime" label="删除时间" width="160" />
        <el-table-column prop="size" label="大小" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="restoreItem(row)">
              <el-icon><RefreshLeft /></el-icon>
              恢复
            </el-button>
            <el-button text type="danger" size="small" @click="deleteForever(row)">
              <el-icon><Delete /></el-icon>
              永久删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Bottom Info -->
    <div class="bottom-info">
      <span class="expire-notice">文件将在删除后保留30天，过期后将自动永久删除</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const selected = ref([])

const recycleItems = ref([
  {
    name: '旧版主图_沙发A.png',
    path: '夏季新品系列 / 主图',
    type: '主图设计',
    typeClass: 'type-hero',
    deleteTime: '2024-05-30 14:22',
    size: '4.2 MB',
    icon: 'DataBoard',
    thumbBg: 'linear-gradient(135deg, #E0E7FF, #C7D2FE)',
  },
  {
    name: '测试背景_v2.png',
    path: '家居场景图 / 背景',
    type: '背景图',
    typeClass: 'type-bg',
    deleteTime: '2024-05-28 09:15',
    size: '2.8 MB',
    icon: 'PictureFilled',
    thumbBg: 'linear-gradient(135deg, #D1FAE5, #A7F3D0)',
  },
  {
    name: '废弃Banner_618.png',
    path: '品牌Banner / 活动',
    type: 'Banner',
    typeClass: 'type-banner',
    deleteTime: '2024-05-25 16:40',
    size: '1.5 MB',
    icon: 'Postcard',
    thumbBg: 'linear-gradient(135deg, #FCE7F3, #FBCFE8)',
  },
  {
    name: '产品详情_初稿.psd',
    path: '产品详情A+ / 草稿',
    type: '详情图',
    typeClass: 'type-detail',
    deleteTime: '2024-05-22 11:30',
    size: '18.6 MB',
    icon: 'Document',
    thumbBg: 'linear-gradient(135deg, #FEF3C7, #FDE68A)',
  },
  {
    name: 'AI模特_测试批次.jpg',
    path: '618大促主图 / 模特',
    type: 'AI模特',
    typeClass: 'type-model',
    deleteTime: '2024-05-20 08:55',
    size: '3.1 MB',
    icon: 'User',
    thumbBg: 'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  },
])

const totalSize = computed(() => {
  const sizes = recycleItems.value.map(item => parseFloat(item.size))
  const total = sizes.reduce((a, b) => a + b, 0)
  return total.toFixed(1) + ' MB'
})

function handleSelectionChange(val) {
  selected.value = val
}

function restoreItem(row) {
  console.log('Restore:', row.name)
}

function deleteForever(row) {
  console.log('Delete forever:', row.name)
}

function batchDelete() {
  console.log('Batch delete:', selected.value.length, 'items')
}

function restoreAll() {
  console.log('Restore all')
}
</script>

<style lang="scss" scoped>
.page-container { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

.header-actions {
  display: flex;
  gap: 8px;
}

.info-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #EFF6FF;
  border: 1px solid #DBEAFE;
  border-radius: 10px;
  font-size: 13px;
  color: #1E40AF;
  margin-bottom: 16px;

  .el-icon { font-size: 16px; color: #2563FF; }

  strong { font-weight: 600; }
}

.table-wrapper {
  padding: 0;
  overflow: hidden;
}

.file-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-thumb {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gh-primary);
}

.file-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--gh-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-path {
  font-size: 11px;
  color: var(--gh-text-placeholder);
  margin-top: 2px;
}

.type-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 6px;

  &.type-hero { background: #E0E7FF; color: #4F46E5; }
  &.type-bg { background: #D1FAE5; color: #059669; }
  &.type-banner { background: #FCE7F3; color: #DB2777; }
  &.type-detail { background: #FEF3C7; color: #D97706; }
  &.type-model { background: #F3E8FF; color: #7C3AED; }
}

.bottom-info {
  margin-top: 16px;
  text-align: center;
}

.expire-notice {
  font-size: 12px;
  color: var(--gh-text-placeholder);
}
</style>
