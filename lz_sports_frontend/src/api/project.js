import request from '@/utils/request'

export function getProjectList(params) {
  return request({
    url: '/project/page',
    method: 'get',
    params
  })
}

export function addProject(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}

export function updateProject(id, data) {
  return request({
    url: `/project/${id}`,
    method: 'put',
    data
  })
}

export function getProjectById(id) {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}
