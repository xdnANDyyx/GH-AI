<template>
  <div class="fav-page">
    <!-- Page Header -->
    <div class="page-head">
      <div class="head-left">
        <div class="page-title-row">
          <el-icon class="title-heart"><StarFilled /></el-icon>
          <h2 class="gh-page-title">我的收藏</h2>
        </div>
        <p class="gh-page-subtitle">收藏的优质素材，方便快速查找和使用</p>
      </div>
    </div>

    <!-- Search -->
    <div class="toolbar">
      <el-input
        v-model="searchText"
        placeholder="搜索你收藏的素材"
        clearable
        class="gh-search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="gh-tabs">
      <el-tab-pane label="全部收藏" name="all" />
      <el-tab-pane label="文件夹" name="folder" />
      <el-tab-pane label="标签管理" name="tag" />
    </el-tabs>

    <!-- Toolbar: select all + batch + sort -->
    <div class="action-toolbar">
      <div class="toolbar-left">
        <el-checkbox v-model="selectAll" @change="handleSelectAll" class="select-all-cb">
          全选
        </el-checkbox>
        <span class="selected-count">已选择 <strong>{{ selectedCount }}</strong> 项</span>
        <el-button class="batch-btn" size="small" :disabled="selectedCount === 0">
          批量管理
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select v-model="sortTime" size="small" class="gh-sort-select">
          <el-option label="收藏时间" value="time" />
          <el-option label="浏览量" value="views" />
          <el-option label="收藏量" value="saves" />
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
    </div>

    <!-- Body: grid + right panel -->
    <div class="layout-body">
      <!-- Main grid -->
      <main class="grid-wrap">
        <div class="fav-grid">
          <div v-for="m in items" :key="m.id" class="fav-card">
            <div class="fav-thumb" :style="{ background: m.bg }">
              <div class="fav-icon">{{ m.icon }}</div>
              <div class="fav-heart" :class="{ on: m.liked }" @click.stop="m.liked = !m.liked">
                <el-icon><StarFilled /></el-icon>
              </div>
            </div>
            <div class="fav-body">
              <div class="fav-title">{{ m.title }}</div>
              <div class="fav-creator">
                <span class="avatar" :style="{ background: m.avatarBg }">{{ m.avatarChar }}</span>
                <span>{{ m.creator }}</span>
              </div>
              <div class="fav-tags">
                <el-tag v-for="t in m.tags" :key="t" size="small" effect="plain" class="fav-tag">{{ t }}</el-tag>
              </div>
              <div class="fav-stats">
                <span><el-icon><View /></el-icon> {{ m.views }}</span>
                <span><el-icon><FolderOpened /></el-icon> {{ m.saves }}</span>
                <span><el-icon><Star /></el-icon> {{ m.likes }}</span>
              </div>
              <div class="fav-actions">
                <el-button class="ref-btn" size="small">
                  <el-icon><PictureFilled /></el-icon>
                  设为参考图
                </el-button>
                <el-button class="icon-btn" size="small" circle>
                  <el-icon><Share /></el-icon>
                </el-button>
                <el-dropdown trigger="click" @click.stop>
                  <el-button class="icon-btn" size="small" circle>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>移动到文件夹</el-dropdown-item>
                      <el-dropdown-item>添加标签</el-dropdown-item>
                      <el-dropdown-item divided>取消收藏</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-row">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="8"
            :total="64"
            layout="prev, pager, next"
            background
          />
        </div>
      </main>

      <!-- Right Panel -->
      <aside class="fav-panel">
        <!-- Stats hero -->
        <div class="panel-section stat">
          <div class="stat-hero">
            <div class="stat-heart-icon">
              <el-icon><StarFilled /></el-icon>
            </div>
            <span class="stat-num">128</span>
            <span class="stat-lbl">共收藏 128 个素材</span>
          </div>
        </div>

        <!-- Folders -->
        <div class="panel-section">
          <h3>文件夹</h3>
          <ul class="folder-list">
            <li
              v-for="f in visibleFolders"
              :key="f.name"
              :class="{ active: activeFolder === f.name }"
              @click="activeFolder = f.name"
            >
              <div class="f-left">
                <el-icon><FolderOpened /></el-icon>
                <span>{{ f.name }}</span>
              </div>
              <span class="f-count">{{ f.count }}</span>
            </li>
          </ul>
          <div class="new-folder">
            <el-icon><Plus /></el-icon>
            <span>新建文件夹</span>
          </div>
          <div v-if="folders.length > 6" class="expand-link" @click="showAllFolders = !showAllFolders">
            {{ showAllFolders ? '收起' : '展开更多(' + (folders.length - 6) + ')' }}
          </div>
        </div>

        <!-- Tags -->
        <div class="panel-section">
          <h3>标签</h3>
          <div class="tag-cloud">
            <el-tag v-for="t in tags" :key="t.name" class="cloud-tag" effect="plain" size="large">
              {{ t.name }} <span class="t-cnt">{{ t.count }}</span>
            </el-tag>
          </div>
          <div class="more-tags">更多标签 →</div>
        </div>

        <!-- Tips -->
        <div class="panel-section tip">
          <div class="tip-head">
            <el-icon><InfoFilled /></el-icon>
            <h3>小贴士</h3>
          </div>
          <p>收藏素材后可一键设为参考图，在设计工作台中快速调用；也可以使用文件夹+标签双重管理，让灵感归类更高效。</p>
          <a class="tip-link">查看教程 →</a>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const searchText = ref('')
