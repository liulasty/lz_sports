import request from '@/utils/request'

/** 无需登录：分页赛事列表（与 /api/event/page 同源数据） */
export function getPublicEventList(params) {
  return request({
    url: '/public/events',
    method: 'get',
    params
  })
}

/** 无需登录：赛事详情 */
export function getPublicEventById(id) {
  return request({
    url: `/public/events/${id}`,
    method: 'get'
  })
}
