import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'
import { checkInit } from '@/api/init'
import { setupInterceptors } from '@/utils/request'

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
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/login/ForgotPassword.vue'),
      meta: { requiresAuth: false, title: '忘记密码' }
    },
  {
    path: '/401',
    name: 'Error401',
    component: () => import('@/views/error/401.vue'),
    meta: { title: '未授权', requiresAuth: false }
  },
  {
    path: '/403',
    name: 'Error403',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '禁止访问', requiresAuth: false }
  },
  {
    path: '/500',
    name: 'Error500',
    component: () => import('@/views/error/500.vue'),
    meta: { title: '服务器错误', requiresAuth: false }
  },
  {
    path: '/404',
    name: 'Error404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
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
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/my/Registrations.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my-applications',
        name: 'MyApplications',
        component: () => import('@/views/my/Applications.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/my/Notifications.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my-score',
        name: 'MyScore',
        component: () => import('@/views/score/index.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'event-create',
        name: 'EventCreate',
        component: () => import('@/views/admin/EventCreate.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'school-settings',
        name: 'SchoolSettings',
        component: () => import('@/views/admin/SchoolSettings.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'event-manage',
        name: 'EventManage',
        component: () => import('@/views/admin/EventManage.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'EVENT_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'athlete-audit',
        name: 'AthleteAudit',
        component: () => import('@/views/admin/AthleteAudit.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'EVENT_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'project-manage',
        name: 'ProjectManage',
        component: () => import('@/views/admin/ProjectManage.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'registration-audit',
        name: 'RegistrationAudit',
        component: () => import('@/views/admin/RegistrationAudit.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'EVENT_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'score-manage',
        name: 'ScoreManage',
        component: () => import('@/views/admin/ScoreManage.vue'),
        meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'EVENT_ADMIN', 'SCHOOL_ADMIN'] }
      },
      {
        path: 'user-audit',
        name: 'UserAudit',
        component: () => import('@/views/admin/UserAudit.vue'),
        meta: {
          requiresAuth: true,
          roles: ['SUPER_ADMIN', 'SCHOOL_ADMIN']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'CatchAll404',
    redirect: '/404'
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
      next('/403')
    }
  } else {
    next()
  }
})

router.isReady().then(() => {
  const userStore = useUserStore()
  setupInterceptors(router, userStore)
})

export default router
