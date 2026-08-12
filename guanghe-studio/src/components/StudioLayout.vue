<template>
  <div class="studio-layout">
    <header class="topbar">
      <div class="topbar-left">
        <button class="hamburger" type="button" @click="mobileSidebarOpen = !mobileSidebarOpen" :aria-label="mobileSidebarOpen ? '关闭菜单' : '打开菜单'">
          <span></span><span></span><span></span>
        </button>
        <Logo />
        <div class="page-copy" v-if="route.meta?.title">
          <div class="page-title">{{ route.meta.title }}</div>
          <div class="page-subtitle" v-if="route.meta.subtitle">{{ route.meta.subtitle }}</div>
        </div>
      </div>

      <div class="topbar-right">
        <button class="points-badge" type="button" @click="goTo('/points')">
          <el-icon><StarFilled /></el-icon>
          <span>{{ displayPoints }} 积分</span>
        </button>
        <button class="icon-btn" type="button" title="帮助中心" @click="goTo('/help/help-center')">
          <el-icon><QuestionFilled /></el-icon>
        </button>
        <button class="icon-btn has-dot" type="button" title="通知" @click="goTo('/help/message-center')">
          <el-icon><Bell /></el-icon>
          <i class="dot"></i>
        </button>
        <el-button type="primary" class="upgrade-btn" @click="goTo('/points')">
          <el-icon><ShoppingCart /></el-icon>
          升级套餐
        </el-button>
      </div>
    </header>

    <div class="studio-body">
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed, open: mobileSidebarOpen }">
        <el-scrollbar class="sidebar-scroll">
          <nav class="sidebar-nav">
            <div class="nav-group">
              <div class="nav-group-title">工作台</div>
              <router-link
                v-for="item in workMenus"
                :key="item.path"
                :to="item.path"
                class="nav-item"
                :class="{ active: route.path === item.path }"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
                <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
              </router-link>
            </div>

            <div class="nav-group">
              <div class="nav-group-title">资源管理</div>
              <router-link
                v-for="item in resourceMenus"
                :key="item.path"
                :to="item.path"
                class="nav-item"
                :class="{ active: route.path === item.path }"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </router-link>
            </div>

            <div class="nav-group">
              <div class="nav-group-title">素材管理</div>
              <router-link
                v-for="item in materialMenus"
                :key="item.path"
                :to="item.path"
                class="nav-item"
                :class="{ active: route.path === item.path }"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
                <span v-if="item.badge" class="nav-badge new">{{ item.badge }}</span>
              </router-link>
            </div>
          </nav>
        </el-scrollbar>

        <div class="sidebar-footer">
          <div v-if="showPointsCard" class="points-card">
            <div class="points-head">
              <span>我的积分</span>
              <button class="points-close" type="button" @click="showPointsCard = false">×</button>
            </div>
            <div class="points-value">{{ displayPoints }}</div>
            <div class="points-action">
              <span>上传素材 / 被下载可获得积分</span>
              <button type="button" class="points-link" @click="goTo('/points-center')">积分明细</button>
            </div>
          </div>

          <div class="account-panel">
            <button class="account-row" type="button" @click="toggleAccountMenu">
              <el-avatar :size="34" src="/images/logo-guanghe-ai.png" />
              <div class="account-copy">
                <div class="account-name">{{ displayName }}</div>
                <div class="account-meta"><span class="vip-tag">VIP</span><span>{{ membershipLabel }}</span></div>
              </div>
              <el-icon class="expand-icon" :class="{ expanded: showAccountMenu }"><ArrowDown /></el-icon>
            </button>
            <div v-if="showAccountMenu" class="account-menu">
              <button type="button" class="account-menu-item" @click="goTo('/projects')">我的项目</button>
              <button type="button" class="account-menu-item" @click="goTo('/points-center')">积分明细</button>
              <button type="button" class="account-menu-item" @click="handleUpgradeClick">我的套餐</button>
              <button type="button" class="account-menu-item" @click="goTo('/help/message-center')">消息中心</button>
              <button type="button" class="account-menu-item danger" @click="logout">退出登录</button>
            </div>
          </div>
        </div>
      </aside>

      <button class="sidebar-toggle" type="button" @click="sidebarCollapsed = !sidebarCollapsed" v-if="!isMobile">
        <svg viewBox="0 0 16 16" width="14" height="14" :style="{ transform: sidebarCollapsed ? 'rotate(180deg)' : 'none' }">
          <path d="M5 2 L11 8 L5 14" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div v-if="isMobile && mobileSidebarOpen" class="sidebar-overlay" @click="mobileSidebarOpen = false"></div>

      <main class="main-area">
        <BannerCarousel v-if="showBannerCarousel" :position="bannerPosition" />
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedWorkspaceViews">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import Logo from '@/components/Logo.vue'
import BannerCarousel from '@/components/BannerCarousel.vue'
import { useFeatureToggles } from '@/composables/useFeatureToggles'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showPointsCard = ref(true)
const showAccountMenu = ref(false)
const sidebarCollapsed = ref(false)
const isMobile = ref(false)
const mobileSidebarOpen = ref(false)
const displayName = computed(() => userStore.userInfo.nickname || '光合设计师')
const displayPoints = computed(() => userStore.userInfo.points ?? 0)
const membershipLabel = computed(() => userStore.userInfo.vipLevel > 0 ? '专业版' : '体验版')
const showBannerCarousel = computed(() => userStore.isLoggedIn)

