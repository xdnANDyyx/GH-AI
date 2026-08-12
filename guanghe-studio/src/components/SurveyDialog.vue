<template>
  <el-dialog
    v-model="visible"
    title="用户调研"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    center
  >
    <div class="survey-container">
      <p class="survey-desc">为了更好地为您提供服务，请填写以下问卷（约1分钟）</p>

      <el-form :model="form" label-width="140px" label-position="right">
        <!-- 公司名称（选填） -->
        <el-form-item label="公司名称">
          <el-input
            v-model="form.companyName"
            placeholder="请输入公司名称（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 公司规模 -->
        <el-form-item label="公司规模" required>
          <el-radio-group v-model="form.companyScale">
            <el-radio label="1-20人">1-20人</el-radio>
            <el-radio label="20-50人">20-50人</el-radio>
            <el-radio label="50-100人">50-100人</el-radio>
            <el-radio label="100人以上">100人以上</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 每月上新产品数量 -->
        <el-form-item label="每月上新产品数量" required>
          <el-radio-group v-model="form.monthlyProducts">
            <el-radio label="1-20">1-20</el-radio>
            <el-radio label="20-50">20-50</el-radio>
            <el-radio label="50-100">50-100</el-radio>
            <el-radio label="100+">100+</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 每月制作图片数量 -->
        <el-form-item label="每月制作图片数量" required>
          <el-radio-group v-model="form.monthlyImages">
            <el-radio label="100张以内">100张以内</el-radio>
            <el-radio label="100-500张">100-500张</el-radio>
            <el-radio label="500-2000张">500-2000张</el-radio>
            <el-radio label="2000+">2000+</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 最希望新增什么功能（多选） -->
        <el-form-item label="希望新增功能" required>
          <el-checkbox-group v-model="form.desiredFeatures">
            <el-checkbox label="视频生成">视频生成</el-checkbox>
            <el-checkbox label="爆款分析">爆款分析</el-checkbox>
            <el-checkbox label="AI文案">AI文案</el-checkbox>
            <el-checkbox label="自动排版">自动排版</el-checkbox>
            <el-checkbox label="品牌模板">品牌模板</el-checkbox>
            <el-checkbox label="一键翻译">一键翻译</el-checkbox>
            <el-checkbox label="团队协作">团队协作</el-checkbox>
            <el-checkbox label="API接口">API接口</el-checkbox>
            <el-checkbox label="PSD导出">PSD导出</el-checkbox>
          </el-checkbox-group>
          <div class="other-feature">
            <el-input
              v-model="form.otherFeature"
              placeholder="其他功能（选填）"
              maxlength="200"
              show-word-limit
            />
          </div>
        </el-form-item>

        <!-- 联系方式（选填） -->
        <el-form-item label="联系方式">
          <el-input
            v-model="form.contactInfo"
            placeholder="请输入联系方式（选填）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleSkip">跳过</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitSurvey } from '@/api/customer'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'submitted', 'skipped'])

const visible = ref(props.modelValue)
const submitting = ref(false)

const form = reactive({
  companyName: '',
  companyScale: '',
  monthlyProducts: '',
  monthlyImages: '',
  desiredFeatures: [],
  otherFeature: '',
  contactInfo: ''
})

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function handleSubmit() {
  // 验证必填项
  if (!form.companyScale) {
    ElMessage.warning('请选择公司规模')
    return
  }
  if (!form.monthlyProducts) {
    ElMessage.warning('请选择每月上新产品数量')
    return
  }
  if (!form.monthlyImages) {
    ElMessage.warning('请选择每月制作图片数量')
    return
  }
  if (form.desiredFeatures.length === 0) {
    ElMessage.warning('请选择希望新增的功能')
    return
  }

  submitting.value = true
  try {
    // 合并其他功能到desiredFeatures
    const features = [...form.desiredFeatures]
    if (form.otherFeature) {
      features.push(form.otherFeature)
    }

    await submitSurvey({
      companyName: form.companyName,
      companyScale: form.companyScale,
      monthlyProducts: form.monthlyProducts,
      monthlyImages: form.monthlyImages,
      desiredFeatures: JSON.stringify(features),
      contactInfo: form.contactInfo
    })

    ElMessage.success('提交成功，感谢您的反馈！')
    emit('submitted')
    visible.value = false
  } catch (e) {
    ElMessage.error(e.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function handleSkip() {
  visible.value = false
  emit('skipped')
}
</script>

<style lang="scss" scoped>
.survey-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--gh-border, #E5E7EB);
    border-radius: 4px;
  }
}

.survey-desc {
  text-align: center;
  color: var(--gh-text-secondary, #6B7280);
  font-size: 14px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--gh-border, #E5E7EB);
}

.el-form-item {
  margin-bottom: 20px;
}

.el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;

  .el-radio {
    margin-right: 0;
  }
}

.el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;

  .el-checkbox {
    margin-right: 0;
    min-width: 100px;
  }
}

.other-feature {
  margin-top: 12px;
  width: 100%;

  .el-input {
    width: 100%;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;

  .el-button {
    min-width: 120px;
  }
}
</style>