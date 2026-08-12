<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">我的积分</h2>
        <p class="gh-page-subtitle">查看积分余额、升级套餐与积分明细</p>
      </div>
    </div>

    <div class="points-card">
      <div class="points-left">
        <div class="points-label">当前积分</div>
        <div class="points-number">{{ currentPoints.toLocaleString() }}</div>
        <div class="points-sub-row">
          <span class="points-sub">一次购买，永久有效</span>
          <span class="points-divider">|</span>
          <span class="points-sub">积分可用于所有功能</span>
          <span class="points-divider">|</span>
          <span class="points-sub" @click="goToPointsCenter" style="cursor:pointer;color:#6ee7b7;">积分明细 ›</span>
        </div>
      </div>
      <div class="points-right">
        <el-button type="primary" round size="large" class="recharge-btn" :loading="submitting" @click="handleRecharge">
          立即充值
        </el-button>
        <el-button round size="large" class="redeem-btn" @click="handleRedeemCode">兑换码</el-button>
      </div>
    </div>

    <!-- ===== 我的套餐 ===== -->
    <div class="my-plan-section">
      <div class="section-header">
        <h3 class="section-title">我的套餐</h3>
        <span class="section-tip">当前使用中的套餐信息</span>
      </div>
      <div class="my-plan-card">
        <div class="plan-info-left">
          <div class="plan-current-badge">
            <el-icon :size="16"><StarFilled /></el-icon>
            当前套餐
          </div>
          <div class="plan-current-name">
            {{ membershipLabel }}
            <span class="vip-badge" v-if="isVip">VIP</span>
          </div>
          <div class="plan-current-desc">
            {{ isVip ? '畅享全部 AI 功能，积分更多，单价更低' : '体验基础功能，升级套餐解锁更多权益' }}
          </div>
          <div class="plan-current-meta">
            <span class="meta-item"><el-icon :size="13"><Coin /></el-icon> 当前积分：<strong>{{ currentPoints.toLocaleString() }}</strong></span>
            <span class="meta-divider">|</span>
            <span class="meta-item"><el-icon :size="13"><Clock /></el-icon> 有效期：<strong>永久有效</strong></span>
          </div>
        </div>
        <div class="plan-info-right">
          <el-button type="primary" round @click="scrollToPricing">
            <el-icon><ShoppingCart /></el-icon>
            升级套餐
          </el-button>
          <el-button round class="plan-detail-btn" @click="goToPointsCenter">积分明细</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 积分套餐（设计稿样式） ===== -->
    <div class="pricing-section" id="pricing-section">
      <!-- 顶部标签 -->
      <div class="top-label"><span>简单定价 · 无套路</span></div>
      <h2 class="pricing-title">选择适合您的方案</h2>
      <p class="pricing-subtitle">积分越多，单价越低，性价比越高</p>

      <!-- 优势列表 -->
      <div class="advantages">
        <div class="advantage">
          <svg viewBox="0 0 20 20" fill="none"><path d="M10 2L2 7l8 5 8-5-8-5z" stroke="currentColor" stroke-width="1.5"/><path d="M2 12l8 5 8-5" stroke="currentColor" stroke-width="1.5"/></svg>
          一次购买，永久有效
        </div>
        <div class="advantage">
          <svg viewBox="0 0 20 20" fill="none"><circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5"/><path d="M7 10l2 2 4-4" stroke="currentColor" stroke-width="1.5"/></svg>
          积分可用于所有功能
        </div>
        <div class="advantage">
          <svg viewBox="0 0 20 20" fill="none"><rect x="3" y="3" width="14" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M3 8h14" stroke="currentColor" stroke-width="1.2"/></svg>
          生成效果受原图与描述影响
        </div>
        <div class="advantage">
          <svg viewBox="0 0 20 20" fill="none"><rect x="4" y="8" width="12" height="9" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M7 8V6a3 3 0 016 0v2" stroke="currentColor" stroke-width="1.5"/></svg>
          安全支付，隐私无忧
        </div>
      </div>

      <!-- 套餐卡片 -->
      <el-empty v-if="!packages.length" description="暂无可购买套餐" style="margin: 32px 0" />
      <div v-else class="plans-grid">
        <div
          v-for="(pkg, idx) in packages"
          :key="pkg.id || idx"
          class="plan-card"
          :class="{
            featured: idx === recommendedIndex,
            selected: selectedPkg === idx,
            disabled: pkg.status !== '0'
          }"
          @click="selectPackage(idx)"
        >
          <div v-if="isTeamPlan(idx)" class="plan-badge badge-purple">团队用户推荐</div>
          <div v-else-if="idx === recommendedIndex" class="plan-badge badge-green">个人用户首选</div>
          <div class="hot-tag" v-if="idx === recommendedIndex">🔥 最受欢迎</div>
          <div class="plan-icon" :class="iconClass(idx)">
            <el-icon :size="24"><component :is="packageIcon(idx)" /></el-icon>
          </div>
          <div class="plan-name">{{ packageName(pkg, idx) }}</div>
          <div class="plan-desc">{{ packageDesc(pkg, idx) }}</div>
          <div class="plan-price" :class="priceClass(idx)"><span class="currency">¥</span>{{ packagePrice(pkg) }}</div>
          <div class="plan-points" :class="pointsClass(idx)">{{ Number(pkg.points || 0).toLocaleString() }} 积分</div>
          <div class="plan-gen-label">约可生成</div>
          <div class="plan-gen">
            <div class="plan-gen-item"><div class="num" :style="{ color: planColor(idx) }">{{ Math.floor(Number(pkg.points || 0) / 10).toLocaleString() }} 张</div><div class="type">Banana2 图</div></div>
            <div class="plan-gen-item"><div class="num" :style="{ color: planColor(idx) }">{{ Math.floor(Number(pkg.points || 0) / 5).toLocaleString() }} 张</div><div class="type">Image2 图</div></div>
          </div>
          <ul class="plan-features">
            <li v-for="(f, fi) in packageFeatures(pkg, idx)" :key="fi" :class="isTeamPlan(idx) ? 'purple' : ''">{{ f }}</li>
          </ul>
          <el-button
            :class="btnClass(idx)"
            round
            style="width: 100%; margin-top: 4px"
            :disabled="pkg.status !== '0'"
            @click.stop="selectPackage(idx)"
          >
            {{ selectedPkg === idx ? '已选择' : '立即选择' }}
          </el-button>
          <div class="pkg-validity" v-if="pkg.validityDays">{{ pkg.validityDays }}天有效期</div>
        </div>
      </div>

      <!-- 信息卡片 -->
      <div class="info-grid">
        <div class="info-card">
          <div class="info-icon">
            <el-icon :size="26" color="#2563FF"><Coin /></el-icon>
          </div>
          <div><h4>积分说明</h4><p>积分可用于平台所有功能，不同功能消耗积分不同。</p><a @click="goToPointsCenter">查看积分消耗详情 ›</a></div>
        </div>
        <div class="info-card">
          <div class="info-icon">
            <el-icon :size="26" color="#059669"><Clock /></el-icon>
          </div>
          <div><h4>积分有效期</h4><p>一次购买，永久有效，不清零，不过期。</p></div>
        </div>
        <div class="info-card">
          <div class="info-icon">
            <el-icon :size="26" color="#D97706"><MagicStick /></el-icon>
          </div>
          <div><h4>生成说明</h4><p>实际生成数量受原图质量、描述复杂度和所选功能影响。</p></div>
        </div>
        <div class="info-card">
          <div class="info-icon">
            <el-icon :size="26" color="#7C3AED"><Lock /></el-icon>
          </div>
          <div><h4>安全保障</h4><p>安全支付，隐私无忧，支持主流支付方式。</p></div>
        </div>
      </div>

      <div class="footer-note">注：生成效果受原图与描述影响，具体以实际效果为准。</div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Coin, Clock, MagicStick, Lock, Lightning, StarFilled, UserFilled, Trophy, Medal, Present, ShoppingCart } from '@element-plus/icons-vue'
