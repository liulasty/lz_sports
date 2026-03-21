import request from '@/utils/request'

export function getOverviewStats() {
  return request({
    url: '/admin/stats/overview',
    method: 'get'
  })
}

export function getEventStats() {
  return request({
    url: '/admin/stats/events',
    method: 'get'
  })
}
