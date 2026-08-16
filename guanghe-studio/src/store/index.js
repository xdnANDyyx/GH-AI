import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const DEV_MODE = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

let apiModule = null
async function getApi() {
  if (!apiModule) {
    apiModule = await import('@/api/customer')
  }
  return apiModule
}

function unwrapPayload(response) {
  return response?.data ?? response
}

function createDefaultUserInfo() {
  return {
    userId: null,
    nickname: '设计师',
    avatar: '',
    points: 0,
    phone: '',
    email: '',
    vipLevel: 0,
    role: '',
    roles: [],
    platform: '',
    source: '',
    purposes: [],
    loginType: '',
    authScope: 'customer',
    userName: '',
    inviteCode: '',
    status: '',
    userType: ''
  }
}

function normalizeUserInfo(data = {}, previous = createDefaultUserInfo()) {
  const roles = Array.isArray(data.roles) ? data.roles : (previous.roles || [])
  const explicitRole = data.role || previous.role || ''
  const userType = data.userType || previous.userType || ''
  const isAdminUser = data.isAdmin === true || explicitRole === 'admin' || userType === 'admin' || userType === '1' || roles.includes('admin') || roles.includes('ROLE_ADMIN') || data.userName === 'admin'

  return {
    ...previous,
    userId: data.userId ?? previous.userId,
    nickname: data.nickname || data.nickName || data.userName || previous.nickname || '设计师',
    avatar: data.avatar || previous.avatar || '',
    points: typeof data.points === 'number' ? data.points : (previous.points || 0),
    phone: data.phone || data.phonenumber || previous.phone || '',
    email: data.email || previous.email || '',
    vipLevel: typeof data.vipLevel === 'number' ? data.vipLevel : (previous.vipLevel || 0),
    role: isAdminUser ? 'admin' : (explicitRole || previous.role || 'user'),
    roles: isAdminUser ? Array.from(new Set([...roles, 'admin'])) : roles,
    platform: data.platform || previous.platform || '',
    source: data.source || previous.source || '',
    purposes: Array.isArray(data.purposes) ? data.purposes : (previous.purposes || []),
    loginType: data.loginType || previous.loginType || '',
    authScope: data.authScope || previous.authScope || 'customer',
    userName: data.userName || previous.userName || '',
    inviteCode: data.inviteCode || previous.inviteCode || '',
    status: data.status || previous.status || '',
    userType: isAdminUser ? 'admin' : userType
  }
}

function resolveAuthScope(user = {}, roles = [], previousScope = 'customer') {
  const userType = user.userType || ''
  const isAdminUser = user.userName === 'admin' || userType === '1' || userType === 'admin' || roles.includes('admin') || roles.includes('ROLE_ADMIN')
  return isAdminUser ? 'system' : 'customer'
}

function unwrapUserInfoResponse(data = {}) {
  const nestedUser = data.user || {}
  const roles = Array.isArray(data.roles) ? data.roles : []

  return {
    userId: data.userId ?? nestedUser.userId ?? null,
    nickName: data.nickName || nestedUser.nickName || '',
    userName: data.userName || nestedUser.userName || '',
    avatar: data.avatar || nestedUser.avatar || '',
    phonenumber: data.phonenumber || nestedUser.phonenumber || '',
    email: data.email || nestedUser.email || '',
    inviteCode: data.inviteCode || nestedUser.inviteCode || '',
    status: data.status || nestedUser.status || '',
    userType: data.userType || nestedUser.userType || '',
    role: data.role || '',
    roles,
    permissions: Array.isArray(data.permissions) ? data.permissions : data.permissions || []
  }
}

function isNotFoundLike(error) {
  const message = error?.message || ''
  return message.includes('No static resource') || message.includes('404')
}