import {
  getPointsPackages,
  purchasePackage,
  queryPaymentStatus
} from '@/api/customer'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const selectedPkg = ref(0)
const packages = ref([])

// 设计稿默认套餐（API 无数据时的兜底展示）
const DEFAULT_PLANS = [
  { id: 'default-1', packageName: '入门版', description: '适合新手体验', price: 19.9, points: 100, sort: 0, status: '0', features: ['适合个人轻量使用', '适合日常基础出图', '所有基础功能可用', '一次购买，永久有效'] },
  { id: 'default-2', packageName: '基础版', description: '适合个人日常使用', price: 199, points: 500, sort: 0, status: '0', features: ['适合个人日常出图', '更多生成次数', '所有基础功能可用', '一次购买，永久有效'] },
  { id: 'default-3', packageName: '专业版', description: '适合高频个人用户', price: 499, points: 2400, sort: 1, status: '0', features: ['适合高频个人使用', '更多积分，更低单价', '所有功能畅享', '一次购买，永久有效'] },
  { id: 'default-4', packageName: '团队版', description: '适合小团队协作', price: 1299, points: 7500, sort: 0, status: '0', features: ['适合团队日常使用', '可添加子账号（3人）', '统一管理，灵活分配', '一次购买，永久有效'] },
  { id: 'default-5', packageName: '企业版', description: '适合中大型团队/企业', price: 3299, points: 20000, sort: 0, status: '0', features: ['适合企业大规模使用', '可添加子账号（10人）', '专属客户经理服务', '优先技术支持', '一次购买，永久有效'] }
]

