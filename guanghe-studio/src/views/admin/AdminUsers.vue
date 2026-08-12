<template>
  <div class="admin-users">
    <!-- 搜索与筛选 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input v-model="searchKey" placeholder="搜索用户名、手机号" clearable size="default" style="width: 260px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterVip" placeholder="VIP状态" clearable style="width: 140px">
          <el-option label="全部用户" value="" />
          <el-option label="VIP用户" value="vip" />
          <el-option label="普通用户" value="normal" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="账号状态" clearable style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="正常" value="active" />
          <el-option label="封禁" value="banned" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <div class="filter-right">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table :data="filteredUsers" :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontSize: '12px' }" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ row.nickname[0] }}</el-avatar>
              <div class="user-cell-info">
                <div class="user-cell-name">{{ row.nickname }}</div>
                <div class="user-cell-phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="vipLevel" label="VIP" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.vipLevel > 0" type="warning" size="small">VIP{{ row.vipLevel }}</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column prop="totalGenerated" label="生成次数" width="100" />
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewUser(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="editUser(row)">编辑</el-button>
            <el-button link :type="row.status === '正常' ? 'danger' : 'success'" size="small" @click="toggleBan(row)">
              {{ row.status === '正常' ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="table-footer">
        <div class="table-footer-info">共 {{ filteredUsers.length }} 条记录</div>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalUsers"
          layout="prev, pager, next"
          small
        />
      </div>
    </div>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="用户详情" size="400px">
      <template v-if="currentUser">
        <div class="drawer-user-header">
          <el-avatar :size="56">{{ currentUser.nickname[0] }}</el-avatar>
          <div class="drawer-user-info">
            <div class="drawer-user-name">{{ currentUser.nickname }}</div>
            <div class="drawer-user-phone">{{ currentUser.phone }}</div>
          </div>
        </div>
        <el-divider />
        <div class="drawer-detail-list">
          <div class="drawer-detail-item"><span class="drawer-label">用户ID</span><span>{{ currentUser.id }}</span></div>
          <div class="drawer-detail-item"><span class="drawer-label">VIP等级</span><span>{{ currentUser.vipLevel > 0 ? 'VIP' + currentUser.vipLevel : '普通用户' }}</span></div>
          <div class="drawer-detail-item"><span class="drawer-label">积分余额</span><span>{{ currentUser.points }}</span></div>
          <div class="drawer-detail-item"><span class="drawer-label">生成次数</span><span>{{ currentUser.totalGenerated }}</span></div>
          <div class="drawer-detail-item"><span class="drawer-label">注册时间</span><span>{{ currentUser.registerTime }}</span></div>
          <div class="drawer-detail-item"><span class="drawer-label">账号状态</span><span>{{ currentUser.status }}</span></div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKey = ref('')
const filterVip = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10
const totalUsers = 58
const drawerVisible = ref(false)
const currentUser = ref(null)
const selectedRows = ref([])

const mockUsers = [
  { id: 1001, nickname: '张小明', phone: '138****1234', vipLevel: 2, points: 5680, totalGenerated: 326, registerTime: '2026-03-12 10:20', status: '正常' },
  { id: 1002, nickname: '李婷婷', phone: '139****5678', vipLevel: 1, points: 3200, totalGenerated: 198, registerTime: '2026-04-05 14:30', status: '正常' },
  { id: 1003, nickname: '王大力', phone: '136****9012', vipLevel: 3, points: 12400, totalGenerated: 856, registerTime: '2026-01-18 09:15', status: '正常' },
  { id: 1004, nickname: '赵雪', phone: '137****3456', vipLevel: 1, points: 1850, totalGenerated: 142, registerTime: '2026-05-22 16:45', status: '正常' },
  { id: 1005, nickname: '陈宇', phone: '135****7890', vipLevel: 0, points: 420, totalGenerated: 38, registerTime: '2026-06-10 11:20', status: '正常' },
  { id: 1006, nickname: '刘芳', phone: '133****2345', vipLevel: 0, points: 150, totalGenerated: 12, registerTime: '2026-07-01 08:30', status: '封禁' },
  { id: 1007, nickname: '孙磊', phone: '158****6789', vipLevel: 2, points: 8900, totalGenerated: 520, registerTime: '2026-02-28 13:00', status: '正常' },
  { id: 1008, nickname: '周洁', phone: '159****0123', vipLevel: 1, points: 2100, totalGenerated: 165, registerTime: '2026-04-15 17:50', status: '正常' }
]

const filteredUsers = computed(() => {
  let list = [...mockUsers]
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    list = list.filter(u => u.nickname.toLowerCase().includes(key) || u.phone.includes(key))
  }
  if (filterVip.value === 'vip') list = list.filter(u => u.vipLevel > 0)
  if (filterVip.value === 'normal') list = list.filter(u => u.vipLevel === 0)
  if (filterStatus.value === 'active') list = list.filter(u => u.status === '正常')
  if (filterStatus.value === 'banned') list = list.filter(u => u.status === '封禁')
  return list
})

function handleSearch() {
  currentPage.value = 1
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function viewUser(row) {
  currentUser.value = row
  drawerVisible.value = true
}

function editUser(row) {
  ElMessage.info(`编辑用户: ${row.nickname}`)
}

function toggleBan(row) {
  const action = row.status === '正常' ? '封禁' : '解封'
  ElMessageBox.confirm(`确定要${action}用户 "${row.nickname}" 吗？`, '确认操作', {
    type: 'warning'
  }).then(() => {
    row.status = row.status === '正常' ? '封禁' : '正常'
    ElMessage.success(`已${action}用户 ${row.nickname}`)
  }).catch(() => {})
}

function handleExport() {
  ElMessage.success('用户数据导出成功（模拟）')
}
</script>

<style lang="scss" scoped>
.admin-users {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #64748b;
  border-radius: 8px;
  overflow: hidden;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell-info {
  min-width: 0;
}

.user-cell-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.user-cell-phone {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.table-footer-info {
  font-size: 13px;
  color: #94a3b8;
}

// ========== 抽屉 ==========
.drawer-user-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.drawer-user-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.drawer-user-phone {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.drawer-detail-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.drawer-detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #334155;
}

.drawer-label {
  color: #94a3b8;
}
</style>
