import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/sports/user/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/sports/user/register',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/sports/user/logout',
    method: 'delete'
  })
}

export function getUserInfo() {
  return request({
    url: '/sports/user/info',
    method: 'get'
  })
}

export function updateUser(data) {
  return request({
    url: '/sports/user/update',
    method: 'post',
    data
  })
}

export function getUserList(data) {
  return request({
    url: '/sports/user/list',
    method: 'post',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/sports/user/${id}`,
    method: 'delete'
  })
}

export function examinePlayer(id) {
  return request({
    url: `/sports/user/examine/${id}`,
    method: 'put'
  })
}