// 套餐默认名称/描述（按推荐顺序）
const defaultPlanNames = ['入门版', '基础版', '专业版', '团队版', '企业版']
const defaultPlanDescs = [
  '适合新手体验',
  '适合个人日常使用',
  '适合高频个人用户',
  '适合小团队协作',
  '适合中大型团队/企业'
]
const defaultPlanFeatures = [
  ['适合个人轻量使用', '适合日常基础出图', '所有基础功能可用', '一次购买，永久有效'],
  ['适合个人日常出图', '更多生成次数', '所有基础功能可用', '一次购买，永久有效'],
  ['适合高频个人使用', '更多积分，更低单价', '所有功能畅享', '一次购买，永久有效'],
  ['适合团队日常使用', '可添加子账号（3人）', '统一管理，灵活分配', '一次购买，永久有效'],
  ['适合企业大规模使用', '可添加子账号（10人）', '专属客户经理服务', '优先技术支持', '一次购买，永久有效']
]

const defaultPlanDesc = '优质积分套餐'
const defaultFeatures = ['所有基础功能可用', '积分即时到账', '一次购买，永久有效']

const currentPoints = computed(() => userStore.userInfo.points ?? 0)
const membershipLabel = computed(() => userStore.userInfo.vipLevel > 0 ? '专业版' : '体验版')
const isVip = computed(() => (userStore.userInfo.vipLevel || 0) > 0)

// 推荐套餐索引：sort=1 或第二个
const recommendedIndex = computed(() => {
  if (!packages.value.length) return 0
  const idx = packages.value.findIndex(item => Number(item.sort || 0) === 1)
  return idx >= 0 ? idx : Math.min(1, packages.value.length - 1)
})

// ===== 图标（符合页面风格，替换 emoji）=====
function packageIcon(idx) {
  const icons = [Present, Lightning, StarFilled, UserFilled, Trophy]
  return icons[idx % icons.length] || Medal
}

function iconClass(idx) {
  const classes = ['icon-green', 'icon-blue', 'icon-green', 'icon-purple', 'icon-amber']
  return classes[idx % classes.length]
}

