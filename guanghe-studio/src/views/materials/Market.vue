<template>
  <div class="market-page">
    <!-- Top Area -->
    <div class="market-top">
      <div class="search-row">
        <el-input
          v-model="searchText"
          placeholder="搜索素材关键词，例如：奶油风 客厅 沙发"
          clearable
          class="gh-search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="chip-row">
        <div class="chip">
          <span class="chip-label">产品分类</span>
          <span class="chip-val">沙发</span>
          <el-icon class="chip-arrow"><ArrowDown /></el-icon>
        </div>
        <div class="chip">
          <span class="chip-label">空间分类</span>
          <span class="chip-val">客厅</span>
          <el-icon class="chip-arrow"><ArrowDown /></el-icon>
        </div>
        <div class="chip">
          <span class="chip-label">风格标签</span>
          <span class="chip-val">奶油风</span>
          <el-icon class="chip-arrow"><ArrowDown /></el-icon>
        </div>
        <div class="chip">
          <span class="chip-label">平台适配</span>
          <span class="chip-val">Amazon</span>
          <el-icon class="chip-arrow"><ArrowDown /></el-icon>
        </div>
        <div class="chip ghost">
          <span class="chip-val">更多筛选</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
      </div>

      <div class="selected-row">
        <span class="selected-label">已选条件：</span>
        <el-tag
          v-for="s in selectedFilters"
          :key="s"
          closable
          size="small"
          effect="plain"
          class="sel-tag"
          @close="removeFilter(s)"
        >{{ s }}</el-tag>
        <span class="clear-link" @click="clearFilters">清空全部</span>
        <span class="spacer" />
        <div class="sort-block">
          <span class="sort-label">排序：</span>
          <el-select v-model="sortBy" size="small" class="gh-sort-select">
            <el-option label="推荐" value="recommend" />
            <el-option label="最新" value="latest" />
            <el-option label="最热" value="hot" />
            <el-option label="下载量" value="downloads" />
          </el-select>
          <div class="view-toggle">
            <div class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Body: grid + detail panel -->
    <div class="market-body">
      <!-- Center grid -->
      <main class="market-grid-wrap">
        <div class="market-grid">
          <div
            v-for="(m, idx) in materials"
            :key="idx"
            class="m-card"
            :class="{ selected: selectedId === m.id }"
            @click="selectedId = m.id"
          >
            <div class="m-thumb" :style="{ background: m.bg }">
              <div class="m-icon">{{ m.icon }}</div>
              <span class="m-badge" :class="m.badgeType">{{ m.badge }}</span>
              <div class="m-heart" :class="{ on: m.liked }" @click.stop="m.liked = !m.liked">
                <el-icon v-if="m.liked"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon>
              </div>
            </div>
            <div class="m-body">
              <div class="m-title">{{ m.title }}</div>
              <div class="m-creator">
                <span class="avatar" :style="{ background: m.avatarBg }">{{ m.avatarChar }}</span>
                <span class="creator-name">{{ m.creator }}</span>
                <el-icon class="verified"><CircleCheckFilled /></el-icon>
              </div>
              <div class="m-tags">
                <el-tag v-for="t in m.tags" :key="t" size="small" effect="plain" class="m-tag">{{ t }}</el-tag>
              </div>
              <div class="m-match">
                <span class="match-bar" :style="{ width: m.match + '%' }"></span>
                <span class="match-val">{{ m.match }}% 匹配度</span>
              </div>
              <div class="m-foot">
                <span class="m-stat"><el-icon><Star /></el-icon> {{ m.stars }}</span>
                <span class="m-stat"><el-icon><View /></el-icon> {{ m.views }}</span>
                <span class="m-points" :class="{ free: m.points === '免费' }">{{ m.points }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- Right detail panel -->
      <aside class="market-detail" v-if="currentMaterial">
        <div class="d-preview" :style="{ background: currentMaterial.bg }">
          <div class="d-icon">{{ currentMaterial.icon }}</div>
        </div>
        <div class="d-thumbs">
          <div
            v-for="i in 4"
            :key="i"
            class="d-thumb"
            :class="{ active: i === 1 }"
            :style="{ background: currentMaterial.bg }"
          ></div>
        </div>

        <h3 class="d-title">{{ currentMaterial.title }}</h3>
        <div class="d-creator">
          <span class="avatar" :style="{ background: currentMaterial.avatarBg }">{{ currentMaterial.avatarChar }}</span>
          <span>{{ currentMaterial.creator }}</span>
          <el-icon class="verified"><CircleCheckFilled /></el-icon>
          <span class="d-date">· 2026-03-12</span>
        </div>
        <div class="d-id">素材 ID：GH-20260312-{{ String(currentMaterial.id).padStart(4, '0') }}</div>

        <div class="d-actions">
          <el-button type="primary" class="gh-btn-primary">
            {{ currentMaterial.points === '免费' ? '免费下载' : currentMaterial.points + ' 下载' }}
          </el-button>
          <el-button class="gh-btn-outline">加入当前项目</el-button>
          <div
            class="d-heart-btn"
            :class="{ on: currentMaterial.liked }"
            @click="currentMaterial.liked = !currentMaterial.liked"
          >
            <el-icon v-if="currentMaterial.liked"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
          </div>
        </div>

        <div class="d-section">
          <h4>AI 识别信息</h4>
          <div class="ai-row" v-for="r in aiInfo" :key="r.k">
            <span class="ai-k">{{ r.k }}</span>
            <span class="ai-v">{{ r.v }}</span>
            <span class="ai-s" v-if="r.s">{{ r.s }}%</span>
          </div>
        </div>

        <div class="d-section">
          <h4>匹配分析</h4>
          <div class="match-row" v-for="r in matchInfo" :key="r.k">
            <div class="match-top">
              <span>{{ r.k }}</span>
              <span>{{ r.v }}%</span>
            </div>
            <div class="match-track">
              <span class="match-fill" :style="{ width: r.v + '%' }"></span>
            </div>
          </div>
        </div>

        <div class="d-section">
          <h4>AI 推荐理由</h4>
          <p>该素材与您的「奶油风客厅」项目高度契合：柔和的米白与浅杏色调、低对比度的光影氛围，完美匹配目标客群审美；场景构图符合 Amazon 主图与 A+ 页面的展示规范，可直接用于产品详情与广告素材。</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listOfficialMaterials } from '@/api/customer'

