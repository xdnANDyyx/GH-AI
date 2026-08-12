<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">快捷键帮助</h2>
        <p class="gh-page-subtitle">掌握快捷键，提升操作效率</p>
      </div>
      <el-button round @click="printShortcuts">
        <el-icon><Printer /></el-icon>
        打印快捷键
      </el-button>
    </div>

    <!-- Search / Filter -->
    <div class="filter-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索快捷键..."
        clearable
        :prefix-icon="Search"
        class="shortcut-search"
      />
      <div class="gh-tag-group">
        <div
          v-for="g in filterGroups"
          :key="g"
          class="gh-tag"
          :class="{ active: activeFilter === g }"
          @click="activeFilter = g"
        >{{ g }}</div>
      </div>
    </div>

    <!-- Shortcut Groups -->
    <div class="shortcuts-content">
      <template v-for="group in filteredGroups" :key="group.category">
        <div class="shortcut-group">
          <div class="group-header">
            <div class="group-icon" :style="{ background: group.iconBg }">
              <el-icon :size="18" :style="{ color: group.iconColor }">
                <component :is="group.icon" />
              </el-icon>
            </div>
            <h3 class="group-title">{{ group.category }}</h3>
            <el-tag size="small" type="info">{{ group.shortcuts.length }} 个</el-tag>
          </div>

          <div class="shortcut-list gh-card">
            <div
              v-for="(sc, idx) in group.shortcuts"
              :key="idx"
              class="shortcut-item"
              :class="{ 'is-last': idx === group.shortcuts.length - 1 }"
            >
              <div class="sc-info">
                <span class="sc-name">{{ sc.name }}</span>
                <span class="sc-desc">{{ sc.desc }}</span>
              </div>
              <div class="sc-keys">
                <template v-for="(key, kIdx) in sc.keys" :key="kIdx">
                  <kbd class="key">{{ key }}</kbd>
                  <span v-if="kIdx < sc.keys.length - 1" class="key-plus">+</span>
                </template>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Empty State -->
      <div v-if="filteredGroups.length === 0" class="empty-state">
        <el-icon :size="48" color="#D1D5DB"><Keyboard /></el-icon>
        <div class="empty-text">没有找到匹配的快捷键</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Search,
  Printer,
  Monitor,
  Setting,
  PictureFilled,
  MagicStick
} from '@element-plus/icons-vue'

const searchQuery = ref('')
const activeFilter = ref('全部')
const filterGroups = ['全部', '通用操作', '画布操作', 'AI功能']

const shortcutGroups = [
  {
    category: '通用操作',
    icon: 'Setting',
    iconBg: '#EFF6FF',
    iconColor: '#2563FF',
    shortcuts: [
      { name: '保存项目', desc: '保存当前工作进度', keys: ['Ctrl', 'S'] },
      { name: '撤销', desc: '撤销上一步操作', keys: ['Ctrl', 'Z'] },
      { name: '重做', desc: '恢复已撤销的操作', keys: ['Ctrl', 'Shift', 'Z'] },
      { name: '复制', desc: '复制选中内容', keys: ['Ctrl', 'C'] },
      { name: '粘贴', desc: '粘贴已复制的内容', keys: ['Ctrl', 'V'] },
      { name: '全选', desc: '选中所有内容', keys: ['Ctrl', 'A'] },
      { name: '删除', desc: '删除选中内容', keys: ['Delete'] },
      { name: '搜索', desc: '打开全局搜索', keys: ['Ctrl', 'F'] },
      { name: '新建项目', desc: '创建新项目', keys: ['Ctrl', 'N'] },
      { name: '导出', desc: '导出当前作品', keys: ['Ctrl', 'E'] },
      { name: '快捷键帮助', desc: '显示此快捷键面板', keys: ['?'] },
      { name: '切换侧边栏', desc: '显示/隐藏侧边栏', keys: ['Ctrl', 'B'] }
    ]
  },
  {
    category: '画布操作',
    icon: 'PictureFilled',
    iconBg: '#ECFDF5',
    iconColor: '#059669',
    shortcuts: [
      { name: '放大', desc: '放大画布视图', keys: ['Ctrl', '+'] },
      { name: '缩小', desc: '缩小画布视图', keys: ['Ctrl', '-'] },
      { name: '适应画布', desc: '画布自适应窗口大小', keys: ['Ctrl', '0'] },
      { name: '100%缩放', desc: '恢复100%缩放比例', keys: ['Ctrl', '1'] },
      { name: '平移画布', desc: '按住空格拖动平移画布', keys: ['Space', '拖拽'] },
      { name: '水平翻转', desc: '水平翻转选中图层', keys: ['Shift', 'H'] },
      { name: '垂直翻转', desc: '垂直翻转选中图层', keys: ['Shift', 'V'] },
      { name: '图层上移', desc: '将选中图层上移一层', keys: ['Ctrl', ']'] },
      { name: '图层下移', desc: '将选中图层下移一层', keys: ['Ctrl', '['] },
      { name: '锁定图层', desc: '锁定/解锁当前图层', keys: ['Ctrl', 'L'] },
      { name: '对齐左边缘', desc: '左对齐选中元素', keys: ['Alt', 'L'] },
      { name: '居中对齐', desc: '水平居中对齐', keys: ['Alt', 'C'] }
    ]
  },
  {
    category: 'AI功能',
    icon: 'MagicStick',
    iconBg: '#FFF7ED',
    iconColor: '#D97706',
    shortcuts: [
      { name: 'AI生成', desc: '触发AI生成操作', keys: ['Ctrl', 'G'] },
      { name: '智能抠图', desc: '一键智能去除背景', keys: ['Ctrl', 'Shift', 'K'] },
      { name: 'AI精修', desc: '对选中图片进行AI精修', keys: ['Ctrl', 'Shift', 'R'] },
      { name: '生成背景', desc: '为当前图片生成AI背景', keys: ['Ctrl', 'Shift', 'B'] },
      { name: 'AI模特', desc: '生成AI虚拟模特效果', keys: ['Ctrl', 'Shift', 'M'] },
      { name: '重新生成', desc: '使用当前参数重新生成', keys: ['Ctrl', 'R'] },
      { name: '切换AI面板', desc: '显示/隐藏右侧AI助手面板', keys: ['Ctrl', 'J'] },
      { name: '提示词搜索', desc: '搜索提示词模板', keys: ['Ctrl', 'Shift', 'F'] }
    ]
  }
]

