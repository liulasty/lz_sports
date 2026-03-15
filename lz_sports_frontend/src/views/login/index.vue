<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <img v-if="schoolInfo.logoUrl" :src="schoolInfo.logoUrl" class="school-logo" alt="Logo" />
          <h2>{{ schoolInfo.schoolName || 'LZ Sports Login' }}</h2>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="User" placeholder="用户名/邮箱" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%;">登录</el-button>
        </el-form-item>
        <div class="login-footer">
          <el-button link @click="$router.push('/register')">注册账号</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { getSchoolConfig } from '@/api/init'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const schoolInfo = reactive({
  schoolName: '',
  logoUrl: ''
})

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  fetchSchoolInfo()
})

const fetchSchoolInfo = async () => {
  try {
    const res = await getSchoolConfig()
    if (res.code === 200 && res.data) {
      schoolInfo.schoolName = res.data.schoolName
      schoolInfo.logoUrl = res.data.logoUrl
    }
  } catch (error) {
    console.error('Failed to fetch school config', error)
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        
        if (res.code === 200) {
          const { token, ...userInfo } = res.data
          userStore.setToken(token)
          userStore.setUserInfo(userInfo)
          ElMessage.success('登录成功')
          
          // Check for first login (assuming isFirstLogin flag in userInfo)
          // If backend doesn't support it, this condition will be false
          if (userInfo.isFirstLogin) {
             await router.replace('/reset-password')
          } else {
             await router.replace('/dashboard')
          }
        } else {
          // Error handled by interceptor usually, but if not:
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--main-bg-color, #f0f2f5);
  background-image: url('https://gw.alipayobjects.com/zos/rmsportal/TVYTbAXWheQpRcWDaDMu.svg');
  background-repeat: no-repeat;
  background-position: center 110px;
  background-size: 100%;
}
.login-card {
  width: 400px;
  border-radius: 8px;
}
.login-header {
  text-align: center;
  margin-bottom: 20px;
}
.school-logo {
  height: 50px;
  margin-bottom: 10px;
}
.login-footer {
  display: flex;
  justify-content: space-between;
}
</style>
