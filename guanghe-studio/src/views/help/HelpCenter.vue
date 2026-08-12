<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">帮助中心</h2>
        <p class="gh-page-subtitle">搜索常见问题，快速找到解决方案</p>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索常见问题、教程或关键词..."
        size="large"
        clearable
        class="help-search"
        :prefix-icon="Search"
      />
    </div>

    <!-- Quick Links -->
    <div class="quick-links">
      <div
        v-for="link in quickLinks"
        :key="link.label"
        class="quick-link-card gh-card"
        @click="scrollToSection(link.section)"
      >
        <div class="ql-icon" :style="{ background: link.bg }">
          <el-icon :size="22" :style="{ color: link.color }">
            <component :is="link.icon" />
          </el-icon>
        </div>
        <div class="ql-info">
          <div class="ql-title">{{ link.label }}</div>
          <div class="ql-desc">{{ link.desc }}</div>
        </div>
      </div>
    </div>

    <!-- FAQ Sections -->
    <div class="faq-section" ref="faqSection">
      <h3 class="section-title">常见问题</h3>
      <el-collapse v-model="activeFaq" class="faq-collapse gh-card">
        <el-collapse-item
          v-for="(item, idx) in filteredFaqList"
          :key="idx"
          :name="idx"
        >
          <template #title>
            <div class="faq-item-title">
              <el-icon class="faq-q-icon"><QuestionFilled /></el-icon>
              <span>{{ item.question }}</span>
              <el-tag size="small" :type="item.tagType" class="faq-tag">{{ item.category }}</el-tag>
            </div>
          </template>
          <div class="faq-answer">
            <p>{{ item.answer }}</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- Tutorial Section -->
    <div class="tutorial-section" ref="tutorialSection">
      <h3 class="section-title">使用教程</h3>
      <div class="tutorial-grid">
        <div
          v-for="(tutorial, idx) in tutorials"
          :key="idx"
          class="tutorial-card gh-card"
        >
          <div class="tutorial-thumb" :style="{ background: tutorial.thumbBg }">
            <el-icon :size="36" color="#fff"><VideoPlay /></el-icon>
            <div class="play-overlay">
              <el-icon :size="28" color="#fff"><CaretRight /></el-icon>
            </div>
            <div class="tutorial-duration">{{ tutorial.duration }}</div>
          </div>
          <div class="tutorial-body">
            <div class="tutorial-title">{{ tutorial.title }}</div>
            <div class="tutorial-desc">{{ tutorial.desc }}</div>
            <div class="tutorial-meta">
              <el-tag size="small" type="info">{{ tutorial.level }}</el-tag>
              <span class="tutorial-views">
                <el-icon><View /></el-icon>
                {{ tutorial.views }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Best Practices -->
    <div class="practices-section" ref="practicesSection">
      <h3 class="section-title">最佳实践</h3>
      <div class="practices-list">
        <div
          v-for="(practice, idx) in practices"
          :key="idx"
          class="practice-item gh-card"
        >
          <div class="practice-number">{{ String(idx + 1).padStart(2, '0') }}</div>
          <div class="practice-content">
            <div class="practice-title">{{ practice.title }}</div>
            <div class="practice-desc">{{ practice.desc }}</div>
          </div>
          <el-tag :type="practice.tagType" size="small">{{ practice.tag }}</el-tag>
        </div>
      </div>
    </div>

    <!-- Contact Support -->
    <div class="contact-support">
      <div class="support-card">
        <div class="support-left">
          <el-icon :size="32" color="#2563FF"><Service /></el-icon>
          <div>
            <div class="support-title">没有找到答案？</div>
            <div class="support-desc">我们的客服团队随时为您提供帮助</div>
          </div>
        </div>
        <div class="support-actions">
          <el-button type="primary" round @click="$router.push('/help/customer-support')">
            <el-icon><ChatDotRound /></el-icon>
            联系客服
          </el-button>
          <el-button round @click="$router.push('/help/feedback')">
            <el-icon><EditPen /></el-icon>
            提交反馈
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Search,
  QuestionFilled,
  VideoPlay,
  CaretRight,
  View,
  Service,
  ChatDotRound,
  EditPen,
  Picture,
  MagicStick,
  DataBoard,
  Coin
} from '@element-plus/icons-vue'

const searchQuery = ref('')
const activeFaq = ref([0])
const faqSection = ref(null)
const tutorialSection = ref(null)
const practicesSection = ref(null)

