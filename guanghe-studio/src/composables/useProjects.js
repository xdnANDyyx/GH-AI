/**
 * useProjects.js
 * 我的项目管理 composable（localStorage 持久化）
 *  - 创建/编辑/删除/恢复项目
 *  - 自动保存生成结果到对应项目
 *  - 排序与筛选
 */
import { ref, computed } from 'vue'

const STORAGE_KEY = 'gh_projects'

function loadProjects() {
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

export function useProjects() {
  const projects = ref(loadProjects())
  const sortBy = ref('updatedAt')   // createdAt | updatedAt | name
  const sortOrder = ref('desc')     // asc | desc
  const statusFilter = ref('')      // '' = 全部 | active | completed

  /** 获取未删除项目 */
  const activeProjects = computed(() => projects.value.filter(p => !p.deleted))

  /** 回收站项目 */
  const recycledProjects = computed(() => projects.value.filter(p => p.deleted))

  /** 排序+筛选后的项目 */
  const displayProjects = computed(() => {
    let list = activeProjects.value.slice()
    if (statusFilter.value) {
      list = list.filter(p => p.status === statusFilter.value)
    }
    const dir = sortOrder.value === 'asc' ? 1 : -1
    list.sort((a, b) => {
      if (sortBy.value === 'name') return a.name.localeCompare(b.name) * dir
      return (a[sortBy.value] - b[sortBy.value]) * dir
    })
    return list
  })

  /** 新建项目 */
  function createProject(name, description = '') {
    const now = Date.now()
    const project = {
      id: 'prj_' + now + '_' + Math.random().toString(36).slice(2, 8),
      name,
      description,
      thumbnail: '',
      images: [],
      imageCount: 0,
      createdAt: now,
      updatedAt: now,
      status: 'active',
      deleted: false,
    }
    const list = [project, ...loadProjects()]
    persist(list)
    projects.value = list
    return project
  }

  /** 编辑项目 */
  function updateProject(id, patch) {
    const list = loadProjects().map(p => {
      if (p.id === id) return { ...p, ...patch, updatedAt: Date.now() }
      return p
    })
    persist(list)
    projects.value = list
  }

  /** 删除项目（移入回收站） */
  function deleteProject(id) {
    updateProject(id, { deleted: true })
  }

  /** 彻底删除 */
  function destroyProject(id) {
    const list = loadProjects().filter(p => p.id !== id)
    persist(list)
    projects.value = list
  }

  /** 从回收站恢复 */
  function restoreProject(id) {
    updateProject(id, { deleted: false })
  }

  /** 添加生成图到项目（自动保存） */
  function addImageToProject(id, imageUrl) {
    if (!id || !imageUrl) return
    const list = loadProjects().map(p => {
      if (p.id === id) {
        const images = [imageUrl, ...(p.images || [])]
        return {
          ...p,
          images,
          imageCount: images.length,
          thumbnail: images[0],
          updatedAt: Date.now(),
        }
      }
      return p
    })
    persist(list)
    projects.value = list
  }

  /** 获取单个项目 */
  function getProject(id) {
    return loadProjects().find(p => p.id === id)
  }

  function setSortBy(val) { sortBy.value = val }
  function setSortOrder(val) { sortOrder.value = val }
  function setStatusFilter(val) { statusFilter.value = val }

  return {
    projects,
    activeProjects,
    recycledProjects,
    displayProjects,
    sortBy,
    sortOrder,
    statusFilter,
    createProject,
    updateProject,
    deleteProject,
    destroyProject,
    restoreProject,
    addImageToProject,
    getProject,
    setSortBy,
    setSortOrder,
    setStatusFilter,
  }
}