const searchText = ref('')
const sortBy = ref('recommend')
const viewMode = ref('grid')
const selectedId = ref(null)
const loading = ref(false)

const selectedFilters = ref([])

const removeFilter = (tag) => {
  selectedFilters.value = selectedFilters.value.filter(f => f !== tag)
}

const clearFilters = () => {
  selectedFilters.value = []
}

const materials = ref([])
const totalCount = ref(0)

async function loadMaterials() {
  loading.value = true
  try {
    const res = await listOfficialMaterials({
      pageNum: 1,
      pageSize: 50,
      materialName: searchText.value || undefined
    })
    const rows = res.rows || []
    materials.value = rows.map(item => ({
      id: item.id,
      title: item.materialName || '未命名素材',
      creator: '官方素材',
      avatarChar: '官',
      avatarBg: '#2563FF',
      icon: '🎨',
      bg: item.thumbnailUrl ? `url(${item.thumbnailUrl}) center/cover no-repeat` : '',
      badge: item.materialType || '素材',
      badgeType: 'green',
      tags: item.tags ? (Array.isArray(item.tags) ? item.tags.map(t => t.tagName || t) : []) : [],
      match: null,
      stars: (item.favoriteCount || 0).toString(),
      views: (item.downloadCount || 0).toString(),
      points: '免费',
      liked: false,
      thumbUrl: item.thumbnailUrl,
      materialUrl: item.materialUrl
    }))
    totalCount.value = res.total || 0
    if (materials.value.length > 0) {
      selectedId.value = materials.value[0].id
    }
  } catch (e) {
    console.warn('加载官方素材失败:', e?.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMaterials()
})

const currentMaterial = computed(() => materials.value.find(m => m.id === selectedId.value))

const aiInfo = [
  { k: '产品类型', v: '沙发', s: 98 },
  { k: '空间类型', v: '客厅', s: 97 },
  { k: '风格判断', v: '奶油风', s: 96 },
  { k: '适用用途', v: '主图 / 详情图 / A+', s: null },
  { k: '平台适配', v: 'Amazon', s: 98 },
  { k: '推荐使用', v: '产品详情 / 广告素材', s: null },
]

const matchInfo = [
  { k: '风格匹配度', v: 98 },
  { k: '平台匹配度', v: 96 },
  { k: '产品适配度', v: 97 },
]
</script>

<style lang="scss" scoped>
.market-page {
  padding: var(--gh-space-24);
  min-height: 100%;
  background: var(--gh-bg-page);
}

/* ===== Top area ===== */
.market-top {
  display: flex;
  flex-direction: column;
  gap: var(--gh-space-16);
  margin-bottom: var(--gh-space-20);
}

.search-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.gh-search-input {
  flex: 1;
  height: var(--gh-input-height);
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-btn);
    box-shadow: 0 0 0 1px var(--gh-border) inset;
    padding: 0 var(--gh-space-16);
  }
  :deep(.el-input__inner) {
    font-size: var(--gh-font-body);
  }
}

