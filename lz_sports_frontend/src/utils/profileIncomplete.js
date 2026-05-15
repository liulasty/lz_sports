export const PROFILE_INCOMPLETE_KEYWORD = '请先在个人中心完善身份信息'

export function isProfileIncompleteMessage(message) {
  return typeof message === 'string' && message.includes(PROFILE_INCOMPLETE_KEYWORD)
}

export function extractApiMessage(response) {
  return response?.msg || response?.message || ''
}

export function isProfileComplete(user = {}) {
  return Boolean(
    user.name?.trim?.() &&
    user.gender?.trim?.() &&
    user.contact?.trim?.() &&
    user.deptId
  )
}

export function findDeptLabel(tree = [], deptId) {
  if (!deptId) return ''
  for (const node of tree) {
    if (node.value === deptId) return node.label || ''
    if (node.children?.length) {
      const childLabel = findDeptLabel(node.children, deptId)
      if (childLabel) return childLabel
    }
  }
  return ''
}
