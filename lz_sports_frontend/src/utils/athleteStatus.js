const ATHLETE_STATUS_ALIASES = {
  PENDING: ['PENDING', '申请中', '审核中', '待审核'],
  APPROVED: ['APPROVED', 'SUCCESS', '成功', '通过', '已通过', '审核通过'],
  REJECTED: ['REJECTED', '拒绝', '未通过', '已拒绝', '审核拒绝']
}

const ATHLETE_STATUS_TEXT = {
  PENDING: '审核中',
  APPROVED: '审核通过',
  REJECTED: '审核拒绝'
}

export function normalizeAthleteStatus(status) {
  if (!status) return ''
  const rawStatus = String(status).trim()
  if (!rawStatus) return ''
  const upperStatus = rawStatus.toUpperCase()

  if (Object.prototype.hasOwnProperty.call(ATHLETE_STATUS_ALIASES, upperStatus)) {
    return upperStatus
  }

  for (const [targetStatus, aliases] of Object.entries(ATHLETE_STATUS_ALIASES)) {
    if (aliases.includes(rawStatus) || aliases.includes(upperStatus)) {
      return targetStatus
    }
  }

  return rawStatus
}

export function getAthleteStatusText(status) {
  const normalizedStatus = normalizeAthleteStatus(status)
  return ATHLETE_STATUS_TEXT[normalizedStatus] || normalizedStatus
}

export function getAthleteStatusType(status) {
  const normalizedStatus = normalizeAthleteStatus(status)
  if (normalizedStatus === 'APPROVED') return 'success'
  if (normalizedStatus === 'PENDING') return 'warning'
  return 'error'
}