const quickLinks = [
  { label: '快速入门', desc: '5分钟上手指南', icon: 'MagicStick', bg: '#EFF6FF', color: '#2563FF', section: 'tutorial' },
  { label: '素材广场', desc: '浏览设计素材', icon: 'DataBoard', bg: '#ECFDF5', color: '#059669', section: 'faq' },
  { label: '积分说明', desc: '了解积分规则', icon: 'Coin', bg: '#FFF7ED', color: '#D97706', section: 'faq' },
  { label: '图片处理', desc: '白底图与精修', icon: 'Picture', bg: '#F3E8FF', color: '#7C3AED', section: 'practices' }
]

const faqList = [
  { question: '如何生成AI白底图？', answer: '在左侧导航栏选择"AI白底图"功能，上传产品原图后，系统会自动去除背景并生成干净的白底图。支持批量处理，一次最多可上传20张图片。生成结果可在"我的项目"中查看和下载。', category: '图片处理', tagType: '' },
  { question: '支持哪些图片格式上传？', answer: '目前支持 JPG、PNG、WebP 三种主流图片格式。建议上传图片分辨率不低于 1000x1000 像素，文件大小不超过 20MB，以获得最佳处理效果。', category: '基础功能', tagType: 'info' },
  { question: '积分如何获取？积分有效期是多久？', answer: '积分可通过充值套餐获取，也可通过上传素材被其他用户下载来赚取。充值积分长期有效，赠送积分有效期为一年，具体以充值页面说明为准。', category: '积分相关', tagType: 'warning' },
  { question: '如何选择合适的背景场景？', answer: '系统提供数百种预设场景，您可以根据产品类型（服装、家居、食品等）在场景分类中筛选。也可以使用"智能推荐"功能，系统会根据产品特征自动推荐最适合的场景。', category: '图片处理', tagType: '' },
  { question: '生成的图片版权归属如何？', answer: '通过光合AI Studio生成的所有图片，版权完全归属于创作者本人。您可以自由地将生成图片用于商业用途，包括电商平台、社交媒体、广告宣传等场景。', category: '基础功能', tagType: 'info' },
  { question: 'AI模特功能如何使用？', answer: '选择"AI模特"功能后，上传服装平铺图或产品图，从模特库中选择虚拟模特形象（包括性别、体型、姿态等），系统会自动生成穿着效果图。', category: '图片处理', tagType: '' },
  { question: '如何批量下载处理结果？', answer: '在"我的项目"页面中，选择对应项目，点击右上角的"批量下载"按钮。支持选择下载原图、处理后图片或对比图，格式支持 JPG 和 PNG。', category: '基础功能', tagType: 'info' },
  { question: '详情页/A+ 功能怎么排版？', answer: '选择"详情页/A+"功能后，上传产品图片和文案素材，从模板库中选择合适的排版模板。系统支持自定义模块顺序、字体颜色、间距等参数调整。', category: '设计生成', tagType: 'success' }
]

const filteredFaqList = computed(() => {
  if (!searchQuery.value.trim()) return faqList
  const q = searchQuery.value.toLowerCase()
  return faqList.filter(
    item => item.question.toLowerCase().includes(q) || item.answer.toLowerCase().includes(q) || item.category.toLowerCase().includes(q)
  )
})

const tutorials = [
  { title: '快速入门指南', desc: '5分钟了解光合AI Studio核心功能', duration: '05:30', level: '入门', views: '12.5K', thumbBg: 'linear-gradient(135deg, #2563FF, #60A5FA)' },
  { title: 'AI白底图全流程', desc: '从上传到导出的完整操作步骤', duration: '08:15', level: '入门', views: '8.3K', thumbBg: 'linear-gradient(135deg, #059669, #34D399)' },
  { title: '场景背景选择技巧', desc: '如何根据产品选择最佳背景', duration: '06:42', level: '进阶', views: '6.1K', thumbBg: 'linear-gradient(135deg, #D97706, #FBBF24)' },
  { title: '详情页高级排版', desc: '使用模板与自定义排版的技巧', duration: '10:20', level: '进阶', views: '4.7K', thumbBg: 'linear-gradient(135deg, #7C3AED, #A78BFA)' },
  { title: 'AI模特换装教程', desc: '虚拟模特生成与姿态调整', duration: '07:55', level: '进阶', views: '9.2K', thumbBg: 'linear-gradient(135deg, #DC2626, #F87171)' },
  { title: 'Banner营销图设计', desc: '高转化率Banner设计方法论', duration: '09:10', level: '高级', views: '3.8K', thumbBg: 'linear-gradient(135deg, #0891B2, #67E8F9)' }
]