.chip-row {
  display: flex;
  gap: var(--gh-space-8);
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  background: #F4F7FF;
  border: 1px solid #E5EEFF;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;

  .chip-label { color: var(--gh-text-secondary); }
  .chip-val { color: var(--gh-primary); font-weight: var(--gh-weight-medium); }
  .chip-arrow { color: var(--gh-text-placeholder); font-size: 12px; }

  &:hover { border-color: var(--gh-primary); }

  &.ghost {
    background: #fff;
    border: 1px dashed var(--gh-border);
    .chip-val { color: var(--gh-text-secondary); font-weight: var(--gh-weight-regular); }
  }
}

.selected-row {
  display: flex;
  align-items: center;
  gap: var(--gh-space-8);
  flex-wrap: wrap;
  font-size: 13px;
}
.selected-label { color: var(--gh-text-secondary); }
.sel-tag {
  border-radius: 14px;
  :deep(.el-tag__close) { font-size: 12px; }
}
.clear-link {
  color: var(--gh-primary);
  cursor: pointer;
  font-size: var(--gh-font-helper);
  &:hover { text-decoration: underline; }
}
.spacer { flex: 1; }

.sort-block {
  display: flex;
  align-items: center;
  gap: 10px;
}
.sort-label {
  color: var(--gh-text-secondary);
  font-size: 13px;
}
.gh-sort-select {
  width: 110px;
  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-btn);
  }
}
.view-toggle {
  display: flex;
  background: #F5F6F8;
  border-radius: var(--gh-radius-btn);
  padding: 3px;
  .view-btn {
    width: 32px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 7px;
    cursor: pointer;
    color: var(--gh-text-placeholder);
    transition: all 0.2s;
    &.active {
      background: #fff;
      color: var(--gh-primary);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    }
  }
}

/* ===== Body layout (grid + detail) ===== */
.market-body {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--gh-space-20);
}

/* Grid */
.market-grid-wrap { min-width: 0; }

.market-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--gh-space-16);
}

.m-card {
  padding: 0;
  overflow: hidden;
  border-radius: var(--gh-radius-card);
  border: 1px solid var(--gh-border);
  background: var(--gh-bg-card);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  box-shadow: var(--gh-shadow-card);

  &.selected {
    border-color: var(--gh-primary);
    box-shadow: 0 8px 24px rgba(37, 99, 255, 0.15);
  }
  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--gh-shadow-hover);
  }
}

.m-thumb {
  height: 160px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  .m-icon { font-size: 56px; opacity: 0.85; }
}

