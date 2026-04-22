import request, { apiRequest } from '@/utils/request'

export function exportRegistration(eventId) {
  return request({
    url: `/score/export-registration/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function exportScore(eventId) {
  return request({
    url: `/score/export/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function downloadScoreTemplate(eventId) {
  return request({
    url: `/score/template/${eventId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function importScores(eventId, file, mode = 'BEST_EFFORT') {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/score/import/${eventId}`,
    method: 'post',
    data: formData,
    params: { mode },
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function publishScores(eventId) {
  return request({
    url: `/score/publish/${eventId}`,
    method: 'put'
  })
}

export function getScorePage(params) {
  return request({
    url: '/score/page',
    method: 'get',
    params
  })
}

export function getScoreEntryCandidates(eventId, itemId) {
  return request({
    url: `/score/entry-candidates/${eventId}`,
    method: 'get',
    params: { itemId }
  })
}

export function upsertScore(data) {
  return request({
    url: '/score/upsert',
    method: 'post',
    data
  })
}

export function updateScore(scoreId, data) {
  return request({
    url: `/score/${scoreId}`,
    method: 'put',
    data
  })
}

export function getMyScores(params) {
  return apiRequest({
    url: '/score/my',
    method: 'get',
    params
  }, '查询我的成绩')
}

export function getPublicScores(eventId) {
  return request({
    url: `/score/public/${eventId}`,
    method: 'get'
  })
}
