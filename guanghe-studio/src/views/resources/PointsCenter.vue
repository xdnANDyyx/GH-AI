<template>
  <div class="points-center-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">积分变动明细</h2>
        <p class="gh-page-subtitle">记录您的积分收支情况，清晰透明</p>
      </div>
      <div class="rule-link" @click="showRules">
        <el-icon :size="16"><QuestionFilled /></el-icon>
        积分规则说明
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="icon">
          <el-icon :size="48" color="#F59E0B"><StarFilled /></el-icon>
        </div>
        <div class="stat-label">当前可用积分</div>
        <div class="stat-value">{{ currentPoints.toLocaleString() }} <span class="star">★</span></div>
      </div>
      <div class="stat-card">
        <div class="icon">
          <el-icon :size="48" color="#EF4444"><TrendCharts /></el-icon>
        </div>
        <div class="stat-label">本月消耗积分</div>
        <div class="stat-value red">-{{ monthConsume.toLocaleString() }}</div>
        <div class="stat-change down">较上月 {{ monthConsumeChange }}% ↓</div>
      </div>
      <div class="stat-card">
        <div class="icon">
          <el-icon :size="48" color="#22C55E"><TrendCharts /></el-icon>
        </div>
        <div class="stat-label">本月充值积分</div>
        <div class="stat-value green">+{{ monthRecharge.toLocaleString() }}</div>
        <div class="stat-change up">较上月 {{ monthRechargeChange }}% ↑</div>
      </div>
      <div class="stat-card">
        <div class="icon">
          <el-icon :size="48" color="#2563FF"><CreditCard /></el-icon>
        </div>
        <div class="stat-label">历史总充值</div>
        <div class="stat-value">¥{{ totalRecharge.toFixed(2) }}</div>
        <div class="stat-sub">共充值 {{ totalRechargeCount }} 次</div>
      </div>
    </div>

    <!-- 筛选行 -->
    <div class="tabs-row">
      <div class="tabs">
        <div
          v-for="t in tabs"
          :key="t.value"
          class="tab"
          :class="{ active: activeTab === t.value }"
          @click="activeTab = t.value"
        >
          {{ t.label }}
        </div>
      </div>
      <div class="filter-right">
        <el-select v-model="filterType" placeholder="全部类型" clearable class="filter-el-select">
          <el-option label="全部类型" value="all" />
          <el-option label="积分消耗" value="consume" />
          <el-option label="充值积分" value="recharge" />
          <el-option label="赠送积分" value="gift" />
          <el-option label="积分退还" value="refund" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="~"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="filter-el-daterange"
          @change="onDateRangeChange"
        />
        <el-button type="primary" plain @click="exportRecords">
          <el-icon :size="14"><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 明细表格 -->
    <div class="table-wrapper">
      <el-table v-loading="loading" :data="filteredRecords" style="width: 100%" :header-cell-style="headerStyle">
        <el-table-column prop="time" label="时间" width="170" />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <div class="type-badge">
              <div class="type-dot" :class="typeDotClass(row.category)">
                {{ typeDotSign(row.category) }}
              </div>
              {{ typeLabel(row.category) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变动积分" width="120">
          <template #default="{ row }">
            <span :class="isPositiveCategory(row.category) ? 'change-pos' : 'change-neg'">{{ row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="积分余额" width="110" />
        <el-table-column label="说明" min-width="200">
          <template #default="{ row }">
            <div class="desc-cell">
              <span class="desc-text">{{ row.desc }}</span>
              <span class="desc-detail">{{ row.detail }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单信息" min-width="180">
          <template #default="{ row }">
            <div class="desc-cell" v-if="row.orderNo">
              <span class="desc-text">订单号：{{ row.orderNo }}</span>
              <span class="desc-detail" v-if="row.payAmount">支付金额：¥{{ row.payAmount }}</span>
            </div>
            <span v-else class="dim-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <span class="link-blue" @click="viewDetail(row)">查看详情</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !filteredRecords.length" description="暂无积分记录" />
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span>共 {{ totalRecords }} 条记录</span>
      <div class="pagination-right">
        <el-select v-model="pageSize" class="filter-el-select" style="width: 110px;">
          <el-option :value="10" label="10条/页" />
          <el-option :value="20" label="20条/页" />
          <el-option :value="50" label="50条/页" />
        </el-select>
        <div class="page-btns">
          <div class="page-btn" @click="prevPage">‹</div>
          <div
            v-for="p in totalPages"
            :key="p"
            class="page-btn"
            :class="{ active: currentPage === p }"
            @click="currentPage = p"
          >
            {{ p }}
          </div>
          <div class="page-btn" @click="nextPage">›</div>
        </div>
        <div class="page-jump">
          跳至 <input type="number" v-model.number="jumpPage" min="1" :max="totalPages" @change="jumpToPage" /> 页
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, TrendCharts, CreditCard, Download, QuestionFilled } from '@element-plus/icons-vue'
import { getDeductRecords, getPackageRecords } from '@/api/customer'
import { useUserStore } from '@/store'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])
const activeTab = ref('all')
const filterType = ref('all')
const dateRange = ref([])
const startDate = ref('')
const endDate = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref(1)

const headerStyle = { background: '#FAFBFC', color: '#6B7280', fontWeight: 500 }
const tabs = [
  { label: '全部记录', value: 'all' },
  { label: '积分消耗', value: 'expense' },
  { label: '充值记录', value: 'income' }
]

const currentPoints = computed(() => userStore.userInfo.points ?? 0)

// 统计
const monthConsume = computed(() => {
  const now = new Date()
  return records.value
    .filter(r => r.category === 'expense' && isThisMonth(r.time))
    .reduce((sum, r) => sum + Math.abs(Number(r.rawPoints || 0)), 0)
})
const monthRecharge = computed(() => {
  return records.value
    .filter(r => r.category === 'income' && isThisMonth(r.time))
    .reduce((sum, r) => sum + Number(r.rawPoints || 0), 0)
})
const monthConsumeChange = computed(() => 15)
const monthRechargeChange = computed(() => 25)
const totalRecharge = computed(() => {
  return records.value
    .filter(r => r.category === 'income')
    .reduce((sum, r) => sum + Number(r.payAmount || 0), 0)
})
const totalRechargeCount = computed(() => records.value.filter(r => r.category === 'income').length)

// 筛选
function applyFilters(list) {
  let result = list
  if (activeTab.value !== 'all') {
    result = result.filter(r => r.category === activeTab.value)
  }
  if (filterType.value && filterType.value !== 'all') {
    const categoryMap = { consume: 'expense', recharge: 'income', gift: 'gift', refund: 'refund' }
    const targetCat = categoryMap[filterType.value]
    if (targetCat) result = result.filter(r => r.category === targetCat)
  }
  if (startDate.value) {
    result = result.filter(r => new Date(r.time) >= new Date(startDate.value))
  }
  if (endDate.value) {
    result = result.filter(r => new Date(r.time) <= new Date(endDate.value + ' 23:59:59'))
  }
  return result
}

const filteredRecords = computed(() => {
  const list = applyFilters(records.value)
  const start = (currentPage.value - 1) * pageSize.value
  return list.slice(start, start + pageSize.value)
})

const totalRecords = computed(() => applyFilters(records.value).length)

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

function isThisMonth(timeStr) {
  if (!timeStr) return false
  const d = new Date(timeStr)
  const now = new Date()
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
}

function normalizeType(type) {
  if (['consume', 'deduct', 'usage', 'expense'].includes(type)) return 'expense'
  if (['gift', 'bonus', 'reward'].includes(type)) return 'gift'
  if (['refund', 'return', 'rollback'].includes(type)) return 'refund'
  return 'income'
}

function typeLabel(category) {
  return { expense: '积分消耗', income: '充值积分', gift: '赠送积分', refund: '积分退还' }[category] || '积分变动'
}

function typeDotClass(category) {
  return { expense: 'consume', income: 'recharge', gift: 'gift', refund: 'refund' }[category] || 'recharge'
}

function typeDotSign(category) {
  return category === 'expense' ? '−' : '+'
}

function isPositiveCategory(category) {
  return category !== 'expense'
}

function onDateRangeChange(val) {
  if (val && val.length === 2) {
    startDate.value = val[0]
    endDate.value = val[1]
  } else {
    startDate.value = ''
    endDate.value = ''
  }
  currentPage.value = 1
}

function mapPackageRecord(item) {
  const totalPoints = Number(item.points || 0)
  return {
    time: item.payTime || item.createTime || '-',
    category: 'income',
    points: `+${totalPoints.toLocaleString()}`,
    rawPoints: totalPoints,
    balance: '-',
    desc: '积分充值',
    detail: item.packageName || '积分套餐充值',
    orderNo: item.orderNo || '-',
    payAmount: item.payAmount || item.amount || 0,
  }
}

function mapDeductRecord(item) {
  const changePoints = Number(item.changePoints ?? item.points ?? 0)
  const category = normalizeType(item.changeType || item.type)
  return {
    time: item.createTime || item.time || '-',
    category,
    points: `${changePoints > 0 ? '+' : ''}${changePoints.toLocaleString()}`,
    rawPoints: changePoints,
    balance: item.balance || '-',
    desc: item.reason || item.deductTypeName || item.typeName || '积分变动',
    detail: item.relatedOrderNo || item.relatedBizId || item.remark || '-',
    orderNo: item.relatedOrderNo || '',
    payAmount: 0,
  }
}

function sortRecords(list) {
  return list.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
}

async function fetchRecords() {
  loading.value = true
  try {
    const [packageRes, deductRes] = await Promise.all([
      getPackageRecords(1, 100),
      getDeductRecords()
    ])
    const packageRows = packageRes.rows || packageRes.data?.rows || packageRes.data || []
    const deductRows = deductRes.rows || deductRes.data?.rows || deductRes.data || deductRes || []
    const packageList = Array.isArray(packageRows) ? packageRows.map(mapPackageRecord) : []
    const deductList = Array.isArray(deductRows) ? deductRows.map(mapDeductRecord) : []
    records.value = sortRecords([...packageList, ...deductList])
  } catch (error) {
    ElMessage.error(error.message || '获取积分明细失败')
  } finally {
    loading.value = false
  }
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}
function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}
function jumpToPage() {
  const p = Math.max(1, Math.min(totalPages.value, jumpPage.value || 1))
  currentPage.value = p
  jumpPage.value = p
}
function viewDetail(row) {
  ElMessage.info(`查看详情：${row.desc}`)
}
function exportRecords() {
  const list = applyFilters(records.value)
  if (!list.length) {
    ElMessage.warning('暂无可导出的记录')
    return
  }
  const headers = ['时间', '类型', '变动积分', '积分余额', '说明', '详情', '订单号', '支付金额']
  const rows = list.map(r => [
    r.time,
    typeLabel(r.category),
    r.points,
    r.balance,
    r.desc || '',
    r.detail || '',
    r.orderNo || '',
    r.payAmount || '',
  ])
  const csvContent = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `积分明细_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${list.length} 条记录`)
}
function showRules() {
  ElMessage.info('积分规则：积分可用于平台所有功能，不同功能消耗积分不同。一次购买，永久有效。')
}

