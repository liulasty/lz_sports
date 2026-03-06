import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: '/api', // Proxy target
  timeout: 5000
})

// HTTP status code mapping
const errorCode = {
  '401': '认证失败，无法访问系统资源',
  '403': '当前操作没有权限',
  '404': '访问资源不存在',
  'default': '系统未知错误，请反馈给管理员'
}

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
    // 兼容部分非 Result 结构的响应（如直接返回二进制流）
    if (response.config.responseType === 'blob') {
      return res
    }

    // 200: 成功
    if (res.code === 200) {
      return res
    }
    
    // 处理业务错误码
    const msg = res.msg || errorCode[res.code] || errorCode['default']
    
    // 401: 未登录
    if (res.code === 401) {
       ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }).catch(() => {})
      return Promise.reject(new Error('无效的会话，或者会话已过期，请重新登录。'))
    }
    
    // 500: 服务器错误; 400: 参数错误; 403: 无权限; 409: 业务冲突
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    let { message } = error;
    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.substr(message.length - 3) + "异常";
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
