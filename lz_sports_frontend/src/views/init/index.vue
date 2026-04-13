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
        <template #header>
          <div class="card-header">
            <div>
              <h2 class="card-title">系统初始化向导</h2>
              <p class="card-subtitle">{{ stepDescriptions[active] }}</p>
            </div>
            <div class="step-index">STEP {{ active + 1 }} / 4</div>
          </div>
        </template>

        <el-steps :active="active" finish-status="success" align-center class="init-steps">
          <el-step title="学校信息" />
          <el-step title="管理员设置" />
          <el-step title="基础数据" />
          <el-step title="完成" />
        </el-steps>

        <div class="step-content">
          <!-- Step 1: School Info -->
          <el-form v-if="active === 0" :model="form" label-position="top" :rules="rules" ref="step1Form" class="lz-form">
            <el-form-item label="学校名称" prop="schoolName">
              <el-input v-model="form.schoolName" placeholder="例如：xx大学" />
            </el-form-item>
            <el-form-item label="组织模式" prop="orgMode">
              <el-radio-group v-model="form.orgMode" @change="handleModeChange">
                <el-radio value="UNIVERSITY">大学模式 (学院-专业-班级)</el-radio>
                <el-radio value="K12">K12模式 (年级-班级)</el-radio>
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
          <el-form v-if="active === 1" :model="form" label-position="top" :rules="rules" ref="step2Form" class="lz-form">
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
            <p class="grade-tip">请确认顶级部门/院系列表（可添加或删除）：</p>
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
            <el-descriptions title="配置摘要" :column="1" border class="summary-table">
              <el-descriptions-item label="学校名称">{{ form.schoolName }}</el-descriptions-item>
              <el-descriptions-item label="管理员账号">{{ form.adminUsername }}</el-descriptions-item>
              <el-descriptions-item label="部门数量">{{ form.grades.length }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="step-footer lz-actions">
          <el-button @click="prev" v-if="active > 0">上一步</el-button>
          <el-button @click="next" v-if="active < 3">下一步</el-button>
          <el-button type="primary" @click="submit" v-if="active === 3" :loading="loading">完成初始化</el-button>
          <el-button v-if="active < 3" class="ghost-btn" @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-card>
    </div>
  </div>
 </template>

<script setup>
import { ref, reactive, nextTick, computed } from 'vue'
import { initSystem } from '@/api/init'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const active = ref(0)
const step1Form = ref(null)
const step2Form = ref(null)
const loading = ref(false)

const templates = {
  K12: [
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
    selectedTemplate.value = templates.K12[0].name
    form.grades = [...templates.K12[0].grades]
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

const submit = () => {
  loading.value = true
  initSystem(form).then(res => {
    ElMessage.success('初始化成功！')
    localStorage.setItem('isInitialized', 'true')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
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
  color: #7f8898;
}

.step-index {
  font-size: 11px;
  color: #ff6b35;
  letter-spacing: 0.12em;
  font-weight: 700;
}

.init-steps {
  margin-bottom: 18px;
}

.step-content {
  margin: 22px 0 28px;
  min-height: 265px;
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
  color: #8f98a9;
}

.summary-table {
  width: min(86%, 560px);
  margin: 0 auto;
  text-align: left;
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

::deep(.el-step__title) {
  color: #9ba4b5;
}

::deep(.el-step__title.is-process),
::deep(.el-step__title.is-success) {
  color: #cdd5e2;
}

::deep(.el-descriptions__label) {
  width: 130px;
  color: #c4ccda !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

::deep(.el-descriptions__content) {
  color: #9eabbe !important;
  background: rgba(255, 255, 255, 0.03) !important;
}

::deep(.summary-table .el-descriptions__title) {
  color: #cfd7e4;
  margin-bottom: 12px;
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

  .summary-table {
    width: 100%;
  }
}
</style>
