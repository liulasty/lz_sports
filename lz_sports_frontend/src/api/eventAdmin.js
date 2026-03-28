import request from '@/utils/request'

// 运动员申请列表（按赛事）
export function getAthleteApplications(eventId, params) {
  return request({
    url: `/event-admin/${eventId}/athlete-applications`,
    method: 'get',
    params
  })
}

// 审核通过单条运动员申请
export function approveApplication(eventId, applicationId) {
  return request({
    url: `/event-admin/${eventId}/athlete-applications/${applicationId}/approve`,
    method: 'post'
  })
}

// 审核拒绝单条运动员申请
export function rejectApplication(eventId, applicationId, rejectReason) {
  return request({
    url: `/api/event-admin/${eventId}/athlete-applications/${applicationId}/reject`,
    method: 'post',
    data: { rejectReason }
  })
}

// 批量审核通过
export function batchApproveApplications(eventId, applicationIds) {
  return request({
    url: `/event-admin/${eventId}/athlete-applications/batch-approve`,
    method: 'post',
    data: applicationIds
  })
}

// 报名统计
export function getRegistrationStats(eventId) {
  return request({
    url: `/event-admin/${eventId}/registrations/stats`,
    method: 'get'
  })
}
