<template>
  <div class="admin-ai-model">
    <div class="section-header">
      <div>
        <h3 class="section-title">AI 模特管理</h3>
        <p class="section-desc">管理 AI 模特素材，控制前台上架与商用授权状态。</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>添加模特
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="queryParams.name" placeholder="模特名称" clearable style="width: 180px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="queryParams.gender" placeholder="性别" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="男" value="male" />
        <el-option label="女" value="female" />
      </el-select>
      <el-select v-model="queryParams.ageGroup" placeholder="年龄段" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="青年" value="youth" />
        <el-option label="中年" value="middle" />
        <el-option label="老年" value="elder" />
      </el-select>
      <el-select v-model="queryParams.ethnicity" placeholder="种族" clearable style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="亚洲" value="asian" />
        <el-option label="欧美" value="western" />
        <el-option label="非洲" value="african" />
        <el-option label="拉美" value="latin" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="0" />
        <el-option label="草稿" value="1" />
      </el-select>
      <el-select v-model="queryParams.commercialAuth" placeholder="商用授权" clearable style="width: 150px">
        <el-option label="全部" value="" />
        <el-option label="已授权" value="0" />
        <el-option label="未授权" value="1" />
        <el-option label="需额外购买" value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div style="flex: 1"></div>
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button label="grid">
          <el-icon><Grid /></el-icon>卡片
        </el-radio-button>
        <el-radio-button label="list">
          <el-icon><List /></el-icon>列表
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'grid'" class="model-grid" v-loading="loading">
      <div v-for="item in modelList" :key="item.id" class="model-card">
        <div class="card-image">
          <el-image
            :src="getImageUrl(item.previewUrl) || 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22><rect fill=%22%23e5e7eb%22 width=%22200%22 height=%22200%22/><text x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%239ca3af%22 font-size=%2236%22>暂无</text></svg>'"
            fit="cover"
            :preview-src-list="[getImageUrl(item.previewUrl)]"
          />
          <div class="card-overlay">
            <el-button link type="primary" size="small" @click="openDialog(item)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
        <div class="card-body">
          <div class="card-title">{{ item.name }}</div>
          <div class="card-meta">
            <span>{{ genderText(item.gender) }}</span>
            <span>·</span>
            <span>{{ ageGroupText(item.ageGroup) }}</span>
            <span>·</span>
            <span>{{ ethnicityText(item.ethnicity) }}</span>
          </div>
          <div class="card-tags">
            <el-tag
              :type="item.status === '0' ? 'success' : 'info'"
              size="small"
              effect="dark"
            >
              {{ item.status === '0' ? '已发布' : '草稿' }}
            </el-tag>
            <el-tag
              :type="commercialAuthTagType(item.commercialAuth)"
              size="small"
              effect="plain"
            >
              {{ commercialAuthText(item.commercialAuth) }}
            </el-tag>
          </div>
        </div>
      </div>
      <div v-if="modelList.length === 0 && !loading" class="empty-state">暂无模特数据</div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="table-card" v-loading="loading">
      <el-table :data="modelList" :header-cell-style="headerStyle">
        <el-table-column label="预览" width="90">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.previewUrl) || ''"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px"
              :preview-src-list="[getImageUrl(row.previewUrl)]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column label="年龄段" width="80">
          <template #default="{ row }">{{ ageGroupText(row.ageGroup) }}</template>
        </el-table-column>
        <el-table-column label="种族" width="90">
          <template #default="{ row }">{{ ethnicityText(row.ethnicity) }}</template>
        </el-table-column>
        <el-table-column label="发型" width="90">
          <template #default="{ row }">{{ row.hairStyle || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === '0'"
              active-text="发布"
              inactive-text="草稿"
              inline-prompt
              size="small"
              @change="(val) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="商用授权" width="120">
          <template #default="{ row }">
            <el-tag :type="commercialAuthTagType(row.commercialAuth)" size="small" effect="plain">
              {{ commercialAuthText(row.commercialAuth) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="70" prop="sort" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模特' : '添加模特'"
      width="720px"
      destroy-on-close
      top="5vh"
      class="model-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="right"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="模特名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入模特名称" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="年龄段" prop="ageGroup">
              <el-select v-model="form.ageGroup" placeholder="请选择" style="width: 100%">
                <el-option label="青年" value="youth" />
                <el-option label="中年" value="middle" />
                <el-option label="老年" value="elder" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="种族" prop="ethnicity">
              <el-select v-model="form.ethnicity" placeholder="请选择" style="width: 100%">
                <el-option label="亚洲" value="asian" />
                <el-option label="欧美" value="western" />
                <el-option label="非洲" value="african" />
                <el-option label="拉美" value="latin" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发型" prop="hairStyle">
              <el-select v-model="form.hairStyle" placeholder="请选择" style="width: 100%" clearable allow-create filterable>
                <el-option label="短发" value="short" />
                <el-option label="长发" value="long" />
                <el-option label="卷发" value="curly" />
                <el-option label="直发" value="straight" />
                <el-option label="中发" value="medium" />
                <el-option label="盘发" value="updo" />
                <el-option label="寸头" value="buzz" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="姿势">
              <el-input v-model="form.pose" placeholder="如：站立、坐姿、行走" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="着装">
              <el-input v-model="form.clothing" placeholder="如：休闲、商务、运动" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="场景">
              <el-input v-model="form.scene" placeholder="如：客厅、卧室、户外" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="9999" placeholder="数值越小越靠前" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">已发布</el-radio>
                <el-radio value="1">草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商用授权" prop="commercialAuth">
              <el-select v-model="form.commercialAuth" placeholder="请选择" style="width: 100%">
                <el-option label="已授权 — 可商用" value="0" />
                <el-option label="未授权 — 不可商用" value="1" />
                <el-option label="需额外购买" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="模特图片" required>
          <div class="upload-wrap">
            <el-upload
              ref="uploadRef"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleUploadRemove"
              :before-upload="beforeUpload"
              :file-list="fileList"
              list-type="picture-card"
              :limit="1"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">支持 JPG/PNG/WebP，单张不超过 10MB，建议尺寸 600×800 以上</p>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Grid, List } from '@element-plus/icons-vue'
import { getAiModelList, addAiModel, updateAiModel, deleteAiModel, changeAiModelStatus } from '@/api/operation'
import { useUserStore } from '@/store'
import { getImageUrl, normalizeImageUrl } from '@/utils/image'

const userStore = useUserStore()

// ===== Upload config =====
const uploadUrl = computed(() => {
  const base = import.meta.env.VITE_APP_BASE_API || '/api'
  return base + '/common/upload'
})
const uploadHeaders = computed(() => {
  const token = typeof userStore.token === 'string' ? userStore.token : userStore.token?.value
  return { Authorization: 'Bearer ' + (token || '') }
})

// ===== Data =====
const viewMode = ref('grid')
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const modelList = ref([])
const total = ref(0)
const fileList = ref([])
const tempUploadUrl = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  name: '',
  gender: '',
  ageGroup: '',
  ethnicity: '',
  status: '',
  commercialAuth: ''
})

