import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function sendCode(email) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    params: { email }
  })
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

export function examinePlayer(id) {
  return request({
    url: `/auth/examine/${id}`,
    method: 'put'
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
