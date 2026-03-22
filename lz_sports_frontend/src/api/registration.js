import request from '@/utils/request'

export function getRegistrationList(params) {
  return request({
    url: '/registration/page',
    method: 'get',
    params
  })
}

export function approveRegistration(id) {
  return request({
    url: `/registration/attend/${id}`,
    method: 'put'
  })
}

export function refuseRegistration(id) {
  return request({
    url: `/registration/refuse/${id}`,
    method: 'put'
  })
}

export function applyProject(projectId) {
  return request({
    url: `/registration/apply/${projectId}`,
    method: 'post'
  })
}

export function cancelRegistration(id) {
  return request({
    url: `/registration/${id}`,
    method: 'delete'
  })
}

export function batchAuditRegistration(ids, approve) {
  return request({
    url: '/registration/batch-audit',
    method: 'put',
    data: ids,
    params: { approve }
  })
}

export function exportRegistrationList(eventId) {
  return request({
    url: `/registration/export/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}
