export const ERROR_STRATEGY = {
  401: {
    message: '登录已过期，请重新登录',
    notifyType: 'warning',
    redirect: '/401',
    clearAuth: true,
    before: null
  },
  403: {
    message: '您没有权限访问该资源',
    notifyType: 'error',
    redirect: '/403',
    clearAuth: false,
    before: null
  },
  404: {
    message: '请求的资源不存在',
    notifyType: 'warning',
    redirect: '/404',
    clearAuth: false,
    before: null
  },
  500: {
    message: '服务器开小差了，请稍后重试',
    notifyType: 'error',
    redirect: '/500',
    clearAuth: false,
    before: null
  },
  504: {
    message: '网络超时，请检查网络后重试',
    notifyType: 'warning',
    redirect: null,
    clearAuth: false,
    before: null
  }
}

export const FALLBACK_STRATEGY = {
  message: '请求失败，请稍后重试',
  notifyType: 'error',
  redirect: null,
  clearAuth: false,
  before: null
}
