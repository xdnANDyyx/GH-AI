<template>
  <div class="admin-content">
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane label="素材管理" name="materials" />
      <el-tab-pane label="模板管理" name="templates" />
      <el-tab-pane label="Banner配置" name="banners" />
      <el-tab-pane label="推荐位管理" name="recommend" />
    </el-tabs>

    <!-- 素材管理 -->
    <div v-if="activeTab === 'materials'" class="tab-content">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="materialSearch" placeholder="搜索素材名称" clearable style="width: 220px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="materialCategory" placeholder="素材分类" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="客厅" value="客厅" />
            <el-option label="卧室" value="卧室" />
            <el-option label="厨房" value="厨房" />
            <el-option label="户外" value="户外" />
          </el-select>
          <el-select v-model="materialStatus" placeholder="审核状态" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="已通过" value="已通过" />
            <el-option label="待审核" value="待审核" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button type="primary" @click="showUploadDialog = true">
            <el-icon><Plus /></el-icon>上传素材
          </el-button>
        </div>
      </div>

      <div class="material-grid">
        <div class="material-card" v-for="item in materialList" :key="item.id">
          <div class="material-cover" :style="{ background: item.coverColor }">
            <el-icon :size="32" color="rgba(255,255,255,0.6)"><Picture /></el-icon>
            <el-tag class="material-status-tag" :type="item.status === '已通过' ? 'success' : item.status === '待审核' ? 'warning' : 'danger'" size="small">{{ item.status }}</el-tag>
          </div>
          <div class="material-info">
            <div class="material-name">{{ item.name }}</div>
            <div class="material-meta">
              <span>{{ item.category }}</span>
              <span>{{ item.downloads }} 次下载</span>
            </div>
            <div class="material-actions">
              <el-button link type="primary" size="small">编辑</el-button>
              <el-button v-if="item.status === '待审核'" link type="success" size="small" @click="approveMaterial(item)">通过</el-button>
              <el-button v-if="item.status === '待审核'" link type="danger" size="small" @click="rejectMaterial(item)">拒绝</el-button>
              <el-button link type="danger" size="small" @click="deleteMaterial(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板管理 -->
    <div v-if="activeTab === 'templates'" class="tab-content">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="templateSearch" placeholder="搜索模板名称" clearable style="width: 220px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="templateType" placeholder="模板类型" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="主图" value="主图" />
            <el-option label="详情页" value="详情页" />
            <el-option label="Banner" value="Banner" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>新建模板
          </el-button>
        </div>
      </div>

      <div class="table-card">
        <el-table :data="templateList" :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontSize: '12px' }">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="模板名称" min-width="180" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="useCount" label="使用次数" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '已上线' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small">编辑</el-button>
              <el-button link :type="row.status === '已上线' ? 'warning' : 'success'" size="small">
                {{ row.status === '已上线' ? '下线' : '上线' }}
              </el-button>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Banner配置 -->
    <div v-if="activeTab === 'banners'" class="tab-content">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-tip">管理首页轮播Banner图，支持拖拽排序</span>
        </div>
        <div class="filter-right">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>新增Banner
          </el-button>
        </div>
      </div>

      <div class="banner-list">
        <div class="banner-item" v-for="item in bannerList" :key="item.id">
          <div class="banner-drag">
            <el-icon :size="16"><Rank /></el-icon>
          </div>
          <div class="banner-preview" :style="{ background: item.color }">
            <span class="banner-preview-text">{{ item.title }}</span>
          </div>
          <div class="banner-info">
            <div class="banner-title">{{ item.title }}</div>
            <div class="banner-desc">{{ item.desc }}</div>
          </div>
          <div class="banner-status">
            <el-switch v-model="item.enabled" active-text="启用" inactive-text="禁用" size="small" />
          </div>
          <div class="banner-actions">
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐位管理 -->
    <div v-if="activeTab === 'recommend'" class="tab-content">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-tip">管理素材广场推荐位，控制首页展示内容</span>
        </div>
        <div class="filter-right">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>新增推荐位
          </el-button>
        </div>
      </div>

      <div class="table-card">
        <el-table :data="recommendList" :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontSize: '12px' }">
          <el-table-column prop="position" label="位置" width="80" />
          <el-table-column prop="title" label="推荐标题" min-width="180" />
          <el-table-column prop="type" label="推荐类型" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="clickCount" label="点击量" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small">编辑</el-button>
              <el-button link :type="row.enabled ? 'warning' : 'success'" size="small">
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('materials')

// ========== 素材管理 ==========
const materialSearch = ref('')
const materialCategory = ref('')
const materialStatus = ref('')
const showUploadDialog = ref(false)

