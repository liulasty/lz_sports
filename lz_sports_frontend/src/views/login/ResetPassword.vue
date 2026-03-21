<template>
  <div class="reset-container">
    <div class="reset-card">

      <div class="reset-header">
        <div class="lock-icon">
          <el-icon :size="28" color="var(--el-color-primary)"><Lock /></el-icon>
        </div>
        <h2>修改初始密码</h2>
        <p class="tip">为了您的账号安全，首次登录请修改密码</p>
      </div>

      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-position="top"
          size="large"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
              v-model="form.oldPassword"
              type="password"
              show-password
              placeholder="请输入旧密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="form.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码（8-20位，含字母和数字）"
          />
          <!-- 密码强度条 -->
          <div class="strength-bar" v-if="form.newPassword">
            <div class="strength-track">
              <div
                  v-for="i in 4"
                  :key="i"
                  class="strength-segment"
                  :class="{ active: i <= passwordStrength.level }"
                  :style="{ background: i <= passwordStrength.level ? passwordStrength.color : '' }"
              />
            </div>
            <span class="strength-label" :style="{ color: passwordStrength.color }">
              {{ passwordStrength.text }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
          />
        </el-form-item>

        <el-form-item style="margin-top: 8px;">
          <el-button
              type="primary"
              @click="handleSubmit"
              :loading="loading"
              style="width: 100%;"
          >
            确认修改
          </el-button>
        </el-form-item>
      </el-form>

      <p class="footer-tip">修改成功后将自动退出登录</p>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = form.newPassword
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
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
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

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await updateUserInfo({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      })
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        userStore.logout()
        router.push('/login')
      } else {
        ElMessage.error(res.msg || '修改失败，请检查旧密码是否正确')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
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
  font-size: 13px;
  color: var(--el-text-color-placeholder);
  margin: 28px 0 0;
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
</style>