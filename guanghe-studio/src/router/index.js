import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'
import { useFeatureToggles } from '@/composables/useFeatureToggles'

// ========== 页面 meta 信息 ==========
const studioMeta = {
  '/index':        { title: '首页', subtitle: '欢迎使用光合AI Studio' },
  '/whiteBg':      { title: 'AI白底图', subtitle: '智能识别商品主体，一键生成干净白底图' },
  '/whiteToBg':    { title: '白底图生成背景', subtitle: '智能识别商品主体，一键生成高质量场景背景' },
  '/refine':       { title: '产品精修', subtitle: 'AI智能精修，提升商品质感，让每一个细节都更美观' },
  '/aiModel':      { title: 'AI模特', subtitle: '为家居产品匹配适合的模特场景，提升商品吸引力' },
  '/mainImage':    { title: '主图', subtitle: '高转化主图设计，提升点击率和销量' },
  '/detailImg':    { title: '详情图 / A+ 设计', subtitle: '专业电商视觉设计，提升转化率与品牌形象' },
  '/banner':       { title: 'Banner设计', subtitle: '专业的 Banner 设计工具，快速生成高转化电商 Banner' },
  '/size':         { title: '尺寸标记', subtitle: '自动标注商品尺寸信息' },
  '/batchProcess': { title: '批量生成', subtitle: '上传产品图与参考图，批量生成多套主图 / 详情图设计方案' },
  '/materialPlaza':     { title: '素材广场', subtitle: '探索精选素材，AI智能识别，精准匹配您的需求' },
  '/myMaterials':       { title: '我的素材', subtitle: '管理我上传的素材，查看审核状态与积分收益' },
  '/assetManagement':   { title: '我的收藏', subtitle: '管理您收藏的素材和作品' },
  '/projects':     { title: '我的项目', subtitle: '所有项目与生成记录' },
  '/history':      { title: '历史记录', subtitle: '查看您生成的所有内容，支持预览、编辑、下载和再次使用' },
  '/recycleBin':   { title: '回收站', subtitle: '已删除的文件' },
  '/points':       { title: '我的积分', subtitle: '积分明细与兑换' },
  '/points-center': { title: '积分中心', subtitle: '记录您的积分收支情况，清晰透明' }
}

const adminMeta = {
  '/admin':        { title: '数据看板' },
  '/admin/users':  { title: '用户管理' },
  '/admin/orders': { title: '订单中心' },
  '/admin/points': { title: '积分管理' },
  '/admin/content':{ title: '内容管理' },
  '/admin/banner': { title: 'Banner管理' },
  '/admin/material': { title: '官方素材管理' },
  '/admin/ai-model': { title: 'AI模特管理' },
  '/admin/creation-config': { title: '创作功能管理' },
  '/admin/system': { title: '系统设置' }
}

