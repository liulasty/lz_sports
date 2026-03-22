<template>
  <div class="theme-toggle">
    <button
      v-for="opt in options"
      :key="opt.value"
      :class="['toggle-btn', { active: preference === opt.value }]"
      :title="opt.label"
      @click="setPreference(opt.value)"
    >
      <el-icon><component :is="opt.icon" /></el-icon>
      <span>{{ opt.label }}</span>
    </button>
  </div>
</template>

<script setup>
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const { preference, setPreference } = useTheme()

const options = [
  { value: 'light', label: '亮色', icon: Sunny },
  { value: 'system', label: '跟随系统', icon: Monitor },
  { value: 'dark', label: '暗色', icon: Moon }
]
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 3px;
  gap: 2px;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: var(--accent);
  color: #fff;
}

.toggle-btn:hover:not(.active) {
  background: var(--border);
  color: var(--text-primary);
}
</style>
