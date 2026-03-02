import request from '@/utils/request'

export function checkInit() {
  return request({
    url: '/sports/init/check',
    method: 'get'
  })
}

export function initSystem(data) {
  return request({
    url: '/sports/init',
    method: 'post',
    data
  })
}
