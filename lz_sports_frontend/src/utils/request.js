import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ERROR_STRATEGY, FALLBACK_STRATEGY } from '@/config/errorStrategy'

let runtimeRouter = null
let runtimeStore = null

export function setupInterceptors(router, store) {
  runtimeRouter = router
  runtimeStore = store
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

service.interceptors.request.use(
  (config) => {
    const token = runtimeStore?.token || localStorage.getItem('token')
    if (token) {
      config.headers.token = token
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response.data
    }
    return response.data
  },
  (error) => {
    const status = error.response?.status
    const strategy = ERROR_STRATEGY[status] ?? FALLBACK_STRATEGY
    const store = runtimeStore
    const router = runtimeRouter

    if (strategy.clearAuth && store?.logout) {
      store.logout()
    }

    if (typeof strategy.before === 'function') {
      strategy.before(router, store)
    }

    const serverMsg = error.response?.data?.msg || error.response?.data?.message
    ElMessage[strategy.notifyType](serverMsg || strategy.message)

    if (strategy.redirect && router?.currentRoute?.value?.path !== strategy.redirect) {
      router.push(strategy.redirect)
    }

    return Promise.reject(error)
  }
)

export default service
