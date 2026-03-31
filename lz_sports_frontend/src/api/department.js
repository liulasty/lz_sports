import request from '../utils/request'

export function getDepartmentTree() {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}