<template>
  <div class="navbar">
    <div class="hamburger" @click="toggleSidebar">
      <el-icon :class="{ 'is-active': isCollapse }"><Fold /></el-icon>
    </div>
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRouteName && currentRouteName !== 'Home' && currentRouteName !== 'Dashboard'">
          {{ currentRouteName }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right-menu">
      <ThemeToggle class="theme-toggle-wrapper" />
      <div class="notice-wrapper" @click="openDrawer">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-badge">
          <el-icon><Bell /></el-icon>
        </el-badge>
      </div>
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="30" :src="userInfo.avatarSrc || defaultAvatar" />
          <span class="user-name">{{ userInfo.userName || '用户' }}</span>
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
  <el-drawer v-model="drawerVisible" title="站内通知" direction="rtl" size="420px">
    <div class="drawer-actions">
      <el-radio-group v-model="readFilter" size="small" @change="loadNotifications">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="false">未读</el-radio-button>
        <el-radio-button :label="true">已读</el-radio-button>
      </el-radio-group>
      <el-button type="primary" link @click="handleMarkAllRead">全部已读</el-button>
    </div>
    <div class="notice-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        :class="['notice-item', item.isRead ? 'read' : 'unread']"
        @click="handleMarkRead(item)"
      >
        <div class="notice-title">{{ item.title }}</div>
        <div class="notice-content">{{ item.content }}</div>
        <div class="notice-time">{{ formatTime(item.createTime) }}</div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/user'
import { getNotificationPage, getUnreadNotificationCount, markAllNotificationRead, markNotificationRead } from '@/api/notification'
import { Fold, CaretBottom, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import defaultAvatar from '@/assets/vue.svg'
import ThemeToggle from '@/components/ThemeToggle.vue'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-sidebar'])

const toggleSidebar = () => {
  emit('toggle-sidebar')
}

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const userInfo = computed(() => userStore.userInfo)
const unreadCount = ref(0)
const drawerVisible = ref(false)
const notifications = ref([])
const readFilter = ref(null)
let timer = null

// 映射路由名称为中文展示
const routeNameMap = {
  'Dashboard': '系统首页',
  'EventList': '赛事大厅',
  'EventDetail': '赛事详情',
  'ProjectList': '项目报名',
  'Profile': '个人中心',
  'MyRegistrations': '我的报名',
  'MyApplications': '我的申请',
  'Notifications': '消息通知',
  'MyScore': '我的成绩',
  'UserManage': '用户管理',
  'EventCreate': '赛事创建',
  'SchoolSettings': '学校配置',
  'EventManage': '赛事管理',
  'AthleteAudit': '运动员审核',
  'ProjectManage': '项目管理',
  'RegistrationAudit': '报名审核',
  'UserAudit': '用户审核',
  'ScoreManage': '成绩管理'
}

const currentRouteName = computed(() => {
  const name = route.name
  return routeNameMap[name] || route.meta.title || name
})

const loadUnreadCount = async () => {
  const res = await getUnreadNotificationCount()
  if (res.code === 200) {
    unreadCount.value = res.data || 0
  }
}

const loadNotifications = async () => {
  const res = await getNotificationPage({
    currentPage: 1,
    pageSize: 50,
    isRead: readFilter.value
  })
  if (res.code === 200) {
    notifications.value = res.data.records || []
  }
}

const openDrawer = async () => {
  drawerVisible.value = true
  await loadNotifications()
}

const handleMarkRead = async (item) => {
  if (item.isRead) {
    return
  }
  const res = await markNotificationRead(item.id)
  if (res.code === 200) {
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const handleMarkAllRead = async () => {
  const res = await markAllNotificationRead()
  if (res.code === 200) {
    unreadCount.value = 0
    notifications.value = notifications.value.map(item => ({ ...item, isRead: true }))
    ElMessage.success('已全部标记为已读')
  }
}

const handleLogout = async () => {
  try {
    await logout()
  } catch (error) {
  } finally {
    userStore.logout()
    router.push('/login')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(async () => {
  await loadUnreadCount()
  timer = setInterval(() => {
    loadUnreadCount()
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: var(--bg-card);
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  padding: 0 15px;
}

.hamburger {
  padding: 0 15px;
  cursor: pointer;
  transition: transform 0.3s;
  color: var(--el-text-color-regular);
}

.hamburger:hover {
  color: var(--el-color-primary);
}

.hamburger .is-active {
  transform: rotate(180deg);
}

.breadcrumb {
  flex: 1;
  margin-left: 10px;
}

/* 面包屑美化 */
:deep(.el-breadcrumb__inner) {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.right-menu {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.theme-toggle-wrapper {
  transform: scale(0.88);
}

.notice-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--el-text-color-regular);
  transition: color 0.3s;
  padding: 4px;
}

.notice-wrapper:hover {
  color: var(--el-color-primary);
}

.notice-badge {
  display: flex;
}
.notice-badge .el-icon {
  font-size: 20px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.avatar-wrapper:hover {
  background-color: var(--el-fill-color-light);
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.drawer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-item {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  cursor: pointer;
}

.notice-item.unread {
  background: var(--el-color-primary-light-9);
}

.notice-item.read {
  background: var(--el-bg-color-page);
}

.notice-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.notice-content {
  color: var(--el-text-color-regular);
  margin-bottom: 6px;
}

.notice-time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
