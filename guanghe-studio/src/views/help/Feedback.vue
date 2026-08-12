<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2 class="gh-page-title">意见反馈</h2>
        <p class="gh-page-subtitle">您的反馈是我们改进的动力</p>
      </div>
    </div>

    <div class="feedback-layout">
      <!-- Main Form -->
      <div class="feedback-form gh-card">
        <!-- Feedback Type -->
        <div class="form-group">
          <label class="form-label">反馈类型 <span class="required">*</span></label>
          <div class="type-grid">
            <div
              v-for="ft in feedbackTypes"
              :key="ft.value"
              class="type-card"
              :class="{ active: selectedType === ft.value }"
              @click="selectedType = ft.value"
            >
              <div class="type-icon" :style="{ background: ft.bg }">
                <el-icon :size="22" :style="{ color: ft.color }">
                  <component :is="ft.icon" />
                </el-icon>
              </div>
              <div class="type-name">{{ ft.label }}</div>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="form-group">
          <label class="form-label">详细描述 <span class="required">*</span></label>
          <el-input
            v-model="description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您遇到的问题或建议，包括操作步骤、预期结果和实际结果..."
            maxlength="1000"
            show-word-limit
            class="desc-textarea"
          />
        </div>

        <!-- Screenshot Upload -->
        <div class="form-group">
          <label class="form-label">截图附件 <span class="form-hint">(最多5张，支持 JPG/PNG 格式)</span></label>
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/jpeg,image/png"
            :on-exceed="handleExceed"
            class="screenshot-upload"
          >
            <el-icon :size="24"><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">点击上传截图，单张不超过 10MB</div>
            </template>
          </el-upload>
        </div>

        <!-- Contact Info -->
        <div class="form-group">
          <label class="form-label">联系方式 <span class="form-hint">(选填，方便我们与您联系)</span></label>
          <div class="contact-row">
            <el-input
              v-model="contactEmail"
              placeholder="邮箱地址"
              :prefix-icon="Message"
              clearable
              class="contact-input"
            />
            <el-input
              v-model="contactPhone"
              placeholder="手机号码"
              :prefix-icon="Phone"
              clearable
              class="contact-input"
            />
          </div>
        </div>

        <!-- Submit -->
        <div class="form-actions">
          <el-button type="primary" size="large" round :loading="submitting" @click="handleSubmit" class="submit-btn">
            <el-icon><Promotion /></el-icon>
            提交反馈
          </el-button>
          <el-button size="large" round @click="resetForm">重置</el-button>
        </div>
      </div>

      <!-- Side Tips -->
      <div class="feedback-tips">
        <div class="tips-card gh-card">
          <div class="tips-title">
            <el-icon color="#2563FF"><InfoFilled /></el-icon>
            提交须知
          </div>
          <ul class="tips-list">
            <li>请尽量详细描述问题，方便我们快速定位</li>
            <li>附上截图能帮助我们更准确地理解问题</li>
            <li>反馈提交后，我们将在 1-3 个工作日内回复</li>
            <li>紧急问题请联系在线客服获取即时帮助</li>
          </ul>
        </div>

        <div class="tips-card gh-card">
          <div class="tips-title">
            <el-icon color="#059669"><ChatLineSquare /></el-icon>
            其他方式
          </div>
          <div class="other-methods">
            <div class="method-item" @click="$router.push('/help/customer-support')">
              <el-icon><Service /></el-icon>
              <span>在线客服</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="method-item">
              <el-icon><Phone /></el-icon>
              <span>400-888-6688</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
            <div class="method-item">
              <el-icon><Message /></el-icon>
              <span>support@guanghe.ai</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Message,
  Phone,
  Promotion,
  InfoFilled,
  ChatLineSquare,
  Service,
  ArrowRight,
  EditPen,
  Warning,
  Brush,
  MoreFilled
} from '@element-plus/icons-vue'

const selectedType = ref('')
const description = ref('')
const contactEmail = ref('')
const contactPhone = ref('')
const fileList = ref([])
const submitting = ref(false)

