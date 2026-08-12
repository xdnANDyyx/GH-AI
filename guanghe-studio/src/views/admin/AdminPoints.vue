<template>
  <div class="admin-points">
    <div class="stats-row">
      <div class="stat-card" v-for="item in pointStats" :key="item.label">
        <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-left">
        <el-input v-model="filters.userKeyword" placeholder="用户名/手机号/用户ID" clearable style="width: 220px" @keyup.enter="handleSearch" />
        <el-select v-model="filters.changeType" placeholder="变更类型" clearable style="width: 160px">
          <el-option label="全部" value="" />
          <el-option label="充值" :value="1" />
          <el-option label="消费" :value="2" />
          <el-option label="退款" :value="3" />
          <el-option label="赠送" :value="4" />
          <el-option label="过期" :value="5" />
          <el-option label="手动调整" :value="6" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
        <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button @click="openAdjustDialog()">积分调整</el-button>
        <el-button type="primary" plain @click="openBatchDialog()">批量积分操作</el-button>
        <el-button @click="exportPoints">导出明细</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="displayRecords" :header-cell-style="headerStyle">
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <span>{{ row.userName || row.userId || '-' }}</span>
              <span class="muted">{{ row.phone || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="changeType" label="变更类型" width="120">
          <template #default="{ row }">
            <el-tag :type="changeTypeTag(row.changeType)" size="small">{{ changeTypeText(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changePoints" label="变更积分" width="120">
          <template #default="{ row }">
            <span :class="Number(row.changePoints || 0) >= 0 ? 'rise' : 'fall'">{{ formatPoints(row.changePoints) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变更后余额" width="120" />
        <el-table-column prop="relatedOrderNo" label="关联订单号" min-width="180" />
        <el-table-column prop="reason" label="原因/备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="table-footer-info">前端筛选后 {{ displayRecords.length }} / 总计 {{ total }} 条</div>
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchRecords"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="adjustDialogVisible" title="积分调整" width="520px">
      <el-form :model="adjustForm" label-width="90px">
        <el-form-item label="用户">
          <el-select v-model="adjustForm.userId" filterable placeholder="请选择用户" style="width: 100%">
            <el-option v-for="item in customerOptions" :key="item.userId" :label="customerLabel(item)" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整积分">
          <el-input-number v-model="adjustForm.changePoints" :step="10" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingAdjust" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量积分操作" width="560px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="用户ID列表">
          <el-input v-model="batchForm.userIdsText" type="textarea" :rows="4" placeholder="请输入用户ID，使用逗号、空格或换行分隔" />
        </el-form-item>
        <el-form-item label="统一积分变更">
          <el-input-number v-model="batchForm.changePoints" :step="10" />
        </el-form-item>
        <el-form-item label="操作原因">
          <el-input v-model="batchForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingBatch" @click="submitBatchAdjust">执行批量操作</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adjustAdminPoints,
  batchAdjustAdminPoints,
  deleteAdminPointsRecord,
  exportAdminPointsRecords,
  listAdminCustomers,
  listAdminPointsRecords
} from '@/api/customer'

const loading = ref(false)
const submittingAdjust = ref(false)
const submittingBatch = ref(false)
const adjustDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const total = ref(0)

const records = ref([])
const customerOptions = ref([])
const customerMap = ref(new Map())
const dateRange = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10 })
const filters = reactive({ userKeyword: '', changeType: '' })
const adjustForm = reactive(createAdjustForm())
const batchForm = reactive(createBatchForm())

const headerStyle = { background: '#f8fafc', color: '#64748b', fontSize: '12px' }

function createAdjustForm() {
  return { userId: undefined, changePoints: 0, reason: '' }
}

function createBatchForm() {
  return { userIdsText: '', changePoints: 0, reason: '' }
}

function resetAdjustForm() {
  Object.assign(adjustForm, createAdjustForm())
}

function resetBatchForm() {
  Object.assign(batchForm, createBatchForm())
}

function customerLabel(item) {
  return `${item.nickName || item.userName || item.userId}${item.phonenumber ? ` / ${item.phonenumber}` : ''}`
}

function changeTypeText(value) {
  const map = {
    1: '充值',
    2: '消费',
    3: '退款',
    4: '赠送',
    5: '过期',
    6: '手动调整'
  }
  return map[value] || value || '-'
}

function changeTypeTag(value) {
  if (value === 2) return 'warning'
  if (value === 6) return 'primary'
  if (value === 3) return 'success'
  if (value === 4) return 'info'
  if (value === 5) return 'info'
  return 'success'
}

function formatPoints(value) {
  const number = Number(value || 0)
  return number > 0 ? `+${number}` : `${number}`
}

const displayRecords = computed(() => {
  const keyword = filters.userKeyword.trim().toLowerCase()
  return records.value.filter(item => {
    const userHit = !keyword || [item.userName, item.phone, String(item.userId || '')].some(field => (field || '').toLowerCase().includes(keyword))
    const typeHit = !filters.changeType || item.changeType === filters.changeType
    return userHit && typeHit
  })
})

const pointStats = computed(() => {
  const totalChange = displayRecords.value.reduce((sum, item) => sum + Number(item.changePoints || 0), 0)
  const manualCount = displayRecords.value.filter(item => item.changeType === 'manual').length
  const increaseCount = displayRecords.value.filter(item => Number(item.changePoints || 0) > 0).length
  const decreaseCount = displayRecords.value.filter(item => Number(item.changePoints || 0) < 0).length
  return [
    { label: '明细总数', value: total.value, color: '#2563ff' },
    { label: '人工调整笔数', value: manualCount, color: '#0f766e' },
    { label: '增加记录', value: increaseCount, color: '#22c55e' },
    { label: '净变动', value: formatPoints(totalChange), color: decreaseCount > increaseCount ? '#ef4444' : '#f97316' }
  ]
})

async function fetchCustomers() {
  try {
    const res = await listAdminCustomers({ pageNum: 1, pageSize: 500 })
    customerOptions.value = res.rows || []
    const map = new Map()
    customerOptions.value.forEach(item => map.set(item.userId, item))
    customerMap.value = map
  } catch (error) {
    ElMessage.error(error.message || '获取用户列表失败')
  }
}

async function fetchRecords() {
  loading.value = true
  try {
    const matchedCustomer = filters.userKeyword
      ? customerOptions.value.find(item => customerLabel(item).toLowerCase().includes(filters.userKeyword.trim().toLowerCase()))
      : null
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      userId: matchedCustomer?.userId,
      changeType: filters.changeType || undefined,
      beginDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    }
    const res = await listAdminPointsRecords(params)
    records.value = (res.rows || []).map(item => {
      const customer = customerMap.value.get(item.userId) || {}
      return {
        ...item,
        userName: customer.nickName || customer.userName || '',
        phone: customer.phonenumber || '',
        operatorName: item.operatorName || item.operator || '-'
      }
    })
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取积分明细失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  fetchRecords()
}

function handleReset() {
  filters.userKeyword = ''
  filters.changeType = ''
  dateRange.value = []
  pagination.pageNum = 1
  fetchRecords()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchRecords()
}

function openAdjustDialog() {
  resetAdjustForm()
  adjustDialogVisible.value = true
}

function openBatchDialog() {
  resetBatchForm()
  batchDialogVisible.value = true
}

async function submitAdjust() {
  if (!adjustForm.userId) {
    ElMessage.warning('请选择用户')
    return
  }
  if (!adjustForm.reason.trim()) {
    ElMessage.warning('请输入调整原因')
    return
  }
  submittingAdjust.value = true
  try {
    await adjustAdminPoints({ ...adjustForm, reason: adjustForm.reason.trim() })
    ElMessage.success('积分调整成功')
    adjustDialogVisible.value = false
    fetchRecords()
  } catch (error) {
    ElMessage.error(error.message || '积分调整失败')
  } finally {
    submittingAdjust.value = false
  }
}

async function submitBatchAdjust() {
  const userIds = batchForm.userIdsText
    .split(/[\s,，]+/)
    .map(item => item.trim())
    .filter(Boolean)
    .map(item => Number(item))
    .filter(item => Number.isFinite(item))

  if (!userIds.length) {
    ElMessage.warning('请至少输入一个用户ID')
    return
  }
  if (!batchForm.reason.trim()) {
    ElMessage.warning('请输入操作原因')
    return
  }

  submittingBatch.value = true
  try {
    await batchAdjustAdminPoints({
      userIds,
      changePoints: batchForm.changePoints,
      reason: batchForm.reason.trim()
    })
    ElMessage.success('批量积分操作成功')
    batchDialogVisible.value = false
    fetchRecords()
  } catch (error) {
    ElMessage.error(error.message || '批量积分操作失败')
  } finally {
    submittingBatch.value = false
  }
}

async function exportPoints() {
  try {
    const blobRes = await exportAdminPointsRecords({
      changeType: filters.changeType || undefined,
      beginDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    })
    const blob = blobRes instanceof Blob ? blobRes : new Blob([blobRes])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `积分明细-${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('积分明细已导出')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除该积分变动记录？删除后将自动重算该用户后续余额，此操作不可逆。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteAdminPointsRecord(row.id)
    ElMessage.success('删除成功，已重算余额')
    fetchRecords()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await fetchCustomers()
  fetchRecords()
})
</script>

<style lang="scss" scoped>
.admin-points {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card,
.table-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

.stat-card {
  padding: 18px 20px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
}

.stat-label,
.muted,
.table-footer-info {
  color: #64748b;
  font-size: 13px;
}

.filter-bar,
.filter-left,
.filter-right,
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-bar {
  justify-content: space-between;
}

.table-card {
  padding: 20px;
}

.user-cell {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.rise {
  color: #16a34a;
  font-weight: 600;
}

.fall {
  color: #dc2626;
  font-weight: 600;
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  border-radius: 8px;
  overflow: hidden;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .table-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>