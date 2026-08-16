import { ref } from 'vue'

const STORAGE_KEY = 'gh_workflow_progress'

export const WORKFLOW_STEPS = [
  { key: 'white_bg',   label: '白底图' },
  { key: 'background', label: '生成背景' },
  { key: 'main_image', label: '主/副图' },
  { key: 'detail_img', label: '详情/A+' },
  { key: 'banner',     label: 'Banner设计' }
]

const STEP_KEY_TO_INDEX = {}
WORKFLOW_STEPS.forEach((s, i) => { STEP_KEY_TO_INDEX[s.key] = i + 1 })

function loadCompleted() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed && typeof parsed === 'object') return parsed
    }
  } catch { }
  return {}
}

const completedMap = ref(loadCompleted())

function persist() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(completedMap.value))
  } catch { }
}

export function useWorkflowProgress() {
  function isStepDone(stepKeyOrIndex) {
    const key = typeof stepKeyOrIndex === 'number'
      ? WORKFLOW_STEPS[stepKeyOrIndex - 1]?.key
      : stepKeyOrIndex
    return !!completedMap.value[key]
  }

  function markStepCompleted(stepKey) {
    if (!STEP_KEY_TO_INDEX[stepKey]) return
    if (!completedMap.value[stepKey]) {
      completedMap.value = { ...completedMap.value, [stepKey]: true }
      persist()
    }
  }

  function getStepClass(stepIndex, currentStepNumber) {
    const key = WORKFLOW_STEPS[stepIndex - 1]?.key
    if (!key) return ''
    if (completedMap.value[key]) return 'done'
    if (stepIndex === currentStepNumber) return 'active'
    return ''
  }

  function isStepLineDone(stepIndex) {
    const key = WORKFLOW_STEPS[stepIndex - 1]?.key
    return !!completedMap.value[key]
  }

  function resetProgress() {
    completedMap.value = {}
    persist()
  }

  return {
    steps: WORKFLOW_STEPS,
    completedMap,
    isStepDone,
    markStepCompleted,
    getStepClass,
    isStepLineDone,
    resetProgress
  }
}