.m-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 11px;
  font-weight: var(--gh-weight-semibold);
  padding: 3px 8px;
  border-radius: var(--gh-radius-btn);
  color: #fff;
  &.green { background: var(--gh-success); }
  &.purple { background: #8B5CF6; }
  &.orange { background: var(--gh-warning); }
  &.yellow { background: #EAB308; }
}

.m-heart {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gh-text-placeholder);
  transition: all 0.15s;
  &:hover { color: #EF4444; }
  &.on { color: #EF4444; }
}

.m-body {
  padding: var(--gh-space-12) 14px 14px;
}

.m-title {
  font-size: var(--gh-font-body);
  font-weight: var(--gh-weight-medium);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-creator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: var(--gh-space-8);
  .avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    color: #fff;
    font-size: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: var(--gh-weight-semibold);
  }
  .creator-name { color: var(--gh-text-primary); }
  .verified { color: var(--gh-primary); font-size: 13px; }
}

.m-tags {
  display: flex;
  gap: var(--gh-space-4);
  flex-wrap: wrap;
  margin-bottom: 10px;
  .m-tag {
    border-radius: var(--gh-radius-btn);
    font-size: 11px;
  }
}

.m-match {
  height: 4px;
  background: #F0F1F3;
  border-radius: 2px;
  position: relative;
  margin-bottom: var(--gh-space-4);
  .match-bar {
    height: 100%;
    background: linear-gradient(90deg, var(--gh-primary), var(--gh-success));
    border-radius: 2px;
  }
}
.match-val {
  font-size: 11px;
  color: var(--gh-primary);
  font-weight: var(--gh-weight-semibold);
  display: block;
  margin-bottom: var(--gh-space-8);
}

.m-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  .m-stat {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
  .m-points {
    margin-left: auto;
    background: #FFF4E5;
    color: var(--gh-warning);
    padding: 2px 8px;
    border-radius: var(--gh-radius-btn);
    font-weight: var(--gh-weight-semibold);
    font-size: 11px;
    &.free {
      background: #E8F8EE;
      color: var(--gh-success);
    }
  }
}

/* ===== Right Detail Panel ===== */
.market-detail {
  background: var(--gh-bg-card);
  border: 1px solid var(--gh-border);
  border-radius: var(--gh-radius-card);
  padding: var(--gh-space-16);
  height: fit-content;
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  box-shadow: var(--gh-shadow-card);
}

.d-preview {
  height: 200px;
  border-radius: var(--gh-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  .d-icon { font-size: 72px; }
}

.d-thumbs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  margin-bottom: var(--gh-space-16);
  .d-thumb {
    height: 48px;
    border-radius: var(--gh-radius-sm);
    cursor: pointer;
    opacity: 0.5;
    transition: opacity 0.2s;
    border: 2px solid transparent;
    &.active {
      opacity: 1;
      border-color: var(--gh-primary);
    }
    &:hover { opacity: 0.8; }
  }
}

.d-title {
  font-size: var(--gh-font-module);
  font-weight: var(--gh-weight-semibold);
  color: var(--gh-text-primary);
  margin-bottom: var(--gh-space-8);
}

.d-creator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--gh-font-helper);
  color: var(--gh-text-secondary);
  margin-bottom: 6px;
  .avatar {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    color: #fff;
    font-size: 11px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: var(--gh-weight-semibold);
  }
  .verified { color: var(--gh-primary); font-size: 13px; }
  .d-date { color: var(--gh-text-placeholder); }
}

.d-id {
  font-size: 11px;
  color: var(--gh-text-placeholder);
  margin-bottom: 14px;
}

.d-actions {
  display: flex;
  gap: var(--gh-space-8);
  margin-bottom: 18px;
  align-items: center;
  .el-button {
    flex: 1;
    height: var(--gh-btn-height);
    border-radius: var(--gh-radius-btn);
  }
}
.gh-btn-primary {
  background: var(--gh-primary);
  border-color: var(--gh-primary);
  color: #fff;
}
.gh-btn-outline {
  border: 1px solid var(--gh-primary);
  color: var(--gh-primary);
  background: #fff;
}
.d-heart-btn {
  width: var(--gh-btn-height);
  height: var(--gh-btn-height);
  border-radius: var(--gh-radius-btn);
  border: 1px solid var(--gh-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--gh-text-placeholder);
  transition: all 0.2s;
  flex-shrink: 0;
  &:hover { color: #EF4444; border-color: #EF4444; }
  &.on { color: #EF4444; border-color: #EF4444; }
}

.d-section {
  padding: 14px 0;
  border-top: 1px solid var(--gh-border-light);
  h4 {
    font-size: 13px;
    font-weight: var(--gh-weight-semibold);
    color: var(--gh-text-primary);
    margin-bottom: 10px;
  }
  .ai-row {
    display: flex;
    align-items: center;
    font-size: var(--gh-font-helper);
    padding: var(--gh-space-4) 0;
    .ai-k {
      color: var(--gh-text-secondary);
      width: 70px;
      flex-shrink: 0;
    }
    .ai-v {
      color: var(--gh-text-primary);
      flex: 1;
    }
    .ai-s {
      color: var(--gh-primary);
      font-weight: var(--gh-weight-semibold);
      font-size: 11px;
      background: #EEF3FF;
      padding: 1px 6px;
      border-radius: var(--gh-radius-sm);
    }
  }
  .match-row {
    margin-bottom: 10px;
    .match-top {
      display: flex;
      justify-content: space-between;
      font-size: var(--gh-font-helper);
      margin-bottom: var(--gh-space-4);
      span:first-child { color: var(--gh-text-secondary); }
      span:last-child { color: var(--gh-primary); font-weight: var(--gh-weight-semibold); }
    }
    .match-track {
      height: 4px;
      background: #F0F1F3;
      border-radius: 2px;
      overflow: hidden;
      .match-fill {
        height: 100%;
        background: linear-gradient(90deg, var(--gh-primary), var(--gh-success));
        border-radius: 2px;
      }
    }
  }
  p {
    font-size: var(--gh-font-helper);
    color: var(--gh-text-secondary);
    line-height: 1.7;
    margin: 0;
  }
}
</style>
