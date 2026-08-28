<template>
  <div class="admin-layout">
    <!-- 左侧深色侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <Logo size="sm" />
        <span v-if="!isCollapsed" class="sidebar-title">管理后台</span>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <nav class="admin-nav">
          <div
            v-for="group in menuGroups"
            :key="group.label"
            class="nav-group"
          >
            <div class="nav-group-title" v-if="!isCollapsed">{{ group.label }}</div>
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="admin-nav-item"
              :class="{ active: route.path === item.path }"
            >
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
              <span v-if="!isCollapsed">{{ item.title }}</span>
            </router-link>
          </div>
        </nav>
      </el-scrollbar>

      <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
        <el-icon :size="16">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <div class="admin-main">
      <!-- 顶栏 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentGroup">{{ currentGroup }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta?.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <button class="topbar-btn" title="切换前台" @click="goToFront">
            <el-icon :size="16"><Monitor /></el-icon>
            <span>前台预览</span>
          </button>
          <button class="topbar-btn has-dot" title="通知">
            <el-icon :size="16"><Bell /></el-icon>
            <i class="dot"></i>
          </button>
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-trigger">
              <el-avatar :size="30" src="/images/logo-guanghe-ai.png" />
              <span class="user-name">{{ userStore.userInfo.nickname || '管理员' }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import Logo from '@/components/Logo.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const menuGroups = [
  {
    label: '概览',
    items: [
      { path: '/admin/dashboard', title: '数据看板', icon: 'DataLine' }
    ]
  },
  {
    label: '业务管理',
    items: [
      { path: '/admin/users', title: '用户管理', icon: 'UserFilled' },
      { path: '/admin/orders', title: '订单中心', icon: 'List' },
      { path: '/admin/points', title: '积分管理', icon: 'Coin' },
      { path: '/admin/content', title: '内容管理', icon: 'PictureFilled' }
    ]
  },
  {
    label: '运营中心',
    items: [
      { path: '/admin/banner', title: 'Banner管理', icon: 'PictureFilled' },
      { path: '/admin/material', title: '官方素材管理', icon: 'FolderOpened' },
      //{ path: '/admin/ai-model', title: 'AI模特管理', icon: 'User' },
      //{ path: '/admin/creation-config', title: '创作功能管理', icon: 'Setting' }
    ]
  },
  {
    label: '系统',
    items: [
      { path: '/admin/system', title: '系统设置', icon: 'Setting' }
    ]
  }
]

const currentGroup = computed(() => {
  for (const group of menuGroups) {
    if (group.items.find(i => i.path === route.path)) {
      return group.label
    }
  }
  return ''
})

function goToFront() {
  router.push('/whiteBg')
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/admin')
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100%;
  display: flex;
  background: var(--gh-bg-page);
}

// ========== 侧边栏 ==========
.admin-sidebar {
  width: 220px;
  background: #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  flex-shrink: 0;

  &.collapsed {
    width: 64px;

    .sidebar-header {
      justify-content: center;
      padding: 16px 0;
    }
  }
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;

  :deep(.logo-img) {
    height: 28px;
  }
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #f1f5f9;
  white-space: nowrap;
}

.sidebar-scroll {
  flex: 1;
  overflow: hidden;
}

.admin-nav {
  padding: 12px 8px;
}

.nav-group {
  margin-bottom: 8px;
}

.nav-group-title {
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 12px 12px 6px;
  white-space: nowrap;
}

.admin-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 14px;
  border-radius: 8px;
  text-decoration: none;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  margin: 2px 0;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #e2e8f0;
  }

  &.active {
    background: var(--gh-primary);
    color: #fff;
  }
}

.sidebar-toggle {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: #64748b;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.2s;

  &:hover {
    color: #e2e8f0;
  }
}

// ========== 右侧主区域 ==========
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--gh-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topbar-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--gh-border-light);
  background: #fff;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--gh-primary);
    color: var(--gh-primary);
  }

  &.has-dot {
    position: relative;
    padding: 0 8px;

    .dot {
      position: absolute;
      right: 4px;
      top: 4px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #ef4444;
      box-shadow: 0 0 0 2px #fff;
    }
  }
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f8fafc;
  }

  .user-name {
    font-size: 14px;
    color: #1e293b;
    font-weight: 500;
  }

  .el-icon {
    color: #94a3b8;
  }
}

// ========== 内容区 ==========
.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
