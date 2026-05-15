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

export function getProjectsByEventId(eventId) {
  return request({
    url: `/project/event/${eventId}`,
    method: 'get'
  })
}

// ---- 资格规则引擎 ----

export function getEligibilityConfig(itemId) {
  return request({
    url: `/admin/project/${itemId}/eligibility`,
    method: 'get'
  })
}

export function saveEligibilityConfig(itemId, data) {
  return request({
    url: `/admin/project/${itemId}/eligibility`,
    method: 'put',
    data
  })
}

export function deleteEligibilityConfig(itemId) {
  return request({
    url: `/admin/project/${itemId}/eligibility`,
    method: 'delete'
  })
}

/** 当前用户的资格预览 */
export function getEligibilityPreview(itemId) {
  return request({
    url: `/project/${itemId}/eligibility/preview`,
    method: 'get'
  })
}

/** 批量资格检查（列表灰显用） */
export function batchCheckEligibility(itemIds) {
  return request({
    url: '/project/eligibility/batch-check',
    method: 'post',
    data: itemIds
  })
}
