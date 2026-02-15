<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>LZ Sports Register</h2>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="80px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="registerForm.username" placeholder="Enter username" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="registerForm.email" placeholder="Enter email" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="Enter password" show-password />
        </el-form-item>
        <el-form-item label="Confirm" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="Confirm password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">Register</el-button>
          <el-button @click="$router.push('/login')">Back to Login</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input the password again'))
  } else if (value !== registerForm.password) {
    callback(new Error("Two inputs don't match!"))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  email: [
    { required: true, message: 'Please input email', trigger: 'blur' },
    { type: 'email', message: 'Please input correct email address', trigger: ['blur', 'change'] }
  ],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...data } = registerForm
        const res = await register(data)
        
        if (res.code === 1) {
          ElMessage.success('Registration successful, please login')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || 'Registration failed')
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 400px;
}
</style>