// ========== 路由定义 ==========
const routes = [
  // ===== Landing 页（未登录首页）=====
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/Landing.vue'),
    meta: {}
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guestOnly: true }
  },
  // ===== 管理后台 =====
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { ...adminMeta['/admin'], requiresAdmin: true } },
      { path: 'users',     name: 'AdminUsers',     component: () => import('@/views/admin/AdminUsers.vue'),     meta: { ...adminMeta['/admin/users'], requiresAdmin: true } },
      { path: 'orders',    name: 'AdminOrders',    component: () => import('@/views/admin/AdminOrders.vue'),    meta: { ...adminMeta['/admin/orders'], requiresAdmin: true } },
      { path: 'points',    name: 'AdminPoints',    component: () => import('@/views/admin/AdminPoints.vue'),    meta: { ...adminMeta['/admin/points'], requiresAdmin: true } },
      { path: 'content',   name: 'AdminContent',   component: () => import('@/views/admin/AdminContent.vue'),   meta: { ...adminMeta['/admin/content'], requiresAdmin: true } },
      { path: 'banner',    name: 'AdminBanner',    component: () => import('@/views/admin/AdminBanner.vue'),    meta: { ...adminMeta['/admin/banner'], requiresAdmin: true } },
      { path: 'material',  name: 'AdminMaterial',  component: () => import('@/views/admin/AdminMaterial.vue'),  meta: { ...adminMeta['/admin/material'], requiresAdmin: true } },
      { path: 'ai-model',  name: 'AdminAiModel',   component: () => import('@/views/admin/AdminAiModel.vue'),   meta: { ...adminMeta['/admin/ai-model'], requiresAdmin: true } },
      { path: 'creation-config', name: 'AdminCreationConfig', component: () => import('@/views/admin/AdminCreationConfig.vue'), meta: { ...adminMeta['/admin/creation-config'], requiresAdmin: true } },
      { path: 'system',    name: 'AdminSystem',    component: () => import('@/views/admin/AdminSystem.vue'),    meta: { ...adminMeta['/admin/system'], requiresAdmin: true } }
    ]
  },
  // ===== 用户工作台 =====
  {
    path: '/',
    component: () => import('@/components/StudioLayout.vue'),
    redirect: '/whiteBg',
    meta: { requiresAuth: true },
    children: [
      // 工作台 — 使用 workspace 组件（有真实交互）
      { path: 'whiteBg',    name: 'WhiteBg',    component: () => import('@/views/workspace/WhiteBg.vue'),    meta: { ...studioMeta['/whiteBg'], sessionType: 'white_bg' } },
      { path: 'whiteToBg',  name: 'WhiteToBg',  component: () => import('@/views/workspace/Background.vue'), meta: { ...studioMeta['/whiteToBg'], sessionType: 'render' } },
      { path: 'refine',     name: 'Refine',     component: () => import('@/views/workspace/Retouch.vue'),    meta: { ...studioMeta['/refine'], sessionType: 'render' } },
      { path: 'aiModel',    name: 'AiModel',    component: () => import('@/views/workspace/AiModel.vue'),    meta: { ...studioMeta['/aiModel'], sessionType: 'render' } },
      { path: 'mainImage',  name: 'MainImage',  component: () => import('@/views/workspace/HeroImage.vue'),  meta: { ...studioMeta['/mainImage'], sessionType: 'main_image' } },
      { path: 'detailImg',  name: 'DetailImg',  component: () => import('@/views/workspace/DetailImg.vue'),  meta: { ...studioMeta['/detailImg'], sessionType: 'render' } },
      { path: 'banner',     name: 'Banner',     component: () => import('@/views/workspace/Banner.vue'),     meta: { ...studioMeta['/banner'], sessionType: 'render' } },
      { path: 'size',       name: 'Size',       component: () => import('@/views/workspace/SizeMark.vue'),   meta: { ...studioMeta['/size'], sessionType: 'render' } },
      { path: 'batchProcess', name: 'BatchProcess', component: () => import('@/views/workspace/BatchProcess.vue'), meta: { ...studioMeta['/batchProcess'], sessionType: 'render' } },

      // 资源管理 — 使用 StudioPage 占位
      { path: 'materialPlaza',   name: 'MaterialPlaza',   component: () => import('@/views/MaterialPlaza.vue'), meta: { ...studioMeta['/materialPlaza'] } },
      { path: 'myMaterials',     name: 'MyMaterials',     component: () => import('@/views/MyMaterials.vue'),   meta: { ...studioMeta['/myMaterials'] } },
      { path: 'assetManagement', name: 'AssetManagement', component: () => import('@/views/resources/MyFavorites.vue'), meta: { ...studioMeta['/assetManagement'] } },
      { path: 'projects',  name: 'Projects',  component: () => import('@/views/resources/Projects.vue'), meta: { ...studioMeta['/projects'] } },
      { path: 'history',   name: 'History',   component: () => import('@/views/resources/History.vue'), meta: { ...studioMeta['/history'] } },
      { path: 'recycleBin', name: 'RecycleBin', component: () => import('@/views/resources/RecycleBin.vue'), meta: { ...studioMeta['/recycleBin'] } },
      { path: 'points',    name: 'Points',    component: () => import('@/views/resources/Points.vue'), meta: { ...studioMeta['/points'] } },
      { path: 'points-center', name: 'PointsCenter', component: () => import('@/views/resources/PointsCenter.vue'), meta: { ...studioMeta['/points-center'] } }
    ]
  },
  // ===== 兼容旧路由重定向 =====
  { path: '/white-bg',      redirect: '/whiteBg' },
  { path: '/background',    redirect: '/whiteToBg' },
  { path: '/retouch',       redirect: '/refine' },
  { path: '/ai-model',      redirect: '/aiModel' },
  { path: '/hero-image',    redirect: '/mainImage' },
  { path: '/detail-page',   redirect: '/detailImg' },
  { path: '/size-mark',     redirect: '/size' },
  { path: '/batch-process', redirect: '/batchProcess' },
  { path: '/material-market', redirect: '/materialPlaza' },
  { path: '/my-materials',  redirect: '/assetManagement' },
  // 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', redirect: '/whiteBg' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ========== 路由守卫 ==========
let tokenCaptured = false
let initialSetupDone = false

router.beforeEach((to) => {
  const userStore = useUserStore()

  // 首次加载时：捕获 URL token + 后台获取用户信息（不阻塞路由）
  if (!tokenCaptured) {
    tokenCaptured = true
    const urlParams = new URLSearchParams(window.location.search)
    const tokenFromUrl = urlParams.get('token')
    if (tokenFromUrl) {
      userStore.loginWithToken(tokenFromUrl).catch(() => {})
      window.history.replaceState({}, '', window.location.pathname)
    }
  }

  // 登录成功后后台异步获取用户信息（仅一次，不阻塞路由）
  if (!initialSetupDone && userStore.isLoggedIn) {
    initialSetupDone = true
    if (!userStore.userInfo.userId) {
      userStore.fetchUserInfo().catch(() => {})
    }
    // 异步获取积分（不阻塞）
    userStore.fetchPoints().catch(() => {})
  }

  // 未登录 → 跳转登录页（检查所有匹配路由的 meta，包括父路由）
  if (to.matched.some(r => r.meta?.requiresAuth) && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 管理员路由权限
  if (to.matched.some(r => r.meta?.requiresAdmin) && !userStore.isAdmin) {
    return '/whiteBg'
  }

  // 功能开关拦截：非管理员访问已关闭的工作台功能时重定向
  if (!userStore.isAdmin && to.path) {
    const { isPathEnabled, loadFeatureToggles } = useFeatureToggles()
    // 确保开关数据已加载（不阻塞首次导航，loaded后同步检查）
    loadFeatureToggles()
    if (!isPathEnabled(to.path)) {
      return '/whiteBg'
    }
  }

  return true
})

export default router