// 套餐主题色按设计稿：绿/蓝/绿/紫/琥珀
const PLAN_THEME = [
  { color: '#059669', bg: '#D1FAE5', price: 'price-green', points: 'points-green', btn: 'btn-green-outline' },
  { color: '#2563FF', bg: '#EFF6FF', price: 'price-blue', points: 'points-blue', btn: 'btn-blue-outline' },
  { color: '#059669', bg: '#D1FAE5', price: 'price-green', points: 'points-green', btn: 'btn-green-fill' },
  { color: '#8B5CF6', bg: '#F3E8FF', price: 'price-purple', points: 'points-purple', btn: 'btn-purple-outline' },
  { color: '#D97706', bg: '#FFFBEB', price: 'price-amber', points: 'points-amber', btn: 'btn-amber-outline' }
]

function priceClass(idx) {
  return PLAN_THEME[idx % PLAN_THEME.length]?.price || 'price-green'
}

function pointsClass(idx) {
  return PLAN_THEME[idx % PLAN_THEME.length]?.points || 'points-green'
}

function btnClass(idx) {
  const b = PLAN_THEME[idx % PLAN_THEME.length]?.btn || 'btn-green-outline'
  return `plan-btn ${b}`
}

function planColor(idx) {
  return PLAN_THEME[idx % PLAN_THEME.length]?.color || '#059669'
}

function isTeamPlan(idx) {
  return PLAN_THEME[idx % PLAN_THEME.length]?.btn === 'btn-purple-outline'
}

// ===== 套餐展示辅助 =====
function packageName(pkg, idx) {
  if (pkg.packageName) return pkg.packageName
  if (pkg.name) return pkg.name
  return defaultPlanNames[idx] || `套餐${idx + 1}`
}

function packageDesc(pkg, idx) {
  if (pkg.description) return pkg.description
  return defaultPlanDescs[idx] || defaultPlanDesc
}

function packagePrice(pkg) {
  const price = Number(pkg.price || 0)
  return price.toLocaleString('zh-CN', { minimumFractionDigits: price % 1 === 0 ? 0 : 2, maximumFractionDigits: 2 })
}

function packageFeatures(pkg, idx) {
  if (pkg.features && Array.isArray(pkg.features) && pkg.features.length) {
    return pkg.features
  }
  if (pkg.rightsText) {
    return [pkg.rightsText, '积分即时到账', '一次购买，永久有效']
  }
  return defaultPlanFeatures[idx] || defaultFeatures
}

// ===== 图标颜色（深绿替换） =====
const iconPalette = [
  { bg: '#D1FAE5', color: '#059669' },
  { bg: '#EFF6FF', color: '#2563FF' },
  { bg: '#FEF3C7', color: '#D97706' },
  { bg: '#F3E8FF', color: '#7C3AED' }
]

// ===== 数据逻辑 =====
function selectPackage(idx) {
  if (packages.value[idx]?.status !== '0') return
  selectedPkg.value = idx
}

async function fetchPackages() {
  try {
    const res = await getPointsPackages({ pageNum: 1, pageSize: 100 })
    const rows = res.rows || res.data?.rows || res.data || res || []
    const apiPackages = Array.isArray(rows) ? rows.filter(item => item.status === '0') : []
    // API 有数据用 API 数据，无数据用设计稿默认套餐
    packages.value = apiPackages.length > 0 ? apiPackages : DEFAULT_PLANS
  } catch (e) {
    // API 失败时展示设计稿默认套餐
    packages.value = DEFAULT_PLANS
  }
  if (selectedPkg.value >= packages.value.length) {
    selectedPkg.value = 0
  }
}

async function handleRecharge() {
  const pkg = packages.value[selectedPkg.value]
  if (!pkg) {
    ElMessage.warning('请先选择可购买套餐')
    return
  }
  submitting.value = true
  try {
    const purchaseRes = await purchasePackage({ packageId: pkg.id })
    const paymentRecordId = purchaseRes.paymentRecordId || purchaseRes.data?.paymentRecordId || purchaseRes.id
    if (paymentRecordId) {
      try {
        await queryPaymentStatus(paymentRecordId)
      } catch {
      }
    }
    await userStore.fetchPoints()
    ElMessage.success('套餐下单成功，请按实际支付结果完成充值')
  } catch (error) {
    ElMessage.error(error.message || '创建充值订单失败')
  } finally {
    submitting.value = false
  }
}

