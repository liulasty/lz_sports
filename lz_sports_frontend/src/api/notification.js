import request from '@/utils/request'

export function getNotificationPage(params) {
  return request({
    url: '/notification/page',
    method: 'get',
    params
  })
}

export function markNotificationRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

export function markAllNotificationRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

export function getUnreadNotificationCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}
