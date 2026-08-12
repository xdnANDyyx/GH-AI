<template>
  <div class="landing">
    <!-- ===== 顶部导航栏 ===== -->
    <nav class="topnav" :class="{ 'mobile-open': mobileMenuOpen }">
      <div class="logo-area">
        <Logo size="md" />
        <span class="logo-sub">家具设计 · AI出图平台</span>
      </div>
      <div class="nav-links" :class="{ show: mobileMenuOpen }">
        <a href="#hero" :class="{ active: activeNav === 'hero' }" @click="scrollTo('hero')">首页</a>
        <a href="#features" :class="{ active: activeNav === 'features' }" @click="scrollTo('features')">功能</a>
        <a href="#pricing" :class="{ active: activeNav === 'pricing' }" @click="scrollTo('pricing')">价格</a>
        <a href="#help" :class="{ active: activeNav === 'help' }" @click="scrollTo('help')">帮助</a>
      </div>
      <div class="nav-right" :class="{ show: mobileMenuOpen }">
        <button class="btn btn-outline" @click="$router.push('/login')">登录</button>
        <button class="btn btn-primary" @click="$router.push('/login?tab=register')">免费试用</button>
      </div>
      <button class="hamburger" @click="mobileMenuOpen = !mobileMenuOpen" :aria-label="mobileMenuOpen ? '关闭菜单' : '打开菜单'">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </nav>

    <!-- ===== Hero 区域 ===== -->
    <section class="hero" id="hero">
      <div class="hero-left">
        <h1>家具电商<br><span class="blue">AI出图平台</span></h1>
        <p class="subtitle">AI一站式解决家具电商图片设计需求</p>
        <ul class="hero-features">
          <li>
            <span class="check"><el-icon><Check /></el-icon></span>
            高效生成白底图、场景图与精修图
          </li>
          <li>
            <span class="check"><el-icon><Check /></el-icon></span>
            提升转化率，降低成本
          </li>
        </ul>
        <div class="hero-buttons">
          <button class="btn btn-primary btn-lg" @click="$router.push('/login?tab=register')">立即免费试用</button>
          <a href="#features"><button class="btn btn-outline btn-lg">查看功能</button></a>
        </div>
        <div class="hero-tags">
          <span>白底图</span><span>场景图</span><span>精修</span><span>Banner</span><span>模特图</span><span>尺寸标记</span>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-img-left">
          <img src="/images/chair-white-bg.png" alt="椅子产品图" />
        </div>
        <div class="hero-arrow">
          <el-icon :size="22"><ArrowRight /></el-icon>
        </div>
        <div class="hero-img-right">
          <img src="/images/chair-scene-bg.png" alt="客厅场景图" />
        </div>
      </div>
    </section>

    <!-- ===== 核心功能区块 ===== -->
    <section class="section features-section" id="features">
      <h2 class="section-title">核心功能</h2>
      <p class="section-subtitle">AI驱动，满足家具电商全链路图像设计需求</p>
      <div class="feature-grid">
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><Picture /></el-icon></div>
          <h3>AI白底图</h3>
          <p>一键去除背景，纯净白底<br>提升商品展示效果</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><HomeFilled /></el-icon></div>
          <h3>白底生成场景图</h3>
          <p>多风格场景，一键生成<br>真实还原家居氛围</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><MagicStick /></el-icon></div>
          <h3>智能产品精修</h3>
          <p>AI智能优化，细节出众<br>质感更专业</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><UserFilled /></el-icon></div>
          <h3>AI模特</h3>
          <p>真实模特替换，展示穿搭<br>提升购买信心</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><Brush /></el-icon></div>
          <h3>主图与详情设计</h3>
          <p>主图、详情页一体化设计<br>提升品牌专业度</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><PictureRounded /></el-icon></div>
          <h3>Banner设计</h3>
          <p>多场景电商Banner<br>高效出图，助力营销</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><EditPen /></el-icon></div>
          <h3>尺寸标记</h3>
          <p>智能尺寸标注<br>规范清晰，减少售后</p>
        </div>
        <div class="feature-card" @click="$router.push('/login?tab=register')">
          <div class="feature-icon"><el-icon :size="24"><Odometer /></el-icon></div>
          <h3>批量处理</h3>
          <p>批量上传，一键生成<br>大幅提升工作效率</p>
        </div>
      </div>
    </section>

    <!-- ===== 功能流程区块 ===== -->
    <section class="section workflow">
      <h2 class="section-title">功能流程</h2>
      <p class="section-subtitle">简单5步，快速生成高质量图片</p>
      <div class="workflow-steps">
        <div class="workflow-step">
          <div class="step-num">01</div>
          <div class="step-icon"><el-icon :size="28"><Upload /></el-icon></div>
          <h4>上传产品图</h4>
          <p>支持 JPG / PNG<br>批量上传更高效</p>
        </div>
        <div class="workflow-step">
          <div class="step-num">02</div>
          <div class="step-icon"><el-icon :size="28"><Aim /></el-icon></div>
          <h4>选择功能/风格</h4>
          <p>选择所需功能<br>或场景风格</p>
        </div>
        <div class="workflow-step">
          <div class="step-num">03</div>
          <div class="step-icon"><el-icon :size="28"><Monitor /></el-icon></div>
          <h4>AI生成</h4>
          <p>AI智能生成高质量<br>图片</p>
        </div>
        <div class="workflow-step">
          <div class="step-num">04</div>
          <div class="step-icon"><el-icon :size="28"><Edit /></el-icon></div>
          <h4>继续修改</h4>
          <p>在线编辑微调<br>细节更完美</p>
        </div>
        <div class="workflow-step">
          <div class="step-num">05</div>
          <div class="step-icon"><el-icon :size="28"><Download /></el-icon></div>
          <h4>导出使用</h4>
          <p>一键导出高清图<br>用于电商平台</p>
        </div>
      </div>
    </section>

    <!-- ===== 场景展示区块 ===== -->
    <section class="section scene-section">
      <h2 class="section-title">场景展示</h2>
      <p class="section-subtitle">多种风格与应用，激发更多灵感</p>
      <div class="scene-grid">
        <div class="scene-card" @click="$router.push('/login?tab=register')">
          <div class="scene-card-img compare">
            <div class="scene-half">
              <img src="/images/chair-white-bg.png" alt="沙发白底图" />
            </div>
            <div class="scene-arrow-circle">
              <el-icon :size="16"><ArrowRight /></el-icon>
            </div>
            <div class="scene-half">
              <img src="/images/cream-livingroom.png" alt="沙发场景图" />
            </div>
          </div>
          <div class="scene-card-body">
            <h4>沙发白底图 & 场景图</h4>
            <p>一键生成多种客厅场景<br>真实还原家居氛围</p>
          </div>
        </div>
        <div class="scene-card" @click="$router.push('/login?tab=register')">
          <div class="scene-card-img single">
            <img src="/images/kitchen-dining.png" alt="餐桌椅场景图" />
          </div>
          <div class="scene-card-body">
            <h4>餐桌椅场景图</h4>
            <p>多风格餐厅场景<br>提升商品吸引力</p>
          </div>
        </div>
        <div class="scene-card" @click="$router.push('/login?tab=register')">
          <div class="scene-card-img single">
            <img src="/images/modern-bedroom.png" alt="床类场景图" />
          </div>
          <div class="scene-card-body">
            <h4>床类场景图</h4>
            <p>卧室多风格展示<br>增强购买决策</p>
          </div>
        </div>
        <div class="scene-card" @click="$router.push('/login?tab=register')">
          <div class="scene-card-img compare">
            <div class="scene-half">
              <img src="/images/nordic-office.png" alt="精修前" />
            </div>
            <div class="scene-arrow-circle">
              <el-icon :size="16"><ArrowRight /></el-icon>
            </div>
            <div class="scene-half">
              <img src="/images/outdoor-patio.png" alt="精修后" />
            </div>
          </div>
          <div class="scene-card-body">
            <h4>产品精修前后对比</h4>
            <p>AI精修，提升质感与细节<br>让产品更出众</p>
          </div>
        </div>
      </div>
      <div class="scene-more">
        <a href="#features">查看更多案例 →</a>
      </div>
    </section>

    <!-- ===== 价格方案区块 ===== -->
    <section class="section pricing" id="pricing">
      <h2 class="section-title">灵活定价，按需选择</h2>
      <p class="section-subtitle">透明价格，无隐藏费用</p>
      <div class="pricing-toggle">
        <button
          class="toggle-btn"
          :class="{ active: billingCycle === 'monthly' }"
          @click="billingCycle = 'monthly'"
        >按月付费</button>
        <button
          class="toggle-btn"
          :class="{ active: billingCycle === 'yearly' }"
          @click="billingCycle = 'yearly'"
        >按年付费 (享8折)</button>
      </div>
        <div class="pricing-grid">
          <div class="pricing-card" :class="{ selected: selectedPlan === 'free' }" @click="selectPlan('free')">
            <h3>免费版</h3>
            <p class="plan-desc">适合入门体验</p>
            <div class="price">¥0 <span class="unit">/永久免费</span></div>
            <ul>
              <li>20积分 / 月</li>
              <li>基础功能体验</li>
              <li>导出带水印</li>
            </ul>
            <button class="btn btn-outline" @click.stop="$router.push('/login?tab=register')">免费试用</button>
          </div>
          <div class="pricing-card" :class="{ selected: selectedPlan === 'basic' }" @click="selectPlan('basic')">
            <h3>基础版</h3>
            <p class="plan-desc">适合个人用户</p>
            <div class="price">
              <template v-if="billingCycle === 'monthly'">¥99 <span class="period">/ 月</span></template>
              <template v-else>¥950 <span class="period">/ 年</span><span class="save-tag">省¥238</span></template>
            </div>
            <ul>
              <li>500积分 / 月</li>
              <li>高清无水印导出</li>
              <li>基础功能全开放</li>
            </ul>
            <button class="btn btn-primary" @click.stop="$router.push('/login?tab=register')">立即购买</button>
          </div>
          <div class="pricing-card featured" :class="{ selected: selectedPlan === 'pro' }" @click="selectPlan('pro')">
            <div class="pricing-badge">推荐</div>
            <h3>专业版</h3>
            <p class="plan-desc">适合专业设计师</p>
            <div class="price">
              <template v-if="billingCycle === 'monthly'">¥299 <span class="period">/ 月</span></template>
              <template v-else>¥2,870 <span class="period">/ 年</span><span class="save-tag">省¥718</span></template>
            </div>
            <ul>
              <li>2000积分 / 月</li>
              <li>全部功能开放</li>
              <li>批量处理 & 尺寸标记</li>
              <li>优先客服支持</li>
            </ul>
            <button class="btn btn-primary" @click.stop="$router.push('/login?tab=register')">立即购买</button>
          </div>
          <div class="pricing-card" :class="{ selected: selectedPlan === 'enterprise' }" @click="selectPlan('enterprise')">
            <h3>企业版</h3>
            <p class="plan-desc">适合团队与企业</p>
            <div class="price">
              <template v-if="billingCycle === 'monthly'">¥999 <span class="period">/ 月起</span></template>
              <template v-else>¥9,590 <span class="period">/ 年起</span><span class="save-tag">省¥2,398</span></template>
            </div>
            <ul>
              <li>自定义积分包</li>
              <li>团队协作管理</li>
              <li>专属客户成功服务</li>
              <li>API 接口支持</li>
            </ul>
            <button class="btn btn-outline" @click.stop="scrollTo('footer')">联系我们</button>
          </div>
        </div>
      <p class="pricing-note">* 积分可用于生成图片，不同功能消耗不同积分。详细说明见价格页。</p>
    </section>

    <!-- ===== 帮助中心区块 ===== -->
    <section class="section help-section" id="help">
      <h2 class="section-title">帮助中心</h2>
      <p class="section-subtitle">学习使用，解决问题，获取最新动态</p>
      <div class="help-grid">
        <div class="help-card" @click="scrollTo('features')">
          <div class="icon"><el-icon :size="28"><VideoPlay /></el-icon></div>
          <h4>新手教程</h4>
          <p>快速上手，轻松出图<br>从入门到精通</p>
        </div>
        <div class="help-card" @click="scrollTo('features')">
          <div class="icon"><el-icon :size="28"><Document /></el-icon></div>
          <h4>功能指南</h4>
          <p>详解各项功能用法<br>提升设计效率</p>
        </div>
        <div class="help-card" @click="scrollTo('help')">
          <div class="icon"><el-icon :size="28"><QuestionFilled /></el-icon></div>
          <h4>常见问题</h4>
          <p>解答常见问题<br>解决使用疑惑</p>
        </div>
        <div class="help-card">
          <div class="icon"><el-icon :size="28"><UploadFilled /></el-icon></div>
          <h4>更新日志</h4>
          <p>查看最新功能更新<br>了解产品动态</p>
        </div>
      </div>
      <div class="help-more">
        <a href="#">查看帮助中心 →</a>
      </div>
    </section>

    <!-- ===== CTA 横幅 ===== -->
    <section class="cta-banner">
      <h2>开启高效AI出图之旅</h2>
      <p>让设计更简单，让电商视觉更出色</p>
      <div class="cta-buttons">
        <button class="btn btn-white" @click="$router.push('/login?tab=register')">立即免费试用</button>
        <button class="btn btn-ghost-w" @click="$router.push('/login')">查看功能</button>
      </div>
    </section>

    <!-- ===== 页脚 ===== -->
    <footer class="footer" id="footer">
      <div class="footer-grid">
        <div class="footer-col footer-brand">
          <Logo size="md" :show-studio="true" />
          <p>家具设计 · AI出图平台<br>AI驱动的一站式家具电商图片设计平台，<br>助力商家提升效率与转化。</p>
          <div class="footer-social">
            <span><el-icon :size="14"><Share /></el-icon></span>
            <span><el-icon :size="14"><Link /></el-icon></span>
          </div>
        </div>
        <div class="footer-col">
          <h4>产品</h4>
          <a href="javascript:void(0)" @click="scrollTo('features')">功能介绍</a>
          <a href="javascript:void(0)" @click="scrollTo('pricing')">价格方案</a>
          <a href="javascript:void(0)" @click="scrollTo('features')">场景案例</a>
          <a href="javascript:void(0)">更新日志</a>
        </div>
        <div class="footer-col">
          <h4>帮助</h4>
          <a href="javascript:void(0)" @click="scrollTo('features')">新手教程</a>
          <a href="javascript:void(0)" @click="scrollTo('features')">功能指南</a>
          <a href="javascript:void(0)" @click="scrollTo('help')">常见问题</a>
          <a href="javascript:void(0)" @click="scrollTo('help')">帮助中心</a>
        </div>
        <div class="footer-col">
          <h4>关于我们</h4>
          <a href="javascript:void(0)" @click="scrollTo('footer')">公司介绍</a>
          <a href="javascript:void(0)" @click="scrollTo('footer')">联系我们</a>
          <a href="javascript:void(0)">加入我们</a>
          <a href="javascript:void(0)">隐私政策</a>
        </div>
        <div class="footer-col footer-qr">
          <div class="qr-box">
            <el-icon :size="32"><PictureFilled /></el-icon>
          </div>
          <p>关注我们<br>获取最新资讯与优惠</p>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2025 光合AI Studio. 保留所有权利。</span>
        <span>服务条款 | 隐私政策 | 备案号：粤ICP备2025000000号-1</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  Check, ArrowRight, Picture, HomeFilled, MagicStick, UserFilled,
  Brush, PictureRounded, EditPen, Odometer, Upload, Aim, Monitor,
  Edit, Download, VideoPlay, Document, QuestionFilled, UploadFilled,
  PictureFilled, Share, Link
} from '@element-plus/icons-vue'
import Logo from '@/components/Logo.vue'

