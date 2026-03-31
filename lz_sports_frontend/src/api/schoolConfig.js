import request from '@/utils/request'

export function updateSchoolConfig(data) {
  return request({
    url: '/admin/school-config',
    method: 'put',
    data
  })
}

export function uploadSchoolLogo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/school-config/logo',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function resetSystem() {
  return request({
    url: '/admin/school-config/reset',
    method: 'post'
  })
}