const feedbackTypes = [
  { value: 'feature', label: '功能建议', icon: 'EditPen', bg: '#EFF6FF', color: '#2563FF' },
  { value: 'bug', label: 'Bug反馈', icon: 'Warning', bg: '#FEF2F2', color: '#DC2626' },
  { value: 'experience', label: '体验优化', icon: 'Brush', bg: '#FFF7ED', color: '#D97706' },
  { value: 'other', label: '其他', icon: 'MoreFilled', bg: '#F3E8FF', color: '#7C3AED' }
]

function handleExceed() {
  ElMessage.warning('最多上传5张截图')
}

async function handleSubmit() {
  if (!selectedType.value) {
    ElMessage.warning('请选择反馈类型')
    return
  }
  if (!description.value.trim()) {
    ElMessage.warning('请填写详细描述')
    return
  }
  submitting.value = true
  // Simulate submit
  await new Promise(resolve => setTimeout(resolve, 1500))
  submitting.value = false
  ElMessage.success('反馈提交成功，感谢您的宝贵意见！')
  resetForm()
}

function resetForm() {
  selectedType.value = ''
  description.value = ''
  contactEmail.value = ''
  contactPhone.value = ''
  fileList.value = []
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

.feedback-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Form Card */
.feedback-form {
  flex: 1;
  padding: 28px;
  min-width: 0;
}

.form-group {
  margin-bottom: 28px;

  &:last-of-type {
    margin-bottom: 20px;
  }
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 12px;
}

.required {
  color: #DC2626;
}

.form-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--gh-text-placeholder);
}

/* Type Selection */
.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  border: 2px solid var(--gh-border-light);
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--gh-primary);
    background: #FAFBFF;
  }

  &.active {
    border-color: var(--gh-primary);
    background: #EFF6FF;
    box-shadow: 0 0 0 1px var(--gh-primary);
  }
}

.type-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--gh-text-primary);
}

/* Textarea */
.desc-textarea {
  :deep(.el-textarea__inner) {
    border-radius: var(--gh-radius-sm);
    font-size: 13px;
    line-height: 1.7;
    resize: none;
  }
}

/* Upload */
.screenshot-upload {
  :deep(.el-upload--picture-card) {
    border-radius: var(--gh-radius-sm);
    border: 2px dashed var(--gh-border);
    width: 100px;
    height: 100px;

    &:hover {
      border-color: var(--gh-primary);
    }
  }

  :deep(.el-upload-list__item) {
    border-radius: var(--gh-radius-sm);
    width: 100px;
    height: 100px;
  }
}

.upload-tip {
  font-size: 12px;
  color: var(--gh-text-placeholder);
  margin-top: 8px;
}

/* Contact */
.contact-row {
  display: flex;
  gap: 12px;
}

.contact-input {
  flex: 1;

  :deep(.el-input__wrapper) {
    border-radius: var(--gh-radius-sm);
  }
}

/* Actions */
.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--gh-border-light);
}

.submit-btn {
  min-width: 140px;
}

/* Tips Sidebar */
.feedback-tips {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tips-card {
  padding: 20px;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--gh-text-primary);
  margin-bottom: 14px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    font-size: 12px;
    color: var(--gh-text-secondary);
    line-height: 1.7;
    padding: 4px 0;
    padding-left: 14px;
    position: relative;

    &::before {
      content: '';
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: var(--gh-text-placeholder);
      position: absolute;
      left: 0;
      top: 11px;
    }
  }
}

.other-methods {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 8px;
  border-radius: var(--gh-radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--gh-text-secondary);
  transition: all 0.2s;

  &:hover {
    background: var(--gh-bg-page);
    color: var(--gh-primary);
  }

  .arrow {
    margin-left: auto;
    font-size: 12px;
    color: var(--gh-text-placeholder);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .feedback-layout {
    flex-direction: column;
  }

  .feedback-tips {
    width: 100%;
  }

  .type-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .contact-row {
    flex-direction: column;
  }
}
</style>
