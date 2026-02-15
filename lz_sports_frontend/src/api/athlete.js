import request from '@/utils/request'

export function applyAthlete(data) {
  return request({
    url: '/sports/athlete',
    method: 'post',
    data
  })
}

export function getAthleteApply(id) {
  return request({
    url: `/sports/athlete/apply/${id}`,
    method: 'get'
  })
}

export function getAthleteInfo(id) {
  return request({
    url: `/sports/athlete/${id}`,
    method: 'get'
  })
}

export function updateAthlete(id, data) {
  return request({
    url: `/sports/athlete/${id}`,
    method: 'put',
    data
  })
}

export function deleteAthleteRecord(id) {
  return request({
    url: `/sports/athlete/${id}`,
    method: 'delete'
  })
}
