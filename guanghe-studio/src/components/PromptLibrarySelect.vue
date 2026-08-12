<template>
  <el-select
    v-model="selected"
    :multiple="multiple"
    :collapse-tags="multiple"
    :collapse-tags-tooltip="multiple"
    :placeholder="placeholder || (multiple ? '请选择' : '请选择')"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    :size="size"
    style="width: 100%"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.promptKey"
      :label="item.label"
      :value="item.promptKey"
    >
      <span style="float: left">{{ item.label }}</span>
      <span v-if="item.isDefault === '1'" style="float: right; color: #67c23a; font-size: 12px">默认</span>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { listPromptLibrary } from '@/api/customer'

const props = defineProps({
  category: { type: String, required: true },
  scope: { type: String, default: '' },
  multiple: { type: Boolean, default: false },
  modelValue: { type: [String, Array], default: null },
  placeholder: { type: String, default: '' },
  clearable: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: 'default' },
  autoSelectDefault: { type: Boolean, default: false },
  keyPrefix: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change', 'loaded'])

const selected = ref(props.modelValue || (props.multiple ? [] : ''))
const options = ref([])
const loading = ref(false)

async function loadOptions() {
  loading.value = true
  try {
    const res = await listPromptLibrary(props.category, props.scope)
    let list = res.data || res.rows || []
    if (props.keyPrefix) {
      list = list.filter(o => o.promptKey && o.promptKey.startsWith(props.keyPrefix))
    }
    options.value = list
    emit('loaded', options.value)
    if (props.autoSelectDefault && !selected.value) {
      const def = options.value.find(o => o.isDefault === '1')
      if (def) {
        selected.value = def.promptKey
        emit('update:modelValue', selected.value)
        emit('change', selected.value, getSelectedItems())
      }
    }
  } catch (e) {
    options.value = []
  } finally {
    loading.value = false
  }
}

function getSelectedItems() {
  if (props.multiple) {
    return options.value.filter(o => (selected.value || []).includes(o.promptKey))
  }
  return options.value.filter(o => o.promptKey === selected.value)
}

function handleChange(val) {
  emit('update:modelValue', val)
  emit('change', val, getSelectedItems())
}

watch(() => props.modelValue, (val) => {
  selected.value = val || (props.multiple ? [] : '')
})

watch(() => [props.category, props.scope], () => {
  loadOptions()
})

onMounted(() => {
  loadOptions()
})

defineExpose({ getSelectedItems, refresh: loadOptions, options })
</script>