import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ERROR_STRATEGY, FALLBACK_STRATEGY } from '@/config/errorStrategy'

let runtimeRouter = null
let runtimeStore = null

const CLIENT_REQUEST_ID_HEADER = 'X-Client-Request-Id'

function buildClientRequestId() {
  return `req_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`
}

function resolveOperationName(config) {
  return config?.meta?.operation || '请求'
}

function resolveTraceId(error) {
  return (
    error?.response?.headers?.[CLIENT_REQUEST_ID_HEADER.toLowerCase()] ||
    error?.config?.headers?.[CLIENT_REQUEST_ID_HEADER] ||
    null
  )
}

function buildErrorMessage(error, strategy) {
  const operation = resolveOperationName(error?.config)
  const serverMsg = error?.response?.data?.msg || error?.response?.data?.message
  const fallbackMessage = strategy?.message || FALLBACK_STRATEGY.message
  const traceId = resolveTraceId(error)
  const core = serverMsg || `${operation}失败：${fallbackMessage}`
  return traceId ? `${core}（请求ID: ${traceId}）` : core
}

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
    config.headers = config.headers || {}
    if (!config.headers[CLIENT_REQUEST_ID_HEADER]) {
      config.headers[CLIENT_REQUEST_ID_HEADER] = buildClientRequestId()
    }
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

    ElMessage[strategy.notifyType](buildErrorMessage(error, strategy))

    if (strategy.redirect && router?.currentRoute?.value?.path !== strategy.redirect) {
      router.push(strategy.redirect)
    }

    return Promise.reject(error)
  }
)

export default service

export function apiRequest(config, operation) {
  return service({
    ...config,
    meta: {
      ...(config?.meta || {}),
      operation
    }
  })
}
