<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :size="22" :style="{ color: stat.color }"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
          {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
          <span class="trend-label">较昨日</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-card wide">
        <div class="card-header">
          <span class="card-title">用户增长趋势</span>
          <el-radio-group v-model="userChartRange" size="small">
            <el-radio-button label="7d">7天</el-radio-button>
            <el-radio-button label="30d">30天</el-radio-button>
            <el-radio-button label="90d">90天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="chart-body">
          <div class="simple-bar-chart">
            <div class="chart-y-axis">
              <span>300</span>
              <span>200</span>
              <span>100</span>
              <span>0</span>
            </div>
            <div class="chart-bars">
              <div v-for="(bar, i) in userChartData" :key="i" class="chart-bar-wrapper">
                <div class="chart-bar" :style="{ height: bar.height + '%' }"></div>
                <div class="chart-label">{{ bar.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <span class="card-title">功能使用分布</span>
          <span class="card-subtitle">本月累计</span>
        </div>
        <div class="chart-body">
          <div class="usage-list">
            <div class="usage-item" v-for="item in usageData" :key="item.name">
              <div class="usage-head">
                <span class="usage-name">{{ item.name }}</span>
                <span class="usage-count">{{ item.count }} 次</span>
              </div>
              <div class="usage-bar-bg">
                <div class="usage-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近订单 + 活跃用户 -->
    <div class="detail-row">
      <div class="detail-card">
        <div class="card-header">
          <span class="card-title">最近订单</span>
          <router-link to="/admin/orders" class="card-link">查看全部</router-link>
        </div>
        <el-table :data="recentOrders" size="small" :header-cell-style="{ background: '#f8fafc', color: '#64748b', fontSize: '12px' }">
          <el-table-column prop="orderId" label="订单号" width="160" />
          <el-table-column prop="user" label="用户" width="120" />
          <el-table-column prop="product" label="套餐" width="120" />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">
              <span class="amount-text">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '已完成' ? 'success' : row.status === '处理中' ? 'warning' : 'info'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="时间" min-width="150" />
        </el-table>
      </div>

      <div class="detail-card">
        <div class="card-header">
          <span class="card-title">今日活跃用户</span>
          <router-link to="/admin/users" class="card-link">查看全部</router-link>
        </div>
        <div class="active-user-list">
          <div class="active-user-item" v-for="(u, i) in activeUsers" :key="i">
            <el-avatar :size="32" :src="u.avatar || ''">{{ u.name[0] }}</el-avatar>
            <div class="user-info-text">
              <div class="user-name">{{ u.name }}</div>
              <div class="user-desc">{{ u.desc }}</div>
            </div>
            <span class="user-rank">#{{ i + 1 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const userChartRange = ref('7d')

const stats = [
  { label: '总用户数', value: '12,846', icon: 'UserFilled', color: '#2563ff', bgColor: 'rgba(37,99,255,0.1)', trend: 12.5 },
  { label: '今日新增', value: '286', icon: 'Plus', color: '#22c55e', bgColor: 'rgba(34,197,94,0.1)', trend: 8.2 },
  { label: '今日收入', value: '¥18,420', icon: 'Wallet', color: '#f59e0b', bgColor: 'rgba(245,158,11,0.1)', trend: -3.1 },
  { label: '总订单数', value: '8,532', icon: 'ShoppingCart', color: '#8b5cf6', bgColor: 'rgba(139,92,246,0.1)', trend: 5.7 }
]

const userChartData = computed(() => {
  const data7d = [
    { label: '周一', height: 45 },
    { label: '周二', height: 62 },
    { label: '周三', height: 55 },
    { label: '周四', height: 78 },
    { label: '周五', height: 90 },
    { label: '周六', height: 40 },
    { label: '周日', height: 35 }
  ]
  const data30d = [
    { label: '第1周', height: 50 },
    { label: '第2周', height: 65 },
    { label: '第3周', height: 72 },
    { label: '第4周', height: 88 }
  ]
  const data90d = [
    { label: '1月', height: 40 },
    { label: '2月', height: 55 },
    { label: '3月', height: 85 }
  ]
  if (userChartRange.value === '30d') return data30d
  if (userChartRange.value === '90d') return data90d
  return data7d
})

const usageData = [
  { name: '白底图生成', count: 8456, percent: 92, color: '#2563ff' },
  { name: '背景替换', count: 6832, percent: 78, color: '#22c55e' },
  { name: '产品精修', count: 5210, percent: 62, color: '#f59e0b' },
  { name: '主图设计', count: 4120, percent: 48, color: '#8b5cf6' },
  { name: 'AI模特', count: 3540, percent: 38, color: '#ec4899' },
  { name: '详情图/A+', count: 2890, percent: 32, color: '#06b6d4' },
  { name: 'Banner设计', count: 1860, percent: 22, color: '#f97316' },
  { name: '批量处理', count: 1240, percent: 15, color: '#64748b' }
]

const recentOrders = [
  { orderId: 'GH20260714001', user: '张小明', product: '专业版月卡', amount: '99.00', status: '已完成', time: '2026-07-14 15:30' },
  { orderId: 'GH20260714002', user: '李婷婷', product: '积分包 500', amount: '49.00', status: '已完成', time: '2026-07-14 14:22' },
  { orderId: 'GH20260714003', user: '王大力', product: '企业版年卡', amount: '2999.00', status: '处理中', time: '2026-07-14 13:45' },
  { orderId: 'GH20260714004', user: '赵雪', product: '专业版季卡', amount: '249.00', status: '已完成', time: '2026-07-14 11:18' },
  { orderId: 'GH20260714005', user: '陈宇', product: '积分包 1000', amount: '89.00', status: '已退款', time: '2026-07-14 09:05' }
]

const activeUsers = [
  { name: '张小明', desc: '今日生成 128 张', avatar: '' },
  { name: '李婷婷', desc: '今日生成 96 张', avatar: '' },
  { name: '王大力', desc: '今日生成 82 张', avatar: '' },
  { name: '赵雪', desc: '今日生成 65 张', avatar: '' },
  { name: '陈宇', desc: '今日生成 54 张', avatar: '' }
]
</script>

<style lang="scss" scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ========== 统计卡片 ==========
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;

  &.up { color: #22c55e; }
  &.down { color: #ef4444; }
}

.trend-label {
  margin-left: 2px;
  color: #94a3b8;
  font-weight: 400;
}

// ========== 图表区域 ==========
.chart-row {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.card-subtitle {
  font-size: 12px;
  color: #94a3b8;
}

.card-link {
  font-size: 13px;
  color: var(--gh-primary);
  text-decoration: none;

  &:hover { text-decoration: underline; }
}

// ========== 柱状图 ==========
.simple-bar-chart {
  display: flex;
  gap: 8px;
  height: 200px;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 32px;
  flex-shrink: 0;

  span {
    font-size: 11px;
    color: #94a3b8;
    text-align: right;
  }
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding-bottom: 24px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 24px;
    left: 0;
    right: 0;
    height: 1px;
    background: #f1f5f9;
  }
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  position: relative;
}

.chart-bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, var(--gh-primary), rgba(37, 99, 255, 0.6));
  border-radius: 4px 4px 0 0;
  transition: height 0.5s ease;
  min-height: 4px;
  margin-top: auto;
}

.chart-label {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 8px;
  white-space: nowrap;
  position: absolute;
  bottom: -20px;
}

// ========== 使用分布 ==========
.usage-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.usage-item {
  .usage-head {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
  }

  .usage-name {
    font-size: 13px;
    color: #334155;
    font-weight: 500;
  }

  .usage-count {
    font-size: 12px;
    color: #64748b;
    font-weight: 600;
  }
}

.usage-bar-bg {
  height: 6px;
  background: #f1f5f9;
  border-radius: 3px;
  overflow: hidden;
}

.usage-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

// ========== 详情行 ==========
.detail-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-card {
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

.amount-text {
  font-weight: 600;
  color: #1e293b;
}

// ========== 活跃用户 ==========
.active-user-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.active-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f8fafc;
  }
}

.user-info-text {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.user-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.user-rank {
  font-size: 13px;
  color: #cbd5e1;
  font-weight: 600;
  flex-shrink: 0;
}
</style>
