import request from '@/utils/request'

export function applyAthlete(data) {
  return request({
    url: '/athlete',
    method: 'post',
    data
  })
}

export function getAthleteApply(id, eventId) {
  return request({
    url: `/athlete/apply/${id}`,
    method: 'get',
    params: { eventId }
  })
}

export function getAthleteInfo(id) {
  return request({
    url: `/athlete/${id}`,
    method: 'get'
  })
}

export function updateAthlete(id, data) {
  return request({
    url: `/athlete/${id}`,
    method: 'put',
    data
  })
}

export function deleteAthleteRecord(id) {
  return request({
    url: `/athlete/${id}`,
    method: 'delete'
  })
}

// 我的所有运动员申请记录
export function getMyApplications() {
  return request({
    url: '/athlete/my-applications',
    method: 'get'
  })
}