const practices = [
  { title: '产品图片预处理规范', desc: '上传前对产品图进行裁剪、调光等预处理，可显著提高AI生成质量', tag: '推荐', tagType: 'success' },
  { title: '白底图拍摄技巧', desc: '使用纯色背景拍摄，保持产品居中，留出足够边距，避免阴影干扰', tag: '高效', tagType: '' },
  { title: '场景背景搭配原则', desc: '根据产品风格选择场景：简约产品配极简场景，生活方式产品配实景场景', tag: '设计', tagType: 'warning' },
  { title: '批量处理效率优化', desc: '使用批量上传+统一参数设置，可将处理效率提升3倍以上', tag: '效率', tagType: 'info' },
  { title: '素材上传与分享', desc: '高质量的原创素材可获得更多下载和积分回报，注意标注准确的关键词', tag: '创作', tagType: 'danger' }
]

function scrollToSection(section) {
  const sectionMap = {
    faq: faqSection.value,
    tutorial: tutorialSection.value,
    practices: practicesSection.value
  }
  sectionMap[section]?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<style lang="scss" scoped>
.page-container {
  padding: 4px;
  max-width: 960px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* Search */
.search-section {
  margin-bottom: 24px;
}

.help-search {
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius);
    box-shadow: 0 2px 8px rgba(37, 99, 255, 0.08);
    padding: 4px 16px;

    &:hover,
    &.is-focus {
      box-shadow: 0 2px 12px rgba(37, 99, 255, 0.15);
    }
  }
}

/* Quick Links */
.quick-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.quick-link-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.ql-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ql-info {
  min-width: 0;
}

.ql-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 2px;
}

.ql-desc {
  font-size: 12px;
  color: var(--gh-text-secondary);
}

/* Section Title */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 16px;
}

/* FAQ */
.faq-section {
  margin-bottom: 32px;
}

.faq-collapse {
  padding: 0;
  overflow: hidden;
}

:deep(.el-collapse-item__header) {
  padding: 14px 20px;
  height: auto;
  min-height: 52px;
  line-height: 1.5;
}

:deep(.el-collapse-item__wrap) {
  padding: 0 20px;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 16px;
}

.faq-item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--gh-text-primary);
}

.faq-q-icon {
  color: #2563FF;
  flex-shrink: 0;
}

.faq-tag {
  margin-left: auto;
  flex-shrink: 0;
}

.faq-answer {
  font-size: 13px;
  color: var(--gh-text-secondary);
  line-height: 1.7;
  padding-left: 28px;
}

/* Tutorials */
.tutorial-section {
  margin-bottom: 32px;
}

.tutorial-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.tutorial-card {
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

    .play-overlay {
      opacity: 1;
    }
  }
}

.tutorial-thumb {
  height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.tutorial-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.tutorial-body {
  padding: 14px 16px;
}

.tutorial-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 4px;
}

.tutorial-desc {
  font-size: 12px;
  color: var(--gh-text-secondary);
  margin-bottom: 10px;
  line-height: 1.5;
}

.tutorial-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tutorial-views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--gh-text-placeholder);
}

/* Best Practices */
.practices-section {
  margin-bottom: 32px;
}

.practices-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.practice-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
}

.practice-number {
  font-size: 24px;
  font-weight: 700;
  color: #E5E7EB;
  flex-shrink: 0;
  width: 40px;
  text-align: center;
}

.practice-content {
  flex: 1;
  min-width: 0;
}

.practice-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 4px;
}

.practice-desc {
  font-size: 12px;
  color: var(--gh-text-secondary);
  line-height: 1.5;
}

/* Contact Support */
.contact-support {
  margin-bottom: 24px;
}

.support-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #EFF6FF, #F0F9FF);
  border-radius: var(--gh-radius);
  border: 1px solid #DBEAFE;
}

.support-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.support-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 2px;
}

.support-desc {
  font-size: 13px;
  color: var(--gh-text-secondary);
}

.support-actions {
  display: flex;
  gap: 10px;
}

/* Responsive */
@media (max-width: 768px) {
  .quick-links {
    grid-template-columns: repeat(2, 1fr);
  }

  .tutorial-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .support-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .support-left {
    flex-direction: column;
  }
}
</style>
