/**
 * useBatchTasks.js
 * 批量生成任务管理 composable（云端数据库持久化 + 临时本地状态混合）
 */
import { ref } from 'vue'
import { useUserStore } from '@/store'
import { listBatchTasks, deleteBatchTask, clearBatchTasks } from '@/api/customer'

function getStorageKey() {
  const userStore = useUserStore()
  const userId = userStore.userInfo?.userId || 'guest'
  return `gh_batch_tasks_${userId}`
}

function loadTasks() {
  try {
    const raw = localStorage.getItem(getStorageKey())
    if (!raw) return []
    return JSON.parse(raw)
  } catch {
    return []
  }
}

function cleanTaskForStorage(task) {
  if (!task) return task
  const clean = { ...task }
  if (clean.thumbs) {
    clean.thumbs = clean.thumbs.map(img => {
      if (img && img.url && img.url.startsWith('data:') && img.url.length > 2000) {
        // 如果是极长的 Base64 图片，将其移除以节省本地存储空间
        return { ...img, url: '' }
      }
      return img
    })
  }
  if (clean.resultImages) {
    clean.resultImages = clean.resultImages.map(img => {
      if (img && img.url && img.url.startsWith('data:') && img.url.length > 2000) {
        // 如果是极长的 Base64 图片，将其移除以节省本地存储空间
        return { ...img, url: '' }
      }
      return img
    })
  }
  return clean
}

function persist(list) {
  const key = getStorageKey()
  try {
    const cleanedList = list.map(cleanTaskForStorage)
    localStorage.setItem(key, JSON.stringify(cleanedList))
  } catch (e) {
    console.warn('保存任务到 localStorage 失败，可能超出了额度，正在尝试进一步压缩保存...', e)
    try {
      // 1. 更加激进的压缩：将已完成/失败任务的 resultImages/thumbs 彻底清空
      const compressedList = list.map(t => {
        const clean = { ...t }
        if (clean.status === 'done' || clean.status === 'failed') {
          clean.thumbs = []
          clean.resultImages = []
        }
        return cleanTaskForStorage(clean)
      })
      localStorage.setItem(key, JSON.stringify(compressedList))
    } catch (e2) {
      console.warn('压缩后依然失败，尝试仅保留最新 20 条任务...', e2)
      try {
        // 2. 仅保留最前面 20 条任务
        const minimalList = list.slice(0, 20).map(t => {
          const clean = { ...t }
          clean.thumbs = []
          clean.resultImages = []
          return clean
        })
        localStorage.setItem(key, JSON.stringify(minimalList))
      } catch (e3) {
        console.error('所有尝试均失败，清除本地任务缓存以防止程序崩溃:', e3)
        // 3. 彻底兜底：移除 key，防止应用报错崩溃
        localStorage.removeItem(key)
      }
    }
  }
}

export function useBatchTasks() {
  const tasks = ref(loadTasks())

  /** 生成新 ID */
  function _genId() {
    return 'bt_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
  }

  /** 格式化时间 */
  function _formatTime(ts) {
    const d = new Date(ts)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }

  /** 创建任务 */
  function createTask(data) {
    const now = Date.now()
    const task = {
      id: _genId(),
      name: data.name || '批量生成任务',
      designType: data.designType || '批量生成',
      materialCount: data.materialCount || '',
      genCount: data.genCount || 1,
      status: 'queued',          // queued | processing | done | failed | cancelled
      statusText: '排队中',
      statusClass: 'orange',
      progress: null,
      createdAt: _formatTime(now),
      createdAtTs: now,
      thumbs: data.thumbs || [],
      resultImages: [],
      prompt: data.prompt || '',
      productImages: data.productImages || [],
    }
    const list = [task, ...loadTasks()]
    if (list.length > 200) list.length = 200
    persist(list)
    tasks.value = list
    return task
  }

  /** 更新任务 */
  function updateTask(id, patch) {
    const list = loadTasks().map(t => {
      if (t.id !== id) return t
      const updated = { ...t, ...patch }
      if (patch.status) {
        const statusMap = {
          queued:     { statusText: '排队中',  statusClass: 'orange', progress: null },
          processing: { statusText: '生成中',  statusClass: 'blue',   progress: patch.progress ?? t.progress ?? 0 },
          done:       { statusText: '已完成',  statusClass: 'green',  progress: 100 },
          failed:     { statusText: '生成失败', statusClass: 'red',    progress: null },
          cancelled:  { statusText: '已取消',  statusClass: 'gray',   progress: null },
        }
        const meta = statusMap[patch.status]
        if (meta) {
          updated.statusText = meta.statusText
          updated.statusClass = meta.statusClass
          if (meta.progress !== undefined) updated.progress = meta.progress
          if (patch.status !== 'failed') {
            updated.errorMsg = undefined
          }
        }
      }
      return updated
    })
    persist(list)
    tasks.value = list
  }

  /** 更新进度 */
  function updateProgress(id, progress) {
    const list = loadTasks().map(t => {
      if (t.id !== id) return t
      if (t.status !== 'processing' && t.status !== 'queued') return t
      return { ...t, progress }
    })
    persist(list)
    tasks.value = list
  }

  /** 删除任务 */
  async function deleteTask(id) {
    // 1. 本地删除
    const list = loadTasks().filter(t => t.id !== id)
    persist(list)
    tasks.value = list
    
    // 2. 尝试云端同步删除
    try {
      await deleteBatchTask(id)
    } catch { /* 容错 */ }
    
    // 3. 重新获取云端最新记录
    await reloadTasks()
  }

  /** 清空全部 */
  async function clearAll() {
    // 1. 本地清空
    persist([])
    tasks.value = []
    
    // 2. 尝试云端同步清空
    try {
      await clearBatchTasks()
    } catch { /* 容错 */ }
    
    // 3. 重新获取云端最新记录
    await reloadTasks()
  }

  /** 获取单个任务 */
  function getTask(id) {
    return tasks.value.find(t => t.id === id)
  }

  /** 从云端数据库加载批量生图记录 */
  async function reloadTasks() {
    try {
      const res = await listBatchTasks()
      const serverTasks = res.data || res || []
      
      // 合并本地“正在处理”的临时状态，保证切换页面时流畅度
      const localTasks = loadTasks()
      const activeLocalTasks = localTasks.filter(t => t.status === 'processing' || t.status === 'queued')
      
      // 过滤掉服务器中已存在的记录（以防重复）
      const serverIds = new Set(serverTasks.map(t => t.id))
      const uniqueActiveLocal = activeLocalTasks.filter(t => !serverIds.has(t.id))
      
      // 将远程数据和本地处理中数据合并，并按时间降序
      const merged = [...uniqueActiveLocal, ...serverTasks]
      persist(merged)
      tasks.value = merged
    } catch (e) {
      console.warn('获取云端批量生图任务失败，退回到本地模式:', e)
      tasks.value = loadTasks()
    }
  }

  function fixStaleTasks() {
    // 空实现，交由外部处理
  }

  return {
    tasks,
    createTask,
    updateTask,
    updateProgress,
    deleteTask,
    clearAll,
    getTask,
    reloadTasks,
    fixStaleTasks,
  }
}
