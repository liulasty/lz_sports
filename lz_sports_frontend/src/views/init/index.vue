<template>
  <div class="init-container">
    <div class="bg-layer">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="init-wrapper">
      <div class="intro-panel">
        <div class="brand-badge">SYSTEM SETUP</div>
        <h1 class="brand-title">
          启动你的<br />
          <span>体育管理平台</span>
        </h1>
        <p class="brand-sub">
          只需 4 个步骤，即可完成学校基础配置、管理员设置与组织结构初始化，
          后续可在后台持续调整。
        </p>
        <div class="intro-stats">
          <div class="stat">
            <span class="stat-num">4</span>
            <span class="stat-label">初始化步骤</span>
          </div>
          <div class="stat">
            <span class="stat-num">3min</span>
            <span class="stat-label">平均耗时</span>
          </div>
          <div class="stat">
            <span class="stat-num">0</span>
            <span class="stat-label">复杂部署</span>
          </div>
        </div>
      </div>

      <el-card class="wizard-card lz-ep-dark">
        <div v-if="pageMode === 'loading'" class="state-panel">
          <el-icon class="state-icon is-loading"><Loading /></el-icon>
          <p class="state-text">正在检查系统状态…</p>
        </div>

        <div v-else-if="pageMode === 'already_initialized'" class="state-panel">
          <div class="finish-icon">✓</div>
          <h3 class="finish-title">系统已完成初始化</h3>
          <p class="finish-subtitle">当前环境已配置完毕，请使用管理员账号登录系统。</p>
          <div class="step-footer lz-actions state-actions">
            <el-button type="primary" @click="goLogin">前往登录</el-button>
          </div>
        </div>

        <div v-else-if="pageMode === 'success'" class="state-panel">
          <div class="finish-icon">✓</div>
          <h3 class="finish-title">初始化成功</h3>
          <p class="finish-subtitle">系统已就绪，{{ redirectCountdown }} 秒后自动跳转至登录页。</p>
          <div class="step-footer lz-actions state-actions">
            <el-button type="primary" @click="goLogin">立即登录</el-button>
          </div>
        </div>

        <template v-else>
        <div class="wizard-header card-header">
          <div>
            <h2 class="card-title">系统初始化向导</h2>
            <p class="card-subtitle">{{ stepDescriptions[active] }}</p>
          </div>
          <div class="step-index">STEP {{ active + 1 }} / 4</div>
        </div>

        <el-steps :active="active" finish-status="success" align-center class="init-steps">
          <el-step title="学校信息" />
          <el-step title="管理员设置" />
          <el-step title="基础数据" />
          <el-step title="完成" />
        </el-steps>

        <div class="step-content">
          <!-- Step 1: School Info -->
          <el-form v-if="active === 0" :model="form" label-position="top" :rules="rules" ref="step1Form" class="lz-form setup-form">
            <el-form-item label="学校名称" prop="schoolName">
              <el-input v-model="form.schoolName" placeholder="例如：xx大学" />
            </el-form-item>
            <el-form-item label="组织模式" prop="orgMode">
              <el-radio-group v-model="form.orgMode" @change="handleModeChange">
                <el-radio value="UNIVERSITY">大学模式 (学院-专业-班级)</el-radio>
                <el-radio value="HIGH_SCHOOL">K12模式 (年级-班级)</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="Logo 链接" prop="logoUrl">
              <el-input v-model="form.logoUrl" placeholder="https://example.com/logo.png" />
            </el-form-item>
            <el-form-item label="主题色" prop="themeColor">
              <el-color-picker v-model="form.themeColor" />
            </el-form-item>
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="admin@school.edu.cn" />
            </el-form-item>
          </el-form>

          <!-- Step 2: Admin Info -->
          <el-form v-if="active === 1" :model="form" label-position="top" :rules="rules" ref="step2Form" class="lz-form setup-form">
            <el-form-item label="管理员账号" prop="adminUsername">
              <el-input v-model="form.adminUsername" />
            </el-form-item>
            <el-form-item label="管理员密码" prop="adminPassword">
              <el-input v-model="form.adminPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="form.adminEmail" />
            </el-form-item>
          </el-form>

          <!-- Step 3: Grades -->
          <div v-if="active === 2">
            <div class="grade-toolbar">
              <span>快速填充模板：</span>
              <el-radio-group v-model="selectedTemplate" @change="applyTemplate">
                <el-radio-button v-for="tpl in currentTemplates" :key="tpl.name" :value="tpl.name">
                  {{ tpl.name }}
                </el-radio-button>
              </el-radio-group>
            </div>
            <p class="grade-tip">请确认顶级部门/院系列表（可添加或删除）。保存后将自动为每个学院/年级生成默认班级（大学：大一至大四；K12：1班至3班）。</p>
            <div class="grade-tags">
              <el-tag
                v-for="tag in form.grades"
                :key="tag"
                closable
                :disable-transitions="false"
                @close="handleClose(tag)"
                class="grade-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
            <el-input
              v-if="inputVisible"
              ref="InputRef"
              v-model="inputValue"
              class="input-new-tag"
              size="small"
              @keyup.enter="handleInputConfirm"
              @blur="handleInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showInput">
              + 添加部门
            </el-button>
          </div>

          <!-- Step 4: Finish -->
          <div v-if="active === 3" class="finish-panel">
            <div class="finish-icon">✓</div>
            <h3 class="finish-title">确认初始化</h3>
            <p class="finish-subtitle">请确认以上信息无误，点击下方按钮开始初始化系统。</p>
            <div class="summary-panel">
              <h4 class="summary-heading">配置摘要</h4>
              <div class="summary-list">
                <div v-for="item in summaryItems" :key="item.label" class="summary-row">
                  <span class="summary-label">{{ item.label }}</span>
                  <span class="summary-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="step-footer lz-actions">
          <el-button @click="prev" v-if="active > 0">上一步</el-button>
          <el-button type="primary" @click="next" v-if="active < 3">下一步</el-button>
          <el-button type="primary" @click="submit" v-if="active === 3" :loading="loading">完成初始化</el-button>
          <el-button v-if="active < 3" class="ghost-btn" @click="goLogin">返回登录</el-button>
        </div>
        </template>
      </el-card>
    </div>
  </div>
 </template>

