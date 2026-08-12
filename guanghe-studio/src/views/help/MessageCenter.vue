<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">消息中心</h2>
        <p class="gh-page-subtitle">查看系统通知、活动消息和互动提醒</p>
      </div>
      <el-button round @click="markAllRead" :disabled="unreadCount === 0">
        <el-icon><Check /></el-icon>
        全部已读
      </el-button>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="msg-tabs" @tab-change="handleTabChange">
      <el-tab-pane name="all">
        <template #label>
          <span>全部</span>
          <el-badge :value="allMessages.length" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="system">
        <template #label>
          <span>系统通知</span>
          <el-badge :value="systemCount" :max="99" class="tab-badge" :hidden="systemCount === 0" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="activity">
        <template #label>
          <span>活动消息</span>
          <el-badge :value="activityCount" :max="99" class="tab-badge" :hidden="activityCount === 0" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="interaction">
        <template #label>
          <span>互动消息</span>
          <el-badge :value="interactionCount" :max="99" class="tab-badge" :hidden="interactionCount === 0" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Message List -->
    <div class="msg-list" v-if="filteredMessages.length > 0">
      <div
        v-for="(msg, idx) in filteredMessages"
        :key="msg.id"
        class="msg-item gh-card"
        :class="{ unread: !msg.isRead }"
        @click="markRead(msg)"
      >
        <div class="msg-avatar" :style="{ background: msg.avatarBg }">
          <el-icon :size="20" :color="msg.avatarColor">
            <component :is="msg.avatarIcon" />
          </el-icon>
        </div>
        <div class="msg-body">
          <div class="msg-top-row">
            <span class="msg-title">{{ msg.title }}</span>
            <el-tag size="small" :type="msg.tagType" class="msg-type-tag">{{ msg.typeLabel }}</el-tag>
          </div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-footer">
            <span class="msg-time">{{ msg.time }}</span>
            <span v-if="!msg.isRead" class="unread-dot"></span>
          </div>
        </div>
        <div class="msg-actions">
          <el-button text size="small" @click.stop="deleteMessage(idx)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-illustration">
        <el-icon :size="64" color="#D1D5DB"><Bell /></el-icon>
      </div>
      <div class="empty-title">暂无消息</div>
      <div class="empty-desc">当前分类下没有任何消息</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Check,
  Delete,
  Bell,
  Setting,
  Promotion,
  ChatDotRound,
  Present,
  Warning,
  Star,
  Trophy,
  Coin,
  UserFilled
} from '@element-plus/icons-vue'

const activeTab = ref('all')

