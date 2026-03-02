<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="header-container">
          <h2>用户注册</h2>
          <span class="sub-title">LZ Sports</span>
        </div>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="QQ邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入QQ邮箱">
             <template #append>@qq.com</template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div style="display: flex; width: 100%;">
            <el-input v-model="registerForm.code" placeholder="6位验证码" style="flex: 1; margin-right: 10px;" />
            <el-button type="primary" :disabled="isSending || countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%;">注册</el-button>
        </el-form-item>
        <div style="text-align: center;">
          <el-button link type="primary" @click="$router.push('/login')">已有账号？去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendCode } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const isSending = ref(false)
const countdown = ref(0)
let timer = null

const registerForm = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const fullEmail = computed(() => {
  return registerForm.email ? registerForm.email + '@qq.com' : ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入密码不一致!"))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入QQ号', trigger: 'blur' },
    { pattern: /^[1-9][0-9]{4,10}$/, message: '请输入正确的QQ号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入QQ号')
    return
  }
  if (!/^[1-9][0-9]{4,10}$/.test(registerForm.email)) {
    ElMessage.warning('QQ号格式不正确')
    return
  }
  
  isSending.value = true
  try {
    const res = await sendCode(fullEmail.value)
    if (res.code === 1) {
      ElMessage.success('验证码已发送，请查收邮件')
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    isSending.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = {
          username: registerForm.username,
          password: registerForm.password,
          email: fullEmail.value,
          code: registerForm.code
        }
        const res = await register(data)
        
        if (res.code === 1) {
          ElMessage.success(res.data || '注册成功，请等待审核')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || '注册失败')
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
