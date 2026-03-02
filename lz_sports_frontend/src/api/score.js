import request from '@/utils/request'

export function exportRegistration(eventId) {
  return request({
    url: `/sports/registration/export/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function importScores(eventId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/sports/score/import/${eventId}`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function publishScores(eventId) {
  return request({
    url: `/sports/score/publish/${eventId}`,
    method: 'put'
  })
}
