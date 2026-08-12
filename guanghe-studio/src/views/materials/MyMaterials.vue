<template>
  <div class="my-materials-page">
    <!-- Page Header -->
    <div class="page-head">
      <div class="head-left">
        <h2 class="gh-page-title">我的上传</h2>
        <p class="gh-page-subtitle">管理已上传的素材资源</p>
      </div>
      <el-button type="primary" class="gh-btn-upload">
        <el-icon><Upload /></el-icon>
        上传素材
      </el-button>
    </div>

    <!-- Search toolbar -->
    <div class="toolbar">
      <el-input
        v-model="searchText"
        placeholder="搜索素材名称、关键词"
        clearable
        class="gh-search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- Filter tabs -->
    <el-tabs v-model="activeTab" class="gh-tabs">
      <el-tab-pane label="全部素材" name="all" />
      <el-tab-pane label="图片素材" name="image" />
      <el-tab-pane label="设计稿" name="design" />
      <el-tab-pane label="参考图" name="reference" />
    </el-tabs>

    <!-- Sort row + Stats bar -->
    <div class="sort-stats-row">
      <div class="sort-block">
        <span class="sort-label">排序：</span>
        <el-select v-model="sortBy" size="small" class="gh-sort-select">
          <el-option label="上传时间" value="upload" />
          <el-option label="名称" value="name" />
          <el-option label="大小" value="size" />
        </el-select>
        <div class="view-toggle">
          <div class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
          </div>
        </div>
      </div>
      <div class="stats-bar">
        <span class="stats-text">共 <strong>{{ totalItems }}</strong> 个素材 · 已使用 {{ usedSpace }} / {{ totalSpace }}</span>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: (usedPercent) + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- Main grid -->
    <div class="mat-grid">
      <div v-for="m in items" :key="m.id" class="mat-card">
        <div class="mat-thumb" :style="{ background: m.bg }">
          <div class="mat-icon">{{ m.icon }}</div>
          <span class="mat-type-badge" :class="m.fileType">{{ m.typeLabel }}</span>
        </div>
        <div class="mat-body">
          <div class="mat-title">{{ m.title }}</div>
          <div class="mat-meta">
            <span class="meta-item">{{ m.date }}</span>
            <span class="meta-dot">·</span>
            <span class="meta-item">{{ m.size }}</span>
            <span class="meta-dot">·</span>
            <span class="meta-item">{{ m.dimensions }}</span>
          </div>
          <div class="mat-usage">
            <el-icon><DocumentCopy /></el-icon>
            已使用 {{ m.usage }} 次
          </div>
          <div class="mat-actions">
            <el-button class="act-btn" size="small" text>
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button class="act-btn" size="small" text>
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button class="act-btn danger" size="small" text>
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Upload zone -->
    <div class="upload-zone" @click="triggerUpload">
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="upload-text">
        拖拽文件到此处上传，或<span class="highlight">点击选择文件</span>
      </div>
      <div class="upload-hint">支持 JPG、PNG、PSD、AI、Figma 格式，单个文件不超过 50MB</div>
    </div>

    <!-- Pagination -->
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="6"
        :total="totalItems"
        layout="prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const searchText = ref('')
const activeTab = ref('all')
const sortBy = ref('upload')
const viewMode = ref('grid')
const currentPage = ref(1)
const totalItems = ref(56)
const usedSpace = ref('2.3GB')
const totalSpace = ref('10GB')
const usedPercent = computed(() => 23)

const triggerUpload = () => {
  // placeholder for file picker
}

const items = ref([
  {
    id: 1,
    title: '奶油风客厅主图场景.png',
    icon: '🛋️',
    bg: 'linear-gradient(135deg, #FDF2E9 0%, #F5E6D3 50%, #E8D5B7 100%)',
    fileType: 'image',
    typeLabel: 'PNG',
    date: '2026-03-10',
    size: '12.6 MB',
    dimensions: '2000×2000',
    usage: 3,
  },
  {
    id: 2,
    title: '原木风卧室床铺套图.psd',
    icon: '🛏️',
    bg: 'linear-gradient(135deg, #E8DCC4 0%, #D4B896 50%, #B8956A 100%)',
    fileType: 'design',
    typeLabel: 'PSD',
    date: '2026-03-08',
    size: '48.2 MB',
    dimensions: '3000×3000',
    usage: 5,
  },
  {
    id: 3,
    title: '北欧风餐桌场景图.jpg',
    icon: '🍽️',
    bg: 'linear-gradient(135deg, #D6E6F2 0%, #AACBE2 50%, #7BAFD4 100%)',
    fileType: 'image',
    typeLabel: 'JPG',
    date: '2026-03-12',
    size: '8.4 MB',
    dimensions: '2400×1600',
    usage: 1,
  },
  {
    id: 4,
    title: '日式书房参考图.jpg',
    icon: '📚',
    bg: 'linear-gradient(135deg, #EDE7D9 0%, #D4C5A0 50%, #B8A476 100%)',
    fileType: 'reference',
    typeLabel: '参考',
    date: '2026-03-05',
    size: '6.1 MB',
    dimensions: '1800×1200',
    usage: 2,
  },
  {
    id: 5,
    title: '法式客厅装饰柜.ai',
    icon: '🪞',
    bg: 'linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 50%, #CE93D8 100%)',
    fileType: 'design',
    typeLabel: 'AI',
    date: '2026-03-02',
    size: '32.8 MB',
    dimensions: '4000×4000',
    usage: 0,
  },
  {
    id: 6,
    title: '现代简约落地灯场景.png',
    icon: '💡',
    bg: 'linear-gradient(135deg, #FFF3E0 0%, #FFD180 50%, #FFAB40 100%)',
    fileType: 'image',
    typeLabel: 'PNG',
    date: '2026-02-28',
    size: '14.2 MB',
    dimensions: '2000×2000',
    usage: 8,
  },
])
</script>

