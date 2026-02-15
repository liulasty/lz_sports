<template>
  <div class="navbar">
    <div class="hamburger">
      <el-icon><Fold /></el-icon>
    </div>
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item v-if="$route.path !== '/'">{{ $route.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right-menu">
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="30" :src="userInfo.avatarSrc || defaultAvatar" />
          <span class="user-name">{{ userInfo.userName }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/user'
import { Fold, CaretBottom } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/vue.svg'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const userInfo = computed(() => userStore.userInfo)

const handleLogout = async () => {
  try {
    await logout()
    userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error(error)
    // Force logout even if API fails
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 15px;
}

.hamburger {
  padding: 0 15px;
  cursor: pointer;
}

.breadcrumb {
  flex: 1;
}

.right-menu {
  float: right;
  height: 100%;
  line-height: 50px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  margin: 0 5px;
  font-size: 14px;
}
</style>
