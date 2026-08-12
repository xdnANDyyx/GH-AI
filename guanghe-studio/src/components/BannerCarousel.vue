<template>
  <div class="banner-carousel" v-if="banners.length > 0">
    <button
      v-if="needCarousel"
      class="carousel-arrow arrow-left"
      @click="prevPage"
      @mouseenter="stopAutoPlay"
      @mouseleave="startAutoPlay"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <div class="banner-cards-wrap">
      <div
        class="banner-cards"
        :class="{ 'has-transition': needCarousel }"
        :style="cardsStyle"
      >
        <div
          v-for="(item, idx) in banners"
          :key="item.id"
          class="banner-card"
          :class="getBannerTheme(idx)"
          @click="handleBannerClick(item)"
        >
          <div class="banner-content">
            <h3 class="banner-title">{{ item.title }}</h3>
            <p class="banner-desc">{{ item.description || item.linkUrl || '点击了解更多' }}</p>
            <button class="banner-btn" :class="getBtnTheme(idx)">{{ getBtnText(idx) }}</button>
          </div>
          <div class="banner-image" v-if="item.imageUrl">
            <img :src="getImageUrl(item.imageUrl)" :alt="item.title" />
          </div>
        </div>
      </div>
    </div>

    <button
      v-if="needCarousel"
      class="carousel-arrow arrow-right"
      @click="nextPage"
      @mouseenter="stopAutoPlay"
      @mouseleave="startAutoPlay"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <div v-if="needCarousel && totalPages > 1" class="carousel-dots">
      <span
        v-for="i in totalPages"
        :key="i"
        :class="{ active: currentIndex === i - 1 }"
        @click="goToPage(i - 1)"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { listPublicBanners, recordBannerClick } from '@/api/customer'
import { getImageUrl } from '@/utils/image'

const PAGE_SIZE = 3
const AUTO_PLAY_INTERVAL = 4000

const props = defineProps({
  position: {
    type: String,
    default: '0'
  }
})

const defaultBanners = [
  { id: 1, title: '光合AI Studio 2.0 上线', description: '全新界面，更高效的设计体验', imageUrl: '/banner/banner_1.jpg', linkUrl: '' },
  { id: 2, title: 'AI模特功能升级', description: '更自然的光影和细节表现', imageUrl: '/banner/banner_2.jpg', linkUrl: '' },
  { id: 3, title: '邀请好友得积分', description: '双方均可获得500积分', imageUrl: '/banner/banner_3.jpg', linkUrl: '' }
]

const banners = ref(defaultBanners)
const currentIndex = ref(0)

const needCarousel = computed(() => banners.value.length > PAGE_SIZE)
const totalPages = computed(() => Math.ceil(banners.value.length / PAGE_SIZE))

const cardsStyle = computed(() => {
  if (!needCarousel.value) return {}
  return {
    transform: `translateX(-${currentIndex.value * 100}%)`
  }
})

let autoPlayTimer = null

function startAutoPlay() {
  stopAutoPlay()
  if (needCarousel.value && totalPages.value > 1) {
    autoPlayTimer = setInterval(() => {
      currentIndex.value = (currentIndex.value + 1) % totalPages.value
    }, AUTO_PLAY_INTERVAL)
  }
}

function stopAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

function prevPage() {
  currentIndex.value = (currentIndex.value - 1 + totalPages.value) % totalPages.value
}

function nextPage() {
  currentIndex.value = (currentIndex.value + 1) % totalPages.value
}

function goToPage(idx) {
  currentIndex.value = idx
}

async function fetchBanners() {
  try {
    const res = await listPublicBanners({
      pageNum: 1,
      pageSize: 50,
      position: props.position
    })
    const list = (res.rows || [])
      .filter(item => item.status === '0' || item.status === '2')
      .sort((a, b) => (b.sort || 0) - (a.sort || 0))
    banners.value = list.length > 0 ? list : defaultBanners
    currentIndex.value = 0
    startAutoPlay()
  } catch (error) {
    console.error('获取 Banner 列表失败，使用默认 Banner:', error)
    banners.value = defaultBanners
  }
}