const billingCycle = ref('monthly')
const mobileMenuOpen = ref(false)
const activeNav = ref('hero')
const selectedPlan = ref('')

function selectPlan(plan) {
  selectedPlan.value = plan
}

const navSections = ['hero', 'features', 'pricing', 'help']

function scrollTo(id) {
  mobileMenuOpen.value = false
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function onScroll() {
  const scrollPos = document.querySelector('.landing')?.scrollTop || window.scrollY
  const offset = 100
  let current = 'hero'
  for (const id of navSections) {
    const el = document.getElementById(id)
    if (el) {
      const rect = el.getBoundingClientRect()
      // Use position relative to the scrolling container
      const container = document.querySelector('.landing')
      const containerTop = container ? container.getBoundingClientRect().top : 0
      const elemTop = rect.top - containerTop
      if (elemTop <= offset) {
        current = id
      }
    }
  }
  activeNav.value = current
}

onMounted(() => {
  const landing = document.querySelector('.landing')
  if (landing) {
    landing.addEventListener('scroll', onScroll, { passive: true })
  }
})

onUnmounted(() => {
  const landing = document.querySelector('.landing')
  if (landing) {
    landing.removeEventListener('scroll', onScroll)
  }
})
</script>

<style lang="scss" scoped>
.landing {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  overflow-x: hidden;
  font-family: 'PingFang SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: #F7F9FC;
  color: #1F2937;

  &::-webkit-scrollbar { width: 5px; }
  &::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }
}

