import request from '@/utils/request'

// 查询赛事管理员列表
export function getEventAdmins(eventId) {
  return request({
    url: `/api/admin/events/${eventId}/admins`,
    method: 'get'
  })
}

// 批量指定赛事管理员
export function addEventAdmins(eventId, userIds) {
  return request({
    url: `/api/admin/events/${eventId}/admins`,
    method: 'post',
    data: { userIds }
  })
}

// 移除单个赛事管理员
export function removeEventAdmin(eventId, userId) {
  return request({
    url: `/api/admin/events/${eventId}/admins/${userId}`,
    method: 'delete'
  })
}

// 撤回赛事
export function withdrawEvent(eventId) {
  return request({
    url: `/api/admin/events/${eventId}/withdraw`,
    method: 'post'
  })
}
