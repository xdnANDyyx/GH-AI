import axios from 'axios'
import { useUserStore } from '@/store'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000
})

api.interceptors.request.use(config => {
  const userStore = useUserStore()
  const token = typeof userStore.token === 'string' ? userStore.token : userStore.token?.value
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    if (response.config?.responseType === 'blob' || response.data instanceof Blob) {
      return response.data
    }
    const res = response.data
    if (res.code === 200) return res
    if (res.code === 401) {
      // 只在真正的token失效时才退出登录，避免权限问题误触发
      const errorMsg = res.msg || '登录已过期，请重新登录'
      const isTokenExpired = errorMsg.includes('过期') || errorMsg.includes('失效') || errorMsg.includes('Token')
      
      if (isTokenExpired) {
        const store = useUserStore()
        store.logoutLocal()
        if (window.location.pathname !== '/login') {
          const redirect = `${window.location.pathname}${window.location.search || ''}`
          window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
        }
      } else {
        // 权限不足等401错误，只提示不退出登录
        console.warn('API权限错误:', errorMsg)
      }
      return Promise.reject(new Error(errorMsg))
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    // 处理网络错误或服务器错误
    if (error.response?.status === 401) {
      const errorMsg = error.response.data?.msg || '登录已过期，请重新登录'
      // 与响应拦截器保持一致：只在真正的token失效时才退出登录，避免积分不足等业务错误误触发
      const isTokenExpired = errorMsg.includes('过期') || errorMsg.includes('失效') || errorMsg.includes('Token') || errorMsg.includes('登录') || errorMsg.includes('未认证')
      if (isTokenExpired) {
        const store = useUserStore()
        store.logoutLocal()
        if (window.location.pathname !== '/login') {
          const redirect = `${window.location.pathname}${window.location.search || ''}`
          window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
        }
      } else {
        // 积分不足等业务错误，只提示不退出登录
        console.warn('API业务错误:', errorMsg)
      }
      return Promise.reject(new Error(errorMsg))
    }
    return Promise.reject(error)
  }
)

export default api
