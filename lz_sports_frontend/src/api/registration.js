import request, { apiRequest } from '@/utils/request'

export function getRegistrationList(params) {
  return request({
    url: '/registration/page',
    method: 'get',
    params
  })
}

export function approveRegistration(id, eventId) {
  return request({
    url: `/registration/attend/${id}`,
    method: 'put',
    params: { eventId }
  })
}

export function refuseRegistration(id, eventId) {
  return request({
    url: `/registration/refuse/${id}`,
    method: 'put',
    params: { eventId }
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

export function batchAuditRegistration(ids, approve, eventId) {
  return request({
    url: '/registration/batch-audit',
    method: 'put',
    data: ids,
    params: { approve, eventId }
  })
}

export function exportRegistrationList(eventId) {
  return request({
    url: `/registration/export/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}
