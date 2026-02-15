<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>LZ Sports Login</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="loginForm.username" placeholder="Enter username or email" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="Enter password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">Login</el-button>
          <el-button @click="$router.push('/register')">Register</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // Call backend API
        const res = await login(loginForm)
        
        if (res.code === 1) { // Assuming 1 is success
          const { token, ...userInfo } = res.data
          console.log('Login success:', { token, userInfo })
          userStore.setToken(token)
          userStore.setUserInfo(userInfo)
          ElMessage.success('Login successful')
          // Use replace to avoid history stack issues and explicit path
          await router.replace('/dashboard').catch(err => {
            console.error('Router replace error:', err)
          })
        } else {
          ElMessage.error(res.msg || 'Login failed')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.resetFields()
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
</style>