const workspacePaths = ['/whiteBg', '/whiteToBg', '/refine', '/aiModel', '/mainImage', '/detailImg', '/banner', '/size', '/batchProcess']

const cachedWorkspaceViews = [
  'WhiteBg',
  'BackgroundView',
  'RetouchView',
  'AiModelView',
  'HeroImageView',
  'DetailImgView',
  'BannerView',
  'SizeMarkView',
  'BatchProcessView'
]
const pointsPaths = ['/points', '/pointsCenter', '/pointsHistory', '/recharge']
const bannerPosition = computed(() => {
  const path = route.path
  if (workspacePaths.some(p => path.startsWith(p))) return '1'
  if (pointsPaths.some(p => path.startsWith(p))) return '2'
  return '0'
})

// 功能开关 - 使用共享composable
const { featureToggleMap, loadFeatureToggles, isFeatureEnabled: _isFeatureEnabled } = useFeatureToggles()

// 开关 key → 菜单路径映射
const toggleToMenuPath = {
  white_bg: '/whiteBg',
  white_to_bg: '/whiteToBg',
  refine: '/refine',
  ai_model: '/aiModel',
  main_image: '/mainImage',
  detail_img: '/detailImg',
  size_mark: '/size',
  banner: '/banner',
  batch_process: '/batchProcess'
}

const allWorkMenus = [
  { path: '/whiteBg', title: 'AI 白底图', icon: 'House', toggleKey: 'white_bg' },
  { path: '/whiteToBg', title: '白底生成背景', icon: 'PictureFilled', toggleKey: 'white_to_bg' },
  { path: '/refine', title: '产品精修', icon: 'MagicStick', toggleKey: 'refine' },
  { path: '/aiModel', title: 'AI 模特', icon: 'User', toggleKey: 'ai_model' },
  { path: '/mainImage', title: '主图设计', icon: 'DataBoard', toggleKey: 'main_image' },
  { path: '/detailImg', title: '详情图 / A+', icon: 'Document', toggleKey: 'detail_img' },
  { path: '/size', title: '尺寸标记', icon: 'ScaleToOriginal', toggleKey: 'size_mark' },
  { path: '/banner', title: 'Banner 设计', icon: 'Postcard', toggleKey: 'banner' },
  { path: '/batchProcess', title: '批量生成', icon: 'Files', toggleKey: 'batch_process' }
]

function isFeatureEnabled(toggleKey) {
  // 管理员始终可见所有功能
  if (userStore.isAdmin) return true
  return _isFeatureEnabled(toggleKey)
}

const workMenus = computed(() =>
  allWorkMenus.filter(m => isFeatureEnabled(m.toggleKey))
)

const resourceMenus = [
  { path: '/history', title: '历史记录', icon: 'Clock' },
  { path: '/points-center', title: '积分中心', icon: 'Coin' }
]

const materialMenus = [
  { path: '/materialPlaza', title: '素材广场', icon: 'Grid', badge: 'NEW' },
  { path: '/myMaterials', title: '我的素材', icon: 'Files' },
  { path: '/assetManagement', title: '我的收藏', icon: 'Folder' }
]

onMounted(() => {
  loadFeatureToggles()
})

function closeAccountMenu() {
  showAccountMenu.value = false
}

function goTo(path) {
  closeAccountMenu()
  router.push(path)
}

function toggleAccountMenu() {
  showAccountMenu.value = !showAccountMenu.value
}

function handleUpgradeClick() {
  closeAccountMenu()
  router.push('/points')
}

async function logout() {
  closeAccountMenu()
  await userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.studio-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--gh-bg-page);
}

