<template>
  <div class="ai-assistant">
    <div class="ai-header">
      <div class="ai-title">
        <el-icon class="ai-icon"><ChatDotRound /></el-icon>
        AI 助手
        <a
          href="https://yuanbao.tencent.com/"
          target="_blank"
          class="yuanbao-link"
        >
          <el-icon :size="12"><Link /></el-icon>
          腾讯元宝
        </a>
      </div>
      <div class="ai-header-right">
        <a class="clear-link" @click="clearChat" v-if="!collapsed">清空对话</a>
        <a class="collapse-link" @click="emit('toggle-collapse')">
          {{ collapsed ? '+' : '−' }}
        </a>
      </div>
    </div>

    <template v-if="!collapsed">
      <div class="ai-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="ai-welcome">
          <div class="welcome-icon">
            <el-icon :size="40"><ChatDotRound /></el-icon>
          </div>
          <p class="welcome-title">你好，我是光合AI助手</p>
          <p class="welcome-desc">我可以帮你优化设计参数、推荐风格搭配、解答使用问题。</p>
          <div class="quick-questions">
            <!-- <div
              v-for="(q, i) in quickQuestions"
              :key="i"
              class="quick-item"
              @click="sendQuick(q)"
            >
              {{ q }}
            </div> -->
          </div>
        </div>

        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="msg-item"
          :class="msg.role"
        >
          <div class="msg-bubble">
            <div class="msg-avatar" v-if="msg.role === 'assistant'">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="msg-content">
              <span v-if="msg.content">{{ msg.content }}</span>
              <div v-if="msg.images && msg.images.length" class="msg-images">
                <div
                  v-for="(img, i) in msg.images"
                  :key="i"
                  class="msg-image-item"
                >
                  <el-image
                    :src="img.url || img"
                    :preview-src-list="msg.images.map(m => m.url || m)"
                    :initial-index="i"
                    fit="cover"
                    preview-teleported
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="isLoading || isGenerating" class="msg-item assistant">
          <div class="msg-bubble">
            <div class="msg-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="msg-content">
              <template v-if="isGenerating">
                <div class="gen-progress-info">
                  <span>{{ genStatus || '正在生成...' }}</span>
                  <el-progress v-if="genProgress > 0" :percentage="genProgress" :stroke-width="4" :show-text="false" />
                </div>
              </template>
              <template v-else>
                <div class="typing">
                  <span></span><span></span><span></span>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div v-if="genError" class="msg-item assistant">
          <div class="msg-bubble">
            <div class="msg-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="msg-content gen-error-msg">{{ genError }}</div>
          </div>
        </div>
      </div>

      <div class="ai-input-area" :style="{ flexBasis: inputAreaHeight + 'px' }">
        <div class="input-resize-handle" @mousedown.prevent="startResize"></div>
        <!-- <div class="points-note">
          <el-icon><InfoFilled /></el-icon>
          本次操作将消耗 <strong>2</strong> 积分
        </div> -->
        <div class="input-wrapper">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            maxlength="2000"
            show-word-limit
            placeholder="生成图片..."
            @keydown.enter.exact.prevent="sendMessage"
            resize="none"
          />
          <div class="input-actions">
            <span class="char-counter">{{ inputText.length }}/2000</span>
            <div class="actions-right">
              <el-select
                v-model="selectedModel"
                size="small"
                class="model-select"
                :disabled="isLoading"
              >
                <el-option
                  v-for="m in modelOptions"
                  :key="m.value"
                  :label="m.label"
                  :value="m.value"
                />
              </el-select>
              <el-button
                type="primary"
                :disabled="isLoading || !props.hasImage"
                :loading="isGenerating"
                @click="sendMessage"
                class="send-btn"
              >
                <el-icon><Promotion /></el-icon>
                {{ isGenerating ? '生成中...' : '发送' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="ai-collapsed-placeholder" @click="emit('toggle-collapse')">
      <div class="collapsed-inner">
        <el-icon :size="36" color="#9CA3AF"><ChatDotRound /></el-icon>
        <p>点击展开 AI 助手</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  collapsed: { type: Boolean, default: false },
  generateFn: { type: Function, default: null },
  isGenerating: { type: Boolean, default: false },
  genStatus: { type: String, default: '' },
  genProgress: { type: Number, default: 0 },
  genError: { type: String, default: '' },
  hasImage: { type: Boolean, default: false },
  onClearImages: { type: Function, default: null }
})
const emit = defineEmits(['toggle-collapse'])

const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)
const messagesRef = ref(null)

const quickQuestions = [
  '推荐适合家居产品的背景风格',
  '如何提升产品图的转化率？',
  '帮我选择合适的灯光方案',
  '白底图最佳输出尺寸是多少？'
]

// ===== 模型选择 =====
const selectedModel = ref('gemini-3-pro-image')
const modelOptions = [
  { label: 'Gemini 3 Pro Image', value: 'gemini-3-pro-image' },
  { label: 'Gemini 3.1 Flash Image', value: 'gemini-3.1-flash-image' }
]

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function sendQuick(text) {
  inputText.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (isLoading.value) return
  // 有图片即可发送，文字可为空
  if (!props.hasImage) return

  // 记录用户消息（文字为空时给默认提示）
  messages.value.push({ role: 'user', content: text || '生成图片' })
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  // 直接触发生成（不走 deepseek 对话，仅生图），将用户选择的模型和提示词传给生成函数
  if (props.generateFn) {
    try {
      await props.generateFn({ model: selectedModel.value, prompt: text })
    } catch (e) {
      console.error('生成触发失败:', e)
    } finally {
      isLoading.value = false
      scrollToBottom()
    }
  } else {
    isLoading.value = false
    scrollToBottom()
  }
}