function handleRedeemCode() {
  ElMessage.info('兑换码功能待接入，当前可先通过套餐充值完成积分升级')
}

function goToPointsCenter() {
  router.push('/points-center')
}

function scrollToPricing() {
  const el = document.getElementById('pricing-section')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

onMounted(async () => {
  await userStore.fetchPoints()
  await fetchPackages()
  if (packages.value.length) {
    selectedPkg.value = recommendedIndex.value
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 4px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.points-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563ff 100%);
  border-radius: var(--gh-radius);
  box-shadow: var(--gh-shadow-card);
  margin-bottom: 28px;
  color: #fff;
}

.points-left {
  flex: 1;
}

.points-label {
  font-size: 13px;
  opacity: 0.8;
  margin-bottom: 6px;
}

.points-number {
  font-size: 48px;
  font-weight: 600;
  line-height: 1.1;
  margin-bottom: 12px;
  letter-spacing: -1px;
}

.points-sub-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.points-sub {
  font-size: 12px;
  opacity: 0.8;

  strong {
    font-weight: 600;
    opacity: 1;

    &.positive {
      color: #6ee7b7;
    }
  }
}

.points-divider {
  opacity: 0.3;
  font-size: 12px;
}

.points-right {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recharge-btn {
  min-width: 120px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}

.redeem-btn {
  min-width: 120px;
  height: 40px;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

/* ============ 我的套餐 ============ */
.my-plan-section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}

.section-tip {
  font-size: 12px;
  color: #9CA3AF;
}

.my-plan-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563ff 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(37, 99, 255, 0.15);
  color: #fff;
}

.plan-info-left {
  flex: 1;
  min-width: 0;
}

.plan-current-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  padding: 3px 12px;
  border-radius: 20px;
  margin-bottom: 10px;
}

.plan-current-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.vip-badge {
  background: linear-gradient(135deg, #F59E0B, #D97706);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.plan-current-desc {
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: 12px;
}

.plan-current-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  opacity: 0.9;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;

  strong {
    color: #fff;
    font-weight: 600;
  }
}

.meta-divider {
  opacity: 0.4;
}

.plan-info-right {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;

  .el-button {
    min-width: 120px;
  }
}

.plan-detail-btn {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.4);
  }
}

/* ============ 积分套餐（设计稿样式） ============ */
.pricing-section {
  margin-bottom: 36px;
  scroll-margin-top: 16px;
}

.top-label {
  text-align: center;
  margin-bottom: 16px;

  span {
    background: #D1FAE5;
    color: #059669;
    font-size: 14px;
    padding: 6px 20px;
    border-radius: 20px;
    font-weight: 500;
  }
}

.pricing-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #1F2937;
  line-height: 1.2;
}

.pricing-subtitle {
  text-align: center;
  font-size: 15px;
  color: #6B7280;
  margin-bottom: 28px;
}

.advantages {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 36px;
  flex-wrap: wrap;
}

.advantage {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6B7280;

  svg {
    width: 20px;
    height: 20px;
    color: #059669;
  }
}

/* 套餐网格 */
.plans-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.plan-card {
  background: #fff;
  border: 1px solid #E8EDF5;
  border-radius: 16px;
  padding: 28px 18px;
  text-align: center;
  position: relative;
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  &.featured {
    border-color: #059669;
    border-width: 2px;
    box-shadow: 0 8px 32px rgba(5, 150, 105, 0.12);
  }

  &.selected {
    border-color: var(--gh-primary);
    box-shadow: 0 0 0 1px var(--gh-primary);
  }

  &.disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }
}

.plan-badge {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 16px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.badge-green {
  background: #059669;
  color: #fff;
}

.hot-tag {
  position: absolute;
  top: 14px;
  right: 14px;
  background: #FEF3C7;
  color: #D97706;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.plan-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-green {
  background: #D1FAE5;
  color: #059669;
}

.icon-blue {
  background: #EFF6FF;
  color: #2563FF;
}

.icon-gold {
  background: #FEF3C7;
  color: #F59E0B;
}

.icon-purple {
  background: #F3E8FF;
  color: #8B5CF6;
}

.icon-amber {
  background: #FFFBEB;
  color: #D97706;
}

.plan-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
  color: #1F2937;
}

