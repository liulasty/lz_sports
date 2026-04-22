<template>
  <el-select
    :model-value="modelValue"
    class="smart-select"
    :class="customClass"
    :filterable="filterable"
    :clearable="clearable"
    :placeholder="placeholder"
    :loading="loading"
    :popper-class="resolvedPopperClass"
    v-bind="$attrs"
    @update:model-value="handleUpdate"
    @change="handleChange"
  >
    <slot v-if="hasDefaultSlot" />
    <el-option
      v-else
      v-for="opt in normalizedOptions"
      :key="String(opt.value)"
      :label="opt.label"
      :value="opt.value"
    />
  </el-select>
</template>

<script setup>
import { computed, useSlots } from 'vue'

defineOptions({
  inheritAttrs: false
})

const props = defineProps({
  modelValue: {
    type: [String, Number, Array, Object, Boolean, null],
    default: null
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  loading: {
    type: Boolean,
    default: false
  },
  filterable: {
    type: Boolean,
    default: true
  },
  clearable: {
    type: Boolean,
    default: true
  },
  labelKey: {
    type: String,
    default: 'label'
  },
  valueKey: {
    type: String,
    default: 'value'
  },
  fallbackLabelPrefix: {
    type: String,
    default: '选项 #'
  },
  customClass: {
    type: String,
    default: ''
  },
  popperClass: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])
const slots = useSlots()

const normalizedOptions = computed(() => {
  return (props.options || []).map((item) => {
    if (item == null || typeof item !== 'object') {
      return { label: String(item ?? ''), value: item }
    }
    const valueFromKey = item[props.valueKey]
    const fallbackValue = item.id ?? item.eventId ?? item.userId ?? item.key ?? item.code
    const value = valueFromKey ?? fallbackValue
    const directLabel = item[props.labelKey]
    const fallbackLabel = item.name || item.eventName || `${props.fallbackLabelPrefix}${value ?? ''}`
    return {
      label: directLabel ?? fallbackLabel,
      value
    }
  })
})

const resolvedPopperClass = computed(() => {
  return props.popperClass ? `smart-select-popper ${props.popperClass}` : 'smart-select-popper'
})

const hasDefaultSlot = computed(() => Boolean(slots.default))

const handleUpdate = (value) => {
  emit('update:modelValue', value)
}

const handleChange = (value) => {
  emit('change', value)
}
</script>

<style scoped>
:deep(.smart-select .el-select__wrapper) {
  min-height: 42px;
  border-radius: 10px;
  background:
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--ps-surface, var(--bg-card, #fff)) 96%, #ffffff 4%),
      color-mix(in srgb, var(--ps-surface, var(--bg-card, #fff)) 90%, #f3f6fb 10%)
    ) !important;
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--ps-border, var(--border, rgba(0, 0, 0, 0.1))) 62%, transparent) inset !important;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

:deep(.smart-select:hover .el-select__wrapper) {
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--el-color-primary, #ff6b35) 34%, var(--ps-border, rgba(0, 0, 0, 0.1))) inset !important;
}

:deep(.smart-select .el-select__wrapper.is-focused) {
  box-shadow:
    0 0 0 1px rgba(255, 107, 53, 0.72) inset !important,
    0 6px 18px rgba(255, 107, 53, 0.14) !important;
  transform: translateY(-1px);
}

:deep(.smart-select .el-select__placeholder) {
  color: var(--ps-text-placeholder, var(--text-secondary, #8b97aa)) !important;
  letter-spacing: 0.01em;
}

:deep(.smart-select .el-select__selected-item),
:deep(.smart-select .el-select__input) {
  color: var(--ps-text-primary, var(--text-primary, #1a1a2e)) !important;
  font-weight: 600;
}

:deep(.smart-select .el-select__caret) {
  color: color-mix(in srgb, var(--el-color-primary, #ff6b35) 54%, var(--ps-text-secondary, var(--text-secondary, #6b7280))) !important;
}

:global(.smart-select-popper) {
  border-radius: 12px !important;
  border: 1px solid color-mix(in srgb, var(--ps-border, var(--border, rgba(0, 0, 0, 0.1))) 70%, transparent) !important;
  background: color-mix(in srgb, var(--ps-surface, var(--bg-card, #fff)) 96%, #f8fbff 4%) !important;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.16) !important;
  overflow: hidden;
}

:global(.smart-select-popper .el-scrollbar__view) {
  padding: 6px !important;
}

:global(.smart-select-popper .el-select-dropdown__item) {
  min-height: 38px !important;
  height: auto !important;
  line-height: 1.4 !important;
  display: flex;
  align-items: center;
  margin: 2px 0 !important;
  border-radius: 8px !important;
  padding: 8px 12px !important;
  color: var(--ps-text-body, var(--text-primary, #1a1a2e)) !important;
  transition: background-color 0.16s ease, color 0.16s ease;
}

:global(.smart-select-popper .el-select-dropdown__item.hover),
:global(.smart-select-popper .el-select-dropdown__item:hover) {
  background: color-mix(in srgb, var(--ps-surface, var(--bg-card, #fff)) 82%, #e7eef8 18%) !important;
  color: var(--ps-text-primary, var(--text-primary, #1a1a2e)) !important;
}

:global(.smart-select-popper .el-select-dropdown__item.selected) {
  background: linear-gradient(90deg, rgba(255, 107, 53, 0.16), rgba(255, 107, 53, 0.05)) !important;
  color: color-mix(in srgb, var(--el-color-primary, #ff6b35) 78%, var(--ps-text-primary, var(--text-primary, #1a1a2e))) !important;
  font-weight: 700 !important;
}

:global(.smart-select-popper .el-select-dropdown__item.selected::before) {
  content: '●';
  display: inline-block;
  margin-right: 8px;
  font-size: 10px;
  color: var(--el-color-primary, #ff6b35);
}

:global(html.dark .smart-select .el-select__wrapper),
:global([data-theme="dark"] .smart-select .el-select__wrapper) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(13, 18, 27, 0.68)) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
}

:global(html.dark .smart-select-popper),
:global([data-theme="dark"] .smart-select-popper) {
  border-color: rgba(255, 255, 255, 0.09) !important;
  background: #121925 !important;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.48) !important;
}

:global(html.dark .smart-select-popper .el-select-dropdown__item),
:global([data-theme="dark"] .smart-select-popper .el-select-dropdown__item) {
  color: #c9d1de !important;
}

:global(html.dark .smart-select-popper .el-select-dropdown__item.hover),
:global(html.dark .smart-select-popper .el-select-dropdown__item:hover),
:global([data-theme="dark"] .smart-select-popper .el-select-dropdown__item.hover),
:global([data-theme="dark"] .smart-select-popper .el-select-dropdown__item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #eef4ff !important;
}

:global(html.dark .smart-select-popper .el-select-dropdown__item.selected),
:global([data-theme="dark"] .smart-select-popper .el-select-dropdown__item.selected) {
  background: linear-gradient(90deg, rgba(255, 107, 53, 0.2), rgba(255, 107, 53, 0.08)) !important;
  color: #ffb394 !important;
}
</style>
