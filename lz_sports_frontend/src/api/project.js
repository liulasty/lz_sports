import request from '@/utils/request'

export function getProjectList(params) {
  return request({
    url: '/sports/project/page',
    method: 'get',
    params
  })
}

export function addProject(data) {
  return request({
    url: '/sports/project',
    method: 'post',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/sports/project/${id}`,
    method: 'delete'
  })
}

export function updateProject(id, data) {
  return request({
    url: `/sports/project/${id}`,
    method: 'put',
    data
  })
}

export function getProjectById(id) {
  return request({
    url: `/sports/project/${id}`,
    method: 'get'
  })
}
