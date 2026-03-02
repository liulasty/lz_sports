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
      </el-steps>

      <div class="step-content">
        <!-- Step 1: School Info -->
        <el-form v-if="active === 0" :model="form" label-width="120px" :rules="rules" ref="step1Form">
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="例如：xx大学" />
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
          <p>请确认年级列表（可添加或删除）：</p>
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
            + New Grade
          </el-button>
        </div>
      </div>

      <div class="step-footer">
        <el-button style="margin-top: 12px" @click="prev" v-if="active > 0">上一步</el-button>
        <el-button style="margin-top: 12px" @click="next" v-if="active < 2">下一步</el-button>
        <el-button type="primary" style="margin-top: 12px" @click="submit" v-if="active === 2">完成初始化</el-button>
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

const form = reactive({
  schoolName: '',
  logoUrl: '',
  themeColor: '#409EFF',
  contactEmail: '',
  adminUsername: 'admin',
  adminPassword: '',
  adminEmail: '',
  grades: ['大一', '大二', '大三', '大四', '研一', '研二', '研三']
})

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
  initSystem(form).then(res => {
    ElMessage.success('初始化成功！')
    localStorage.setItem('isInitialized', 'true')
    router.push('/login')
  })
}
</script>

<style scoped>
.init-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
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
