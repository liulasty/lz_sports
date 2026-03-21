import request from '@/utils/request'

export function getAdminUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function changeUserRole(id, role) {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    params: { role }
  })
}

export function disableUser(id) {
  return request({
    url: `/admin/users/${id}/disable`,
    method: 'post'
  })
}

export function enableUser(id) {
  return request({
    url: `/admin/users/${id}/enable`,
    method: 'post'
  })
}
