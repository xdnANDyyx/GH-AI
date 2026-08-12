<template>
  <div class="admin-banner">
    <div class="section-header">
      <div>
        <h3 class="section-title">Banner 管理</h3>
        <p class="section-desc">管理首页、工作流、积分页等位置的 Banner 展示。</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增 Banner
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="queryParams.title" placeholder="标题" clearable style="width: 180px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="queryParams.position" placeholder="投放位置" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <!-- <el-option label="首页" value="0" /> -->
        <el-option label="工作流" value="0" />
        <el-option label="积分页" value="1" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="0" />
        <el-option label="已下架" value="1" />
        <el-option label="定时发布" value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="bannerList" :header-cell-style="headerStyle">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="预览图" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="getImageUrl(row.imageUrl)"
              :preview-src-list="[getImageUrl(row.imageUrl)]"
              fit="cover"
              class="banner-thumbnail"
            />
          </template>
        </el-table-column>
        <el-table-column prop="position" label="投放位置" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ positionText(row.position) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clickCount" label="点击量" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="success" size="small" @click="viewTrend(row)">统计</el-button>
            <el-button link type="warning" size="small" @click="toggleStatus(row)">
              {{ row.status === '0' ? '下架' : '发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑 Banner' : '新增 Banner'" width="620px">
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入 Banner 标题" />
        </el-form-item>
        <el-form-item label="Banner 图片" required>
          <el-upload
            class="banner-uploader"
            drag
            accept="image/*"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
          >
            <img v-if="form.imageUrl" :src="getImageUrl(form.imageUrl)" class="banner-image" />
            <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸 1200×400，支持 JPG/PNG/GIF/WebP 等图片格式，不超过 50MB</div>
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="点击 Banner 后跳转的链接" />
        </el-form-item>
        <el-form-item label="投放位置" required>
          <el-select v-model="form.position" placeholder="请选择" style="width: 100%">
            <!-- <el-option label="首页" value="0" /> -->
            <el-option label="工作流" value="0" />
            <el-option label="积分页" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">已发布</el-radio>
            <el-radio label="1">已下架</el-radio>
            <el-radio label="2">定时发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 点击趋势对话框 -->
    <el-dialog v-model="trendVisible" title="点击趋势" width="700px">
      <div class="trend-header">
        <div class="trend-title">{{ currentBanner?.title }}</div>
        <el-radio-group v-model="trendRange" size="small" @change="fetchTrend">
          <el-radio-button label="7">近7天</el-radio-button>
          <el-radio-button label="30">近30天</el-radio-button>
          <el-radio-button label="90">近3个月</el-radio-button>
        </el-radio-group>
      </div>
      <div class="trend-chart" v-loading="trendLoading">
        <div v-if="trendData.length === 0" class="empty-trend">暂无数据</div>
        <div v-else class="chart-container">
          <div class="chart-bar-wrap">
            <div v-for="item in trendData" :key="item.clickDate" class="chart-bar-item">
              <div class="chart-bar" :style="{ height: getBarHeight(item.clickCount) + 'px' }">
                <span class="bar-value">{{ item.clickCount }}</span>
              </div>
              <div class="bar-label">{{ formatDateLabel(item.clickDate) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getBannerList,
  addBanner,
  updateBanner,
  deleteBanner,
  changeBannerStatus,
  getBannerClickTrend
} from '@/api/operation'
import { getImageUrl } from '@/utils/image'

const loading = ref(false)
const dialogVisible = ref(false)
const trendVisible = ref(false)
const trendLoading = ref(false)
const formRef = ref(null)
const bannerList = ref([])
const total = ref(0)
const currentBanner = ref(null)
const trendRange = ref('7')
const trendData = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  position: '',
  status: ''
})

const form = reactive({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  position: '0',
  sort: 0,
  startTime: '',
  endTime: '',
  status: '0'
})

const headerStyle = { background: '#f5f7fa', color: '#606266', fontWeight: '600' }

const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('gh_token')
}))

