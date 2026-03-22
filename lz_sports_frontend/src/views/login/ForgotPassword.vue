<template>
  <div class="reset-container">
    <div class="reset-card">

      <div class="reset-header">
        <div class="lock-icon">
          <el-icon :size="28" color="var(--el-color-primary)"><Lock /></el-icon>
        </div>
        <h2>忘记密码</h2>
        <p class="tip">通过邮箱验证重置您的密码</p>
      </div>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px;">
        <el-step title="验证邮箱" />
        <el-step title="输入验证码" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- Step 1: 输入邮箱 -->
      <div v-show="currentStep === 0">
        <el-form :model="formStep1" :rules="rulesStep1" ref="formRef1" label-position="top" size="large">
          <el-form-item label="注册邮箱" prop="email">
            <el-input v-model="formStep1.email" placeholder="请输入注册邮箱" />
          </el-form-item>
          <el-form-item style="margin-top: 8px;">
            <el-button type="primary" @click="handleSendCode" :loading="loading" :disabled="countdown > 0" style="width: 100%;">
              {{ countdown > 0 ? `${countdown}秒后可重发` : '发送验证码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 2: 输入验证码 -->
      <div v-show="currentStep === 1">
        <el-form :model="formStep2" :rules="rulesStep2" ref="formRef2" label-position="top" size="large">
          <el-form-item label="验证码" prop="code">
            <el-input v-model="formStep2.code" placeholder="请输入收到的验证码" />
          </el-form-item>
          <el-form-item style="margin-top: 8px;">
            <el-button type="primary" @click="handleVerifyCode" :loading="loading" style="width: 100%;">验证</el-button>
            <el-button @click="currentStep = 0" style="width: 100%; margin-top: 12px; margin-left: 0;">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 3: 重置密码 -->
      <div v-show="currentStep === 2">
        <el-form :model="formStep3" :rules="rulesStep3" ref="formRef3" label-position="top" size="large">
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="formStep3.newPassword" type="password" show-password placeholder="请输入新密码（8-20位，含字母和数字）" />
            <!-- 密码强度条 -->
            <div class="strength-bar" v-if="formStep3.newPassword">
              <div class="strength-track">
                <div v-for="i in 4" :key="i" class="strength-segment" :class="{ active: i <= passwordStrength.level }" :style="{ background: i <= passwordStrength.level ? passwordStrength.color : '' }" />
              </div>
              <span class="strength-label" :style="{ color: passwordStrength.color }">{{ passwordStrength.text }}</span>
            </div>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="formStep3.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>

          <el-form-item style="margin-top: 8px;">
            <el-button type="primary" @click="handleResetPassword" :loading="loading" style="width: 100%;">确认重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="footer-tip">
        <router-link to="/login" class="back-link">返回登录</router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, verifyCode, resetPassword } from '@/api/auth'

const router = useRouter()

const currentStep = ref(0)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const formRef1 = ref(null)
const formRef2 = ref(null)
const formRef3 = ref(null)

const formStep1 = reactive({ email: '' })
const formStep2 = reactive({ code: '' })
const formStep3 = reactive({ newPassword: '', confirmPassword: '' })

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = formStep3.newPassword
  if (!pwd) return { level: 0, text: '', color: '' }
  let level = 0
  if (pwd.length >= 8) level++
  if (/[a-zA-Z]/.test(pwd)) level++
  if (/[0-9]/.test(pwd)) level++
  if (/[^a-zA-Z0-9]/.test(pwd)) level++
  const map = [
    { text: '弱', color: 'var(--el-color-danger)' },
    { text: '弱', color: 'var(--el-color-danger)' },
    { text: '中', color: 'var(--el-color-warning)' },
    { text: '强', color: 'var(--el-color-success)' },
    { text: '非常强', color: 'var(--el-color-success)' },
  ]
  return { level, ...map[level] }
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formStep3.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rulesStep1 = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ]
}

const rulesStep2 = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const rulesStep3 = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为 8-20 位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).+$/,
      message: '密码需同时包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

let verifyToken = ''

const handleSendCode = async () => {
  if (!formRef1.value) return
  await formRef1.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await sendCode({ email: formStep1.email, scene: 'RESET_PASSWORD' })
      if (res.code === 200) {
        ElMessage.success('验证码已发送至您的邮箱')
        currentStep.value = 1
        startCountdown()
      } else {
        ElMessage.error(res.msg || '发送验证码失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleVerifyCode = async () => {
  if (!formRef2.value) return
  await formRef2.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await verifyCode({ email: formStep1.email, code: formStep2.code, scene: 'RESET_PASSWORD' })
      if (res.code === 200) {
        ElMessage.success('验证成功')
        verifyToken = res.data || formStep2.code // If token is returned, use it. Otherwise, we might use code as token
        currentStep.value = 2
      } else {
        ElMessage.error(res.msg || '验证失败，请检查验证码')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleResetPassword = async () => {
  if (!formRef3.value) return
  await formRef3.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await resetPassword({
        email: formStep1.email,
        newPassword: formStep3.newPassword,
        verifyToken: verifyToken || formStep2.code
      })
      if (res.code === 200) {
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '密码重置失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--el-color-primary-light-8) 0%, var(--el-bg-color-page) 100%);
  padding: 20px;
}

.reset-card {
  width: 100%;
  max-width: 440px;
  background: var(--el-bg-color);
  border: none;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.reset-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

.reset-header {
  text-align: center;
  margin-bottom: 36px;
}

.lock-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 4px 16px var(--el-color-primary-light-8);
}

.reset-header h2 {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}

.tip {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

/* 密码强度条 */
.strength-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding: 0 4px;
}

.strength-track {
  display: flex;
  gap: 6px;
  flex: 1;
}

.strength-segment {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--el-border-color-lighter);
  transition: all 0.3s ease;
}

.strength-label {
  font-size: 13px;
  min-width: 40px;
  text-align: right;
  font-weight: 500;
  transition: color 0.3s ease;
}

.footer-tip {
  text-align: center;
  margin: 28px 0 0;
}

.back-link {
  font-size: 14px;
  color: var(--el-color-primary);
  text-decoration: none;
  transition: color 0.3s;
}

.back-link:hover {
  color: var(--el-color-primary-light-3);
  text-decoration: underline;
}

/* Element Plus 表单样式增强 */
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-border-color-light) inset;
  transition: all 0.3s;
  border-radius: 8px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-button--primary) {
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 12px;
  height: 44px;
  box-shadow: 0 4px 12px var(--el-color-primary-light-5);
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-3);
}

:deep(.el-step__title) {
  font-size: 13px;
}
</style>