const activeTab = ref('all')
const activeFolder = ref('客厅场景')
const viewMode = ref('grid')
const sortTime = ref('time')
const currentPage = ref(1)
const selectAll = ref(false)
const selectedCount = ref(0)
const showAllFolders = ref(false)

const handleSelectAll = (val) => {
  selectedCount.value = val ? items.value.length : 0
}

const items = ref([
  { id: 1, title: '奶油风客厅沙发场景', icon: '🛋️', bg: 'url(/images/cream-livingroom.png) center/cover no-repeat', tags: ['客厅', '奶油风', '自然光'], creator: '设计师小光', avatarChar: '小', avatarBg: '#2563FF', views: '1.2k', saves: 356, likes: 98, liked: true },
  { id: 2, title: '原木风卧室床铺场景', icon: '🛏️', bg: 'url(/images/modern-bedroom.png) center/cover no-repeat', tags: ['卧室', '原木风', '暖光'], creator: '木子设计', avatarChar: '木', avatarBg: '#22C55E', views: '862', saves: 228, likes: 64, liked: true },
  { id: 3, title: '北欧风餐厅实木餐桌', icon: '🍽️', bg: 'url(/images/kitchen-dining.png) center/cover no-repeat', tags: ['餐厅', '北欧风', '自然光'], creator: '家居研究所', avatarChar: '研', avatarBg: '#F59E0B', views: '2.4k', saves: 489, likes: 132, liked: true },
  { id: 4, title: '日式书房阅读角落', icon: '📚', bg: 'url(/images/japanese-room.png) center/cover no-repeat', tags: ['书房', '日式', '暖光'], creator: '和风设计', avatarChar: '和', avatarBg: '#EF4444', views: '512', saves: 146, likes: 42, liked: true },
  { id: 5, title: '法式客厅装饰柜', icon: '🪞', bg: 'url(/images/chair-scene-bg.png) center/cover no-repeat', tags: ['客厅', '法式', '商业空间'], creator: '巴黎美学', avatarChar: '巴', avatarBg: '#8B5CF6', views: '998', saves: 312, likes: 88, liked: true },
  { id: 6, title: '现代简约落地灯', icon: '💡', bg: 'url(/images/nordic-office.png) center/cover no-repeat', tags: ['卧室', '现代简约', '暖光'], creator: '光合设计', avatarChar: '光', avatarBg: '#2563FF', views: '3.2k', saves: 768, likes: 215, liked: true },
  { id: 7, title: '工业风户外装饰摆件', icon: '🪴', bg: 'url(/images/industrial-livingroom.png) center/cover no-repeat', tags: ['户外', '工业风', '自然光'], creator: '创意工坊', avatarChar: '创', avatarBg: '#64748B', views: '326', saves: 98, likes: 28, liked: true },
  { id: 8, title: '奶油风中古茶几', icon: '☕', bg: 'url(/images/outdoor-patio.png) center/cover no-repeat', tags: ['客厅', '奶油风', '自然光'], creator: '家居研究所', avatarChar: '研', avatarBg: '#F59E0B', views: '1.6k', saves: 426, likes: 118, liked: true },
])