function positionText(val) {
  const map = {  '0': '工作流', '1': '积分页' }
  return map[val] || '-'
}

function statusText(val) {
  const map = { '0': '已发布', '1': '已下架', '2': '定时发布' }
  return map[val] || '-'
}

function statusType(val) {
  const map = { '0': 'success', '1': 'info', '2': 'warning' }
  return map[val] || ''
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getBannerList(queryParams)
    bannerList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取 Banner 列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchList()
}

function handleReset() {
  queryParams.title = ''
  queryParams.position = ''
  queryParams.status = ''
  handleSearch()
}

function handleSizeChange() {
  queryParams.pageNum = 1
  fetchList()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: null,
      title: '',
      imageUrl: '',
      linkUrl: '',
      position: '0',
      sort: 0,
      startTime: '',
      endTime: '',
      status: '0'
    })
  }
  dialogVisible.value = true
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isImage) {
    ElMessage.error('只能上传图片文件，例如 JPG/PNG/GIF/WebP')
  }
  if (!isLt50M) {
    ElMessage.error('图片大小不能超过 50MB')
  }
  return isImage && isLt50M
}

function handleUploadSuccess(res) {
  const data = res.data || res
  if (res.code === 200) {
    let url = data.url || data.fileName || ''
    if (url && !url.startsWith('/profile/') && !url.startsWith('http')) {
      url = '/profile/' + url.replace(/^.*?uploadPath[\\/]?/, '')
    }
    form.imageUrl = url
    ElMessage.success('上传成功')
  } else {
    console.error('上传失败响应:', res)
    ElMessage.error(res.msg || '上传失败')
  }
}

function handleUploadError(error) {
  console.error('上传请求失败:', error)
  ElMessage.error('上传请求失败，请检查网络或登录状态')
}

async function handleSubmit() {
  if (!form.title) {
    return ElMessage.warning('请输入标题')
  }
  if (!form.imageUrl) {
    return ElMessage.warning('请上传图片')
  }
  try {
    if (form.id) {
      await updateBanner(form)
      ElMessage.success('修改成功')
    } else {
      await addBanner(form)
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
    await ElMessageBox.confirm(`确认要${text}"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const newStatus = row.status === '0' ? '1' : '0'
    await changeBannerStatus(row.id, newStatus)
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
    await ElMessageBox.confirm('确认删除该 Banner 吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

function viewTrend(row) {
  currentBanner.value = row
  trendRange.value = '7'
  trendVisible.value = true
  fetchTrend()
}

async function fetchTrend() {
  if (!currentBanner.value) return
  trendLoading.value = true
  try {
    const days = parseInt(trendRange.value)
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - days)
    
    const params = {
      startDate: formatDate(startDate),
      endDate: formatDate(endDate)
    }
    
    const res = await getBannerClickTrend(currentBanner.value.id, params)
    trendData.value = res.data || []
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    ElMessage.error('获取趋势数据失败')
  } finally {
    trendLoading.value = false
  }
}

function getBarHeight(count) {
  if (trendData.value.length === 0) return 20
  const maxCount = Math.max(...trendData.value.map(d => d.clickCount), 1)
  return Math.max((count / maxCount) * 200, 20)
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatDateLabel(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

fetchList()
</script>

<style lang="scss" scoped>
.admin-banner {
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

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.banner-thumbnail {
  width: 90px;
  height: 50px;
  border-radius: 4px;
  cursor: pointer;
}

.banner-uploader {
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

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-image {
  width: 300px;
  height: 160px;
  object-fit: cover;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.trend-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.trend-chart {
  min-height: 250px;
  padding: 20px 10px;
}

.empty-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 250px;
  color: #909399;
  font-size: 14px;
}

.chart-container {
  width: 100%;
}

.chart-bar-wrap {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 220px;
  padding: 10px 0;
}

.chart-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 60px;
}

.chart-bar {
  width: 40px;
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: all 0.3s;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 5px;
  
  &:hover {
    opacity: 0.8;
  }
}

.bar-value {
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.bar-label {
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}
</style>
