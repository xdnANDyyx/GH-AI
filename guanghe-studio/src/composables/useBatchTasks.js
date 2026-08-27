/**
 * useBatchTasks.js
 * 批量生成任务管理 composable（localStorage 持久化）
 *  - 创建/查询/删除/清空任务
 *  - 更新任务状态（排队中 → 处理中 → 已完成/失败）
 *  - 按状态筛选
 */
import { ref, computed } from 'vue'

const STORAGE_KEY = 'gh_batch_tasks'

function loadTasks() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    return JSON.parse(raw)
  } catch {
    return []
  }
}

function persist(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
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
      status: 'queued',          // queued | processing | done | failed
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
    // 最多保留 200 条
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
      // 自动维护 statusText / statusClass / progress
      if (patch.status) {
        const statusMap = {
          queued:    { statusText: '排队中',  statusClass: 'orange', progress: null },
          processing:{ statusText: '生成中',  statusClass: 'blue',   progress: patch.progress ?? t.progress ?? 0 },
          done:      { statusText: '已完成',  statusClass: 'green',  progress: 100 },
          failed:    { statusText: '生成失败', statusClass: 'red',    progress: null },
        }
        const meta = statusMap[patch.status]
        if (meta) {
          updated.statusText = meta.statusText
          updated.statusClass = meta.statusClass
          if (meta.progress !== undefined) updated.progress = meta.progress
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
      return { ...t, progress }
    })
    persist(list)
    tasks.value = list
  }

  /** 删除任务 */
  function deleteTask(id) {
    const list = loadTasks().filter(t => t.id !== id)
    persist(list)
    tasks.value = list
  }

  /** 清空全部 */
  function clearAll() {
    persist([])
    tasks.value = []
  }

  /** 获取单个任务 */
  function getTask(id) {
    return loadTasks().find(t => t.id === id)
  }

  return {
    tasks,
    createTask,
    updateTask,
    updateProgress,
    deleteTask,
    clearAll,
    getTask,
  }
}
