<template>
  <div class="init-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统初始化向导</span>
        </div>
      </template>

      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="学校信息" />
        <el-step title="管理员设置" />
        <el-step title="基础数据" />
        <el-step title="完成" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: School Info -->
        <el-form v-if="active === 0" :model="form" label-width="120px" :rules="rules" ref="step1Form">
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="例如：xx大学" />
          </el-form-item>
          <el-form-item label="组织架构模式" prop="orgMode">
            <el-radio-group v-model="form.orgMode" @change="handleModeChange">
              <el-radio value="UNIVERSITY">大学模式 (学院-专业-班级)</el-radio>
              <el-radio value="K12">K12模式 (年级-班级)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Logo链接" prop="logoUrl">
            <el-input v-model="form.logoUrl" placeholder="Logo图片地址" />
          </el-form-item>
          <el-form-item label="主题色" prop="themeColor">
            <el-color-picker v-model="form.themeColor" />
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" />
          </el-form-item>
        </el-form>

        <!-- Step 2: Admin Info -->
        <el-form v-if="active === 1" :model="form" label-width="120px" :rules="rules" ref="step2Form">
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
          <p>请确认顶级部门/院系列表（可添加或删除）：</p>
          <el-tag
            v-for="tag in form.grades"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="handleClose(tag)"
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="InputRef"
            v-model="inputValue"
            class="input-new-tag"
            size="small"
            @keyup.enter="handleInputConfirm"
            @blur="handleInputConfirm"
            style="width: 100px;"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showInput">
            + 添加部门
          </el-button>
        </div>

        <!-- Step 4: Finish -->
        <div v-if="active === 3" style="text-align: center;">
          <el-result
            icon="info"
            title="确认初始化"
            sub-title="请确认以上信息无误，点击下方按钮开始初始化系统。"
          >
          </el-result>
          <el-descriptions title="配置摘要" :column="1" border style="width: 80%; margin: 0 auto; text-align: left;">
            <el-descriptions-item label="学校名称">{{ form.schoolName }}</el-descriptions-item>
            <el-descriptions-item label="管理员账号">{{ form.adminUsername }}</el-descriptions-item>
            <el-descriptions-item label="部门数量">{{ form.grades.length }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="step-footer">
        <el-button style="margin-top: 12px" @click="prev" v-if="active > 0">上一步</el-button>
        <el-button style="margin-top: 12px" @click="next" v-if="active < 3">下一步</el-button>
        <el-button type="primary" style="margin-top: 12px" @click="submit" v-if="active === 3" :loading="loading">完成初始化</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { initSystem } from '@/api/init'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const active = ref(0)
const step1Form = ref(null)
const step2Form = ref(null)
const loading = ref(false)

const form = reactive({
  schoolName: '',
  logoUrl: '',
  themeColor: '#409EFF',
  contactEmail: '',
  adminUsername: 'admin',
  adminPassword: '',
  adminEmail: '',
  orgMode: 'UNIVERSITY',
  grades: ['计算机学院', '理学院', '外国语学院', '体育部']
})

const handleModeChange = (val) => {
  if (val === 'UNIVERSITY') {
    form.grades = ['计算机学院', '理学院', '外国语学院', '体育部']
  } else {
    form.grades = ['高一', '高二', '高三']
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
.init-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--main-bg-color);
}
.box-card {
  width: 800px;
}
.step-content {
  margin: 40px 0;
  min-height: 200px;
}
.step-footer {
  text-align: center;
}
</style>
