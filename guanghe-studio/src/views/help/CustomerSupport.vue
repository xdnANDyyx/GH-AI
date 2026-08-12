<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">客服支持</h2>
        <p class="gh-page-subtitle">多种方式获取帮助，我们随时为您服务</p>
      </div>
    </div>

    <div class="support-layout">
      <!-- Chat Window -->
      <div class="chat-section gh-card">
        <div class="chat-header">
          <div class="chat-header-left">
            <div class="agent-avatar">
              <el-icon :size="20" color="#fff"><Service /></el-icon>
            </div>
            <div>
              <div class="agent-name">光合AI客服</div>
              <div class="agent-status">
                <span class="status-dot online"></span>
                在线
              </div>
            </div>
          </div>
          <el-tag size="small" type="info">工作时间</el-tag>
        </div>

        <div class="chat-messages" ref="chatArea">
          <div class="chat-message system">
            <div class="system-msg">欢迎使用光合AI在线客服，请问有什么可以帮助您的？</div>
          </div>

          <div class="chat-message agent">
            <div class="agent-bubble-avatar">
              <el-icon :size="14" color="#fff"><Service /></el-icon>
            </div>
            <div class="bubble agent-bubble">
              <p>您好！我是光合AI智能客服助手。</p>
              <p>我可以帮您解答以下问题：</p>
              <ul>
                <li>功能使用指导</li>
                <li>积分与账户问题</li>
                <li>技术故障排查</li>
                <li>合作与商务咨询</li>
              </ul>
              <p>请输入您的问题，或点击下方快捷问题。</p>
            </div>
          </div>

          <div v-for="(msg, idx) in chatMessages" :key="idx" class="chat-message" :class="msg.role">
            <div v-if="msg.role === 'agent'" class="agent-bubble-avatar">
              <el-icon :size="14" color="#fff"><Service /></el-icon>
            </div>
            <div class="bubble" :class="msg.role + '-bubble'">
              <p>{{ msg.text }}</p>
              <span class="bubble-time">{{ msg.time }}</span>
            </div>
          </div>
        </div>

        <!-- Quick Questions -->
        <div class="quick-questions">
          <el-tag
            v-for="q in quickQuestions"
            :key="q"
            class="quick-q-tag"
            effect="plain"
            round
            @click="sendQuickQuestion(q)"
          >{{ q }}</el-tag>
        </div>

        <!-- Input -->
        <div class="chat-input-area">
          <el-input
            v-model="chatInput"
            placeholder="输入您的问题..."
            @keyup.enter="sendMessage"
            class="chat-input"
          >
            <template #append>
              <el-button type="primary" @click="sendMessage" :disabled="!chatInput.trim()">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- Right Side -->
      <div class="support-sidebar">
        <!-- Contact Methods -->
        <div class="contact-methods">
          <h3 class="section-title">联系方式</h3>
          <div class="method-cards">
            <div
              v-for="method in contactMethods"
              :key="method.label"
              class="method-card gh-card"
            >
              <div class="method-icon" :style="{ background: method.bg }">
                <el-icon :size="22" :style="{ color: method.color }">
                  <component :is="method.icon" />
                </el-icon>
              </div>
              <div class="method-info">
                <div class="method-label">{{ method.label }}</div>
                <div class="method-value">{{ method.value }}</div>
              </div>
              <el-button text type="primary" size="small" @click="method.action && method.action()">
                {{ method.actionText }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- Business Hours -->
        <div class="business-hours gh-card">
          <h3 class="section-title">
            <el-icon><Clock /></el-icon>
            服务时间
          </h3>
          <div class="hours-list">
            <div class="hours-item">
              <span class="hours-day">在线客服</span>
              <span class="hours-time">周一至周日 9:00 - 22:00</span>
            </div>
            <div class="hours-item">
              <span class="hours-day">电话客服</span>
              <span class="hours-time">周一至周五 9:00 - 18:00</span>
            </div>
            <div class="hours-item">
              <span class="hours-day">邮件支持</span>
              <span class="hours-time">24小时内回复</span>
            </div>
          </div>
          <div class="current-status">
            <span class="status-dot online"></span>
            <span>当前客服在线，平均响应 &lt; 2分钟</span>
          </div>
        </div>

        <!-- FAQ Quick Links -->
        <div class="faq-quick gh-card">
          <h3 class="section-title">
            <el-icon><QuestionFilled /></el-icon>
            常见问题
          </h3>
          <div class="faq-quick-list">
            <div
              v-for="(faq, idx) in faqQuickLinks"
              :key="idx"
              class="faq-quick-item"
              @click="$router.push('/help/help-center')"
            >
              <span class="faq-quick-text">{{ faq }}</span>
              <el-icon class="faq-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Service,
  Promotion,
  Clock,
  QuestionFilled,
  ArrowRight,
  ChatDotRound,
  Phone,
  Message
} from '@element-plus/icons-vue'

const chatInput = ref('')
const chatArea = ref(null)