function isLocalToken(token) {
  return token?.startsWith('mock-token-') || token?.startsWith('dev-token-')
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('gh_token') || '')
  const storedUserInfo = localStorage.getItem('gh_user_info')
  const userInfo = ref(createDefaultUserInfo())

  try {
    const parsed = storedUserInfo ? JSON.parse(storedUserInfo) : null
    if (parsed) {
      userInfo.value = normalizeUserInfo(parsed, createDefaultUserInfo())
    }
  } catch {
    localStorage.removeItem('gh_user_info')
  }

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => {
    const roles = userInfo.value.roles || []
    const role = userInfo.value.role || ''
    const userType = userInfo.value.userType || ''
    return roles.includes('admin')
      || roles.includes('ROLE_ADMIN')
      || role === 'admin'
      || role === 'ROLE_ADMIN'
      || userType === 'admin'
      || userType === '1'
      || userInfo.value.userName === 'admin'
  })

  function setToken(t) {
    token.value = t
    localStorage.setItem('gh_token', t)
  }

  function setUserInfo(info) {
    userInfo.value = normalizeUserInfo(info, userInfo.value)
  }

  watch(userInfo, (value) => {
    localStorage.setItem('gh_user_info', JSON.stringify(value))
  }, { deep: true })

  function deductPoints(amount) {
    userInfo.value.points = Math.max(0, (userInfo.value.points || 0) - amount)
  }

  function addPoints(amount) {
    userInfo.value.points = (userInfo.value.points || 0) + amount
  }

  async function sendSmsCode(phone) {
    if (DEV_MODE) return
    const api = await getApi()
    await api.sendSystemSmsCode(phone)
  }

  async function loginBySms(phone, code) {
    if (DEV_MODE) {
      setToken('dev-token-' + Date.now())
      userInfo.value = createDefaultUserInfo()
      setUserInfo({ nickname: '光合设计师', phone, role: 'user', roles: ['common'], points: 500, loginType: 'sms', authScope: 'system', userName: phone })
      return { token: token.value }
    }
    const api = await getApi()
    const res = await api.loginSystemBySms(phone, code)
    const loginData = unwrapPayload(res)
    setToken(loginData.token)
    userInfo.value = createDefaultUserInfo()
    setUserInfo({ authScope: 'system', loginType: 'sms', points: 0 })
    await fetchUserInfo()
    if (!isAdmin.value) {
      await fetchPoints()
    }
    return loginData
  }

  async function loginByPassword(username, password, code, uuid) {
    if (DEV_MODE) {
      setToken('dev-token-' + Date.now())
      const adminUser = username === 'admin'
      userInfo.value = createDefaultUserInfo()
      setUserInfo({
        nickname: adminUser ? '系统管理员' : username,
        userName: username,
        role: adminUser ? 'admin' : 'user',
        roles: adminUser ? ['admin'] : ['common'],
        points: adminUser ? 0 : 500,
        loginType: 'password',
        authScope: 'system',
        userType: adminUser ? 'admin' : 'user'
      })
      return { token: token.value }
    }
    const api = await getApi()
    const res = await api.loginByPassword(username, password, code, uuid)
    const loginData = unwrapPayload(res)
    setToken(loginData.token)
    userInfo.value = createDefaultUserInfo()
    setUserInfo({
      authScope: username === 'admin' ? 'system' : 'customer',
      loginType: 'password',
      userName: username,
      points: 0
    })
    await fetchUserInfo()
    if (!isAdmin.value) {
      await fetchPoints()
    }
    return loginData
  }

  async function register(registerForm) {
    if (DEV_MODE) {
      return { msg: '注册成功' }
    }
    const api = await getApi()
    const res = await api.registerAccount(registerForm)
    return res.data || res
  }

  async function sendResetPasswordCode(phone) {
    if (DEV_MODE) return
    const api = await getApi()
    await api.sendResetPasswordCode(phone)
  }

  async function resetPassword(resetForm) {
    if (DEV_MODE) {
      return { msg: '密码重置成功' }
    }
    const api = await getApi()
    const res = await api.resetPassword(resetForm)
    return res.data || res
  }

  async function createWechatLoginState() {
    if (DEV_MODE) {
      return { state: 'dev-wechat-state-' + Date.now() }
    }
    const api = await getApi()
    const res = await api.createWechatLoginState()
    return res.data || res
  }

  async function pollWechatLogin(state) {
    if (DEV_MODE) {
      return { status: 'INIT' }
    }
    const api = await getApi()
    const res = await api.pollWechatLogin(state)
    return res.data || res
  }

  async function mockConfirmWechatLogin(state, unionId) {
    if (DEV_MODE) {
      return { msg: 'ok' }
    }
    const api = await getApi()
    const res = await api.mockConfirmWechatLogin(state, unionId)
    return res.data || res
  }

  async function bindWechatLogin(bindForm) {
    if (DEV_MODE) {
      setToken('dev-token-' + Date.now())
      setUserInfo({
        nickname: bindForm.phone,
        phone: bindForm.phone,
        role: 'user',
        roles: ['common'],
        points: 500,
        loginType: 'wechat',
        authScope: 'system',
        userName: bindForm.phone,
        userType: 'admin'
      })
      return { token: token.value }
    }
    const api = await getApi()
    const res = await api.bindWechatLogin(bindForm)
    const loginData = unwrapPayload(res)
    setToken(loginData.token)
    userInfo.value = createDefaultUserInfo()
    setUserInfo({ authScope: 'system', loginType: 'wechat', points: 0 })
    await fetchUserInfo()
    if (!isAdmin.value) {
      await fetchPoints()
    }
    return loginData
  }

  async function fetchUserInfo() {
    if (!token.value) return null
    if (DEV_MODE && isLocalToken(token.value)) return userInfo.value
    try {
      const api = await getApi()
      const authScope = userInfo.value.authScope || 'customer'
      let res
      if (authScope === 'system') {
        res = await api.getAdminInfo()
      } else {
        try {
          res = await api.getInfo()
        } catch (error) {
          if (!isNotFoundLike(error)) {
            throw error
          }
          res = await api.getAdminInfo()
        }
      }
      const data = unwrapUserInfoResponse(unwrapPayload(res))
      const nextAuthScope = resolveAuthScope(data, data.roles || [], authScope)
      setUserInfo({
        userId: data.userId,
        nickname: data.nickName,
        userName: data.userName,
        avatar: data.avatar || '',
        phone: data.phonenumber || '',
        email: data.email || '',
        inviteCode: data.inviteCode || '',
        status: data.status || '',
        userType: data.userType || '',
        role: data.role || '',
        roles: data.roles || [],
        loginType: userInfo.value.loginType || 'api',
        authScope: nextAuthScope
      })
      return data
    } catch (e) {
      console.warn('获取用户信息失败:', e.message)
      throw e
    }
  }

  async function fetchPoints() {
    if (!token.value) return 0
    if (userInfo.value.userType === '1' || userInfo.value.userName === 'admin') return userInfo.value.points || 0
    if (DEV_MODE && isLocalToken(token.value)) return userInfo.value.points || 0
    try {
      const api = await getApi()
      const res = await api.getRemainingPoints()
      const pts = unwrapPayload(res)
      userInfo.value.points = typeof pts === 'number' ? pts : 0
      return userInfo.value.points
    } catch (e) {
      console.warn('获取积分失败:', e?.message)
      return userInfo.value.points || 0
    }
  }

  async function loginWithToken(t) {
    setToken(t)
    if (DEV_MODE && isLocalToken(t)) {
      userInfo.value = createDefaultUserInfo()
      setUserInfo({ role: 'user', roles: ['common'], loginType: 'ruoyi', points: 0 })
      return
    }
    try {
      await fetchUserInfo()
      if (!isAdmin.value && userInfo.value.authScope !== 'system') {
        await fetchPoints()
      }
    } catch {
      logoutLocal()
      throw new Error('登录状态已失效')
    }
  }

  async function logout() {
    if (!DEV_MODE && token.value && !isLocalToken(token.value)) {
      try {
        const api = await getApi()
        await api.logoutApi()
      } catch { }
    }
    logoutLocal()
  }

  function logoutLocal() {
    token.value = ''
    localStorage.removeItem('gh_token')
    localStorage.removeItem('gh_user_info')
    userInfo.value = createDefaultUserInfo()
  }

  return {
    token, userInfo, isLoggedIn, isAdmin,
    setToken, setUserInfo,
    deductPoints, addPoints,
    sendSmsCode, loginBySms, fetchUserInfo, fetchPoints, loginWithToken,
    loginByPassword, register, sendResetPasswordCode, resetPassword,
    createWechatLoginState, pollWechatLogin, mockConfirmWechatLogin, bindWechatLogin,
    logout, logoutLocal
  }
})

/**
 * 图片接力 Store
 * 跨页面传递一张图片，供目标工作台消费：
 *   - 白底图结果 → 右键「放入白底生成背景 / 产品精修」
 *   - 背景生成结果 → 右键「放入产品精修」
 * setImage(url, meta) 写入；consume() 读取并清除（一次性）。
 * 目标工作台在 onActivated / onMounted 里调用 consume()，并把图片作为输入接入。
 */
export const useImageHandoffStore = defineStore('imageHandoff', () => {
  const pendingImage = ref(null) // { url, from, to, timestamp }

  function setImage(url, meta = {}) {
    if (!url) return
    pendingImage.value = { url, ...meta, timestamp: Date.now() }
  }

  function consume() {
    const img = pendingImage.value
    pendingImage.value = null
    return img
  }

  function clear() {
    pendingImage.value = null
  }

  return { pendingImage, setImage, consume, clear }
})