const form = reactive({
  id: null,
  name: '',
  gender: '',
  ageGroup: '',
  ethnicity: '',
  hairStyle: '',
  pose: '',
  clothing: '',
  scene: '',
  status: '0',
  commercialAuth: '0',
  sort: 0,
  remark: '',
  previewUrl: ''
})

const rules = {
  name: [{ required: true, message: '请输入模特名称', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  ageGroup: [{ required: true, message: '请选择年龄段', trigger: 'change' }],
  ethnicity: [{ required: true, message: '请选择种族', trigger: 'change' }],
  commercialAuth: [{ required: true, message: '请选择商用授权', trigger: 'change' }]
}

// ===== Lifecycle =====
onMounted(() => {
  loadList()
})

// ===== Methods =====
function headerStyle() {
  return { background: '#f9fafb', color: '#374151', fontWeight: 600 }
}

async function loadList() {
  loading.value = true
  try {
    const params = {}
    Object.keys(queryParams).forEach((k) => {
      if (queryParams[k] !== '' && queryParams[k] !== null) params[k] = queryParams[k]
    })
    const res = await getAiModelList(params)
    if (res.code === 200) {
      modelList.value = res.rows || []
      total.value = res.total || 0
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (e) {
    ElMessage.error('获取列表异常')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadList()
}

function handleReset() {
  Object.assign(queryParams, { pageNum: 1, pageSize: 12, name: '', gender: '', ageGroup: '', ethnicity: '', status: '', commercialAuth: '' })
  loadList()
}

function openDialog(row) {
  isEdit.value = !!row
  fileList.value = []
  tempUploadUrl.value = ''

  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name || '',
      gender: row.gender || '',
      ageGroup: row.ageGroup || '',
      ethnicity: row.ethnicity || '',
      hairStyle: row.hairStyle || '',
      pose: row.pose || '',
      clothing: row.clothing || '',
      scene: row.scene || '',
      status: row.status || '0',
      commercialAuth: row.commercialAuth || '0',
      sort: row.sort || 0,
      remark: row.remark || '',
      previewUrl: row.previewUrl || ''
    })
    if (row.previewUrl) {
      fileList.value = [{ name: row.name || 'model', url: row.previewUrl }]
    }
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    gender: '',
    ageGroup: '',
    ethnicity: '',
    hairStyle: '',
    pose: '',
    clothing: '',
    scene: '',
    status: '0',
    commercialAuth: '0',
    sort: 0,
    remark: '',
    previewUrl: ''
  })
  fileList.value = []
  tempUploadUrl.value = ''
}

function handleUploadSuccess(response) {
  if (response.code === 200) {
    tempUploadUrl.value = normalizeImageUrl(response.data?.url || response.data || '')
    form.previewUrl = tempUploadUrl.value
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

function handleUploadRemove() {
  tempUploadUrl.value = ''
  form.previewUrl = ''
}

function handleUploadError() {
  ElMessage.error('图片上传失败，请检查网络或登录状态后重试')
  tempUploadUrl.value = ''
  form.previewUrl = ''
}

function beforeUpload(file) {
  const isValid = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!isValid) {
    ElMessage.error('仅支持 JPG/PNG/WebP 格式')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function handleToggleStatus(row, val) {
  const newStatus = val ? '0' : '1'
  try {
    const res = await changeAiModelStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === '0' ? '已发布' : '已设为草稿')
      row.status = newStatus
    } else {
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (e) {
    ElMessage.error('操作异常')
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!form.previewUrl && !tempUploadUrl.value) {
    // 编辑时可以保留旧图片
    if (!isEdit.value) {
      ElMessage.warning('请上传模特图片')
      return
    }
  }

  submitLoading.value = true
  try {
    const payload = { ...form }
    // Ensure modelUrl reflects any new upload
    if (tempUploadUrl.value) {
      payload.previewUrl = tempUploadUrl.value
    }

    let res
    if (isEdit.value) {
      res = await updateAiModel(payload)
    } else {
      delete payload.id
      res = await addAiModel(payload)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存异常')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该模特？删除后不可恢复。', '删除确认', { type: 'warning' })
    const res = await deleteAiModel([id])
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    // user cancelled
  }
}

// ===== Text helpers =====
function genderText(val) {
  const map = { male: '男', female: '女' }
  return map[val] || val
}
function ageGroupText(val) {
  const map = { youth: '青年', middle: '中年', elder: '老年' }
  return map[val] || val
}
function ethnicityText(val) {
  const map = { asian: '亚洲', western: '欧美', african: '非洲', latin: '拉美' }
  return map[val] || val
}
function commercialAuthText(val) {
  const map = { '0': '已授权', '1': '未授权', '2': '需额外购买' }
  return map[val] || val
}
function commercialAuthTagType(val) {
  const map = { '0': 'success', '1': 'danger', '2': 'warning' }
  return map[val] || 'info'
}
</script>

<style scoped lang="scss">
.admin-ai-model {
  padding: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 4px 0;
  }
  .section-desc {
    font-size: 13px;
    color: #6b7280;
    margin: 0;
  }
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 10px;
}

// Card grid
.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.model-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    .card-overlay {
      opacity: 1;
    }
  }
}

.card-image {
  position: relative;
  width: 100%;
  height: 200px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  .el-image {
    width: 100%;
    height: 100%;
  }
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.card-body {
  padding: 12px;
}

.card-title {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  display: flex;
  gap: 2px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #9ca3af;
  font-size: 14px;
}

// Table
.table-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  min-height: 200px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

// Dialog
.model-dialog {
  .upload-wrap {
    width: 100%;
  }
  .upload-tip {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 6px;
  }
}
</style>