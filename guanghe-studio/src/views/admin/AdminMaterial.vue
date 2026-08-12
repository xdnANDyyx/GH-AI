<template>
  <div class="admin-material">
    <div class="section-header">
      <div>
        <h3 class="section-title">官方素材管理</h3>
        <p class="section-desc">管理平台提供的官方素材，供用户在创作时使用。</p>
      </div>
      <div class="header-actions">
        <el-button @click="openTagDialog()">
          <el-icon><Collection /></el-icon>标签管理
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>上传素材
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-input v-model="queryParams.materialName" placeholder="素材名称" clearable style="width: 180px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="queryParams.materialType" placeholder="素材类型" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="图片" value="0" />
        <el-option label="视频" value="1" />
        <el-option label="3D模型" value="2" />
        <el-option label="其他" value="3" />
      </el-select>
      <el-select v-model="queryParams.tagId" placeholder="标签筛选" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option v-for="tag in tagList" :key="tag.id" :label="tag.tagName" :value="tag.id" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="0" />
        <el-option label="已下架" value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div style="flex: 1"></div>
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button label="grid">
          <el-icon><Grid /></el-icon>网格
        </el-radio-button>
        <el-radio-button label="list">
          <el-icon><List /></el-icon>列表
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="material-grid" v-loading="loading">
      <div v-for="item in materialList" :key="item.id" class="material-card">
        <div class="card-image">
          <el-image :src="getImageUrl(item.thumbnailUrl)" fit="cover" :preview-src-list="[getImageUrl(item.materialUrl)]" />
          <div class="card-overlay">
            <el-button link type="primary" @click="openDialog(item)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
        <div class="card-content">
          <div class="card-title">{{ item.materialName }}</div>
          <div class="card-meta">
            <el-tag size="small">{{ materialTypeText(item.materialType) }}</el-tag>
            <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
              {{ item.status === '0' ? '已发布' : '已下架' }}
            </el-tag>
          </div>
          <div v-if="item.tags && item.tags.length" class="card-tags">
            <el-tag v-for="tag in item.tags" :key="tag.id" size="small" effect="plain">{{ tag.tagName }}</el-tag>
          </div>
        </div>
      </div>
      <div v-if="materialList.length === 0" class="empty-state">暂无素材</div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="table-card" v-loading="loading">
      <el-table :data="materialList" :header-cell-style="headerStyle">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="materialName" label="素材名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="缩略图" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.thumbnailUrl"
              :src="getImageUrl(row.thumbnailUrl)"
              :preview-src-list="[getImageUrl(row.materialUrl)]"
              fit="cover"
              class="thumbnail"
            />
          </template>
        </el-table-column>
        <el-table-column prop="materialType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ materialTypeText(row.materialType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag.id" size="small" effect="plain" style="margin-right: 4px">
              {{ tag.tagName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">
              {{ row.status === '0' ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载量" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="toggleStatus(row)">
              {{ row.status === '0' ? '下架' : '发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="table-footer">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑素材对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑素材' : '上传素材'" width="620px">
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item label="素材名称" required>
          <el-input v-model="form.materialName" placeholder="请输入素材名称" />
        </el-form-item>
        <el-form-item label="素材文件" required>
          <el-upload
            class="material-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleFileUploadSuccess"
            :before-upload="beforeFileUpload"
          >
            <img v-if="form.thumbnailUrl" :src="getImageUrl(form.thumbnailUrl)" class="material-preview" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持图片、视频、模型文件，不超过 50MB</div>
        </el-form-item>
        <el-form-item label="素材类型" required>
          <el-select v-model="form.materialType" placeholder="请选择" style="width: 100%">
            <el-option label="图片" value="0" />
            <el-option label="视频" value="1" />
            <el-option label="3D模型" value="2" />
            <el-option label="其他" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
            <el-option v-for="tag in tagList" :key="tag.id" :label="tag.tagName" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入素材描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">已发布</el-radio>
            <el-radio label="1">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 标签管理对话框 -->
    <el-dialog v-model="tagDialogVisible" title="标签管理" width="700px">
      <div class="tag-actions">
        <el-button type="primary" size="small" @click="openTagForm()">
          <el-icon><Plus /></el-icon>新增标签
        </el-button>
      </div>
      <el-table :data="tagList" :header-cell-style="headerStyle" style="margin-top: 16px">
        <el-table-column prop="tagName" label="标签名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openTagForm(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 标签新增/编辑对话框 -->
    <el-dialog v-model="tagFormVisible" :title="tagForm.id ? '编辑标签' : '新增标签'" width="480px">
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="标签名称" required>
          <el-input v-model="tagForm.tagName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="tagForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Grid, List, Collection } from '@element-plus/icons-vue'
import { getImageUrl, normalizeImageUrl } from '@/utils/image'
import {
  getMaterialList,
  addMaterial,
  updateMaterial,
  deleteMaterial,
  changeMaterialStatus,
  getTagList,
  addTag,
  updateTag,
  deleteTag
} from '@/api/operation'

const loading = ref(false)
const dialogVisible = ref(false)
const tagDialogVisible = ref(false)
const tagFormVisible = ref(false)
const formRef = ref(null)
const viewMode = ref('grid')
const materialList = ref([])
const tagList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  materialName: '',
  materialType: '',
  tagId: '',
  status: ''
})

const form = reactive({
  id: null,
  materialName: '',
  materialUrl: '',
  thumbnailUrl: '',
  materialType: '0',
  tagIds: [],
  description: '',
  sort: 0,
  status: '0'
})

const tagForm = reactive({
  id: null,
  tagName: '',
  sort: 0
})

const headerStyle = { background: '#f5f7fa', color: '#606266', fontWeight: '600' }

const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('gh_token')
}))

