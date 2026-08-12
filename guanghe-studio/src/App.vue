<template>
  <router-view />
  <SurveyDialog v-model="showSurveyDialog" @submitted="handleSurveySubmitted" @skipped="handleSurveySkipped" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store'
import { getSurveyStatus } from '@/api/customer'
import SurveyDialog from '@/components/SurveyDialog.vue'

const route = useRoute()
const userStore = useUserStore()
const showSurveyDialog = ref(false)

onMounted(async () => {
  // 如果用户已登录，检查是否需要显示问卷
  if (userStore.isLoggedIn && !userStore.isAdmin) {
    await checkSurveyStatus()
  }
})

async function checkSurveyStatus() {
  try {
    const res = await getSurveyStatus()
    const data = res.data || res
    
    // 需要显示问卷的条件：未完成问卷 && (登录次数>=5 || 图片生成次数>=10)
    if (data.needsSurvey && 
        (data.loginCount >= 5 || data.imageGenerateCount >= 10)) {
      showSurveyDialog.value = true
    }
  } catch (e) {
    console.error('检查问卷状态失败:', e)
  }
}

function handleSurveySubmitted() {
  console.log('用户已提交问卷')
  // 可以在这里添加一些后续逻辑
}

function handleSurveySkipped() {
  console.log('用户跳过问卷')
  // 可以在这里添加一些后续逻辑
}
</script>
