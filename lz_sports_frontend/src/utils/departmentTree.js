export function isK12OrgMode(orgMode) {
  const mode = (orgMode || '').toUpperCase()
  return mode === 'K12' || mode === 'HIGH_SCHOOL'
}

export function getDeptCascaderLabels(orgMode) {
  if (isK12OrgMode(orgMode)) {
    return {
      title: '年级与班级',
      placeholder: '请选择年级，再选择班级（如九年级 → 3班）',
      levels: ['年级', '班级'],
      hint: 'K12 模式：先选年级（如九年级、高一），再选具体班级（如 1班、2班、3班）。'
    }
  }
  return {
    title: '学院与班级',
    placeholder: '请选择学院 → 专业 → 班级（如计算机学院 → 本科 → 大一）',
    levels: ['学院', '专业', '班级'],
    hint: '大学模式：选到最末级「班级」即可，例如 计算机学院 → 本科 → 大一。'
  }
}

export function findDeptLabel(tree = [], deptId) {
  if (!deptId) return ''
  for (const node of tree) {
    if (node.value === deptId) return node.label || ''
    if (node.children?.length) {
      const child = findDeptLabel(node.children, deptId)
      if (child) {
        return `${node.label} / ${child}`
      }
    }
  }
  return ''
}

export function findDeptPathLabels(tree = [], deptId) {
  const walk = (nodes, trail = []) => {
    for (const node of nodes) {
      const next = [...trail, node.label]
      if (node.value === deptId) return next
      if (node.children?.length) {
        const found = walk(node.children, next)
        if (found.length) return found
      }
    }
    return []
  }
  return walk(tree)
}