.topbar {
  height: var(--gh-topbar-height);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(229, 234, 244, 0.72);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px 0 14px;
  flex-shrink: 0;
  z-index: 10;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-copy {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding-left: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  line-height: 1;
}

.page-subtitle {
  font-size: 14px;
  color: var(--gh-text-secondary);
  white-space: nowrap;
}

.points-badge,
.icon-btn {
  height: 32px;
  border: 1px solid rgba(229, 234, 244, 0.8);
  background: #fff;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 12px;
  color: #111827;
  cursor: pointer;
}

.points-badge {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.points-badge .el-icon {
  color: #f59e0b;
}

.icon-btn {
  width: 32px;
  padding: 0;
  position: relative;
  color: #334155;
}

.icon-btn.has-dot .dot {
  position: absolute;
  right: 3px;
  top: 3px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 0 2px #fff;
}

.upgrade-btn {
  height: 32px;
  border-radius: 10px;
  font-size: 14px;
  padding: 0 14px;
}

.studio-body {
  flex: 1;
  min-height: 0;
  display: flex;
}

.sidebar {
  width: var(--gh-sidebar-width);
  background: #fff;
  border-right: 1px solid rgba(229, 234, 244, 0.72);
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sidebar-scroll {
  flex: 1;
}

.sidebar-nav {
  padding: 12px 8px 10px 10px;
}

.nav-group {
  margin-bottom: 12px;
}

.nav-group-title {
  font-size: 12px;
  color: #98a2b3;
  padding: 12px 12px 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 36px;
  margin: 2px 0;
  padding: 0 12px;
  border-radius: 10px;
  text-decoration: none;
  color: #334155;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  background: #f4f7fb;
}

.nav-item.active {
  background: linear-gradient(90deg, rgba(37, 99, 255, 0.12), rgba(37, 99, 255, 0.08));
  color: #2563ff;
}

.nav-item .el-icon {
  font-size: 18px;
}

.nav-badge {
  margin-left: auto;
  font-size: 10px;
  height: 18px;
  padding: 0 6px;
  line-height: 18px;
  border-radius: 9px;
  background: #ef4444;
  color: #fff;
}

.nav-badge.new {
  background: #2563ff;
}

.sidebar-footer {
  padding: 12px 10px 14px;
}

.points-card {
  border: 1px solid #edf1f7;
  border-radius: 14px;
  padding: 12px;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.points-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #2563ff;
  font-weight: 600;
}

.points-close {
  border: 0;
  background: transparent;
  color: #cbd5e1;
  font-size: 18px;
  cursor: pointer;
}

.points-value {
  margin-top: 6px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.points-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  gap: 10px;
}

.points-link {
  border: 0;
  background: transparent;
  color: #2563ff;
  text-decoration: none;
  white-space: nowrap;
  cursor: pointer;
  padding: 0;
}

.account-panel {
  position: relative;
  margin-top: 14px;
}

.account-row {
  width: 100%;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 4px;
  cursor: pointer;
  text-align: left;
}

.account-copy {
  min-width: 0;
  flex: 1;
}

.account-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.account-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.vip-tag {
  padding: 0 6px;
  height: 16px;
  border-radius: 8px;
  line-height: 16px;
  font-size: 10px;
  background: #f59e0b;
  color: #fff;
}

.expand-icon {
  color: #94a3b8;
  transition: transform 0.2s ease;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.account-menu {
  margin-top: 8px;
  padding: 6px;
  border-radius: 12px;
  border: 1px solid rgba(229, 234, 244, 0.9);
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
}

.account-menu-item {
  width: 100%;
  border: 0;
  background: transparent;
  border-radius: 8px;
  height: 34px;
  padding: 0 10px;
  text-align: left;
  color: #334155;
  cursor: pointer;
}

.account-menu-item:hover {
  background: #f4f7fb;
}

.account-menu-item.danger {
  color: #dc2626;
}

.main-area {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

/* sidebar toggle button */
.sidebar-toggle {
  border: 1px solid rgba(229, 234, 244, 0.8);
  background: #fff;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  width: 18px;
  padding: 0;
  flex-shrink: 0;
  transition: color 0.2s;
  &:hover { color: #2563ff; }
}

/* sidebar collapsed state */
.sidebar.collapsed {
  width: 0;
  border-right: none;
  overflow: hidden;
  min-width: 0;
  padding: 0;
}

/* hamburger menu for mobile */
.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  z-index: 101;
  flex-shrink: 0;
  span {
    display: block;
    width: 100%;
    height: 2px;
    background: #1F2937;
    border-radius: 2px;
    transition: all 0.3s;
  }
}

/* sidebar overlay for mobile */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  z-index: 99;
}

/* responsive */
@media (max-width: 1024px) {
  .topbar-left .page-copy .page-subtitle { display: none; }
  .page-title { font-size: 16px; }
  .points-badge span { display: none; }
  .upgrade-btn { padding: 0 10px; font-size: 12px; }
}

@media (max-width: 768px) {
  .hamburger { display: flex; }
  .sidebar {
    position: fixed;
    left: -260px;
    top: var(--gh-topbar-height);
    bottom: 0;
    z-index: 100;
    transition: left 0.3s ease;
    width: 260px;
    box-shadow: 4px 0 24px rgba(0,0,0,0.1);
  }
  .sidebar.open { left: 0; }
  .sidebar.collapsed { left: -260px; }
  .sidebar-toggle { display: none; }
  .page-copy { display: none; }
  .icon-btn { display: none; }
  .main-area { margin-left: 0; }
}
</style>