.plan-desc {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 14px;
  min-height: 18px;
}

.plan-price {
  font-size: 34px;
  font-weight: 800;
  margin-bottom: 4px;
  line-height: 1.1;
}

.plan-price .currency {
  font-size: 18px;
  vertical-align: top;
  margin-right: 2px;
}

.price-green {
  color: #059669;
}

.price-blue {
  color: #2563FF;
}

.price-purple {
  color: #8B5CF6;
}

.price-amber {
  color: #D97706;
}

.plan-points {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 14px;
}

.points-green {
  background: #D1FAE5;
  color: #059669;
}

.points-blue {
  background: #EFF6FF;
  color: #2563FF;
}

.points-purple {
  background: #F3E8FF;
  color: #8B5CF6;
}

.points-amber {
  background: #FEF3C7;
  color: #D97706;
}

/* 套餐按钮（设计稿配色） */
.plan-btn {
  padding: 12px 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-green-outline {
  background: #fff;
  color: #059669;
  border: 2px solid #059669;

  &:hover:not(:disabled) {
    background: #059669;
    color: #fff;
  }
}

.btn-green-fill {
  background: #059669;
  color: #fff;
  border: 2px solid #059669;

  &:hover:not(:disabled) {
    background: #047857;
  }
}

.btn-blue-outline {
  background: #fff;
  color: #2563FF;
  border: 2px solid #2563FF;

  &:hover:not(:disabled) {
    background: #2563FF;
    color: #fff;
  }
}

.btn-purple-outline {
  background: #fff;
  color: #8B5CF6;
  border: 2px solid #8B5CF6;

  &:hover:not(:disabled) {
    background: #8B5CF6;
    color: #fff;
  }
}

.btn-amber-outline {
  background: #fff;
  color: #D97706;
  border: 2px solid #D97706;

  &:hover:not(:disabled) {
    background: #D97706;
    color: #fff;
  }
}

.plan-gen-label {
  font-size: 11px;
  color: #9CA3AF;
  margin-bottom: 10px;
}

.plan-gen {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 16px;
}

.plan-gen-item {
  padding: 8px 4px;
  background: #F7F9FC;
  border-radius: 8px;

  .num {
    font-size: 14px;
    font-weight: 700;
    color: #059669;
  }

  .type {
    font-size: 10px;
    color: #6B7280;
    margin-top: 2px;
  }
}

.plan-features {
  text-align: left;
  margin-bottom: 16px;
  list-style: none;
  padding: 0;

  li {
    font-size: 12px;
    color: #6B7280;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: '✓';
      color: #059669;
      font-weight: 700;
      font-size: 13px;
      flex-shrink: 0;
    }

    &.purple::before {
      color: #8B5CF6;
    }
  }
}

.pkg-validity {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 8px;
}

/* 信息卡片 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #E8EDF5;
  display: flex;
  gap: 14px;
  align-items: flex-start;

  h4 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 6px;
    color: #1F2937;
  }

  p {
    font-size: 12px;
    color: #6B7280;
    line-height: 1.5;
    margin: 0;
  }

  a {
    font-size: 12px;
    color: #2563FF;
    cursor: pointer;
    display: inline-block;
    margin-top: 6px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.info-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #F7F9FC;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.footer-note {
  text-align: center;
  font-size: 12px;
  color: #9CA3AF;
}

/* ============ 响应式 ============ */
@media (max-width: 1200px) {
  .plans-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 960px) {
  .plans-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .advantages {
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .points-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .my-plan-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .plan-info-right {
    flex-direction: row;
    width: 100%;

    .el-button {
      flex: 1;
    }
  }

  .points-number {
    font-size: 40px;
  }

  .plans-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .pricing-title {
    font-size: 26px;
  }
}
</style>