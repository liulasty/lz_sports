import request from '@/utils/request'

export function getRegistrationList(params) {
  return request({
    url: '/sports/registration/page',
    method: 'get',
    params
  })
}

export function approveRegistration(id) {
  return request({
    url: `/sports/registration/attend/${id}`,
    method: 'put'
  })
}

export function refuseRegistration(id) {
  return request({
    url: `/sports/registration/refuse/${id}`,
    method: 'put'
  })
}

export function applyProject(projectId) {
  return request({
    url: `/sports/registration/apply/${projectId}`,
    method: 'post'
  })
}