<script setup>
import { ref, reactive, nextTick, computed, onMounted, onBeforeUnmount } from 'vue'
import { initSystem, checkInit } from '@/api/init'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const pageMode = ref('loading')
const redirectCountdown = ref(3)
const active = ref(0)
let redirectTimer = null
let countdownTimer = null
const step1Form = ref(null)
const step2Form = ref(null)
const loading = ref(false)

const templates = {
  HIGH_SCHOOL: [
    { name: '小学', grades: ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级'] },
    { name: '初中', grades: ['初一', '初二', '初三'] },
    { name: '高中', grades: ['高一', '高二', '高三'] },
    { name: '九年一贯制', grades: ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级', '初一', '初二', '初三'] }
  ],
  UNIVERSITY: [
    { name: '通用大学', grades: ['计算机学院', '理学院', '外国语学院', '体育部'] },
    { name: '医科大学', grades: ['基础医学院', '临床医学院', '药学院', '护理学院'] },
    { name: '师范大学', grades: ['教育学院', '文学院', '历史学院', '马克思主义学院'] }
  ]
}

const form = reactive({
  schoolName: '',
  logoUrl: '',
  themeColor: '#409EFF',
  contactEmail: '',
  adminUsername: 'admin',
  adminPassword: '',
  adminEmail: '',
  orgMode: 'UNIVERSITY',
  grades: [...templates.UNIVERSITY[0].grades]
})

const selectedTemplate = ref(templates.UNIVERSITY[0].name)
const stepDescriptions = [
  '填写学校基础信息并选择组织架构模式',
  '创建系统超级管理员账户',
  '确认默认院系/年级模板并可自定义',
  '校验配置并一键完成系统初始化'
]

const summaryItems = computed(() => [
  { label: '学校名称', value: form.schoolName || '—' },
  { label: '管理员账号', value: form.adminUsername || '—' },
  { label: '部门数量', value: String(form.grades.length) }
])

const currentTemplates = computed(() => {
  return templates[form.orgMode] || []
})

const applyTemplate = (tplName) => {
  const tpl = currentTemplates.value.find(t => t.name === tplName)
  if (tpl) {
    form.grades = [...tpl.grades]
  }
}

const handleModeChange = (val) => {
  if (val === 'UNIVERSITY') {
    selectedTemplate.value = templates.UNIVERSITY[0].name
    form.grades = [...templates.UNIVERSITY[0].grades]
  } else {
    selectedTemplate.value = templates.HIGH_SCHOOL[0].name
    form.grades = [...templates.HIGH_SCHOOL[0].grades]
  }
}

const rules = {
  schoolName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  adminUsername: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  adminPassword: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }],
  adminEmail: [{ required: true, message: '请输入管理员邮箱', trigger: 'blur' }]
}

