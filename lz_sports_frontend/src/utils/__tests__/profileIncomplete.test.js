import { describe, expect, it } from 'vitest'
import {
  extractApiMessage,
  isProfileComplete,
  isProfileIncompleteMessage
} from '@/utils/profileIncomplete'

describe('profileIncomplete', () => {
  it('detects incomplete profile message from backend', () => {
    const msg = '请先在个人中心完善身份信息（姓名、性别、联系方式、部门/班级）后，再申请运动员认证'
    expect(isProfileIncompleteMessage(msg)).toBe(true)
  })

  it('reads msg field from api response', () => {
    expect(extractApiMessage({ msg: '保存失败' })).toBe('保存失败')
  })

  it('checks profile completeness', () => {
    expect(isProfileComplete({ name: '张三', gender: '男', contact: '13800000000', deptId: 1 })).toBe(true)
    expect(isProfileComplete({ name: '张三', gender: '男', contact: '13800000000' })).toBe(false)
  })
})