const themes = ['theme-blue', 'theme-green', 'theme-orange']
function getBannerTheme(idx) {
  return themes[idx % themes.length]
}

const btnThemes = ['btn-blue', 'btn-green', 'btn-orange']
function getBtnTheme(idx) {
  return btnThemes[idx % btnThemes.length]
}

const btnTexts = ['查看详情', '立即体验', '立即邀请']
function getBtnText(idx) {
  return btnTexts[idx % btnTexts.length]
}

async function handleBannerClick(item) {
  try {
    await recordBannerClick(item.id)
  } catch (error) {
    console.error('记录 Banner 点击失败:', error)
  }
  if (item.linkUrl) {
    if (item.linkUrl.startsWith('http://') || item.linkUrl.startsWith('https://')) {
      window.open(item.linkUrl, '_blank')
    } else {
      window.location.href = item.linkUrl
    }
  }
}

onMounted(() => {
  fetchBanners()
})

onBeforeUnmount(() => {
  stopAutoPlay()
})

watch(() => props.position, () => {
  fetchBanners()
})
</script>

<style lang="scss" scoped>
.banner-carousel {
  margin-bottom: 16px;
  flex-shrink: 0;
  position: relative;
  background: #F7F9FC;
  padding: 16px 0 12px;
  border-radius: 8px;
}

.banner-cards-wrap {
  overflow: hidden;
  padding: 0 16px;
}

.banner-cards {
  display: flex;
  gap: 16px;

  &.has-transition {
    transition: transform 0.5s ease;
  }
}

.banner-card {
  flex: 0 0 calc((100% - 32px) / 3);
  min-width: 0;
  height: 140px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.banner-content {
  flex: 1;
  z-index: 1;
  min-width: 0;
}

.banner-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
  line-height: 1.3;
}

.banner-desc {
  font-size: 12px;
  margin: 0 0 12px;
  opacity: 0.85;
  line-height: 1.4;
}

.banner-btn {
  font-size: 12px;
  font-weight: 600;
  height: 30px;
  padding: 0 16px;
  border: none;
  border-radius: 15px;
  color: #fff;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.9;
  }
}

.banner-image {
  width: 160px;
  height: 100px;
  flex-shrink: 0;
  margin-left: 16px;
  border-radius: 8px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// 轮播箭头
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: background 0.2s, color 0.2s;

  &:hover {
    background: #fff;
    color: #2962FF;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.arrow-left {
  left: 8px;
}

.arrow-right {
  right: 8px;
}

// 轮播指示点
.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #D1D5DB;
    cursor: pointer;
    transition: background 0.2s, transform 0.2s;

    &.active {
      background: #2962FF;
      transform: scale(1.25);
    }
  }
}

// 主题色
.theme-blue {
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  .banner-title { color: #1E40AF; }
  .banner-desc { color: #3B82F6; }
}
.theme-green {
  background: linear-gradient(135deg, #F0FDF4, #DCFCE7);
  .banner-title { color: #166534; }
  .banner-desc { color: #22C55E; }
}
.theme-orange {
  background: linear-gradient(135deg, #FFF7ED, #FED7AA);
  .banner-title { color: #9A3412; }
  .banner-desc { color: #EA580C; }
}

// 按钮色
.btn-blue { background: #2563FF; }
.btn-green { background: #22C55E; }
.btn-orange { background: #F39C12; }

@media (max-width: 768px) {
  .banner-card {
    flex: 0 0 calc((100% - 16px) / 2);
    height: auto;
    min-height: 100px;
    padding: 12px 16px;
  }
  .banner-image {
    width: 100px;
    height: 70px;
  }
}
</style>
