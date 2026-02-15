import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'

const routes = [
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  console.log('Router Guard:', { to: to.path, from: from.path, token: userStore.token })
  if (to.meta.requiresAuth && !userStore.token) {
    console.log('Redirecting to login: No token')
    next('/login')
  } else if (to.meta.roles && to.meta.roles.length > 0) {
    // Check role permission
    if (userStore.userInfo && to.meta.roles.includes(userStore.userInfo.type)) {
      next()
    } else {
      console.log('Redirecting to dashboard: No permission')
      next('/dashboard') // No permission, redirect to dashboard or 403
    }
  } else {
    next()
  }
})

export default router
