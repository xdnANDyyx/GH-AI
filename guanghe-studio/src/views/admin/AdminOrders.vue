<template>
  <div class="admin-orders">
    <el-tabs v-model="activeTab" type="border-card" class="order-tabs">
      <el-tab-pane label="套餐管理" name="packages">
        <div class="section-header">
          <div>
            <h3 class="section-title">套餐配置</h3>
            <p class="section-desc">前台“升级套餐”与“积分充值”入口直接读取这里的套餐配置。</p>
          </div>
          <el-button type="primary" @click="openPackageDialog()">
            <el-icon><Plus /></el-icon>新增套餐
          </el-button>
        </div>

        <el-table v-loading="packageLoading" :data="packageList" :header-cell-style="headerStyle">
          <el-table-column prop="name" label="套餐名称" min-width="170" />
          <el-table-column prop="packageType" label="套餐类型" width="110">
            <template #default="{ row }">
              <el-tag :type="row.packageType === 2 ? 'success' : 'warning'" size="small">{{ row.packageType === 2 ? '正常套餐' : '赠送套餐' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="金额" width="110">
            <template #default="{ row }">{{ formatCurrency(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="points" label="积分数量" width="110" />
          <el-table-column prop="validityDays" label="有效期" width="110">
            <template #default="{ row }">{{ row.validityDays ? `${row.validityDays}天` : '永久' }}</template>
          </el-table-column>
          <el-table-column prop="rightsText" label="权益说明" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="status" label="上架状态" width="110">
            <template #default="{ row }">
              <el-switch :model-value="row.status === '0'" @change="(value) => togglePackageStatus(row, value)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openPackageDialog(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeletePackage(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="充值订单列表" name="payments">
        <div class="stats-row">
          <div class="stat-card" v-for="item in paymentStats" :key="item.label">
            <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>

        <div class="filter-bar">
          <div class="filter-left">
            <el-input v-model="filters.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleSearch">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-input v-model="filters.userKeyword" placeholder="用户名/手机号" clearable style="width: 180px" @keyup.enter="handleSearch" />
            <el-select v-model="filters.packageType" placeholder="套餐类型" clearable style="width: 140px">
              <el-option label="全部" value="" />
              <el-option label="免费" :value="0" />
              <el-option label="付费" :value="1" />
            </el-select>
            <el-select v-model="filters.payStatus" placeholder="支付状态" clearable style="width: 140px">
              <el-option label="全部" value="" />
              <el-option label="待支付" value="0" />
              <el-option label="已支付" value="1" />
              <el-option label="已退款" value="2" />
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
            <el-button @click="exportPayments">导出 Excel</el-button>
            <el-button @click="openRefundDrawer()">退款记录</el-button>
          </div>
        </div>

        <div class="table-card">
          <el-table v-loading="loading" :data="displayOrders" :header-cell-style="headerStyle">
            <el-table-column prop="orderNo" label="订单号" min-width="170" />
            <el-table-column label="用户信息" min-width="180">
              <template #default="{ row }">
                <div class="user-cell">
                  <span>{{ row.userName || row.userId || '-' }}</span>
                  <span class="muted">{{ row.phone || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="packageName" label="充值套餐" min-width="170" />
            <el-table-column label="套餐类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.packageType === 2 ? 'success' : 'warning'" size="small">{{ row.packageType === 2 ? '正常套餐' : '赠送套餐' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="payMethod" label="支付方式" width="100">
              <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
            </el-table-column>
            <el-table-column prop="payStatus" label="支付状态" width="100">
              <template #default="{ row }">
                <el-tag :type="payStatusType(row.payStatus)" size="small">{{ payStatusText(row.payStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewOrder(row)">详情</el-button>
                <el-button link type="warning" size="small" @click="openRefundDrawer(row.orderNo)">退款处理</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <div class="table-footer-info">前端筛选后 {{ displayOrders.length }} / 总计 {{ total }} 条</div>
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @current-change="fetchOrders"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="packageDialogVisible" :title="packageForm.id ? '编辑套餐' : '新增套餐'" width="620px">
      <el-form :model="packageForm" label-width="100px">
        <el-form-item label="套餐名称"><el-input v-model="packageForm.name" /></el-form-item>
        <el-form-item label="套餐类型">
          <el-select v-model="packageForm.packageType" style="width: 180px">
            <el-option label="赠送套餐" :value="1" />
            <el-option label="正常套餐" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="packageForm.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="积分"><el-input-number v-model="packageForm.points" :min="0" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="packageForm.validityDays" :min="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="packageForm.sort" :min="0" /></el-form-item>
        <el-form-item label="权益说明"><el-input v-model="packageForm.rightsText" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="套餐描述"><el-input v-model="packageForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="packageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPackage" @click="submitPackage">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="订单详情" width="560px">
      <div v-if="currentOrder" class="detail-list">
        <div class="detail-item"><span class="detail-label">订单号</span><span>{{ currentOrder.orderNo }}</span></div>
        <div class="detail-item"><span class="detail-label">用户</span><span>{{ currentOrder.userName || currentOrder.userId || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">手机号</span><span>{{ currentOrder.phone || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">充值套餐</span><span>{{ currentOrder.packageName || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">金额</span><span>{{ formatCurrency(currentOrder.amount) }}</span></div>
        <div class="detail-item"><span class="detail-label">支付方式</span><span>{{ payMethodText(currentOrder.payMethod) }}</span></div>
        <div class="detail-item"><span class="detail-label">支付状态</span><span>{{ payStatusText(currentOrder.payStatus) }}</span></div>
        <div class="detail-item"><span class="detail-label">创建时间</span><span>{{ currentOrder.createTime || '-' }}</span></div>
      </div>
    </el-dialog>

    <el-drawer v-model="refundDrawerVisible" title="退款记录" size="780px">
      <div class="filter-left refund-toolbar">
        <el-input v-model="refundFilters.originalOrderNo" placeholder="原订单号" clearable style="width: 200px" />
        <el-select v-model="refundFilters.status" placeholder="退款状态" clearable style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="0" />
          <el-option label="已退款" value="1" />
          <el-option label="已拒绝" value="2" />
        </el-select>
        <el-button :loading="refundLoading" @click="fetchRefunds">查询</el-button>
      </div>
      <el-table v-loading="refundLoading" :data="refunds" :header-cell-style="headerStyle">
        <el-table-column prop="refundNo" label="退款单号" min-width="170" />
        <el-table-column prop="originalOrderNo" label="原订单号" min-width="170" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="refundAmount" label="退款金额" width="100">
          <template #default="{ row }">{{ formatCurrency(row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column prop="refundPoints" label="退回积分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="refundStatusType(row.status)" size="small">{{ refundStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '0'" link type="success" size="small" @click="handleRefund(row, '1')">通过</el-button>
            <el-button v-if="row.status === '0'" link type="danger" size="small" @click="handleRefund(row, '2')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  addAdminPackage,
  changeAdminPackageStatus,
  deleteAdminPackage,
  exportAdminPayments,
  listAdminCustomers,
  listAdminPackages,
  listAdminPayments,
  listAdminRefunds,
  processAdminRefund,
  updateAdminPackage
} from '@/api/customer'

const activeTab = ref('packages')
const loading = ref(false)
const refundLoading = ref(false)
const packageLoading = ref(false)
const savingPackage = ref(false)
const packageDialogVisible = ref(false)
const dialogVisible = ref(false)
const refundDrawerVisible = ref(false)
const total = ref(0)

const packageList = ref([])
const orders = ref([])
const refunds = ref([])
const customerMap = ref(new Map())
const currentOrder = ref(null)

const filters = reactive({ orderNo: '', userKeyword: '', packageType: '', payStatus: '' })
const refundFilters = reactive({ originalOrderNo: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10 })
const dateRange = ref([])
const packageForm = reactive(createDefaultPackageForm())

const headerStyle = { background: '#f8fafc', color: '#64748b', fontSize: '12px' }

function createDefaultPackageForm() {
  return {
    id: null,
    name: '',
    packageType: 2,
    price: 0,
    points: 0,
    validityDays: 30,
    description: '',
    rightsText: '',
    status: '0',
    sort: 0,
    remark: ''
  }
}

function resetPackageForm() {
  Object.assign(packageForm, createDefaultPackageForm())
}

function formatCurrency(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function payMethodText(value) {
  return value === 'wechat' ? '微信' : value === 'alipay' ? '支付宝' : value || '-'
}

function payStatusText(value) {
  return value === '0' ? '待支付' : value === '1' ? '已支付' : value === '2' ? '已退款' : '未知'
}

function payStatusType(value) {
  return value === '1' ? 'success' : value === '0' ? 'warning' : 'info'
}

function refundStatusText(value) {
  return value === '0' ? '待处理' : value === '1' ? '已退款' : value === '2' ? '已拒绝' : '未知'
}

function refundStatusType(value) {
  return value === '1' ? 'success' : value === '0' ? 'warning' : 'danger'
}

const displayOrders = computed(() => {
  const keyword = filters.userKeyword.trim().toLowerCase()
  return orders.value.filter(item => {
    const userMatch = !keyword || (item.userName || '').toLowerCase().includes(keyword) || (item.phone || '').toLowerCase().includes(keyword)
    const packageMatch = filters.packageType === '' || item.packageType === filters.packageType
    return userMatch && packageMatch
  })
})

const paymentStats = computed(() => {
  const paid = displayOrders.value.filter(item => item.payStatus === '1')
  const refunded = displayOrders.value.filter(item => item.payStatus === '2')
  const income = paid.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  return [
    { label: '订单总数', value: total.value, color: '#2563ff' },
    { label: '已支付', value: paid.length, color: '#22c55e' },
    { label: '已退款', value: refunded.length, color: '#ef4444' },
    { label: '当前页收入', value: formatCurrency(income), color: '#0f766e' }
  ]
})

async function fetchPackages() {
  packageLoading.value = true
  try {
    const res = await listAdminPackages({ pageNum: 1, pageSize: 100 })
    packageList.value = (res.rows || []).map(item => ({ ...item, packageType: Number(item.packageType || 0) }))
  } catch (error) {
    ElMessage.error(error.message || '获取套餐列表失败')
  } finally {
    packageLoading.value = false
  }
}

async function fetchCustomers() {
  const res = await listAdminCustomers({ pageNum: 1, pageSize: 500 })
  const map = new Map()
  ;(res.rows || []).forEach(item => {
    map.set(item.userId, item)
  })
  customerMap.value = map
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      orderNo: filters.orderNo || undefined,
      payStatus: filters.payStatus || undefined,
      beginDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    }
    const res = await listAdminPayments(params)
    orders.value = (res.rows || []).map(item => {
      const customer = customerMap.value.get(item.userId) || {}
      const pkg = packageList.value.find(pkgItem => pkgItem.id === item.packageId) || packageList.value.find(pkgItem => pkgItem.name === item.packageName)
      return {
        ...item,
        userName: customer.nickName || customer.userName || customer.phonenumber || '',
        phone: customer.phonenumber || '',
        packageType: pkg ? Number(pkg.packageType || 0) : 1
      }
    })
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取充值订单失败')
  } finally {
    loading.value = false
  }
}

async function fetchRefunds() {
  refundLoading.value = true
  try {
    const res = await listAdminRefunds({ pageNum: 1, pageSize: 100, status: refundFilters.status || undefined })
    let list = res.rows || []
    if (refundFilters.originalOrderNo) {
      const keyword = refundFilters.originalOrderNo.toLowerCase()
      list = list.filter(item => (item.originalOrderNo || '').toLowerCase().includes(keyword))
    }
    refunds.value = list
  } catch (error) {
    ElMessage.error(error.message || '获取退款记录失败')
  } finally {
    refundLoading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  fetchOrders()
}

function handleReset() {
  filters.orderNo = ''
  filters.userKeyword = ''
  filters.packageType = ''
  filters.payStatus = ''
  dateRange.value = []
  pagination.pageNum = 1
  fetchOrders()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchOrders()
}

function openPackageDialog(row) {
  resetPackageForm()
  if (row) {
    Object.assign(packageForm, { ...row, price: Number(row.price || 0) })
  }
  packageDialogVisible.value = true
}

async function submitPackage() {
  savingPackage.value = true
  try {
    const payload = { ...packageForm, price: Number(packageForm.price || 0) }
    if (payload.id) {
      await updateAdminPackage(payload)
    } else {
      await addAdminPackage(payload)
    }
    ElMessage.success('套餐保存成功')
    packageDialogVisible.value = false
    await fetchPackages()
  } catch (error) {
    ElMessage.error(error.message || '保存套餐失败')
  } finally {
    savingPackage.value = false
  }
}

async function togglePackageStatus(row, enabled) {
  try {
    await changeAdminPackageStatus({ id: row.id, status: enabled ? '0' : '1' })
    row.status = enabled ? '0' : '1'
    ElMessage.success('套餐状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新套餐状态失败')
  }
}

async function handleDeletePackage(row) {
  try {
    await ElMessageBox.confirm(`确定要删除套餐"${row.name}"吗？删除后用户端将不再展示该套餐。`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteAdminPackage(row.id)
    ElMessage.success('套餐已删除')
    await fetchPackages()
  } catch (error) {
    if (error === 'cancel' || error?.action === 'cancel' || error?.action === 'close') return
    ElMessage.error(error.message || '删除套餐失败')
  }
}

function viewOrder(row) {
  currentOrder.value = row
  dialogVisible.value = true
}

function openRefundDrawer(orderNo = '') {
  refundDrawerVisible.value = true
  refundFilters.originalOrderNo = orderNo
  fetchRefunds()
}

async function handleRefund(row, status) {
  try {
    const result = await ElMessageBox.prompt(status === '1' ? '请输入退款处理备注' : '请输入拒绝原因', status === '1' ? '确认退款' : '拒绝退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await processAdminRefund({ id: row.id, status, processRemark: result.value || '' })
    ElMessage.success(status === '1' ? '退款已处理' : '已拒绝退款')
    fetchRefunds()
    fetchOrders()
  } catch (error) {
    if (error === 'cancel' || error?.action === 'cancel' || error?.action === 'close') return
    ElMessage.error(error.message || '退款处理失败')
  }
}

async function exportPayments() {
  try {
    const blobRes = await exportAdminPayments({
      orderNo: filters.orderNo || undefined,
      payStatus: filters.payStatus || undefined,
      beginDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    })
    const blob = blobRes instanceof Blob ? blobRes : new Blob([blobRes])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `充值订单-${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('订单数据已导出')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  }
}

onMounted(async () => {
  await Promise.all([fetchPackages(), fetchCustomers()])
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.admin-orders {
  display: flex;
  flex-direction: column;
}

.order-tabs {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  border: none;

  :deep(.el-tabs__content) {
    padding: 24px;
  }
}

.section-header,
.filter-bar,
.filter-left,
.filter-right,
.refund-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-header,
.filter-bar {
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
}

.section-desc {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
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
  font-size: 13px;
  color: #64748b;
}

.table-card {
  padding: 20px;
}

.user-cell,
.detail-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
}

.detail-label {
  color: #94a3b8;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
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