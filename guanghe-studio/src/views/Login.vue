<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-overlay"></div>
    </div>

    <div class="auth-card" :class="{ 'register-mode': isRegister }">
      <div class="card-header">
        <Logo size="lg" />
        <div class="card-title">{{ modeTitle }}</div>
        <p class="card-desc">{{ modeDescription }}</p>
      </div>

      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        <el-tab-pane label="账号登录" name="password" />
        <el-tab-pane label="短信登录" name="sms" />
        <el-tab-pane label="微信登录" name="wechat" />
        <el-tab-pane label="注册" name="register" />
        <el-tab-pane label="找回密码" name="reset" />
      </el-tabs>

      <el-form
        v-if="activeTab === 'password'"
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        @submit.prevent
      >
        <el-form-item prop="username">
          <el-input v-model="passwordForm.username" placeholder="请输入账号" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入密码" size="large" @keydown.enter="handlePasswordLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <template v-if="captchaEnabled">
          <el-form-item prop="code">
            <div class="code-row captcha-row">
              <el-input v-model="passwordForm.code" placeholder="请输入图形验证码" size="large" @keydown.enter="handlePasswordLogin">
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
              <img v-if="captchaImage" :src="captchaImage" class="captcha-image" alt="captcha" @click="fetchCaptcha" />
              <el-button v-else @click="fetchCaptcha" size="large" class="captcha-btn">获取验证码</el-button>
            </div>
          </el-form-item>
        </template>
      </el-form>

      <el-form
        v-else-if="activeTab === 'sms'"
        :model="phoneForm"
        :rules="phoneRules"
        ref="phoneFormRef"
        @submit.prevent
      >
        <el-form-item prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入手机号" size="large">
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="phoneForm.code" placeholder="验证码" size="large" @keydown.enter="handleSmsLogin" />
            <el-button :disabled="countdown > 0" @click="sendCode" size="large" class="code-btn">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <el-form
        v-else-if="activeTab === 'register'"
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        @submit.prevent
      >
        <!-- 注册信息收集（可折叠） -->
        <el-collapse v-model="registerInfoCollapse" class="register-info-collapse">
          <el-collapse-item title="基本信息（必填）" name="registerInfo">
            <!-- 岗位 -->
            <el-form-item label="岗位" prop="position" required>
              <el-select v-model="registerForm.position" placeholder="请选择岗位" size="large" style="width: 100%;">
                <el-option label="老板/负责人" value="老板/负责人" />
                <el-option label="运营" value="运营" />
                <el-option label="设计师" value="设计师" />
                <el-option label="美工" value="美工" />
                <el-option label="摄影" value="摄影" />
                <el-option label="采购" value="采购" />
                <el-option label="其他" value="其他" />
              </el-select>
              <el-input
                v-if="registerForm.position === '其他'"
                v-model="registerForm.positionOther"
                placeholder="请说明您的岗位"
                maxlength="100"
                style="margin-top: 8px;"
              />
            </el-form-item>

            <!-- 主营平台 -->
            <el-form-item label="主营平台" prop="platforms" required>
              <el-select
                v-model="registerForm.platforms"
                multiple
                placeholder="请选择主营平台"
                size="large"
                style="width: 100%;"
              >
                <el-option label="Amazon" value="Amazon" />
                <el-option label="Wayfair" value="Wayfair" />
                <el-option label="Walmart" value="Walmart" />
                <el-option label="Shopify" value="Shopify" />
                <el-option label="TikTok Shop" value="TikTok Shop" />
                <el-option label="淘宝" value="淘宝" />
                <el-option label="天猫" value="天猫" />
                <el-option label="京东" value="京东" />
                <el-option label="拼多多" value="拼多多" />
                <el-option label="Shopee" value="Shopee" />
                <el-option label="Lazada" value="Lazada" />
                <el-option label="独立站" value="独立站" />
                <el-option label="其他" value="其他" />
              </el-select>
              <el-input
                v-if="registerForm.platforms.includes('其他')"
                v-model="registerForm.platformOther"
                placeholder="请说明其他平台"
                maxlength="100"
                style="margin-top: 8px;"
              />
            </el-form-item>

            <!-- 了解渠道 -->
            <el-form-item label="从哪里了解到我们" prop="channels" required>
              <el-select
                v-model="registerForm.channels"
                multiple
                placeholder="请选择了解渠道"
                size="large"
                style="width: 100%;"
              >
                <el-option label="抖音" value="抖音" />
                <el-option label="小红书" value="小红书" />
                <el-option label="微信" value="微信" />
                <el-option label="Google" value="Google" />
                <el-option label="百度" value="百度" />
                <el-option label="朋友推荐" value="朋友推荐" />
                <el-option label="展会" value="展会" />
                <el-option label="其他" value="其他" />
              </el-select>
              <el-input
                v-if="registerForm.channels.includes('其他')"
                v-model="registerForm.channelOther"
                placeholder="请说明其他渠道"
                maxlength="100"
                style="margin-top: 8px;"
              />
            </el-form-item>

            <!-- 使用目的 -->
            <el-form-item label="使用目的" prop="purposes" required>
              <el-select
                v-model="registerForm.purposes"
                multiple
                placeholder="请选择使用目的"
                size="large"
                style="width: 100%;"
              >
                <el-option label="白底图" value="白底图" />
                <el-option label="换背景" value="换背景" />
                <el-option label="产品精修" value="产品精修" />
                <el-option label="AI模特" value="AI模特" />
                <el-option label="主图" value="主图" />
                <el-option label="详情/A+" value="详情/A+" />
                <el-option label="Banner" value="Banner" />
                <el-option label="尺寸图" value="尺寸图" />
                <el-option label="批量生成" value="批量生成" />
              </el-select>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>

        <div class="form-divider"></div>

        <!-- 账号密码设置 -->
        <div class="account-section">
          <div class="section-title">账号信息</div>
          
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="请设置账号" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" show-password placeholder="请设置密码" size="large">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" size="large" @keydown.enter="handleRegister">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
        </div>
        
        <template v-if="captchaEnabled">
          <el-form-item prop="code">
            <div class="code-row captcha-row">
              <el-input v-model="registerForm.code" placeholder="请输入图形验证码" size="large" @keydown.enter="handleRegister">
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
              <img v-if="captchaImage" :src="captchaImage" class="captcha-image" alt="captcha" @click="fetchCaptcha" />
              <el-button v-else @click="fetchCaptcha" size="large" class="captcha-btn">获取验证码</el-button>
            </div>
          </el-form-item>
        </template>
      </el-form>

      <div v-else-if="activeTab === 'wechat'" class="wechat-panel">
        <div class="wechat-state-card">
          <div class="wechat-state-title">微信扫码登录</div>
          <div class="wechat-state-desc">
            当前为开发态最小闭环：先创建登录状态，再模拟扫码确认；若账号未绑定，将进入手机号绑定流程。
          </div>
          <div class="wechat-state-meta">State：{{ wechatState || '未创建' }}</div>
          <div class="wechat-state-meta">状态：{{ wechatStatusText }}</div>
          <div class="wechat-actions">
            <el-button size="large" @click="startWechatLogin" :loading="wechatLoading">创建登录状态</el-button>
            <el-button size="large" type="success" @click="confirmWechatLogin" :disabled="!wechatState || wechatLoading">模拟扫码确认</el-button>
          </div>
        </div>

        <el-form
          v-if="showWechatBind"
          :model="wechatBindForm"
          :rules="wechatBindRules"
          ref="wechatBindFormRef"
          class="wechat-bind-form"
          @submit.prevent
        >
          <el-form-item prop="phone">
            <el-input v-model="wechatBindForm.phone" placeholder="请输入已注册管理员手机号" size="large">
              <template #prefix><el-icon><Iphone /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="smsCode">
            <div class="code-row">
              <el-input v-model="wechatBindForm.smsCode" placeholder="短信验证码" size="large" @keydown.enter="handleWechatBind" />
              <el-button :disabled="wechatBindCountdown > 0" @click="sendWechatBindCode" size="large" class="code-btn">
                {{ wechatBindCountdown > 0 ? `${wechatBindCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <el-form
        v-else
        :model="resetForm"
        :rules="resetRules"
        ref="resetFormRef"
        @submit.prevent
      >
        <el-form-item prop="phone">
          <el-input v-model="resetForm.phone" placeholder="请输入注册手机号" size="large">
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="resetForm.code" placeholder="短信验证码" size="large" @keydown.enter="handleResetPassword" />
            <el-button :disabled="resetCountdown > 0" @click="sendResetCode" size="large" class="code-btn">
              {{ resetCountdown > 0 ? `${resetCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="请输入新密码" size="large">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" size="large" @keydown.enter="handleResetPassword">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
      </el-form>

      <div class="form-actions single-line">
        <el-checkbox v-model="rememberMe" class="remember-check">记住登录信息</el-checkbox>
      </div>

      <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
        {{ submitText }}
      </el-button>

      <div class="switch-links">
        <button type="button" class="link-btn" @click="switchTab('password')">账号登录</button>
        <button type="button" class="link-btn" @click="switchTab('sms')">短信登录</button>
        <button type="button" class="link-btn" @click="switchTab('wechat')">微信登录</button>
        <button type="button" class="link-btn" @click="switchTab('register')">立即注册</button>
        <button type="button" class="link-btn" @click="switchTab('reset')">找回密码</button>
      </div>

      <div class="capability-panel">
        <div class="capability-title">当前能力边界</div>
        <div class="capability-text">微信登录已接入开发态后端骨架；Google 登录当前仍没有对应后端接口，前端不做伪实现。</div>
      </div>

      <div class="login-footer">
        <span>登录即表示同意</span>
        <a href="#">《用户协议》</a>
        <span>和</span>
        <a href="#">《隐私政策》</a>
      </div>
    </div>

    <div class="copyright">
      Copyright &copy; 2024-2026 光合AI. All Rights Reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import Logo from '@/components/Logo.vue'
import { Iphone, Key, Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCaptchaImage } from '@/api/customer'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('password')
const isRegister = computed(() => activeTab.value === 'register')
const redirectPath = computed(() => route.query.redirect || '/whiteBg')

const passwordFormRef = ref(null)
const phoneFormRef = ref(null)
const registerFormRef = ref(null)
const resetFormRef = ref(null)
const wechatBindFormRef = ref(null)
const countdown = ref(0)
const resetCountdown = ref(0)
const wechatBindCountdown = ref(0)
const rememberMe = ref(true)
const captchaEnabled = ref(false)
const captchaImage = ref('')
const captchaUuid = ref('')
const wechatLoading = ref(false)
const wechatState = ref('')
const wechatStatus = ref('INIT')
const wechatPollTimer = ref(null)

const passwordForm = ref({ username: '', password: '', code: '' })
const phoneForm = ref({ phone: '', code: '' })
const registerForm = ref({ 
  username: '', 
  password: '', 
  confirmPassword: '', 
  code: '',
  position: '',
  positionOther: '',
  platforms: [],
  platformOther: '',
  channels: [],
  channelOther: '',
  purposes: []
})
const registerInfoCollapse = ref(['registerInfo'])
const resetForm = ref({ phone: '', code: '', newPassword: '', confirmPassword: '' })
const wechatBindForm = ref({ phone: '', smsCode: '' })

const modeTitle = computed(() => {
  if (activeTab.value === 'register') return '欢迎注册'
  return '欢迎登录'
})

const modeDescription = computed(() => {
  if (activeTab.value === 'password') return '支持账号密码登录，管理员会自动跳转后台管理系统'
  if (activeTab.value === 'sms') return '支持手机号短信验证码登录，普通用户自动进入光合AI工作台'
  if (activeTab.value === 'wechat') return '开发态微信扫码登录骨架，可验证扫码确认与手机号绑定登录流程'
  if (activeTab.value === 'reset') return '通过手机号短信验证码重置密码'
  return '注册成功后可直接使用账号密码登录'
})

const submitText = computed(() => {
  if (activeTab.value === 'register') return '注 册'
  if (activeTab.value === 'reset') return '重置密码'
  if (activeTab.value === 'wechat') return showWechatBind.value ? '绑定并登录' : '开始微信登录'
  return '登 录'
})

const showWechatBind = computed(() => wechatStatus.value === 'UNBOUND')

const wechatStatusText = computed(() => {
  if (wechatStatus.value === 'SUCCESS') return '已登录'
  if (wechatStatus.value === 'UNBOUND') return '未绑定管理员账号'
  if (wechatStatus.value === 'EXPIRED') return '登录状态已过期'
  return '等待扫码确认'
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const passwordRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  code: [
    {
      validator: (_rule, value, callback) => {
        if (!captchaEnabled.value) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请输入图形验证码'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const registerRules = {
  position: [
    { required: true, message: '请选择岗位', trigger: 'change' }
  ],
  platforms: [
    { type: 'array', required: true, message: '请选择主营平台', trigger: 'change' }
  ],
  channels: [
    { type: 'array', required: true, message: '请选择了解渠道', trigger: 'change' }
  ],
  purposes: [
    { type: 'array', required: true, message: '请选择使用目的', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能少于5位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  code: [
    {
      validator: (_rule, value, callback) => {
        if (!captchaEnabled.value) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请输入图形验证码'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const resetRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能少于5位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== resetForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const wechatBindRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' }
  ]
}

let timer = null
let resetTimer = null
let wechatBindTimer = null

watch(activeTab, () => {
  if (captchaEnabled.value) {
    fetchCaptcha()
  }
  if (activeTab.value !== 'wechat') {
    stopWechatPolling()
  }
})

watch(wechatStatus, async (status) => {
  if (status !== 'SUCCESS') return
  stopWechatPolling()
  ElMessage.success('微信登录成功')
  const target = userStore.isAdmin ? '/admin/dashboard' : redirectPath.value
  const nextPath = target && target !== '/login' ? target : '/whiteBg'
  await router.replace(nextPath)
})

async function fetchCaptcha() {
  try {
    const res = await getCaptchaImage()
    const data = res.data || res
    captchaEnabled.value = !!data.captchaEnabled
    captchaUuid.value = data.uuid || ''
    captchaImage.value = data.img ? `data:image/gif;base64,${data.img}` : ''
    if (!captchaEnabled.value) {
      captchaUuid.value = ''
      captchaImage.value = ''
    }
  } catch (e) {
    captchaEnabled.value = false
    captchaUuid.value = ''
    captchaImage.value = ''
    ElMessage.error(e.message || '验证码加载失败')
  }
}

async function sendCode() {
  const valid = await phoneFormRef.value?.validateField('phone').catch(() => false)
  if (!valid) return
  try {
    await userStore.sendSmsCode(phoneForm.value.phone)
    countdown.value = 60
    clearInterval(timer)
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
    ElMessage.success('验证码已发送')
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败')
  }
}

async function sendResetCode() {
  const valid = await resetFormRef.value?.validateField('phone').catch(() => false)
  if (!valid) return
  try {
    await userStore.sendResetPasswordCode(resetForm.value.phone)
    resetCountdown.value = 60
    clearInterval(resetTimer)
    resetTimer = setInterval(() => {
      resetCountdown.value--
      if (resetCountdown.value <= 0) clearInterval(resetTimer)
    }, 1000)
    ElMessage.success('重置验证码已发送')
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败')
  }
}

async function sendWechatBindCode() {
  const valid = await wechatBindFormRef.value?.validateField('phone').catch(() => false)
  if (!valid) return
  try {
    await userStore.sendSmsCode(wechatBindForm.value.phone)
    wechatBindCountdown.value = 60
    clearInterval(wechatBindTimer)
    wechatBindTimer = setInterval(() => {
      wechatBindCountdown.value--
      if (wechatBindCountdown.value <= 0) clearInterval(wechatBindTimer)
    }, 1000)
    ElMessage.success('绑定验证码已发送')
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败')
  }
}

function stopWechatPolling() {
  if (wechatPollTimer.value) {
    clearInterval(wechatPollTimer.value)
    wechatPollTimer.value = null
  }
}

async function pollWechatStatus() {
  if (!wechatState.value) return
  try {
    const res = await userStore.pollWechatLogin(wechatState.value)
    const status = res.status || 'INIT'
    wechatStatus.value = status
    if (status === 'SUCCESS' && res.token) {
      await userStore.loginWithToken(res.token)
    }
  } catch (e) {
    stopWechatPolling()
    ElMessage.error(e.message || '微信登录状态查询失败')
  }
}

async function startWechatLogin() {
  wechatLoading.value = true
  try {
    const res = await userStore.createWechatLoginState()
    wechatState.value = res.state || ''
    wechatStatus.value = 'INIT'
    wechatBindForm.value = { phone: '', smsCode: '' }
    stopWechatPolling()
    await pollWechatStatus()
    wechatPollTimer.value = setInterval(pollWechatStatus, 2000)
  } catch (e) {
    ElMessage.error(e.message || '创建微信登录状态失败')
  } finally {
    wechatLoading.value = false
  }
}

async function confirmWechatLogin() {
  if (!wechatState.value) return
  wechatLoading.value = true
  try {
    await userStore.mockConfirmWechatLogin(wechatState.value)
    await pollWechatStatus()
  } catch (e) {
    ElMessage.error(e.message || '模拟扫码确认失败')
  } finally {
    wechatLoading.value = false
  }
}

async function handleWechatBind() {
  const valid = await wechatBindFormRef.value?.validate().catch(() => false)
  if (!valid || !wechatState.value) return

  loading.value = true
  try {
    await userStore.bindWechatLogin({
      state: wechatState.value,
      phone: wechatBindForm.value.phone,
      smsCode: wechatBindForm.value.smsCode
    })
    wechatStatus.value = 'SUCCESS'
  } catch (e) {
    ElMessage.error(e.message || '绑定登录失败')
  } finally {
    loading.value = false
  }
}

async function handlePasswordLogin() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.loginByPassword(
      passwordForm.value.username,
      passwordForm.value.password,
      passwordForm.value.code,
      captchaUuid.value
    )
    ElMessage.success('登录成功')
    const target = userStore.isAdmin ? '/admin/dashboard' : redirectPath.value
    const nextPath = target && target !== '/login' ? target : '/whiteBg'
    await router.replace(nextPath)
  } catch (e) {
    await fetchCaptcha()
    passwordForm.value.code = ''
    ElMessage.error(e.message || '登录失败，请检查账号信息')
  } finally {
    loading.value = false
  }
}

async function handleSmsLogin() {
  const valid = await phoneFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.loginBySms(phoneForm.value.phone, phoneForm.value.code)
    ElMessage.success('登录成功')
    const target = userStore.isAdmin ? '/admin/dashboard' : redirectPath.value
    const nextPath = target && target !== '/login' ? target : '/whiteBg'
    await router.replace(nextPath)
  } catch (e) {
    ElMessage.error(e.message || '登录失败，请检查手机号和验证码')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = {
      username: registerForm.value.username,
      password: registerForm.value.password,
      code: registerForm.value.code,
      uuid: captchaUuid.value
    }
    await userStore.register(payload)
    
    // 提交注册信息
    try {
      const { submitRegisterInfo } = await import('@/api/customer')
      await submitRegisterInfo({
        position: registerForm.value.position,
        positionOther: registerForm.value.positionOther,
        platforms: JSON.stringify(registerForm.value.platforms),
        platformOther: registerForm.value.platformOther,
        channels: JSON.stringify(registerForm.value.channels),
        channelOther: registerForm.value.channelOther,
        purposes: JSON.stringify(registerForm.value.purposes)
      })
    } catch (infoError) {
      console.error('提交注册信息失败:', infoError)
    }
    
    ElMessage.success('注册成功，请使用账号密码登录')
    activeTab.value = 'password'
    passwordForm.value.username = registerForm.value.username
    passwordForm.value.password = ''
    passwordForm.value.code = ''
    registerForm.value = { 
      username: '', 
      password: '', 
      confirmPassword: '', 
      code: '',
      position: '',
      positionOther: '',
      platforms: [],
      platformOther: '',
      channels: [],
      channelOther: '',
      purposes: []
    }
    await fetchCaptcha()
  } catch (e) {
    await fetchCaptcha()
    registerForm.value.code = ''
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

async function handleResetPassword() {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.resetPassword({
      phone: resetForm.value.phone,
      code: resetForm.value.code,
      newPassword: resetForm.value.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    activeTab.value = 'password'
    passwordForm.value.username = resetForm.value.phone
    passwordForm.value.password = ''
    passwordForm.value.code = ''
    resetForm.value = { phone: '', code: '', newPassword: '', confirmPassword: '' }
    await fetchCaptcha()
  } catch (e) {
    ElMessage.error(e.message || '密码重置失败')
  } finally {
    loading.value = false
  }
}

function handleSubmit() {
  if (activeTab.value === 'password') {
    handlePasswordLogin()
    return
  }
  if (activeTab.value === 'sms') {
    handleSmsLogin()
    return
  }
  if (activeTab.value === 'wechat') {
    if (showWechatBind.value) {
      handleWechatBind()
    } else {
      startWechatLogin()
    }
    return
  }
  if (activeTab.value === 'reset') {
    handleResetPassword()
    return
  }
  handleRegister()
}

function switchTab(tab) {
  activeTab.value = tab
}

fetchCaptcha()

onBeforeUnmount(() => {
  clearInterval(timer)
  clearInterval(resetTimer)
  clearInterval(wechatBindTimer)
  stopWechatPolling()
})
</script>

<style lang="scss" scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 440px;
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  padding: 36px 40px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;

  &.register-mode {
    max-height: 90vh;
  }

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--gh-border, #E5E7EB);
    border-radius: 4px;
  }
}

.register-info-collapse {
  margin-bottom: 16px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;

  :deep(.el-collapse-item__header) {
    background: #F8FAFC;
    padding: 0 16px;
    font-size: 14px;
    font-weight: 600;
    color: #334155;
  }

  :deep(.el-collapse-item__content) {
    padding: 16px;
  }

  .el-form-item {
    margin-bottom: 16px;
  }
}

.form-divider {
  height: 1px;
  background: #E2E8F0;
  margin: 20px 0;
}

.account-section {
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #334155;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #E2E8F0;
  }
}

.card-header {
  text-align: center;
  margin-bottom: 24px;

  :deep(.gh-logo) {
    justify-content: center;
  }
}

.card-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--gh-text-secondary, #6B7280);
}

.auth-tabs {
  margin-bottom: 20px;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input { flex: 1; }
  .code-btn { flex-shrink: 0; width: 120px; }
}

.captcha-row {
  align-items: center;
}

.captcha-image,
.captcha-btn {
  width: 120px;
  height: 40px;
  border-radius: 10px;
  flex-shrink: 0;
}

.captcha-image {
  object-fit: cover;
  border: 1px solid var(--gh-border, #E5E7EB);
  cursor: pointer;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.form-actions.single-line {
  justify-content: flex-start;
}

.remember-check {
  :deep(.el-checkbox__label) {
    color: var(--gh-text-secondary);
    font-size: 13px;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  letter-spacing: 4px;
}

.switch-links {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
}

.wechat-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wechat-state-card {
  padding: 16px;
  border-radius: 12px;
  background: #F0FDF4;
  border: 1px solid #BBF7D0;
}

.wechat-state-title {
  font-size: 15px;
  font-weight: 600;
  color: #166534;
}

.wechat-state-desc,
.wechat-state-meta {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #166534;
}

.wechat-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.wechat-bind-form {
  margin-top: 4px;
}

.link-btn {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--gh-primary, #2563FF);
  font-size: 13px;
  cursor: pointer;
}

.capability-panel {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
}

.capability-title {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.capability-text {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--gh-text-secondary, #6B7280);
}

.login-footer {
  text-align: center;
  font-size: 12px;
  color: var(--gh-text-placeholder, #9CA3AF);
  margin-top: 12px;

  a {
    color: var(--gh-primary, #2563FF);
    text-decoration: none;
  }
}

.copyright {
  position: fixed;
  bottom: 16px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  z-index: 1;
}
</style>