/* ===== 按钮通用 ===== */
.btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
}
.btn-outline {
  background: #fff;
  border: 1px solid #E8EDF5;
  color: #1F2937;
  &:hover { border-color: #2563FF; color: #2563FF; }
}
.btn-primary {
  background: #2563FF;
  color: #fff;
  &:hover { background: #1D4ED8; }
}
.btn-lg {
  padding: 14px 32px;
  font-size: 16px;
}

/* ===== 顶部导航 ===== */
.topnav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #E8EDF5;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-sub {
  font-size: 12px;
  color: #6B7280;
  padding-left: 12px;
  border-left: 1px solid #E8EDF5;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  gap: 32px;
  a {
    text-decoration: none;
    font-size: 14px;
    color: #6B7280;
    transition: color 0.2s;
    &:hover, &.active { color: #2563FF; }
    &.active { font-weight: 600; }
  }
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== 汉堡菜单 ===== */
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
  span {
    display: block;
    width: 100%;
    height: 2px;
    background: #1F2937;
    border-radius: 2px;
    transition: all 0.3s;
  }
  .mobile-open & {
    span:nth-child(1) { transform: translateY(7px) rotate(45deg); }
    span:nth-child(2) { opacity: 0; }
    span:nth-child(3) { transform: translateY(-7px) rotate(-45deg); }
  }
}

/* 响应式导航 - 1024px 以下显示汉堡菜单 */
@media (max-width: 1024px) {
  .topnav { padding: 0 24px; }
  .logo-sub { display: none; }
  .hamburger { display: flex; }
  .nav-links, .nav-right {
    display: none;
    &.show {
      display: flex;
    }
  }
  .nav-links {
    flex-direction: column;
    position: absolute;
    top: 64px;
    left: 0;
    right: 0;
    background: #fff;
    padding: 16px 24px;
    gap: 12px;
    border-bottom: 1px solid #E8EDF5;
    box-shadow: 0 4px 12px rgba(0,0,0,0.06);
    a { font-size: 16px; padding: 8px 0; }
  }
  .nav-right.show {
    position: absolute;
    top: 244px;
    left: 0;
    right: 0;
    flex-direction: column;
    background: #fff;
    padding: 0 24px 16px;
    gap: 10px;
    border-bottom: 1px solid #E8EDF5;
    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
    .btn { width: 100%; }
  }
}

/* ===== Hero ===== */
.hero {
  display: flex;
  align-items: center;
  padding: 64px 48px 80px;
  background: linear-gradient(180deg, #fff 0%, #EEF2FF 100%);
  min-height: calc(100vh - 64px);
  margin-top: 64px;
  gap: 60px;
  max-width: 1280px;
  margin-left: auto;
  margin-right: auto;
}

.hero-left {
  flex: 1;
  max-width: 560px;
  h1 {
    font-size: 48px;
    font-weight: 800;
    line-height: 1.3;
    margin-bottom: 16px;
    color: #1F2937;
    .blue { color: #2563FF; }
  }
  .subtitle {
    font-size: 18px;
    color: #6B7280;
    margin-bottom: 24px;
  }
}

.hero-features {
  list-style: none;
  padding: 0;
  margin-bottom: 32px;
  li {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #1F2937;
    margin-bottom: 12px;
    .check {
      width: 20px;
      height: 20px;
      background: #22C55E;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 11px;
      flex-shrink: 0;
    }
  }
}

.hero-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  span {
    padding: 6px 16px;
    background: #fff;
    border-radius: 20px;
    font-size: 13px;
    color: #2563FF;
    font-weight: 500;
    box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  }
}

.hero-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.hero-img-left, .hero-img-right {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.hero-img-left {
  width: 260px;
  height: 300px;
}

.hero-img-right {
  width: 300px;
  height: 260px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f4f8 100%);
}

.hero-arrow {
  width: 48px;
  height: 48px;
  background: #2563FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

/* ===== 通用区块 ===== */
.section {
  padding: 80px 48px;
  max-width: 1280px;
  margin: 0 auto;
}
.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1F2937;
}
.section-subtitle {
  text-align: center;
  font-size: 16px;
  color: #6B7280;
  margin-bottom: 48px;
}

/* ===== 核心功能 ===== */
.features-section {
  background: transparent;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fff;
  cursor: pointer;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  border: 1px solid #E8EDF5;
  box-shadow: 0 8px 24px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  .feature-icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 16px;
    background: #EEF2FF;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563FF;
  }
  h3 { font-size: 16px; font-weight: 600; margin-bottom: 8px; color: #1F2937; }
  p { font-size: 13px; color: #6B7280; line-height: 1.6; }
}

/* ===== 工作流程 ===== */
.workflow {
  background: #fff;
  border-radius: 16px;
  padding: 64px 48px;
  margin-top: 0;
  margin-bottom: 0;
  max-width: 1280px;
}

.workflow-steps {
  display: flex;
  align-items: flex-start;
  gap: 0;
  position: relative;
}

.workflow-step {
  flex: 1;
  text-align: center;
  position: relative;
}

.step-num {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #2563FF;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.step-icon {
  width: 64px;
  height: 64px;
  background: #EEF2FF;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  color: #2563FF;
}

.workflow-step h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #1F2937;
}

.workflow-step p {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.5;
}

/* ===== 场景展示 ===== */
.scene-section {
  background: transparent;
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.scene-card {
  background: #fff;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #E8EDF5;
  box-shadow: 0 8px 24px rgba(0,0,0,0.04);
}

.scene-card-img {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #f9fafb;
  padding: 12px;

  &.compare {
    gap: 0;
    padding: 12px 8px;
  }

  &.single img {
    width: 90%;
    height: 85%;
    object-fit: cover;
    border-radius: 8px;
  }
}

.scene-half {
  flex: 1;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.scene-arrow-circle {
  width: 32px;
  height: 32px;
  background: #2563FF;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  flex-shrink: 0;
  margin: 0 -6px;
}

.scene-card-body {
  padding: 16px;
  h4 { font-size: 15px; font-weight: 600; margin-bottom: 8px; color: #1F2937; }
  p { font-size: 13px; color: #6B7280; line-height: 1.5; }
}

.scene-more {
  text-align: center;
  margin-top: 32px;
  a {
    color: #2563FF;
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}

/* ===== 价格 ===== */
.pricing {
  background: #fff;
  border-radius: 16px;
  padding: 64px 48px;
  margin-top: 0;
  margin-bottom: 0;
  max-width: 1280px;
}

.pricing-toggle {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 40px;
}

.toggle-btn {
  padding: 10px 28px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid #E8EDF5;
  background: #fff;
  color: #6B7280;
  transition: all 0.2s;
  font-family: inherit;
  &:first-child { border-radius: 10px 0 0 10px; }
  &:last-child { border-radius: 0 10px 10px 0; }
  &.active {
    background: #2563FF;
    color: #fff;
    border-color: #2563FF;
  }
}

.pricing-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.pricing-card {
  background: #fff;
  border: 2px solid #E8EDF5;
  border-radius: 16px;
  padding: 32px 24px;
  position: relative;
  text-align: center;
  transition: all 0.2s;
  cursor: pointer;
  &:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
  &.featured {
    border-color: #E8EDF5;
    box-shadow: 0 8px 32px rgba(37,99,255,0.12);
  }
  &.selected {
    border-color: #2563FF;
    box-shadow: 0 0 0 3px rgba(37,99,255,0.15);
    transform: scale(1.03);
  }
  &.featured.selected {
    border-color: #2563FF;
    box-shadow: 0 0 0 3px rgba(37,99,255,0.15), 0 8px 32px rgba(37,99,255,0.12);
    transform: scale(1.03);
  }
}

.pricing-badge {
  position: absolute;
  top: -12px;
  right: 20px;
  background: #2563FF;
  color: #fff;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 6px;
}

.pricing-card h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
  color: #1F2937;
}

.plan-desc {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 16px;
}

.price {
  font-size: 36px;
  font-weight: 800;
  color: #1F2937;
  margin-bottom: 16px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px;
  .unit {
    font-size: 14px;
    font-weight: 400;
    color: #6B7280;
  }
  .period {
    font-size: 14px;
    color: #6B7280;
  }
  .save-tag {
    display: inline-block;
    font-size: 11px;
    font-weight: 600;
    color: #22C55E;
    background: #F0FDF4;
    padding: 2px 8px;
    border-radius: 4px;
    margin-left: 4px;
  }
}

.pricing-card ul {
  list-style: none;
  padding: 0;
  margin: 0 0 20px;
  text-align: left;
  li {
    font-size: 13px;
    color: #6B7280;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 8px;
    &::before {
      content: '✓';
      color: #22C55E;
      font-weight: 700;
    }
  }
}

.pricing-card .btn {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  border-radius: 10px;
}

.pricing-note {
  text-align: center;
  font-size: 12px;
  color: #6B7280;
  margin-top: 20px;
}

/* ===== 帮助中心 ===== */
.help-section {
  background: transparent;
}

.help-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.help-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  border: 1px solid #E8EDF5;
  box-shadow: 0 8px 24px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
  cursor: pointer;
  &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
  .icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 16px;
    background: #EEF2FF;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563FF;
  }
  h4 { font-size: 15px; font-weight: 600; margin-bottom: 8px; color: #1F2937; }
  p { font-size: 13px; color: #6B7280; line-height: 1.5; }
}

.help-more {
  text-align: center;
  margin-top: 32px;
  a {
    color: #2563FF;
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}

/* ===== CTA ===== */
.cta-banner {
  background: linear-gradient(135deg, #2563FF, #1D4ED8);
  padding: 48px;
  text-align: center;
  color: #fff;
  margin-top: 80px;
  h2 { font-size: 28px; font-weight: 700; margin-bottom: 12px; }
  p { font-size: 16px; opacity: 0.9; margin-bottom: 32px; }
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-white {
  background: #fff;
  color: #2563FF;
  border: none;
  font-weight: 600;
  &:hover { background: #f0f4ff; }
}

.btn-ghost-w {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.5);
  color: #fff;
  &:hover { background: rgba(255,255,255,0.1); }
}

/* ===== 页脚 ===== */
.footer {
  background: #1F2937;
  color: #fff;
  padding: 48px 48px 24px;
}

.footer-grid {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 40px;
  margin-bottom: 32px;
}

.footer-col {
  h4 { font-size: 15px; font-weight: 600; margin-bottom: 16px; }
  a {
    display: block;
    font-size: 13px;
    color: #9CA3AF;
    text-decoration: none;
    margin-bottom: 10px;
    &:hover { color: #fff; }
  }
}

.footer-brand {
  p {
    font-size: 13px;
    color: #9CA3AF;
    line-height: 1.6;
    margin: 12px 0 16px;
  }
}

.footer-social {
  display: flex;
  gap: 12px;
  span {
    width: 32px;
    height: 32px;
    background: rgba(255,255,255,0.1);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #9CA3AF;
  }
}

.footer-qr {
  text-align: center;
  .qr-box {
    width: 100px;
    height: 100px;
    background: #374151;
    border-radius: 8px;
    margin: 0 auto 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #9CA3AF;
  }
  p {
    font-size: 12px;
    color: #9CA3AF;
    line-height: 1.6;
  }
}

.footer-bottom {
  max-width: 1280px;
  margin: 0 auto;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 24px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6B7280;
}

/* ===== 响应式 ===== */

/* 平板 (≤1024px) */
@media (max-width: 1024px) {
  .section { padding: 60px 32px; }
  .hero {
    flex-direction: column;
    padding: 40px 32px 60px;
    text-align: center;
    gap: 32px;
    min-height: auto;
  }
  .hero-left { max-width: none; }
  .hero-left h1 { font-size: 36px; }
  .hero-right { flex-direction: column; }
  .hero-img-left { width: 200px; height: 240px; }
  .hero-img-right { width: 240px; height: 200px; }

  .feature-grid, .pricing-grid, .scene-grid, .help-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .footer-grid {
    grid-template-columns: 2fr 1fr 1fr;
  }

  .workflow-steps { flex-wrap: wrap; justify-content: center; gap: 24px; }
  .workflow-step { flex: 0 0 160px; }

  .cta-banner { padding: 40px 24px; }
  .footer { padding: 40px 24px 20px; }
}

/* 小平板 (≤768px) */
@media (max-width: 768px) {
  .section { padding: 48px 20px; }
  .section-title { font-size: 24px; }
  .section-subtitle { font-size: 14px; margin-bottom: 32px; }

  .hero {
    padding: 32px 20px 48px;
    gap: 24px;
    min-height: auto;
  }
  .hero-left h1 { font-size: 28px; }
  .hero-left .subtitle { font-size: 15px; }
  .hero-features li { font-size: 13px; justify-content: center; }
  .hero-buttons { justify-content: center; flex-wrap: wrap; }
  .hero-tags { justify-content: center; gap: 8px; }
  .hero-tags span { font-size: 12px; padding: 4px 12px; }
  .hero-img-left { width: 160px; height: 200px; }
  .hero-img-right { width: 200px; height: 160px; }

  .feature-card { padding: 24px 16px; }
  .feature-card .feature-icon { width: 48px; height: 48px; border-radius: 12px; }
  .feature-card h3 { font-size: 14px; }

  .workflow { padding: 48px 20px; }
  .workflow-steps { gap: 16px; }
  .workflow-step { flex: 0 0 130px; }
  .step-icon { width: 52px; height: 52px; }

  .pricing { padding: 48px 20px; }
  .pricing-card { padding: 24px 16px; }
  .pricing-card h3 { font-size: 16px; }
  .price { font-size: 28px; }

  .footer-grid { grid-template-columns: repeat(2, 1fr); gap: 24px; }
  .footer-bottom { flex-direction: column; text-align: center; gap: 8px; }
  .cta-banner h2 { font-size: 22px; }
  .cta-banner p { font-size: 14px; }
}

/* 手机 (≤480px) */
@media (max-width: 480px) {
  .topnav { height: 56px; padding: 0 16px; }
  .hero { margin-top: 56px; padding: 24px 16px 40px; gap: 20px; }
  .hero-left h1 { font-size: 24px; }
  .hero-left .subtitle { font-size: 14px; }
  .hero-img-left { width: 130px; height: 170px; }
  .hero-img-right { width: 170px; height: 130px; }
  .hero-buttons { flex-direction: column; gap: 10px; }
  .hero-buttons .btn { width: 100%; }

  .section { padding: 40px 16px; }
  .section-title { font-size: 20px; }
  .section-subtitle { font-size: 13px; margin-bottom: 24px; }

  .feature-grid, .pricing-grid, .scene-grid, .help-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .feature-card, .pricing-card, .help-card { padding: 24px 16px; }

  .workflow { padding: 40px 16px; }
  .workflow-steps { flex-direction: column; align-items: center; gap: 20px; }
  .workflow-step { flex: none; width: 100%; max-width: 260px; }

  .scene-grid { gap: 16px; }
  .scene-card-img { height: 160px; }
  .scene-card-body h4 { font-size: 14px; }

  .pricing { padding: 40px 16px; }
  .pricing-toggle { gap: 0; }
  .toggle-btn { padding: 8px 20px; font-size: 13px; }

  .cta-banner { padding: 32px 16px; }
  .cta-banner h2 { font-size: 20px; }
  .cta-buttons { flex-direction: column; gap: 10px; }
  .cta-buttons .btn { width: 100%; }

  .footer { padding: 32px 16px 16px; }
  .footer-grid { grid-template-columns: 1fr; gap: 20px; }
  .footer-brand p { font-size: 12px; }
  .footer-bottom { flex-direction: column; gap: 8px; text-align: center; font-size: 11px; }
}
</style>
