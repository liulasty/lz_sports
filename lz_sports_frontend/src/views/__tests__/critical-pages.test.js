import { defineComponent, h, nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import LoginPage from '@/views/login/index.vue'
import RegisterPage from '@/views/register/index.vue'
import ScorePage from '@/views/score/index.vue'

const mocks = vi.hoisted(() => ({
  pushMock: vi.fn(),
  replaceMock: vi.fn(),
  setTokenMock: vi.fn(),
  setUserInfoMock: vi.fn(),
  fetchConfigMock: vi.fn(),
  warningMock: vi.fn(),
  checkInitMock: vi.fn(),
  sendCodeMock: vi.fn(),
  verifyCodeMock: vi.fn(),
  getMyScoresMock: vi.fn(),
  getEventListMock: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mocks.pushMock,
    replace: mocks.replaceMock
  })
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    setToken: mocks.setTokenMock,
    setUserInfo: mocks.setUserInfoMock
  })
}))

vi.mock('@/stores/config', () => ({
  useConfigStore: () => ({
    logoUrl: '',
    schoolName: '测试学校',
    fetchConfig: mocks.fetchConfigMock
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    warning: mocks.warningMock,
    success: vi.fn(),
    error: vi.fn()
  }
}))

vi.mock('@/api/init', () => ({
  checkInit: (...args) => mocks.checkInitMock(...args)
}))

vi.mock('@/api/user', () => ({
  login: vi.fn(),
  register: vi.fn()
}))

vi.mock('@/api/auth', () => ({
  sendCode: (...args) => mocks.sendCodeMock(...args),
  verifyCode: (...args) => mocks.verifyCodeMock(...args)
}))

vi.mock('@/api/score', () => ({
  getMyScores: (...args) => mocks.getMyScoresMock(...args)
}))

vi.mock('@/api/event', () => ({
  getEventList: (...args) => mocks.getEventListMock(...args)
}))

vi.mock('@/utils/result', () => ({
  isSuccess: (res) => res?.code === 200
}))

const ElFormStub = defineComponent({
  props: {
    model: { type: Object, default: () => ({}) }
  },
  setup(props, { slots, expose }) {
    expose({
      validate(callback) {
        callback(true)
      }
    })
    return () => h('form', { class: 'el-form-stub' }, slots.default?.())
  }
})

const ElFormItemStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', { class: 'el-form-item-stub' }, slots.default?.())
  }
})

const ElInputStub = defineComponent({
  props: {
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  setup(props, { emit, attrs }) {
    return () =>
      h('input', {
        class: 'el-input-stub',
        value: props.modelValue,
        placeholder: attrs.placeholder || '',
        onInput: (e) => emit('update:modelValue', e.target.value)
      })
  }
})

const ElButtonStub = defineComponent({
  emits: ['click'],
  setup(_, { slots, emit, attrs }) {
    return () =>
      h(
        'button',
        {
          class: attrs.class || 'el-button-stub',
          type: 'button',
          onClick: (e) => emit('click', e)
        },
        slots.default?.()
      )
  }
})

const ElSelectStub = defineComponent({
  props: {
    modelValue: { type: [String, Number, null], default: null }
  },
  emits: ['update:modelValue'],
  setup(props, { slots }) {
    return () => h('div', { class: 'el-select-stub', 'data-value': props.modelValue ?? '' }, slots.default?.())
  }
})

const ElOptionStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', { class: 'el-option-stub' }, slots.default?.())
  }
})

const ElTableStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', { class: 'el-table-stub' }, slots.default?.())
  }
})

const ElTableColumnStub = defineComponent({
  setup() {
    return () => h('div', { class: 'el-table-column-stub' })
  }
})

const mountWithStubs = (component) =>
  mount(component, {
    global: {
      config: {
        globalProperties: {
          $router: {
            push: mocks.pushMock,
            replace: mocks.replaceMock
          }
        }
      },
      stubs: {
        ElForm: ElFormStub,
        ElFormItem: ElFormItemStub,
        ElInput: ElInputStub,
        ElButton: ElButtonStub,
        ElSelect: ElSelectStub,
        ElOption: ElOptionStub,
        ElTable: ElTableStub,
        ElTableColumn: ElTableColumnStub
      },
      directives: {
        loading: {}
      }
    }
  })

describe('关键页面最小单测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.checkInitMock.mockResolvedValue({ data: true })
    mocks.sendCodeMock.mockResolvedValue({ code: 200, data: 'verify-token' })
    mocks.verifyCodeMock.mockResolvedValue({ code: 200, data: 'register-token' })
    mocks.getMyScoresMock.mockResolvedValue({ code: 200, data: [] })
    mocks.getEventListMock.mockResolvedValue({ code: 200, data: { records: [] } })
  })

  it('登录页可渲染并可跳转注册页', async () => {
    const wrapper = mountWithStubs(LoginPage)
    await nextTick()

    expect(wrapper.text()).toContain('欢迎回来，请登录您的账户')
    expect(mocks.fetchConfigMock).toHaveBeenCalled()

    const toRegisterBtn = wrapper
      .findAll('button')
      .find((btn) => btn.text().includes('立即注册'))
    expect(toRegisterBtn).toBeTruthy()
    await toRegisterBtn.trigger('click')
    expect(mocks.pushMock).toHaveBeenCalledWith('/register')
  })

  it('报名页在空邮箱时点击发送验证码会提示', async () => {
    const wrapper = mountWithStubs(RegisterPage)
    await nextTick()

    expect(wrapper.text()).toContain('创建账号')

    const sendBtn = wrapper
      .findAll('button')
      .find((btn) => btn.text().includes('发送验证码'))
    expect(sendBtn).toBeTruthy()
    await sendBtn.trigger('click')

    expect(mocks.warningMock).toHaveBeenCalledWith('请先输入QQ号')
  })

  it('成绩页可渲染并触发查询接口', async () => {
    const wrapper = mountWithStubs(ScorePage)
    await nextTick()
    await Promise.resolve()

    expect(wrapper.text()).toContain('我的成绩')
    expect(mocks.getMyScoresMock).toHaveBeenCalled()

    const queryBtn = wrapper
      .findAll('button')
      .find((btn) => btn.text().includes('查询'))
    expect(queryBtn).toBeTruthy()
    await queryBtn.trigger('click')

    expect(mocks.getMyScoresMock).toHaveBeenCalledTimes(2)
  })
})
