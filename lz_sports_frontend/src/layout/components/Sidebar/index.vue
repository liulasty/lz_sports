<template>
  <el-menu
    :collapse="isCollapse"
    active-text-color="var(--el-color-primary)"
    background-color="var(--bg-card)"
    class="sidebar-menu"
    :default-active="$route.path"
    text-color="var(--el-text-color-regular)"
    router
  >
    <div class="sidebar-logo">
      <img v-if="configStore.logoUrl && !isCollapse" :src="configStore.logoUrl" class="sidebar-logo-img" />
      <el-icon v-else-if="isCollapse" size="24" color="var(--el-color-primary)"><Trophy /></el-icon>
      <h2 v-else>{{ configStore.schoolName || '体育赛事管理系统' }}</h2>
    </div>

    <!-- 首页 -->
    <el-menu-item index="/dashboard">
      <el-icon><Menu /></el-icon>
      <template #title><span>系统首页</span></template>
    </el-menu-item>
    
    <!-- 公共赛事大厅 -->
    <el-menu-item index="/event">
      <el-icon><Trophy /></el-icon>
      <template #title><span>赛事大厅</span></template>
    </el-menu-item>

    <!-- 我的（登录后可见，仅非管理员可见） -->
    <el-sub-menu index="my" v-if="isLoggedIn && !isEventAdmin && !isSchoolAdmin">
      <template #title>
        <el-icon><User /></el-icon>
        <span>我的</span>
      </template>
      <el-menu-item index="/my-registrations">我的报名</el-menu-item>
      <el-menu-item index="/my-applications">我的申请</el-menu-item>
      <el-menu-item index="/my-score">我的成绩</el-menu-item>
      <el-menu-item index="/profile">个人资料</el-menu-item>
      <el-menu-item index="/notifications">消息通知</el-menu-item>
    </el-sub-menu>

    <!-- 赛事管理员菜单 (EVENT_ADMIN) -->
    <el-sub-menu index="event-admin" v-if="isEventAdmin">
      <template #title>
        <el-icon><Stamp /></el-icon>
        <span>赛事运维</span>
      </template>
      <el-menu-item index="/event-manage">赛事管理</el-menu-item>
      <el-menu-item index="/athlete-audit">运动员审核</el-menu-item>
      <el-menu-item index="/registration-audit">报名审核</el-menu-item>
      <el-menu-item index="/score-manage">成绩管理</el-menu-item>
    </el-sub-menu>

    <!-- 系统管理员菜单 (SUPER_ADMIN / SCHOOL_ADMIN) -->
    <el-sub-menu index="system-admin" v-if="isSchoolAdmin">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/user-manage">用户管理</el-menu-item>
      <el-menu-item index="/user-audit">用户审核</el-menu-item>
      <el-menu-item index="/project-manage">项目库管理</el-menu-item>
      <el-menu-item index="/school-settings">学校配置</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { Menu, User, Trophy, Stamp, Setting, DataLine } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useConfigStore } from '@/stores/config'
import { computed } from 'vue'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const userStore = useUserStore()
const configStore = useConfigStore()
const isLoggedIn = computed(() => !!userStore.token)

// 适配新旧角色标识
const role = computed(() => {
  return userStore.userInfo?.role || 'USER'
})

// 根据后端UserRole枚举定义的权限
const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')
const isSchoolAdmin = computed(() => role.value === 'SCHOOL_ADMIN' || isSuperAdmin.value)
const isEventAdmin = computed(() => 
  role.value === 'EVENT_ADMIN' || 
  role.value === 'SCHOOL_ADMIN' || 
  isSuperAdmin.value
)
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 210px;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--el-border-color-light);
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-logo-img {
  height: 32px;
  max-width: 180px;
  object-fit: contain;
}

.sidebar-logo h2 {
  margin: 0;
  font-size: 16px;
  color: var(--el-color-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 10px;
}
</style>
