<template>
  <el-menu
    active-text-color="#ffd04b"
    background-color="#304156"
    class="el-menu-vertical-demo"
    :default-active="$route.path"
    text-color="#fff"
    router
  >
    <el-menu-item index="/">
      <el-icon><Menu /></el-icon>
      <span>首页</span>
    </el-menu-item>
    
    <el-menu-item index="/event">
      <el-icon><Trophy /></el-icon>
      <span>赛事列表</span>
    </el-menu-item>

    <el-menu-item index="/project">
      <el-icon><List /></el-icon>
      <span>项目列表</span>
    </el-menu-item>

    <el-sub-menu index="admin" v-if="isAdmin">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/user-manage">用户管理</el-menu-item>
      <el-menu-item index="/event-manage">赛事管理</el-menu-item>
      <el-menu-item index="/project-manage">项目管理</el-menu-item>
      <el-menu-item index="/registration-audit">报名审批</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { Menu, User, Trophy, Stamp, List, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo && userStore.userInfo.type === '管理员')
</script>

<style scoped>
.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
}
</style>
