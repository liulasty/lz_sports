import request from '@/utils/request'

export function checkInit() {
  return request({
    url: '/system/init-status',
    method: 'get'
  })
}

export function initSystem(data) {
  return request({
    url: '/system/init',
    method: 'post',
    data
  })
}
