<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()

useTheme()

onMounted(() => {
  configStore.fetchConfig()
  window.addEventListener('schoolConfigUpdated', () => {
    configStore.fetchConfig()
  })
})
</script>

<style>
#app {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}
</style>
