<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">我的项目</h2>
        <p class="gh-page-subtitle">管理您的所有设计项目</p>
      </div>
      <el-button type="primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>

    <!-- Project Stats Summary -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon :size="20"><Files /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ activeProjects.length }}</span>
          <span class="stat-label">总项目数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon :size="20"><Picture /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalImages }}</span>
          <span class="stat-label">总图片数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon :size="20"><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalStorage }}</span>
          <span class="stat-label">已用存储</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon :size="20"><Check /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ completedCount }}</span>
          <span class="stat-label">已完成</span>
        </div>
      </div>
    </div>

    <!-- Toolbar: Search + Filters + Sort + View Toggle -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchText"
          placeholder="搜索项目名称"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-select v-model="statusFilterLocal" placeholder="项目状态" clearable class="filter-select">
          <el-option label="全部状态" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          class="date-picker"
          clearable
        />
      </div>
      <div class="toolbar-right">
        <el-checkbox
          v-model="selectAll"
          :indeterminate="selectIndeterminate"
          @change="handleSelectAll"
          class="select-all-check"
        >
          全选
        </el-checkbox>

        <el-select v-model="sortByLocal" class="sort-select" placeholder="排序">
          <el-option label="按名称" value="name" />
          <el-option label="按创建日期" value="createdAt" />
          <el-option label="按最后修改" value="updatedAt" />
        </el-select>
        <el-button-group class="sort-dir-group">
          <el-button
            :type="sortOrderLocal === 'asc' ? 'primary' : 'default'"
            @click="setSortOrder('asc')"
            size="default"
          >
            <el-icon><Top /></el-icon>
          </el-button>
          <el-button
            :type="sortOrderLocal === 'desc' ? 'primary' : 'default'"
            @click="setSortOrder('desc')"
            size="default"
          >
            <el-icon><Bottom /></el-icon>
          </el-button>
        </el-button-group>

        <el-button-group class="view-toggle">
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            @click="viewMode = 'grid'"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
          >
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- Bulk Actions Bar -->
    <transition name="slide-down">
      <div v-if="selectedProjects.length > 0" class="bulk-actions">
        <span class="bulk-count">已选择 {{ selectedProjects.length }} 个项目</span>
        <el-button size="small" @click="bulkExport">
          <el-icon><Download /></el-icon>
          批量导出
        </el-button>
        <el-button size="small" type="danger" @click="bulkDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button size="small" text @click="clearSelection">取消选择</el-button>
      </div>
    </transition>

    <!-- Empty State -->
    <el-empty v-if="displayList.length === 0" description="暂无项目，点击右上角新建项目开始创作" />

    <!-- Projects Grid View -->
    <div v-if="viewMode === 'grid' && displayList.length > 0" class="projects-grid">
      <div
        v-for="project in displayList"
        :key="project.id"
        class="project-card gh-card"
        :class="{ selected: selectedProjects.includes(project.id) }"
        @click="openProject(project)"
      >
        <div class="card-checkbox" @click.stop>
          <el-checkbox
            :model-value="selectedProjects.includes(project.id)"
            @change="(val) => toggleSelect(project.id, val)"
          />
        </div>
        <div class="project-cover" :style="{ background: coverGradient(project) }">
          <img v-if="project.thumbnail" :src="project.thumbnail" class="cover-img" alt="封面" />
          <div class="cover-icon" v-else>
            <el-icon :size="32"><Folder /></el-icon>
          </div>
          <div class="cover-overlay">
            <span class="cover-count">{{ project.imageCount || 0 }} 项</span>
          </div>
        </div>
        <div class="project-body">
          <div class="project-name-row">
            <span class="project-name">{{ project.name }}</span>
            <span class="project-status" :class="statusClass(project.status)">{{ statusLabel(project.status) }}</span>
          </div>
          <div class="project-meta">
            <span>{{ project.imageCount || 0 }} 个文件</span>
            <span>修改: {{ formatDate(project.updatedAt) }}</span>
          </div>
          <div class="project-actions">
            <el-button text size="small" @click.stop="openEditDialog(project)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button text size="small" @click.stop="openProject(project)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button text type="danger" size="small" @click.stop="deleteProject(project)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- New Project Card -->
      <div class="project-card gh-card new-project" @click="openCreateDialog">
        <div class="new-project-inner">
          <el-icon :size="40"><Plus /></el-icon>
          <span>新建项目</span>
        </div>
      </div>
    </div>

    <!-- Projects List View -->
    <div v-if="viewMode === 'list' && displayList.length > 0" class="projects-list">
      <div class="list-header">
        <div class="list-col list-col-check">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="selectIndeterminate"
            @change="handleSelectAll"
          />
        </div>
        <div class="list-col list-col-name">项目名称</div>
        <div class="list-col list-col-count">文件数</div>
        <div class="list-col list-col-status">状态</div>
        <div class="list-col list-col-date">最后修改</div>
        <div class="list-col list-col-actions">操作</div>
      </div>
      <div
        v-for="project in displayList"
        :key="project.id"
        class="list-row"
        :class="{ selected: selectedProjects.includes(project.id) }"
        @click="openProject(project)"
      >
        <div class="list-col list-col-check" @click.stop>
          <el-checkbox
            :model-value="selectedProjects.includes(project.id)"
            @change="(val) => toggleSelect(project.id, val)"
          />
        </div>
        <div class="list-col list-col-name">
          <div class="name-cell">
            <div class="name-cover" :style="{ background: coverGradient(project) }">
              <img v-if="project.thumbnail" :src="project.thumbnail" class="cover-img" alt="" />
            </div>
            <span class="name-text">{{ project.name }}</span>
          </div>
        </div>
        <div class="list-col list-col-count">{{ project.imageCount || 0 }}</div>
        <div class="list-col list-col-status">
          <span class="project-status" :class="statusClass(project.status)">{{ statusLabel(project.status) }}</span>
        </div>
        <div class="list-col list-col-date">{{ formatDate(project.updatedAt) }}</div>
        <div class="list-col list-col-actions" @click.stop>
          <el-button text size="small" @click="openEditDialog(project)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button text type="danger" size="small" @click="deleteProject(project)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑项目弹窗 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formMode === 'create' ? '新建项目' : '编辑项目'"
      width="480px"
      :destroy-on-close="true"
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="项目名称" required>
          <el-input v-model="formData.name" placeholder="请输入项目名称" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="项目状态" v-if="formMode === 'edit'">
          <el-select v-model="formData.status" style="width: 100%;">
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 项目详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailProject ? detailProject.name : '项目详情'"
      width="800px"
      :destroy-on-close="true"
    >
      <div v-if="detailProject" class="project-detail">
        <div class="detail-info">
          <div class="info-row">
            <span class="info-label">状态：</span>
            <span class="project-status" :class="statusClass(detailProject.status)">{{ statusLabel(detailProject.status) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间：</span>
            <span>{{ formatFullTime(detailProject.createdAt) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最后修改：</span>
            <span>{{ formatFullTime(detailProject.updatedAt) }}</span>
          </div>
          <div class="info-row" v-if="detailProject.description">
            <span class="info-label">描述：</span>
            <span>{{ detailProject.description }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">图片数量：</span>
            <span>{{ detailProject.imageCount || 0 }} 张</span>
          </div>
        </div>
        <div class="detail-images-section">
          <div class="section-title">项目图片</div>
          <el-empty v-if="!detailProject.images || detailProject.images.length === 0" description="暂无图片，在工作台生成作品时可保存到此项目" />
          <div class="detail-image-grid" v-else>
            <div v-for="(img, i) in detailProject.images" :key="i" class="detail-image-item">
              <img :src="img" :alt="`图片${i+1}`" @click="previewImage(img)" />
              <el-button text size="small" class="img-download" @click="downloadImage(img, i)">
                <el-icon><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEditDialog(detailProject)">编辑项目</el-button>
      </template>
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
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Files, Picture, Coin, Check, Edit, Delete, View, Download,
  Grid, List, Top, Bottom, Folder,
} from '@element-plus/icons-vue'
import { useProjects } from '@/composables/useProjects'

const {
  activeProjects, displayProjects,
  sortBy, sortOrder, statusFilter,
  createProject, updateProject, deleteProject: removeProject,
  setSortBy, setSortOrder, setStatusFilter,
} = useProjects()

const searchText = ref('')
const statusFilterLocal = ref('')
const dateRange = ref(null)
const sortByLocal = ref('updatedAt')
const sortOrderLocal = ref('desc')
const viewMode = ref('grid')
const selectedProjects = ref([])

const formDialogVisible = ref(false)
const formMode = ref('create')
const formData = ref({ name: '', description: '', status: 'active' })
const editingId = ref(null)

const detailDialogVisible = ref(false)
const detailProject = ref(null)

const previewVisible = ref(false)
const previewUrl = ref('')

watch(statusFilterLocal, (v) => setStatusFilter(v))
watch(sortByLocal, (v) => setSortBy(v))
watch(sortOrderLocal, (v) => setSortOrder(v))

const COVER_GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #5ee7df 0%, #b490ca 100%)',
  'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
]

function coverGradient(project) {
  if (project.thumbnail) return '#F3F4F6'
  const idx = (project.name || '').charCodeAt(0) || 0
  return COVER_GRADIENTS[idx % COVER_GRADIENTS.length]
}

function statusLabel(s) {
  return { active: '进行中', completed: '已完成' }[s] || '进行中'
}
function statusClass(s) {
  return { active: 'status-active', completed: 'status-done' }[s] || 'status-active'
}

function formatDate(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
function formatFullTime(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  return `${formatDate(ts)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const totalImages = computed(() => activeProjects.value.reduce((sum, p) => sum + (p.imageCount || 0), 0))
const totalStorage = computed(() => {
  const totalMB = totalImages.value * 4
  return totalMB >= 1024 ? (totalMB / 1024).toFixed(1) + ' GB' : totalMB + ' MB'
})
const completedCount = computed(() => activeProjects.value.filter(p => p.status === 'completed').length)

const displayList = computed(() => {
  let list = displayProjects.value.slice()
  if (searchText.value) {
    const kw = searchText.value.toLowerCase()
    list = list.filter(p => (p.name || '').toLowerCase().includes(kw))
  }
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    list = list.filter(p => {
      const d = formatDate(p.updatedAt)
      return d >= start && d <= end
    })
  }
  return list
})

const selectAll = computed({
  get() {
    return displayList.value.length > 0 && displayList.value.every(p => selectedProjects.value.includes(p.id))
  },
  set() {}
})

const selectIndeterminate = computed(() => {
  const ids = displayList.value.map(p => p.id)
  const selectedInView = ids.filter(id => selectedProjects.value.includes(id))
  return selectedInView.length > 0 && selectedInView.length < ids.length
})

function handleSelectAll(val) {
  const ids = displayList.value.map(p => p.id)
  if (val) {
    selectedProjects.value = [...new Set([...selectedProjects.value, ...ids])]
  } else {
    selectedProjects.value = selectedProjects.value.filter(id => !ids.includes(id))
  }
}

function toggleSelect(id, val) {
  if (val) {
    if (!selectedProjects.value.includes(id)) selectedProjects.value.push(id)
  } else {
    selectedProjects.value = selectedProjects.value.filter(i => i !== id)
  }
}

function clearSelection() {
  selectedProjects.value = []
}

function bulkExport() {
  ElMessage.success(`已导出 ${selectedProjects.value.length} 个项目`)
}

function bulkDelete() {
  ElMessageBox.confirm(`确定将 ${selectedProjects.value.length} 个项目移入回收站吗？`, '提示', { type: 'warning' })
    .then(() => {
      selectedProjects.value.forEach(id => removeProject(id))
      const count = selectedProjects.value.length
      clearSelection()
      ElMessage.success(`已删除 ${count} 个项目`)
    })
    .catch(() => {})
}

function openProject(project) {
  detailProject.value = project
  detailDialogVisible.value = true
}

function openCreateDialog() {
  formMode.value = 'create'
  formData.value = { name: '', description: '', status: 'active' }
  editingId.value = null
  formDialogVisible.value = true
}

function openEditDialog(project) {
  formMode.value = 'edit'
  formData.value = {
    name: project.name,
    description: project.description || '',
    status: project.status || 'active',
  }
  editingId.value = project.id
  formDialogVisible.value = true
  detailDialogVisible.value = false
}

function submitForm() {
  if (!formData.value.name || !formData.value.name.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }
  if (formMode.value === 'create') {
    const p = createProject(formData.value.name.trim(), formData.value.description.trim())
    ElMessage.success('项目创建成功')
    formDialogVisible.value = false
    openProject(p)
  } else {
    updateProject(editingId.value, {
      name: formData.value.name.trim(),
      description: formData.value.description.trim(),
      status: formData.value.status,
    })
    ElMessage.success('项目更新成功')
    formDialogVisible.value = false
    if (detailProject.value && detailProject.value.id === editingId.value) {
      const updated = activeProjects.value.find(p => p.id === editingId.value)
      if (updated) detailProject.value = updated
    }
  }
}

function deleteProject(project) {
  ElMessageBox.confirm(`确定将项目「${project.name}」移入回收站吗？`, '提示', { type: 'warning' })
    .then(() => {
      removeProject(project.id)
      selectedProjects.value = selectedProjects.value.filter(id => id !== project.id)
      if (detailProject.value && detailProject.value.id === project.id) {
        detailDialogVisible.value = false
      }
      ElMessage.success('已移入回收站')
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
    link.download = `project_${Date.now()}_${index + 1}.png`
    link.click()
    URL.revokeObjectURL(link.href)
  } catch {
    ElMessage.warning('下载失败，请稍后重试')
  }
}
</script>

<style lang="scss" scoped>
.page-container { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

// ============ Stats Row ============
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  background: var(--gh-bg-card);
  border: 1px solid var(--gh-border);
  border-radius: var(--gh-radius-card);
  box-shadow: var(--gh-shadow-card);
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: var(--gh-shadow-hover);
  }
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--gh-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.blue { background: var(--gh-primary-bg); color: var(--gh-primary); }
  &.green { background: rgba(34, 197, 94, 0.1); color: var(--gh-success); }
  &.orange { background: rgba(245, 158, 11, 0.1); color: var(--gh-warning); }
  &.purple { background: rgba(124, 58, 237, 0.1); color: #7C3AED; }
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--gh-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--gh-text-secondary);
  margin-top: 2px;
}

// ============ Toolbar ============
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 140px;
}

.date-picker {
  width: 260px;
}

.sort-select {
  width: 130px;
}

.sort-dir-group {
  .el-button {
    padding: 8px 12px;
  }
}

.view-toggle {
  .el-button {
    padding: 8px 14px;
  }
}

.select-all-check {
  margin-right: 4px;
}

// ============ Bulk Actions ============
.bulk-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--gh-primary-bg);
  border: 1px solid rgba(37, 99, 255, 0.2);
  border-radius: var(--gh-radius-btn);
  margin-bottom: 16px;
}

.bulk-count {
  font-size: 13px;
  font-weight: 600;
  color: var(--gh-primary);
  margin-right: 8px;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// ============ Grid View ============
.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.project-card {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.3s;
  position: relative;

  &:hover { transform: translateY(-4px); }

  &.selected {
    outline: 2px solid var(--gh-primary);
    outline-offset: -2px;
  }
}

.card-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.85);
  border-radius: var(--gh-radius-xs);
  padding: 2px;
  backdrop-filter: blur(4px);
  opacity: 0;
  transition: opacity 0.2s;

  .project-card:hover &,
  .project-card.selected & {
    opacity: 1;
  }
}

.project-cover {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: var(--gh-radius) var(--gh-radius) 0 0;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

.cover-overlay {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.cover-count {
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.project-body {
  padding: 14px 16px;
}

.project-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.project-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--gh-text-primary);
}

.project-status {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: var(--gh-radius-xs);

  &.status-active { background: var(--gh-primary-bg); color: var(--gh-primary); }
  &.status-done { background: rgba(34, 197, 94, 0.1); color: var(--gh-success); }
  &.status-draft { background: var(--gh-border-light); color: var(--gh-text-secondary); }
}

.project-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-bottom: 12px;
}

.project-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--gh-border-light);
}

/* New Project Card */
.new-project {
  border: 2px dashed var(--gh-border);
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;

  &:hover {
    border-color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.02);
    transform: none;
  }
}

.new-project-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--gh-text-placeholder);
  font-size: 14px;

  .el-icon { color: var(--gh-primary); }
}

// ============ List View ============
.projects-list {
  background: var(--gh-bg-card);
  border: 1px solid var(--gh-border);
  border-radius: var(--gh-radius-card);
  overflow: hidden;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: var(--gh-bg-page);
  border-bottom: 1px solid var(--gh-border);
  font-size: 12px;
  font-weight: 600;
  color: var(--gh-text-secondary);
}

.list-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--gh-border-light);
  transition: background 0.15s;
  cursor: pointer;

  &:hover {
    background: var(--gh-bg-page);
  }

  &:last-child {
    border-bottom: none;
  }

  &.selected {
    background: var(--gh-primary-bg);
  }
}

.list-col {
  display: flex;
  align-items: center;
}

.list-col-check { width: 48px; flex-shrink: 0; }
.list-col-name { flex: 2; min-width: 0; }
.list-col-count { width: 70px; flex-shrink: 0; font-size: 13px; color: var(--gh-text-primary); }
.list-col-status { width: 90px; flex-shrink: 0; }
.list-col-date { width: 110px; flex-shrink: 0; font-size: 13px; color: var(--gh-text-secondary); }
.list-col-actions { width: 100px; flex-shrink: 0; justify-content: flex-end; gap: 4px; }

.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-cover {
  width: 36px;
  height: 36px;
  border-radius: var(--gh-radius-sm);
  flex-shrink: 0;
  overflow: hidden;

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.name-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--gh-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 项目详情弹窗 */
.project-detail {
  .detail-info {
    margin-bottom: 20px;

    .info-row {
      display: flex;
      margin-bottom: 8px;
      font-size: 14px;

      .info-label {
        width: 80px;
        color: #6B7280;
        flex-shrink: 0;
      }

      span:last-child {
        color: #1F2937;
      }
    }
  }

  .detail-images-section {
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #1F2937;
      margin-bottom: 12px;
    }

    .detail-image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 12px;
    }

    .detail-image-item {
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

      .img-download {
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