const folders = [
  { name: '客厅场景', count: 32 },
  { name: '卧室场景', count: 28 },
  { name: '餐厅场景', count: 18 },
  { name: '户外场景', count: 16 },
  { name: '办公场景', count: 12 },
  { name: '灵感参考', count: 22 },
  { name: '商业空间', count: 14 },
  { name: '浴室设计', count: 10 },
  { name: '儿童房', count: 8 },
]

const visibleFolders = computed(() => {
  return showAllFolders.value ? folders.value : folders.value.slice(0, 6)
})

const tags = [
  { name: '奶油风', count: 28 },
  { name: '原木风', count: 26 },
  { name: '现代简约', count: 24 },
  { name: '北欧风', count: 22 },
  { name: '自然光', count: 36 },
  { name: '曝光', count: 18 },
  { name: '商业空间', count: 20 },
  { name: '日式风', count: 16 },
]
</script>

<style lang="scss" scoped>
.fav-page {
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

.page-title-row {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
}

.title-heart {
  color: #EF4444;
  font-size: 24px;
}

/* ===== Search ===== */
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: var(--gh-space-16);
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
  margin-bottom: var(--gh-space-16);
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

/* ===== Action Toolbar ===== */
.action-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--gh-space-20);
  flex-wrap: wrap;
  gap: var(--gh-space-12);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--gh-space-12);
  .select-all-cb {
    :deep(.el-checkbox__label) {
      font-size: var(--gh-font-body);
      color: var(--gh-text-primary);
    }
  }
  .selected-count {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    strong {
      color: var(--gh-text-primary);
      font-weight: var(--gh-weight-semibold);
    }
  }
  .batch-btn {
    height: 32px;
    border-radius: var(--gh-radius-sm);
    border: 1px solid var(--gh-border);
    color: var(--gh-text-secondary);
    background: #fff;
    font-size: var(--gh-font-helper);
    &:hover:not(:disabled) {
      border-color: var(--gh-primary);
      color: var(--gh-primary);
    }
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
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

/* ===== Body Layout ===== */
.layout-body {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--gh-space-20);
}

.grid-wrap { min-width: 0; }

/* ===== Card Grid ===== */
.fav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gh-space-16);
}

.fav-card {
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

.fav-thumb {
  height: 160px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  .fav-icon { font-size: 56px; opacity: 0.85; }
}

.fav-heart {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--gh-text-placeholder);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 16px;
  &:hover { color: #EF4444; transform: scale(1.1); }
  &.on { color: #EF4444; }
}

.fav-body {
  padding: var(--gh-space-12) 14px 14px;
}

.fav-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-creator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: var(--gh-space-8);
  .avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    color: #fff;
    font-size: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: var(--gh-weight-semibold);
  }
}

.fav-tags {
  display: flex;
  gap: var(--gh-space-4);
  flex-wrap: wrap;
  margin-bottom: 10px;
  .fav-tag {
    border-radius: var(--gh-radius-btn);
    font-size: 11px;
  }
}