<style lang="scss" scoped>
.my-materials-page {
  padding: var(--gh-space-24);
  min-height: 100%;
  background: var(--gh-bg-page);
}

/* ===== Page Header ===== */
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--gh-space-24);
}

.gh-btn-upload {
  height: var(--gh-btn-height);
  border-radius: var(--gh-radius-btn);
  background: var(--gh-primary);
  border-color: var(--gh-primary);
  padding: 0 18px;
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
}

/* ===== Search ===== */
.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: var(--gh-space-16);
  gap: var(--gh-space-12);
}

.gh-search-input {
  flex: 1;
  max-width: 520px;
  height: var(--gh-input-height);
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-btn);
    box-shadow: 0 0 0 1px var(--gh-border) inset;
    padding: 0 var(--gh-space-16);
  }
}

/* ===== Tabs ===== */
.gh-tabs {
  margin-bottom: var(--gh-space-20);
  :deep(.el-tabs__item) {
    font-size: var(--gh-font-body);
    font-weight: var(--gh-weight-medium);
  }
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: var(--gh-border);
  }
  :deep(.el-tabs__active-bar) {
    background: var(--gh-primary);
    height: 3px;
  }
}

/* ===== Sort + Stats Row ===== */
.sort-stats-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gh-space-20);
  flex-wrap: wrap;
  gap: var(--gh-space-12);
}

.sort-block {
  display: flex;
  align-items: center;
  gap: 10px;
}
.sort-label {
  color: var(--gh-text-secondary);
  font-size: 13px;
}
.gh-sort-select {
  width: 120px;
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-btn);
  }
}
.view-toggle {
  display: flex;
  background: #F5F6F8;
  border-radius: var(--gh-radius-btn);
  padding: 3px;
  .view-btn {
    width: 32px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 7px;
    cursor: pointer;
    color: var(--gh-text-placeholder);
    transition: all 0.2s;
    &.active {
      background: #fff;
      color: var(--gh-primary);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    }
  }
}

.stats-bar {
  display: flex;
  align-items: center;
  gap: var(--gh-space-12);
  .stats-text {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    white-space: nowrap;
    strong {
      color: var(--gh-text-primary);
      font-weight: var(--gh-weight-semibold);
    }
  }
}
.progress-track {
  width: 120px;
  height: 6px;
  background: var(--gh-border-light);
  border-radius: 3px;
  overflow: hidden;
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--gh-primary), var(--gh-success));
    border-radius: 3px;
    transition: width 0.3s;
  }
}

/* ===== Card Grid ===== */
.mat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--gh-space-20);
  margin-bottom: var(--gh-space-24);
}

.mat-card {
  padding: 0;
  overflow: hidden;
  border-radius: var(--gh-radius-card);
  border: 1px solid var(--gh-border);
  background: var(--gh-bg-card);
  box-shadow: var(--gh-shadow-card);
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--gh-shadow-hover);
  }
}

.mat-thumb {
  height: 180px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  .mat-icon { font-size: 60px; opacity: 0.85; }
}

.mat-type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 11px;
  font-weight: var(--gh-weight-semibold);
  padding: 3px 10px;
  border-radius: var(--gh-radius-sm);
  color: #fff;
  &.image { background: var(--gh-primary); }
  &.design { background: #8B5CF6; }
  &.reference { background: var(--gh-success); }
}

.mat-body {
  padding: var(--gh-space-12) var(--gh-space-16) var(--gh-space-16);
}

.mat-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mat-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: var(--gh-space-8);
  .meta-dot { color: var(--gh-text-placeholder); }
}

.mat-usage {
  display: flex;
  align-items: center;
  gap: var(--gh-space-4);
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: var(--gh-space-12);
  padding-bottom: var(--gh-space-12);
  border-bottom: 1px solid var(--gh-border-light);
}

.mat-actions {
  display: flex;
  gap: var(--gh-space-4);
  .act-btn {
    flex: 1;
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    padding: 6px 0;
    border-radius: var(--gh-radius-sm);
    transition: all 0.2s;
    &:hover {
      color: var(--gh-primary);
      background: var(--gh-primary-bg);
    }
    &.danger:hover {
      color: var(--gh-danger);
      background: rgba(239, 68, 68, 0.06);
    }
  }
}

/* ===== Upload Zone ===== */
.upload-zone {
  border: 2px dashed var(--gh-border);
  border-radius: var(--gh-radius-card);
  padding: var(--gh-space-40) var(--gh-space-32);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #FAFBFC;
  margin-bottom: var(--gh-space-24);

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);
  }

  .upload-icon {
    font-size: 40px;
    color: var(--gh-primary);
    margin-bottom: var(--gh-space-12);
  }

  .upload-text {
    font-size: var(--gh-font-body);
    color: var(--gh-text-secondary);
    margin-bottom: var(--gh-space-8);
    .highlight {
      color: var(--gh-primary);
      font-weight: var(--gh-weight-medium);
    }
  }

  .upload-hint {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-placeholder);
  }
}

/* ===== Pagination ===== */
.pagination-row {
  display: flex;
  justify-content: center;
  padding: var(--gh-space-8) 0;
  :deep(.el-pagination) {
    .btn-prev,
    .btn-next,
    .el-pager li {
      border-radius: var(--gh-radius-sm);
    }
    .el-pager li.is-active {
      background: var(--gh-primary);
      color: #fff;
    }
  }
}
</style>