const prev = () => {
  if (active.value-- < 0) active.value = 0
}

const next = async () => {
  if (active.value === 0) {
    if (!step1Form.value) return
    await step1Form.value.validate((valid) => {
      if (valid) active.value++
    })
  } else if (active.value === 1) {
    if (!step2Form.value) return
    await step2Form.value.validate((valid) => {
      if (valid) active.value++
    })
  } else if (active.value === 2) {
    active.value++
  }
}

// Tag logic
const inputValue = ref('')
const inputVisible = ref(false)
const InputRef = ref()

const handleClose = (tag) => {
  form.grades.splice(form.grades.indexOf(tag), 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    InputRef.value.input.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value) {
    form.grades.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

const goLogin = () => {
  if (redirectTimer) {
    clearTimeout(redirectTimer)
    redirectTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  router.replace('/login')
}

const startRedirectCountdown = () => {
  redirectCountdown.value = 3
  countdownTimer = setInterval(() => {
    redirectCountdown.value -= 1
    if (redirectCountdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
  redirectTimer = setTimeout(() => {
    goLogin()
  }, 3000)
}

const markInitialized = () => {
  localStorage.setItem('isInitialized', 'true')
}

const resolveInitStatus = async () => {
  if (localStorage.getItem('isInitialized') === 'true') {
    return true
  }
  try {
    const { data } = await checkInit()
    if (data === true) {
      markInitialized()
      return true
    }
  } catch (error) {
    console.error('Init status check failed', error)
  }
  return false
}

onMounted(async () => {
  const initialized = await resolveInitStatus()
  pageMode.value = initialized ? 'already_initialized' : 'wizard'
})

onBeforeUnmount(() => {
  if (redirectTimer) clearTimeout(redirectTimer)
  if (countdownTimer) clearInterval(countdownTimer)
})

const submit = () => {
  loading.value = true
  initSystem({ ...form }).then(() => {
    markInitialized()
    pageMode.value = 'success'
    ElMessage.success('初始化成功！')
    startRedirectCountdown()
  }).catch(() => {
    // 错误信息由 request 拦截器统一提示
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

.init-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #0d0f14;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 107, 53, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 107, 53, 0.04) 1px, transparent 1px);
  background-size: 54px 54px;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  animation: drift 14s ease-in-out infinite alternate;
}

.c1 {
  width: 460px;
  height: 460px;
  background: rgba(255, 107, 53, 0.13);
  top: -140px;
  left: -140px;
}

.c2 {
  width: 360px;
  height: 360px;
  background: rgba(99, 140, 255, 0.1);
  right: -120px;
  bottom: -120px;
  animation-direction: alternate-reverse;
}

@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(28px, 16px) scale(1.06); }
}

.init-wrapper {
  position: relative;
  z-index: 1;
  width: min(1120px, 94vw);
  min-height: 640px;
  display: grid;
  grid-template-columns: 0.95fr 1.35fr;
  border-radius: 22px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.08),
    0 40px 90px rgba(0, 0, 0, 0.45),
    0 0 60px rgba(255, 107, 53, 0.12);
}

.intro-panel {
  background: linear-gradient(145deg, #1a1f2e 0%, #0f1420 100%);
  padding: 56px 42px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  color: #ff6b35;
  border: 1px solid rgba(255, 107, 53, 0.4);
  background: rgba(255, 107, 53, 0.08);
  padding: 5px 12px;
  border-radius: 999px;
  margin-bottom: 26px;
}

.brand-title {
  font-family: 'Bebas Neue', 'Noto Sans SC', sans-serif;
  font-size: 68px;
  line-height: 0.98;
  color: #f0f2f8;
  margin: 0 0 18px;
}

.brand-title span {
  color: #ff6b35;
}

.brand-sub {
  font-size: 14px;
  color: #9ba4b5;
  line-height: 1.9;
  margin: 0 0 44px;
}

.intro-stats {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.stat {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 14px 16px;
}

.stat-num {
  display: block;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 36px;
  color: #ff6b35;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #9ba4b5;
  letter-spacing: 0.08em;
}

.wizard-card {
  border: none;
  border-radius: 0;
  background: #161921;
  --el-bg-color: #161921;
  --el-fill-color-blank: rgba(255, 255, 255, 0.05);
  --el-border-color: rgba(255, 255, 255, 0.12);
  --el-border-color-hover: rgba(255, 107, 53, 0.5);
  --el-text-color-primary: #eef4ff;
  --el-text-color-regular: #c9d1de;
  --el-text-color-secondary: #95a0b3;
  --el-text-color-placeholder: #6f7889;
  --el-color-primary: #ff6b35;
}

.wizard-header {
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-title {
  margin: 0 0 6px;
  font-size: 24px;
  color: #d7deea;
}

.card-subtitle {
  margin: 0;
  font-size: 13px;
  color: #95a0b3;
}

.step-index {
  font-size: 11px;
  color: #ff6b35;
  letter-spacing: 0.12em;
  font-weight: 700;
}

.init-steps {
  margin-bottom: 22px;
  padding: 0 4px;
}

.step-content {
  margin: 22px 0 28px;
  min-height: 265px;
  padding: 0 6px;
}

.setup-form {
  max-width: 520px;
  margin: 0 auto;
}

.grade-toolbar {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: #c0c7d4;
}

.grade-tip {
  color: #9ba4b5;
  margin-bottom: 14px;
}

.grade-tags {
  margin-bottom: 10px;
}

.grade-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.input-new-tag {
  width: 132px;
}

.state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 420px;
  padding: 48px 24px;
  text-align: center;
}

.state-icon {
  font-size: 36px;
  color: #ff6b35;
  margin-bottom: 16px;
}

.state-text {
  margin: 0;
  font-size: 14px;
  color: #95a0b3;
}

.state-actions {
  margin-top: 8px;
}

.finish-panel {
  text-align: center;
  padding-top: 6px;
}

.finish-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.24), rgba(255, 107, 53, 0.1));
  border: 1px solid rgba(255, 107, 53, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff8f66;
  font-size: 28px;
  font-weight: 700;
  box-shadow: 0 8px 28px rgba(255, 107, 53, 0.2);
}

.finish-title {
  margin: 0 0 8px;
  font-size: 24px;
  color: #d7deea;
  letter-spacing: 0.02em;
}

.finish-subtitle {
  margin: 0 0 24px;
  font-size: 14px;
  color: #95a0b3;
}

.summary-panel {
  width: min(92%, 520px);
  margin: 0 auto;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
}

.summary-heading {
  margin: 0;
  padding: 14px 18px;
  font-size: 15px;
  font-weight: 600;
  color: #eef4ff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
}

.summary-list {
  display: flex;
  flex-direction: column;
}

.summary-row {
  display: grid;
  grid-template-columns: 132px 1fr;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  font-size: 13px;
  font-weight: 600;
  color: #95a0b3;
  letter-spacing: 0.02em;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  color: #eef4ff;
  word-break: break-word;
}

.step-footer {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 6px;
}

.ghost-btn {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.2);
  color: #c7ceda;
}

::deep(.el-card__header) {
  border-bottom-color: rgba(255, 255, 255, 0.08);
  padding: 22px 26px 16px;
}

::deep(.el-card__body) {
  padding: 18px 26px 26px;
}

:deep(.init-steps .el-step__title) {
  font-size: 13px;
  line-height: 1.35;
}

:deep(.init-steps .el-step__title.is-wait) {
  color: #95a0b3;
  font-weight: 500;
}

:deep(.init-steps .el-step__title.is-process) {
  color: #eef4ff;
  font-weight: 600;
}

:deep(.init-steps .el-step__title.is-success) {
  color: #c9d1de;
}

:deep(.init-steps .el-step__head.is-wait .el-step__icon) {
  border-color: rgba(255, 255, 255, 0.22);
  color: #95a0b3;
  background: rgba(255, 255, 255, 0.04);
}

:deep(.init-steps .el-step__head.is-process .el-step__icon) {
  border-color: #ff6b35;
  background: rgba(255, 107, 53, 0.15);
  color: #ff8f66;
}

:deep(.init-steps .el-step__head.is-success .el-step__icon) {
  border-color: rgba(255, 107, 53, 0.55);
  color: #ff8f66;
}

:deep(.init-steps .el-step__line) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.init-steps .el-step__line-inner) {
  border-color: #ff6b35;
}

:deep(.setup-form .el-form-item__label) {
  color: #c9d1de !important;
  font-weight: 600;
  font-size: 14px;
  padding-bottom: 6px;
}

:deep(.setup-form .el-form-item__label::before) {
  color: #ff8a6b !important;
}

:deep(.setup-form .el-radio__label) {
  color: #c9d1de;
  font-size: 14px;
}

:deep(.setup-form .el-radio.is-checked .el-radio__label) {
  color: #ff8f66;
}

:deep(.setup-form .el-radio__inner) {
  border-color: rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.04);
}

:deep(.setup-form .el-radio__input.is-checked .el-radio__inner) {
  border-color: #ff6b35;
  background: #ff6b35;
}

:deep(.setup-form .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
  border-radius: 10px;
  min-height: 42px;
}

:deep(.setup-form .el-input__wrapper:hover) {
  border-color: rgba(255, 107, 53, 0.45) !important;
}

:deep(.setup-form .el-input__wrapper.is-focus) {
  border-color: #ff6b35 !important;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.18) !important;
}

:deep(.setup-form .el-input__inner) {
  color: #eef4ff !important;
}

:deep(.setup-form .el-input__inner::placeholder) {
  color: #6f7889;
}

:deep(.setup-form .el-color-picker__trigger) {
  border-color: rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.05);
}

:deep(.grade-toolbar .el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.14);
  color: #c9d1de;
  box-shadow: none;
}

:deep(.grade-toolbar .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(255, 107, 53, 0.18);
  border-color: rgba(255, 107, 53, 0.55);
  color: #ff8f66;
  box-shadow: none;
}

:deep(.grade-tag) {
  background: rgba(255, 107, 53, 0.12);
  border-color: rgba(255, 107, 53, 0.35);
  color: #eef4ff;
}

:deep(.button-new-tag) {
  border-color: rgba(255, 255, 255, 0.2);
  color: #c9d1de;
  background: transparent;
}

:deep(.step-footer .el-button--primary) {
  background: linear-gradient(135deg, #ff6b35, #e0541e);
  border: none;
  box-shadow: 0 4px 18px rgba(255, 107, 53, 0.35);
}

:deep(.step-footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #ff7d4d, #e85f2a);
  box-shadow: 0 6px 22px rgba(255, 107, 53, 0.42);
}

@media (max-width: 900px) {
  .init-wrapper {
    grid-template-columns: 1fr;
    min-height: unset;
    margin: 18px 0;
  }

  .intro-panel {
    padding: 34px 24px 22px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .brand-title {
    font-size: 52px;
  }

  .wizard-card {
    min-height: 540px;
  }

  .summary-panel {
    width: 100%;
  }

  .summary-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}
</style>