.fav-stats {
  display: flex;
  gap: 10px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: var(--gh-space-12);
  span {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
}

.fav-actions {
  display: flex;
  gap: 6px;
  align-items: center;
  padding-top: var(--gh-space-12);
  border-top: 1px solid var(--gh-border-light);
  .ref-btn {
    flex: 1;
    border-radius: var(--gh-radius-sm);
    color: var(--gh-primary);
    background: #EEF3FF;
    border: none;
    font-size: var(--gh-font-helper);
    &:hover { background: #DFE9FF; }
  }
  .icon-btn {
    width: 28px;
    height: 28px;
    border-radius: var(--gh-radius-sm);
    border: 1px solid var(--gh-border);
    color: var(--gh-text-secondary);
    background: #fff;
    padding: 0;
    &:hover {
      color: var(--gh-primary);
      border-color: var(--gh-primary);
    }
  }
  .el-dropdown {
    .icon-btn { cursor: pointer; }
  }
}

/* ===== Pagination ===== */
.pagination-row {
  display: flex;
  justify-content: center;
  padding: var(--gh-space-24) 0 var(--gh-space-8);
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

/* ===== Right Panel ===== */
.fav-panel {
  display: flex;
  flex-direction: column;
  gap: var(--gh-space-16);
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}

.panel-section {
  background: var(--gh-bg-card);
  border: 1px solid var(--gh-border);
  border-radius: var(--gh-radius-card);
  padding: var(--gh-space-16);
  h3 {
    font-size: var(--gh-font-body);
    font-weight: var(--gh-weight-semibold);
    color: var(--gh-text-primary);
    margin-bottom: var(--gh-space-12);
    margin-top: 0;
  }
  &.stat {
    background: linear-gradient(135deg, #2563FF 0%, #3B82F6 100%);
    color: #fff;
    border: none;
  }
}

.stat-hero {
  text-align: center;
  padding: var(--gh-space-8) 0 var(--gh-space-12);
  .stat-heart-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto var(--gh-space-8);
    font-size: 24px;
  }
  .stat-num {
    display: block;
    font-size: 36px;
    font-weight: var(--gh-weight-semibold);
    line-height: 1;
    margin-bottom: var(--gh-space-4);
  }
  .stat-lbl {
    font-size: var(--gh-font-helper);
    opacity: 0.9;
  }
}

.folder-list {
  list-style: none;
  padding: 0;
  margin: 0 0 10px;
  li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--gh-space-8) 10px;
    border-radius: var(--gh-radius-sm);
    cursor: pointer;
    font-size: 13px;
    color: var(--gh-text-secondary);
    transition: all 0.15s;
    &:hover {
      background: #F4F7FF;
      color: var(--gh-primary);
    }
    &.active {
      background: #EEF3FF;
      color: var(--gh-primary);
      font-weight: var(--gh-weight-medium);
    }
    .f-left {
      display: flex;
      gap: var(--gh-space-8);
      align-items: center;
    }
    .f-count {
      font-size: 11px;
      background: #F0F1F3;
      padding: 1px 7px;
      border-radius: var(--gh-radius-btn);
      color: var(--gh-text-secondary);
    }
  }
  li.active .f-count {
    background: var(--gh-primary);
    color: #fff;
  }
}

.new-folder {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px;
  border: 1px dashed var(--gh-border);
  border-radius: var(--gh-radius-btn);
  color: var(--gh-primary);
  cursor: pointer;
  font-size: var(--gh-font-helper);
  justify-content: center;
  margin-bottom: var(--gh-space-8);
  &:hover { background: #F4F7FF; }
}

.expand-link {
  font-size: var(--gh-font-helper);
  color: var(--gh-primary);
  cursor: pointer;
  text-align: center;
  &:hover { text-decoration: underline; }
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  .cloud-tag {
    border-radius: 14px;
    font-size: var(--gh-font-helper);
    .t-cnt {
      margin-left: var(--gh-space-4);
      font-size: 10px;
      color: var(--gh-text-placeholder);
      font-weight: var(--gh-weight-regular);
    }
  }
}

.more-tags {
  margin-top: 10px;
  font-size: var(--gh-font-helper);
  color: var(--gh-primary);
  cursor: pointer;
  &:hover { text-decoration: underline; }
}

.panel-section.tip {
  background: #EEF8FF;
  border: 1px solid #D4ECFF;
  .tip-head {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--gh-primary);
    margin-bottom: var(--gh-space-8);
    h3 {
      margin: 0;
      color: var(--gh-primary);
      font-size: 13px;
    }
  }
  p {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    line-height: 1.7;
    margin: 0 0 10px;
  }
  .tip-link {
    font-size: var(--gh-font-helper);
    color: var(--gh-primary);
    cursor: pointer;
    font-weight: var(--gh-weight-medium);
    &:hover { text-decoration: underline; }
  }
}
</style>