const chatMessages = ref([
  { role: 'user', text: '你好，我想问一下积分怎么充值？', time: '14:30' },
  { role: 'agent', text: '您好！积分充值非常简单：\n1. 点击页面右上角的"积分"按钮\n2. 进入积分中心\n3. 选择合适的积分套餐\n4. 完成支付即可\n\n目前我们有4种套餐可选，最受欢迎的2000积分套餐还额外赠送200积分哦！', time: '14:31' }
])

const quickQuestions = [
  '如何充值积分',
  '图片生成失败',
  '如何下载图片',
  '账户安全问题'
]

const contactMethods = [
  {
    label: '在线客服',
    value: '即时响应',
    icon: 'ChatDotRound',
    bg: '#EFF6FF',
    color: '#2563FF',
    actionText: '开始对话',
    action: () => ElMessage.info('正在连接在线客服...')
  },
  {
    label: '电话客服',
    value: '400-888-6688',
    icon: 'Phone',
    bg: '#ECFDF5',
    color: '#059669',
    actionText: '拨打',
    action: () => ElMessage.info('正在拨打客服电话...')
  },
  {
    label: '邮件支持',
    value: 'support@guanghe.ai',
    icon: 'Message',
    bg: '#FFF7ED',
    color: '#D97706',
    actionText: '发送邮件',
    action: () => ElMessage.info('正在打开邮件客户端...')
  }
]

const faqQuickLinks = [
  '如何生成AI白底图？',
  '积分如何获取？有效期多久？',
  '支持哪些图片格式？',
  '生成的图片版权归属？',
  '如何使用AI模特换装？',
  '批量处理如何操作？'
]

async function sendMessage() {
  const text = chatInput.value.trim()
  if (!text) return

  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`

  chatMessages.value.push({ role: 'user', text, time: timeStr })
  chatInput.value = ''

  await scrollToBottom()

  // Auto reply
  setTimeout(() => {
    chatMessages.value.push({
      role: 'agent',
      text: '感谢您的提问！我已记录您的问题，客服专员会尽快为您解答。您也可以先查看帮助中心获取更多信息。',
      time: timeStr
    })
    scrollToBottom()
  }, 1000)
}

function sendQuickQuestion(q) {
  chatInput.value = q
  sendMessage()
}

async function scrollToBottom() {
  await nextTick()
  if (chatArea.value) {
    chatArea.value.scrollTop = chatArea.value.scrollHeight
  }
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

.support-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Chat Window */
.chat-section {
  flex: 1;
  min-width: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--gh-border-light);
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563FF, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
}

.agent-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
}

.agent-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--gh-text-placeholder);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &.online {
    background: #10B981;
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
  }
}

/* Chat Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  min-height: 340px;
  max-height: 340px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #FAFBFF;
}

.chat-message {
  display: flex;
  gap: 8px;
  max-width: 85%;

  &.system {
    max-width: 100%;
    justify-content: center;
  }

  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
}

.system-msg {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  background: #F3F4F6;
  padding: 4px 14px;
  border-radius: 12px;
}

.agent-bubble-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563FF, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.7;
  position: relative;

  p {
    margin: 0 0 6px;

    &:last-of-type {
      margin-bottom: 0;
    }
  }

  ul {
    margin: 4px 0;
    padding-left: 18px;

    li {
      margin: 2px 0;
    }
  }
}

.agent-bubble {
  background: #fff;
  border: 1px solid var(--gh-border-light);
  color: var(--gh-text-primary);
  border-top-left-radius: 4px;
}

.user-bubble {
  background: var(--gh-primary);
  color: #fff;
  border-top-right-radius: 4px;
}

.bubble-time {
  display: block;
  font-size: 10px;
  opacity: 0.5;
  margin-top: 6px;
}

/* Quick Questions */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 20px;
  border-top: 1px solid var(--gh-border-light);
}

.quick-q-tag {
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;

  &:hover {
    color: var(--gh-primary);
    border-color: var(--gh-primary);
  }
}

/* Chat Input */
.chat-input-area {
  padding: 12px 20px;
  border-top: 1px solid var(--gh-border-light);
}

.chat-input {
  :deep(.el-input-group__append) {
    padding: 0;
    background: transparent;
    border: none;
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-sm) 0 0 var(--gh-radius-sm);
  }
}

/* Sidebar */
.support-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Contact Methods */
.method-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.method-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
}

.method-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.method-info {
  flex: 1;
  min-width: 0;
}

.method-label {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-bottom: 2px;
}

.method-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--gh-text-primary);
}

/* Business Hours */
.business-hours {
  padding: 20px;
}

.hours-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.hours-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.hours-day {
  color: var(--gh-text-secondary);
  font-weight: 500;
}

.hours-time {
  color: var(--gh-text-primary);
}

.current-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  background: #ECFDF5;
  border-radius: var(--gh-radius-sm);
  font-size: 12px;
  color: #059669;
}

/* FAQ Quick Links */
.faq-quick {
  padding: 20px;
}

.faq-quick-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.faq-quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--gh-bg-page);

    .faq-arrow {
      color: var(--gh-primary);
    }
  }
}

.faq-quick-text {
  font-size: 13px;
  color: var(--gh-text-secondary);
}

.faq-arrow {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .support-layout {
    flex-direction: column;
  }

  .support-sidebar {
    width: 100%;
  }
}
</style>
