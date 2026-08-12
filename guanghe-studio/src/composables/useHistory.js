/**
 * useHistory.js
 * 历史记录管理 composable（localStorage 持久化）
 *  - 保存/查询/删除/清空生成历史
 *  - 按日期分组（今天/昨天/更早）形成时间线
 *  - 按时间范围、功能类型、状态筛选
 */
import { ref, computed } from 'vue'

const STORAGE_KEY = 'gh_history_records'

// 功能类型配置：label + 彩色图标配色
export const FEATURE_TYPES = {
  white_bg:    { label: 'AI白底图',     color: '#2563FF', bg: '#EBF2FF' },
  background:  { label: '白底生成背景', color: '#10B981', bg: '#ECFDF5' },
  retouch:     { label: '产品精修',     color: '#F59E0B', bg: '#FFFBEB' },
  ai_model:    { label: 'AI模特',       color: '#8B5CF6', bg: '#F5F3FF' },
  main_image:  { label: '主图设计',     color: '#EC4899', bg: '#FDF2F8' },
  detail_img:  { label: '详情页',       color: '#06B6D4', bg: '#ECFEFF' },
  banner:      { label: 'Banner',       color: '#EF4444', bg: '#FEF2F2' },
  size_mark:   { label: '尺寸标记',     color: '#6366F1', bg: '#EEF2FF' },
  ai_assistant:{ label: 'AI助手',       color: '#14B8A6', bg: '#F0FDFA' },
}

function loadRecords() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    return JSON.parse(raw)
  } catch {
    return []
  }
}

function saveRecords(records) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(records))
}

export function useHistory() {
  const records = ref(loadRecords())
  const filters = ref({
    timeRange: 'all',   // today | 7days | 30days | all | custom
    featureType: '',    // '' = 全部
    status: '',         // '' = 全部
    customStart: '',
    customEnd: '',
  })

  function _save(records) {
    saveRecords(records)
  }

  /** 保存一条历史记录 */
  function saveHistoryRecord(record) {
    const item = {
      id: 'hr_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8),
      timestamp: Date.now(),
      status: 'success',
      consumePoints: 0,
      resultImages: [],
      ...record,
    }
    const list = [item, ...loadRecords()]
    // 最多保留 500 条
    if (list.length > 500) list.length = 500
    saveRecords(list)
    records.value = list
    return item
  }

  /** 更新一条记录（如从 processing 变为 success/failed） */
  function updateHistoryRecord(id, patch) {
    const list = loadRecords().map(r => r.id === id ? { ...r, ...patch } : r)
    saveRecords(list)
    records.value = list
  }

  /** 删除单条记录 */
  function deleteHistoryRecord(id) {
    const list = loadRecords().filter(r => r.id !== id)
    saveRecords(list)
    records.value = list
  }

  /** 清空所有记录 */
  function clearHistory() {
    saveRecords([])
    records.value = []
  }

  /** 日期分组标签 */
  function _dateLabel(ts) {
    const d = new Date(ts)
    const today = new Date(); today.setHours(0, 0, 0, 0)
    const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)
    if (d >= today) return '今天'
    if (d >= yesterday) return '昨天'
    return `${d.getMonth() + 1}月${d.getDate()}日`
  }

  /** 按筛选条件过滤 */
  const filteredRecords = computed(() => {
    let list = records.value.slice()
    const f = filters.value

    // 时间范围
    if (f.timeRange !== 'all' && f.timeRange !== 'custom') {
      const now = Date.now()
      const days = f.timeRange === 'today' ? 1 : (f.timeRange === '7days' ? 7 : 30)
      const cutoff = now - days * 24 * 60 * 60 * 1000
      list = list.filter(r => r.timestamp >= cutoff)
    } else if (f.timeRange === 'custom' && f.customStart && f.customEnd) {
      const s = new Date(f.customStart).getTime()
      const e = new Date(f.customEnd).getTime() + 24 * 60 * 60 * 1000 - 1
      list = list.filter(r => r.timestamp >= s && r.timestamp <= e)
    }

    // 功能类型
    if (f.featureType) {
      list = list.filter(r => r.featureType === f.featureType)
    }

    // 状态
    if (f.status) {
      list = list.filter(r => r.status === f.status)
    }

    return list
  })

  /** 时间线分组 */
  const timelineGroups = computed(() => {
    const groups = {}
    filteredRecords.value.forEach(r => {
      const label = _dateLabel(r.timestamp)
      if (!groups[label]) groups[label] = []
      groups[label].push(r)
    })
    // 转为数组并按时间倒序
    return Object.entries(groups).map(([label, items]) => ({
      label,
      items: items.sort((a, b) => b.timestamp - a.timestamp),
    }))
  })

  const totalCount = computed(() => records.value.length)

  function setFilter(key, value) {
    filters.value[key] = value
  }

  function resetFilters() {
    filters.value = { timeRange: 'all', featureType: '', status: '', customStart: '', customEnd: '' }
  }

  return {
    records,
    filters,
    filteredRecords,
    timelineGroups,
    totalCount,
    saveHistoryRecord,
    updateHistoryRecord,
    deleteHistoryRecord,
    clearHistory,
    setFilter,
    resetFilters,
  }
}