function materialTypeText(val) {
  const map = { '0': '图片', '1': '视频', '2': '3D模型', '3': '其他' }
  return map[val] || '-'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMaterialList(queryParams)
    materialList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取素材列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchTagList() {
  try {
    const res = await getTagList({ pageNum: 1, pageSize: 100 })
    tagList.value = res.rows || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchList()
}

function handleReset() {
  queryParams.materialName = ''
  queryParams.materialType = ''
  queryParams.tagId = ''
  queryParams.status = ''
  handleSearch()
}

function handleSizeChange() {
  queryParams.pageNum = 1
  fetchList()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, {
      ...row,
      tagIds: row.tags ? row.tags.map(t => t.id) : []
    })
  } else {
    Object.assign(form, {
      id: null,
      materialName: '',
      materialUrl: '',
      thumbnailUrl: '',
      materialType: '0',
      tagIds: [],
      description: '',
      sort: 0,
      status: '0'
    })
  }
  dialogVisible.value = true
}

function beforeFileUpload(file) {
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB')
  }
  return isLt50M
}

function handleFileUploadSuccess(res) {
  if (res.code === 200) {
    const data = res.data || {}
    const url = normalizeImageUrl(data.url || data.fileName)
    form.materialUrl = url
    form.thumbnailUrl = url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

async function handleSubmit() {
  if (!form.materialName) {
    return ElMessage.warning('请输入素材名称')
  }
  if (!form.materialUrl) {
    return ElMessage.warning('请上传素材文件')
  }
  try {
    if (form.id) {
      await updateMaterial(form)
      ElMessage.success('修改成功')
    } else {
      await addMaterial(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

async function toggleStatus(row) {
  const text = row.status === '0' ? '下架' : '发布'
  try {
    await ElMessageBox.confirm(`确认要${text}"${row.materialName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newStatus = row.status === '0' ? '1' : '0'
    await changeMaterialStatus(row.id, newStatus)
    ElMessage.success(`${text}成功`)
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error(`${text}失败`)
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该素材吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

function openTagDialog() {
  tagDialogVisible.value = true
  fetchTagList()
}

function openTagForm(row) {
  if (row) {
    Object.assign(tagForm, row)
  } else {
    Object.assign(tagForm, { id: null, tagName: '', sort: 0 })
  }
  tagFormVisible.value = true
}

async function handleSubmitTag() {
  if (!tagForm.tagName) {
    return ElMessage.warning('请输入标签名称')
  }
  try {
    if (tagForm.id) {
      await updateTag(tagForm)
      ElMessage.success('修改成功')
    } else {
      await addTag(tagForm)
      ElMessage.success('新增成功')
    }
    tagFormVisible.value = false
    fetchTagList()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

async function handleDeleteTag(row) {
  try {
    await ElMessageBox.confirm('确认删除该标签吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTag(row.id)
    ElMessage.success('删除成功')
    fetchTagList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchList()
  fetchTagList()
})
</script>

<style lang="scss" scoped>
.admin-material {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.section-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.material-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.material-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    
    .card-overlay {
      opacity: 1;
    }
  }
}

.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  
  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  
  .el-button {
    color: white;
    
    &:hover {
      color: #409eff;
    }
  }
}

.card-content {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 14px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
}

.thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.material-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.2s;
    
    &:hover {
      border-color: #409eff;
    }
  }
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.material-preview {
  width: 200px;
  height: 200px;
  object-fit: cover;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.tag-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
