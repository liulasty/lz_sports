import request, { apiRequest } from '@/utils/request'

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

export function refuseRegistration(id, reason) {
  return request({
    url: `/registration/refuse/${id}`,
    method: 'put',
    params: { reason }
  })
}

export function applyProject(projectId) {
  return apiRequest({
    url: `/registration/apply/${projectId}`,
    method: 'post'
  }, '提交报名')
}

export function cancelRegistration(id) {
  return apiRequest({
    url: `/registration/${id}`,
    method: 'delete'
  }, '取消报名')
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