const filteredGroups = computed(() => {
  let groups = shortcutGroups

  // Filter by category
  if (activeFilter.value !== '全部') {
    groups = groups.filter(g => g.category === activeFilter.value)
  }

  // Filter by search query
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    groups = groups
      .map(g => ({
        ...g,
        shortcuts: g.shortcuts.filter(
          sc =>
            sc.name.toLowerCase().includes(q) ||
            sc.desc.toLowerCase().includes(q) ||
            sc.keys.some(k => k.toLowerCase().includes(q))
        )
      }))
      .filter(g => g.shortcuts.length > 0)
  }

  return groups
})

function printShortcuts() {
  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  let html = `
    <html><head><title>光合AI Studio 快捷键参考</title>
    <style>
      body { font-family: PingFang SC, sans-serif; padding: 40px; color: #1F2937; }
      h1 { font-size: 22px; margin-bottom: 8px; }
      .subtitle { color: #6B7280; font-size: 14px; margin-bottom: 30px; }
      .group { margin-bottom: 24px; }
      .group-title { font-size: 16px; font-weight: 600; margin-bottom: 10px; padding-bottom: 6px; border-bottom: 2px solid #2563FF; display: inline-block; }
      table { width: 100%; border-collapse: collapse; }
      th, td { text-align: left; padding: 8px 12px; border-bottom: 1px solid #EBEDF5; font-size: 13px; }
      th { background: #F7F9FC; font-weight: 600; }
      kbd { display: inline-block; background: #F3F4F6; border: 1px solid #D1D5DB; border-radius: 4px; padding: 2px 8px; font-size: 12px; font-family: monospace; margin: 0 2px; }
      .key-plus { color: #9CA3AF; margin: 0 4px; }
      .footer { margin-top: 30px; font-size: 12px; color: #9CA3AF; }
    </style></head><body>
    <h1>光合AI Studio 快捷键参考</h1>
    <div class="subtitle">打印于 ${new Date().toLocaleDateString('zh-CN')}</div>
  `

  for (const group of shortcutGroups) {
    html += `<div class="group"><div class="group-title">${group.category}</div><table><tr><th>功能</th><th>说明</th><th>快捷键</th></tr>`
    for (const sc of group.shortcuts) {
      const keys = sc.keys.map(k => `<kbd>${k}</kbd>`).join('<span class="key-plus">+</span>')
      html += `<tr><td>${sc.name}</td><td>${sc.desc}</td><td>${keys}</td></tr>`
    }
    html += `</table></div>`
  }

  html += `<div class="footer">光合AI Studio - 让AI赋能每一个创作者</div></body></html>`

  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.print()
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

/* Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.shortcut-search {
  width: 260px;

  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-sm);
  }
}

/* Shortcut Groups */
.shortcuts-content {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.shortcut-group {
  // group styling
}

.group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.group-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gh-text-primary);
  flex: 1;
}

/* Shortcut List */
.shortcut-list {
  padding: 0;
  overflow: hidden;
}

.shortcut-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--gh-border-light);
  transition: background 0.15s;

  &:hover {
    background: #FAFBFF;
  }

  &.is-last {
    border-bottom: none;
  }
}

.sc-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.sc-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--gh-text-primary);
}

.sc-desc {
  font-size: 12px;
  color: var(--gh-text-placeholder);
}

.sc-keys {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 0;
}

.key {
  display: inline-block;
  min-width: 28px;
  padding: 4px 10px;
  text-align: center;
  font-size: 12px;
  font-family: 'SF Mono', 'Consolas', 'Monaco', monospace;
  font-weight: 600;
  color: var(--gh-text-primary);
  background: #F3F4F6;
  border: 1px solid #D1D5DB;
  border-bottom: 2px solid #C9CDD5;
  border-radius: 5px;
  line-height: 1.4;
  white-space: nowrap;
}

.key-plus {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin: 0 4px;
  font-weight: 500;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 12px;
}

.empty-text {
  font-size: 14px;
  color: var(--gh-text-placeholder);
}
</style>