onMounted(async () => {
  await userStore.fetchPoints()
  await fetchRecords()
})
</script>

<style lang="scss" scoped>
.points-center-page {
  padding: 4px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.rule-link {
  font-size: 13px;
  color: #2563FF;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    text-decoration: underline;
  }
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #E8EDF5;
  position: relative;
  overflow: hidden;

  .icon {
    position: absolute;
    right: 12px;
    top: 12px;
    opacity: 0.12;
  }
}

.stat-label {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #1F2937;

  .star {
    font-size: 16px;
    color: #F59E0B;
  }

  &.red {
    color: #EF4444;
  }

  &.green {
    color: #22C55E;
  }
}

.stat-change {
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;

  &.up {
    color: #22C55E;
  }

  &.down {
    color: #EF4444;
  }
}

.stat-sub {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
}

/* 筛选行 */
.tabs-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #E8EDF5;
}

.tab {
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  color: #6B7280;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.15s;

  &.active {
    color: #2563FF;
    border-bottom-color: #2563FF;
    font-weight: 500;
  }

  &:hover {
    color: #2563FF;
  }
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-el-select {
  width: 140px;
  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }
}

.filter-el-daterange {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }
}

.filter-select {
  padding: 8px 14px;
  border: 1px solid #E8EDF5;
  border-radius: 10px;
  font-size: 13px;
  background: #fff;
  outline: none;
  font-family: inherit;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6B7280;

  input {
    padding: 6px 10px;
    border: 1px solid #E8EDF5;
    border-radius: 8px;
    font-size: 13px;
    outline: none;
    font-family: inherit;
  }
}

