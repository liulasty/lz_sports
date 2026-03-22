import request from '@/utils/request'

export function getEventList(params) {
  return request({
    url: '/event/page',
    method: 'get',
    params
  })
}

export function addEvent(data) {
  return request({
    url: '/event',
    method: 'post',
    data
  })
}

export function deleteEvent(id) {
  return request({
    url: `/event/${id}`,
    method: 'delete'
  })
}

export function updateEvent(id, data) {
  return request({
    url: `/event/${id}`,
    method: 'put',
    data
  })
}

export function changeEventStatus(id, status) {
  return request({
    url: `/event/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getEventById(id) {
  return request({
    url: `/event/${id}`,
    method: 'get'
  })
}

export function getNewTenEvents() {
  return request({
    url: '/event/newTen',
    method: 'get'
  })
}

export function getEventTypes() {
  return request({
    url: '/event/getEventType',
    method: 'get'
  })
}

export function getChartData(date) {
  return request({
    url: `/event/chart/${date}`,
    method: 'get'
  })
}

export function getTotalEvents() {
  return request({
    url: '/event/total',
    method: 'get'
  })
}