const allMessages = ref([
  {
    id: 1,
    type: 'system',
    typeLabel: '系统通知',
    tagType: '',
    title: '系统维护通知',
    content: '尊敬的用户，系统将于2024年6月15日凌晨2:00-4:00进行例行维护，届时部分功能可能暂时不可用。',
    time: '2小时前',
    isRead: false,
    avatarIcon: 'Setting',
    avatarBg: '#EFF6FF',
    avatarColor: '#2563FF'
  },
  {
    id: 2,
    type: 'activity',
    typeLabel: '活动消息',
    tagType: 'warning',
    title: '618限时优惠 - 积分充值享8折',
    content: '618大促来啦！6月10日-6月20日期间，所有积分套餐享8折优惠，更有额外赠送积分等你来拿！',
    time: '5小时前',
    isRead: false,
    avatarIcon: 'Present',
    avatarBg: '#FFF7ED',
    avatarColor: '#D97706'
  },
  {
    id: 3,
    type: 'interaction',
    typeLabel: '互动消息',
    tagType: 'success',
    title: '您的素材被下载了！',
    content: '您上传的素材"北欧简约客厅场景"被用户"设计师小明"下载，获得+50积分奖励。',
    time: '1天前',
    isRead: false,
    avatarIcon: 'Star',
    avatarBg: '#ECFDF5',
    avatarColor: '#059669'
  },
  {
    id: 4,
    type: 'system',
    typeLabel: '系统通知',
    tagType: '',
    title: '新功能上线：AI模特换装',
    content: '全新AI模特换装功能已上线！支持多种虚拟模特形象，一键生成穿着效果图，快来体验吧。',
    time: '2天前',
    isRead: true,
    avatarIcon: 'Promotion',
    avatarBg: '#F3E8FF',
    avatarColor: '#7C3AED'
  },
  {
    id: 5,
    type: 'interaction',
    typeLabel: '互动消息',
    tagType: 'success',
    title: '您的评论收到了回复',
    content: '用户"创意工坊"回复了您在素材"日式茶室场景"下的评论："感谢反馈，已更新优化！"',
    time: '3天前',
    isRead: true,
    avatarIcon: 'ChatDotRound',
    avatarBg: '#ECFDF5',
    avatarColor: '#059669'
  },
  {
    id: 6,
    type: 'system',
    typeLabel: '系统通知',
    tagType: '',
    title: '积分余额提醒',
    content: '您当前的积分余额为2,856，足够生成约140张白底图。如需充值，请前往积分中心。',
    time: '3天前',
    isRead: true,
    avatarIcon: 'Coin',
    avatarBg: '#FFF7ED',
    avatarColor: '#D97706'
  },
  {
    id: 7,
    type: 'activity',
    typeLabel: '活动消息',
    tagType: 'warning',
    title: '创作者激励计划启动',
    content: '光合创作者激励计划正式启动！上传优质素材可获得额外30%积分加成，快来参与吧。',
    time: '5天前',
    isRead: true,
    avatarIcon: 'Trophy',
    avatarBg: '#EFF6FF',
    avatarColor: '#2563FF'
  },
  {
    id: 8,
    type: 'interaction',
    typeLabel: '互动消息',
    tagType: 'success',
    title: '您的作品被收藏了',
    content: '您生成的作品"现代简约卧室场景图"被5位用户收藏，人气持续上升中！',
    time: '1周前',
    isRead: true,
    avatarIcon: 'UserFilled',
    avatarBg: '#F3E8FF',
    avatarColor: '#7C3AED'
  }
])

const unreadCount = computed(() => allMessages.value.filter(m => !m.isRead).length)
const systemCount = computed(() => allMessages.value.filter(m => m.type === 'system' && !m.isRead).length)
const activityCount = computed(() => allMessages.value.filter(m => m.type === 'activity' && !m.isRead).length)
const interactionCount = computed(() => allMessages.value.filter(m => m.type === 'interaction' && !m.isRead).length)

const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return allMessages.value
  return allMessages.value.filter(m => m.type === activeTab.value)
})

function markRead(msg) {
  msg.isRead = true
}

function markAllRead() {
  allMessages.value.forEach(m => (m.isRead = true))
}

function deleteMessage(idx) {
  const list = filteredMessages.value
  const target = list[idx]
  const realIdx = allMessages.value.findIndex(m => m.id === target.id)
  if (realIdx !== -1) {
    allMessages.value.splice(realIdx, 1)
  }
}

function handleTabChange() {
  // tab change handled by v-model
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

/* Tabs */
.msg-tabs {
  margin-bottom: 20px;

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: var(--gh-border-light);
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    font-weight: 500;
    color: var(--gh-text-secondary);
    padding: 0 20px;

    &.is-active {
      color: var(--gh-primary);
    }
  }
}

.tab-badge {
  margin-left: 6px;

  :deep(.el-badge__content) {
    font-size: 10px;
  }
}

/* Message List */
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &.unread {
    background: #FAFBFF;
    border-left: 3px solid var(--gh-primary);
  }
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-top-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.msg-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
}

.msg-type-tag {
  flex-shrink: 0;
}

.msg-content {
  font-size: 13px;
  color: var(--gh-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.msg-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.msg-time {
  font-size: 12px;
  color: var(--gh-text-placeholder);
}

.unread-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--gh-primary);
}

.msg-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;

  .msg-item:hover & {
    opacity: 1;
  }
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-illustration {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: var(--gh-bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 13px;
  color: var(--gh-text-placeholder);
}
</style>
