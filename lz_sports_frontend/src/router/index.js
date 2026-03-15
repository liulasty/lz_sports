import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'
import { checkInit } from '@/api/init'

const routes = [
  {
    path: '/init',
    name: 'Init',
    component: () => import('@/views/init/index.vue'),
    meta: { title: '系统初始化' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue')
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/login/ResetPassword.vue'),
    meta: { requiresAuth: true, title: '修改初始密码' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'event',
        name: 'EventList',
        component: () => import('@/views/event/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'event/:id',
        name: 'EventDetail',
        component: () => import('@/views/event/detail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'project',
        name: 'ProjectList',
        component: () => import('@/views/project/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'score',
        name: 'ScoreList',
        component: () => import('@/views/score/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { requiresAuth: true, roles: ['管理员'] }
      },
      {
        path: 'event-manage',
        name: 'EventManage',
        component: () => import('@/views/admin/EventManage.vue'),
        meta: { requiresAuth: true, roles: ['管理员'] }
      },
      {
        path: 'project-manage',
        name: 'ProjectManage',
        component: () => import('@/views/admin/ProjectManage.vue'),
        meta: { requiresAuth: true, roles: ['管理员'] }
      },
      {
        path: 'registration-audit',
        name: 'RegistrationAudit',
        component: () => import('@/views/admin/RegistrationAudit.vue'),
        meta: { requiresAuth: true, roles: ['管理员'] }
      },
      {
        path: 'user-audit',
        name: 'UserAudit',
        component: () => import('@/views/admin/UserAudit.vue'),
        meta: { requiresAuth: true, roles: ['管理员'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // Check System Initialization Status
  const isInit = localStorage.getItem('isInitialized')

  if (isInit === 'true') {
    if (to.path === '/init') {
      next('/login')
      return
    }
  } else {
    // If not cached as true, check API
    if (to.path !== '/init') {
      try {
        const { data } = await checkInit()
        if (data === false) {
          next('/init')
          return
        } else {
          localStorage.setItem('isInitialized', 'true')
        }
      } catch (error) {
        console.error('Init check failed', error)
      }
    } else {
      // If accessing /init, check if already initialized
      try {
        const { data } = await checkInit()
        if (data === true) {
          localStorage.setItem('isInitialized', 'true')
          next('/login')
          return
        }
      } catch (error) {
        console.error('Init check failed', error)
      }
    }
  }

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (userStore.token && userStore.userInfo.isFirstLogin && to.path !== '/reset-password') {
    // Force reset password if first login
    next('/reset-password')
  } else if (to.meta.roles && to.meta.roles.length > 0) {
    // Check role permission
    if (userStore.userInfo && to.meta.roles.includes(userStore.userInfo.type)) {
      next()
    } else {
      next('/dashboard') // No permission
    }
  } else {
    next()
  }
})

export default router
