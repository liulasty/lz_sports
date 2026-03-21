<template>
  <div class="app-wrapper" :class="{ 'hide-sidebar': isCollapse }">
    <div class="sidebar-container">
      <Sidebar :is-collapse="isCollapse" />
    </div>
    <div class="main-container">
      <div class="fixed-header">
        <Navbar @toggle-sidebar="toggleSideBar" :is-collapse="isCollapse" />
      </div>
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar/index.vue'
import Navbar from './components/Navbar.vue'

const isCollapse = ref(false)

const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: var(--bg-card);
  transition: width 0.28s;
  overflow-y: auto;
  border-right: 1px solid var(--el-border-color-light);
}

.hide-sidebar .sidebar-container {
  width: 64px !important;
}

.main-container {
  flex: 1;
  min-height: 100%;
  transition: margin-left 0.28s;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.fixed-header {
  height: 50px;
  width: 100%;
  z-index: 9;
}

.app-main {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-page);
  overflow-y: auto;
}

/* Transitions */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