.export-btn {
  padding: 8px 16px;
  border: 1px solid #2563FF;
  border-radius: 10px;
  background: #fff;
  color: #2563FF;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.15s;

  &:hover {
    background: #EFF6FF;
  }
}

/* 表格 */
.table-wrapper {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #E8EDF5;
  padding: 6px;
}

.type-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.type-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;

  &.consume {
    background: #DCFCE7;
    color: #16A34A;
  }

  &.recharge {
    background: #EEF2FF;
    color: #2563FF;
  }

  &.gift {
    background: #FEF3C7;
    color: #D97706;
  }

  &.refund {
    background: #F3E8FF;
    color: #9333EA;
  }
}

.change-neg {
  color: #EF4444;
  font-weight: 600;
}

.change-pos {
  color: #22C55E;
  font-weight: 600;
}

.desc-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.desc-text {
  font-size: 13px;
  color: #1F2937;
}

.desc-detail {
  font-size: 11px;
  color: #9CA3AF;
}

.dim-text {
  color: #9CA3AF;
  font-size: 13px;
}

.link-blue {
  color: #2563FF;
  cursor: pointer;
  font-size: 13px;

  &:hover {
    text-decoration: underline;
  }
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 13px;
  color: #6B7280;
  flex-wrap: wrap;
  gap: 12px;
}

.pagination-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-btns {
  display: flex;
  gap: 4px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #E8EDF5;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;

  &:hover {
    border-color: #2563FF;
    color: #2563FF;
  }

  &.active {
    background: #2563FF;
    color: #fff;
    border-color: #2563FF;
  }
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 8px;

  input {
    width: 48px;
    padding: 6px 8px;
    border: 1px solid #E8EDF5;
    border-radius: 6px;
    font-size: 13px;
    text-align: center;
    outline: none;
    font-family: inherit;
  }
}

/* 响应式 */
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }

  .tabs-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-right {
    width: 100%;
  }

  .pagination {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>