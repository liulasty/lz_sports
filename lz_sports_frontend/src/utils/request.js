import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: '/api', // Proxy target
  timeout: 5000
})

// Request Interceptor
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['token'] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response Interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // Assuming backend returns { code: 200, data: ..., msg: ... }
    // Or { code: 1, data: ... } based on original Result class
    // Let's check Result.java: code 1 is success, 0 is error.
    
    if (res.code !== 1) {
      ElMessage.error(res.msg || 'Error')
      
      // 401 or specific code for token expiration?
      // Original project might not have specific code for 401, but usually 0 is error.
      // If token invalid, backend might throw 403 or 401 HTTP status?
      // JwtAuthenticationFilter doesn't set status, just logs error and continues.
      // Spring Security will return 403 if not authenticated.
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res
    }
  },
  (error) => {
    if (error.response && error.response.status === 403) {
      // Token expired or invalid
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || 'Request Error')
    }
    return Promise.reject(error)
  }
)

export default service
