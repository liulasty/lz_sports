import request from '@/utils/request'

export function getEventList(params) {
  return request({
    url: '/sports/event/page',
    method: 'get',
    params
  })
}

export function addEvent(data) {
  return request({
    url: '/sports/event/EventList',
    method: 'post',
    data
  })
}

export function deleteEvent(id) {
  return request({
    url: `/sports/event/${id}`,
    method: 'delete'
  })
}

export function updateEvent(id, data) {
  return request({
    url: `/sports/event/${id}`,
    method: 'put',
    data
  })
}

export function changeEventStatus(id, status) {
  return request({
    url: `/sports/event/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getEventById(id) {
  return request({
    url: `/sports/event/${id}`,
    method: 'get'
  })
}

export function getNewTenEvents() {
  return request({
    url: '/sports/event/newTen',
    method: 'get'
  })
}

export function getEventTypes() {
  return request({
    url: '/sports/event/getEventType',
    method: 'get'
  })
}

export function getChartData(date) {
  return request({
    url: `/sports/event/chart/${date}`,
    method: 'get'
  })
}

export function getTotalEvents() {
  return request({
    url: '/sports/event/total',
    method: 'get'
  })
}
