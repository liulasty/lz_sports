import { describe, expect, it } from 'vitest'
import { findDeptPathLabels, getDeptCascaderLabels } from '@/utils/departmentTree'

describe('departmentTree', () => {
  const tree = [
    {
      value: -1,
      label: '计算机学院',
      children: [
        {
          value: -2,
          label: '本科',
          children: [
            { value: 10, label: '大一' },
            { value: 11, label: '大二' }
          ]
        }
      ]
    }
  ]

  it('builds path labels for leaf dept', () => {
    expect(findDeptPathLabels(tree, 11)).toEqual(['计算机学院', '本科', '大二'])
  })

  it('returns university cascader copy', () => {
    const meta = getDeptCascaderLabels('UNIVERSITY')
    expect(meta.levels).toEqual(['学院', '专业', '班级'])
  })
})
