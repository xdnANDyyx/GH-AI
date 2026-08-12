import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/styles/global.scss'

const app = createApp(App)
const pinia = createPinia()

// 全局错误处理：防止组件错误导致页面崩溃刷新
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', info, err)
}

window.addEventListener('unhandledrejection', (event) => {
  console.warn('[Unhandled Promise]', event.reason)
  event.preventDefault()
})

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
