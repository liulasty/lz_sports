import request from '@/utils/request'

export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

export function sendCode(data) {
  return request({
    url: '/auth/send-verify-code',
    method: 'post',
    params: data // { email }
  })
}

export function verifyCode(data) {
  return request({
    url: '/auth/verify-code',
    method: 'post',
    params: data // { verifyToken, code }
  })
}
