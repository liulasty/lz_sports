import { ref, watchEffect, onMounted, onUnmounted } from 'vue'

const STORAGE_KEY = 'theme-preference'
const preference = ref(localStorage.getItem(STORAGE_KEY) || 'system')

let mediaQuery = null
let mediaQueryHandler = null
let stopPersistEffect = null
let mountedCount = 0

function applyTheme(isDark) {
  document.documentElement.classList.toggle('dark', isDark)
}

function resolveAndApply() {
  if (preference.value === 'system') {
    applyTheme(window.matchMedia('(prefers-color-scheme: dark)').matches)
    return
  }
  applyTheme(preference.value === 'dark')
}

function setupThemeRuntime() {
  if (stopPersistEffect) {
    return
  }
  mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQueryHandler = () => resolveAndApply()
  mediaQuery.addEventListener('change', mediaQueryHandler)
  stopPersistEffect = watchEffect(() => {
    localStorage.setItem(STORAGE_KEY, preference.value)
    resolveAndApply()
  })
  resolveAndApply()
}

function teardownThemeRuntime() {
  if (mountedCount > 0) {
    return
  }
  if (mediaQuery && mediaQueryHandler) {
    mediaQuery.removeEventListener('change', mediaQueryHandler)
  }
  mediaQuery = null
  mediaQueryHandler = null
  if (stopPersistEffect) {
    stopPersistEffect()
    stopPersistEffect = null
  }
}

export function useTheme() {
  onMounted(() => {
    mountedCount += 1
    setupThemeRuntime()
  })

  onUnmounted(() => {
    mountedCount = Math.max(0, mountedCount - 1)
    teardownThemeRuntime()
  })

  function setPreference(value) {
    preference.value = value
  }

  return { preference, setPreference }
}
