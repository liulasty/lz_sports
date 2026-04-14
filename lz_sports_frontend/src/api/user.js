import request, { apiRequest } from '@/utils/request'

export function login(data) {
  return apiRequest({
    url: '/auth/login',
    method: 'post',
    data
  }, '登录')
}

export function register(data, registerToken) {
  return apiRequest({
    url: '/auth/register',
    method: 'post',
    data,
    headers: {
      'Authorization': `Bearer ${registerToken}`
    }
  }, '注册')
}

export function auditUser(userId, status, reason) {
  return request({
    url: `/auth/audit/${userId}`,
    method: 'put',
    params: { status, reason }
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'delete'
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

export function updateUser(data) {
  return request({
    url: '/auth/update',
    method: 'post',
    data
  })
}

export const updateUserInfo = updateUser

export function getUserList(data) {
  return request({
    url: '/auth/list',
    method: 'post',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/auth/${id}`,
    method: 'delete'
  })
}

export function getUserNumsByMonth(month) {
  return request({
    url: '/auth/getUserNumsByMonth',
    method: 'get',
    params: { month }
  })
}

export function getUserTypes() {
  return request({
    url: '/auth/getUserType',
    method: 'get'
  })
}

export function getNums() {
  return request({
    url: '/auth/getNums',
    method: 'get'
  })
}