async function clearChat() {
  try {
    await ElMessageBox.confirm(
      '对话内容及生成图片都将一并清空，是否确认清空？',
      '清空确认',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户点击取消
  }
  messages.value = []
  props.onClearImages?.()
}

// ===== 输入区拖拽调整高度 =====
const inputAreaHeight = ref(180) // 默认高度（px）
let isResizing = false
let startY = 0
let startHeight = 0

function startResize(e) {
  isResizing = true
  startY = e.clientY
  startHeight = inputAreaHeight.value
  document.body.style.cursor = 'row-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', stopResize)
}

function onResizeMove(e) {
  if (!isResizing) return
  const delta = startY - e.clientY // 向上拖增大
  const newH = Math.max(120, Math.min(500, startHeight + delta))
  inputAreaHeight.value = newH
}

function stopResize() {
  isResizing = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', stopResize)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', stopResize)
})

// ===== 供外部调用：将生成结果图推入对话框 =====
function addResultImages(images) {
  if (!images || !images.length) return
  messages.value.push({
    role: 'assistant',
    content: '生成完成',
    images: images.map(img => (typeof img === 'string' ? { url: img } : img)),
  })
  scrollToBottom()
}

defineExpose({ inputText, messages, addResultImages, selectedModel })
</script>

<style lang="scss" scoped>
.ai-assistant {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--gh-border-light);
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
}

.yuanbao-link {
  margin-left: 6px;
  font-size: 11px;
  color: var(--gh-primary, #2563FF);
  text-decoration: none;
  font-weight: normal;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: opacity 0.2s;
}
.yuanbao-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.ai-icon {
  color: var(--gh-primary);
}

.ai-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clear-link {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;

  &:hover {
    color: var(--gh-danger);
  }
}

.collapse-link {
  font-size: 16px;
  color: var(--gh-text-placeholder);
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
  &:hover {
    color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.06);
  }
}

.ai-collapsed-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: rgba(37, 99, 255, 0.03); }
  .collapsed-inner {
    text-align: center;
    p { font-size: 12px; color: #2563FF; margin-top: 8px; }
  }
}

// ========== 消息区 ==========
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.ai-welcome {
  text-align: center;
  padding: 24px 12px;

  .welcome-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(37, 99, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: var(--gh-primary);
  }

  .welcome-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--gh-text-primary);
    margin-bottom: 8px;
  }

  .welcome-desc {
    font-size: 13px;
    color: var(--gh-text-secondary);
    line-height: 1.6;
    margin-bottom: 20px;
  }
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-item {
  padding: 10px 14px;
  border-radius: var(--gh-radius-sm);
  border: 1px solid var(--gh-border);
  font-size: 12.5px;
  color: var(--gh-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;

  &:hover {
    border-color: var(--gh-primary);
    color: var(--gh-primary);
    background: rgba(37, 99, 255, 0.03);
  }
}

.msg-item {
  margin-bottom: 16px;

  &.user {
    .msg-bubble {
      flex-direction: row-reverse;
    }
    .msg-content {
      background: var(--gh-primary);
      color: #fff;
      border-radius: 16px 4px 16px 16px;
    }
  }

  &.assistant {
    .msg-content {
      background: var(--gh-bg-page);
      color: var(--gh-text-primary);
      border-radius: 4px 16px 16px 16px;
    }
  }
}

.msg-bubble {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(37, 99, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gh-primary);
  flex-shrink: 0;
  font-size: 14px;
}

.msg-content {
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  max-width: 85%;
  word-break: break-word;
}

.msg-images {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-top: 8px;
}

.msg-image-item {
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;

  .el-image {
    width: 100%;
    height: 100%;
    min-height: 80px;
    border-radius: 8px;
  }
}

.msg-content.typing {
    display: flex;
    gap: 4px;
    padding: 14px 18px;

    span {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--gh-text-placeholder);
      animation: typing 1.4s infinite;

      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
}

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}

// ========== 输入区 ==========
.ai-input-area {
  flex-shrink: 0;
  padding: 12px;
  border-top: 1px solid var(--gh-border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.input-resize-handle {
  height: 6px;
  margin: -6px -12px 8px;
  cursor: row-resize;
  background: transparent;
  border-top: 1px solid var(--gh-border-light);
  position: relative;
  transition: background 0.2s;

  &::after {
    content: '';
    position: absolute;
    top: 2px;
    left: 50%;
    transform: translateX(-50%);
    width: 36px;
    height: 3px;
    border-radius: 2px;
    background: #D1D5DB;
    transition: background 0.2s;
  }

  &:hover {
    background: rgba(37, 99, 255, 0.04);
    &::after { background: var(--gh-primary); }
  }
}

.points-note {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #FFFBEB;
  border-radius: var(--gh-radius-xs);

  strong {
    color: #D97706;
    font-weight: 600;
  }

  .el-icon {
    color: #D97706;
    font-size: 14px;
  }
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.el-textarea) {
    flex: 1;
    overflow: hidden;
  }
  :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 10px 14px;
    font-size: 13px;
    box-shadow: none;
    border: 1px solid var(--gh-border);
    height: 100% !important;
    resize: none;

    &:focus {
      border-color: var(--gh-primary);
    }
  }

  :deep(.el-textarea .el-input__count) {
    display: none;
  }
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.char-counter {
  font-size: 12px;
  color: var(--gh-text-placeholder);
}

.actions-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-select {
  width: 128px;
}

.send-btn {
  height: var(--gh-input-height);
  padding: 0 20px;
  border-radius: var(--gh-radius-btn);
  font-size: 13px;
  font-weight: 500;
}
</style>