const materialList = ref([
  { id: 1, name: '现代简约客厅', category: '客厅', status: '已通过', downloads: 1280, coverColor: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { id: 2, name: '北欧风卧室', category: '卧室', status: '已通过', downloads: 956, coverColor: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { id: 3, name: '中式厨房', category: '厨房', status: '待审核', downloads: 0, coverColor: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { id: 4, name: '户外花园', category: '户外', status: '已通过', downloads: 724, coverColor: 'linear-gradient(135deg, #43e97b, #38f9d7)' },
  { id: 5, name: '极简书房', category: '客厅', status: '已拒绝', downloads: 0, coverColor: 'linear-gradient(135deg, #fa709a, #fee140)' },
  { id: 6, name: '轻奢主卧', category: '卧室', status: '待审核', downloads: 0, coverColor: 'linear-gradient(135deg, #a18cd1, #fbc2eb)' }
])

function approveMaterial(item) {
  item.status = '已通过'
  ElMessage.success(`素材"${item.name}"已通过审核`)
}

function rejectMaterial(item) {
  item.status = '已拒绝'
  ElMessage.warning(`素材"${item.name}"已拒绝`)
}

function deleteMaterial(item) {
  ElMessageBox.confirm(`确定要删除素材"${item.name}"吗？`, '确认删除', { type: 'warning' }).then(() => {
    materialList.value = materialList.value.filter(m => m.id !== item.id)
    ElMessage.success('素材已删除')
  }).catch(() => {})
}

// ========== 模板管理 ==========
const templateSearch = ref('')
const templateType = ref('')

const templateList = ref([
  { id: 1, name: '800x800 白底主图模板', type: '主图', useCount: 3240, status: '已上线', updateTime: '2026-07-10 14:20' },
  { id: 2, name: 'A+ 标准详情模板', type: '详情页', useCount: 1856, status: '已上线', updateTime: '2026-07-08 10:15' },
  { id: 3, name: '大促Banner模板', type: 'Banner', useCount: 980, status: '已上线', updateTime: '2026-07-05 16:30' },
  { id: 4, name: '1:1 场景主图模板', type: '主图', useCount: 0, status: '草稿', updateTime: '2026-07-13 09:45' },
  { id: 5, name: '手机端详情模板', type: '详情页', useCount: 620, status: '已上线', updateTime: '2026-06-28 11:00' }
])

// ========== Banner配置 ==========
const bannerList = ref([
  { id: 1, title: 'AI智能作图，效率翻倍', desc: '新用户首月5折优惠', color: 'linear-gradient(135deg, #667eea, #764ba2)', enabled: true },
  { id: 2, title: '批量处理功能上线', desc: '一次处理100张图片', color: 'linear-gradient(135deg, #f093fb, #f5576c)', enabled: true },
  { id: 3, title: '素材广场全新改版', desc: '海量场景素材等你探索', color: 'linear-gradient(135deg, #4facfe, #00f2fe)', enabled: false }
])

// ========== 推荐位管理 ==========
const recommendList = ref([
  { position: 1, title: '精选客厅场景', type: '素材合集', clickCount: 5680, enabled: true },
  { position: 2, title: '新品上架推荐', type: '模板推荐', clickCount: 3420, enabled: true },
  { position: 3, title: '设计师精选', type: '作品推荐', clickCount: 2890, enabled: true },
  { position: 4, title: '夏日清凉场景', type: '素材合集', clickCount: 0, enabled: false }
])
</script>

<style lang="scss" scoped>
.admin-content {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.content-tabs {
  background: #fff;
  border-radius: 12px 12px 0 0;
  padding: 0 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;

    &.is-active {
      color: var(--gh-primary);
    }
  }

  :deep(.el-tabs__active-bar) {
    background: var(--gh-primary);
    height: 3px;
    border-radius: 2px;
  }
}

.tab-content {
  background: #fff;
  border-radius: 0 0 12px 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-tip {
  font-size: 13px;
  color: #94a3b8;
}

// ========== 素材网格 ==========
.material-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.material-card {
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
  }
}

.material-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.material-status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}

.material-info {
  padding: 12px;
}

.material-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 6px;
}

.material-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.material-actions {
  display: flex;
  gap: 4px;
}

// ========== 表格卡片 ==========
.table-card {
  :deep(.el-table) {
    --el-table-border-color: #f1f5f9;
    --el-table-header-bg-color: #f8fafc;
    --el-table-header-text-color: #64748b;
    border-radius: 8px;
    overflow: hidden;
  }
}

// ========== Banner列表 ==========
.banner-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.banner-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  }
}

.banner-drag {
  color: #cbd5e1;
  cursor: grab;
  padding: 4px;
}

.banner-preview {
  width: 120px;
  height: 48px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.banner-preview-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

.banner-info {
  flex: 1;
  min-width: 0;
}

.banner-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.banner-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.banner-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
