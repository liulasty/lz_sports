<template>
  <div class="login-container">
    <!-- Animated background elements -->
    <div class="bg-layer">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-wrapper">
      <!-- Left branding panel -->
      <div class="brand-panel">
        <div class="brand-content">
          <div class="brand-badge">SPORTS MANAGEMENT</div>
          <h1 class="brand-title">竞技<br /><span>无界</span></h1>
          <p class="brand-sub">专业体育管理平台，助力每一位运动员走向卓越</p>
          <div class="brand-stats">
            <div class="stat">
              <span class="stat-num">10K+</span>
              <span class="stat-label">运动员</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <span class="stat-num">500+</span>
              <span class="stat-label">赛事</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <span class="stat-num">98%</span>
              <span class="stat-label">满意度</span>
            </div>
          </div>
        </div>
        <div class="brand-deco">
          <svg viewBox="0 0 200 200" class="deco-svg" xmlns="http://www.w3.org/2000/svg">
            <circle cx="100" cy="100" r="80" stroke="rgba(255,107,53,0.25)" stroke-width="1" fill="none" />
            <circle cx="100" cy="100" r="55" stroke="rgba(255,107,53,0.15)" stroke-width="1" fill="none" />
            <circle cx="100" cy="100" r="30" stroke="rgba(255,107,53,0.35)" stroke-width="2" fill="none" />
            <line x1="20" y1="100" x2="180" y2="100" stroke="rgba(255,107,53,0.2)" stroke-width="1" />
            <line x1="100" y1="20" x2="100" y2="180" stroke="rgba(255,107,53,0.2)" stroke-width="1" />
          </svg>
        </div>
      </div>

      <!-- Right login card -->
      <div class="login-card">
        <div class="card-inner">
          <div class="login-header">
            <img v-if="configStore.logoUrl" :src="configStore.logoUrl" class="school-logo" alt="Logo" />
            <div v-if="!configStore.logoUrl" class="logo-placeholder">
              <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="20" cy="20" r="18" stroke="#FF6B35" stroke-width="2"/>
                <path d="M12 20 Q20 10 28 20 Q20 30 12 20Z" fill="#FF6B35" opacity="0.8"/>
              </svg>
            </div>
            <h2 class="card-title">{{ configStore.schoolName || 'LZ Sports' }}</h2>
            <p class="card-sub">欢迎回来，请登录您的账户</p>
          </div>

          <el-form
            :model="loginForm"
            :rules="rules"
            ref="loginFormRef"
            label-width="0"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                prefix-icon="User"
                placeholder="用户名 / 邮箱"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                prefix-icon="Lock"
                type="password"
                placeholder="密码"
                show-password
                @keyup.enter="handleLogin"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item>
              <button
                class="login-btn"
                :class="{ loading }"
                @click.prevent="handleLogin"
                :disabled="loading"
              >
                <span v-if="!loading" class="btn-text">
                  登 录
                  <svg class="btn-arrow" viewBox="0 0 24 24" fill="none">
                    <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span v-else class="btn-spinner">
                  <svg class="spin" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-dashoffset="10"/>
                  </svg>
                  登录中...
                </span>
              </button>
            </el-form-item>

            <div class="login-footer">
              <div>
                <span class="footer-text">还没有账号？</span>
                <el-button link class="register-link" @click="$router.push('/register')">
                  立即注册
                </el-button>
              </div>
              <el-button link class="register-link" @click="$router.push('/forgot-password')">
                忘记密码？
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useConfigStore } from '@/stores/config'
import { login } from '@/api/user'
import { checkInit } from '@/api/init'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const configStore = useConfigStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const { data } = await checkInit()
    if (data === false) {
      localStorage.removeItem('isInitialized')
      router.replace('/init')
      return
    } else {
      localStorage.setItem('isInitialized', 'true')
    }
  } catch (error) {
    console.error('Init check failed', error)
  }
  configStore.fetchConfig()
})

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

          if (userInfo.isFirstLogin) {
            await router.replace('/reset-password')
          } else {
            await router.replace('/dashboard')
          }
        } else {
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
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

/* ── Root Variables ── */
* {
  --accent: #FF6B35;
  --accent-dark: #e0541e;
  --dark: #0d0f14;
  --dark-2: #161921;
  --dark-3: #1e2130;
  --dark-4: #252a3a;
  --text-primary: var(--el-bg-color-page);
  --text-secondary: var(--el-text-color-secondary);
  --border: rgba(255, 255, 255, 0.08);
}

/* ── Container ── */
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #0d0f14;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
}

/* ── Animated Background ── */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,107,53,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,107,53,0.04) 1px, transparent 1px);
  background-size: 48px 48px;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: drift 12s ease-in-out infinite alternate;
}
.c1 {
  width: 500px; height: 500px;
  background: rgba(255, 107, 53, 0.12);
  top: -150px; left: -150px;
  animation-duration: 10s;
}
.c2 {
  width: 350px; height: 350px;
  background: rgba(99, 140, 255, 0.08);
  bottom: -100px; right: -100px;
  animation-duration: 14s;
  animation-direction: alternate-reverse;
}
.c3 {
  width: 250px; height: 250px;
  background: rgba(255, 107, 53, 0.06);
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  animation-duration: 18s;
}

@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to   { transform: translate(30px, 20px) scale(1.05); }
}

/* ── Wrapper ── */
.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: min(960px, 95vw);
  min-height: 560px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px var(--border),
    0 40px 80px rgba(0, 0, 0, 0.5),
    0 0 60px rgba(255, 107, 53, 0.08);
  animation: fadeUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Brand Panel (Left) ── */
.brand-panel {
  position: relative;
  flex: 1;
  background: linear-gradient(145deg, #1a1f2e 0%, #0f1420 100%);
  padding: 52px 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-right: 1px solid var(--border);
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  color: var(--accent);
  border: 1px solid rgba(255, 107, 53, 0.4);
  padding: 4px 12px;
  border-radius: 20px;
  margin-bottom: 28px;
  text-transform: uppercase;
  background: rgba(255, 107, 53, 0.06);
}

.brand-title {
  font-family: 'Bebas Neue', 'Noto Sans SC', sans-serif;
  font-size: 72px;
  line-height: 1;
  color: var(--text-primary);
  margin: 0 0 20px;
  letter-spacing: 0.02em;
}
.brand-title span {
  color: var(--accent);
}

.brand-sub {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
  max-width: 260px;
  margin: 0 0 48px;
}

.brand-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat-num {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 28px;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}
.stat-label {
  font-size: 11px;
  color: var(--text-secondary);
  letter-spacing: 0.1em;
}
.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border);
}

.brand-deco {
  position: absolute;
  bottom: -40px;
  right: -40px;
  width: 260px;
  opacity: 0.5;
  animation: rotate 30s linear infinite;
}
.deco-svg { width: 100%; }

@keyframes rotate {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ── Login Card (Right) ── */
.login-card {
  width: 400px;
  flex-shrink: 0;
  background: #161921;
  display: flex;
  align-items: center;
  border-left: 1px solid rgba(255, 255, 255, 0.06);
}

.card-inner {
  width: 100%;
  padding: 52px 44px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.school-logo {
  height: 52px;
  margin-bottom: 16px;
  object-fit: contain;
}

.logo-placeholder {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.logo-placeholder svg {
  width: 48px;
  height: 48px;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--el-bg-color-page);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.card-sub {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

/* ── Form ── */
.login-form {
  --el-border-color: rgba(255, 255, 255, 0.1);
  --el-fill-color-blank: rgba(255, 255, 255, 0.04);
  --el-text-color-regular: var(--el-bg-color-page);
  --el-text-color-placeholder: var(--el-text-color-secondary);
  --el-border-radius-base: 10px;
  --el-color-primary: #FF6B35;
  --el-bg-color: #161921;
  --el-input-bg-color: rgba(255, 255, 255, 0.04);
}

:deep(.custom-input .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding: 0 16px;
  height: 48px;
  transition: border-color 0.2s, background 0.2s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: rgba(255, 107, 53, 0.5) !important;
  background-color: rgba(255, 255, 255, 0.07) !important;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #FF6B35 !important;
  background-color: rgba(255, 107, 53, 0.06) !important;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.15) !important;
}

:deep(.custom-input .el-input__inner) {
  color: var(--el-bg-color-page) !important;
  background-color: transparent !important;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
}

:deep(.custom-input .el-input__prefix-inner .el-icon) {
  color: var(--el-text-color-secondary) !important;
}

:deep(.custom-input .el-input__suffix .el-icon) {
  color: var(--el-text-color-secondary) !important;
}

:deep(.el-form-item__error) {
  color: #ff7a7a;
  font-size: 11px;
  padding-top: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

/* ── Login Button ── */
.login-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border: none;
  border-radius: 10px;
  color: var(--el-color-white);
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.15em;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.18s, box-shadow 0.18s, opacity 0.18s;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.35);
}

.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, transparent 60%);
  pointer-events: none;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(255, 107, 53, 0.45);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 12px rgba(255, 107, 53, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-arrow {
  width: 18px;
  height: 18px;
  transition: transform 0.2s;
}
.login-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.btn-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spin {
  width: 18px;
  height: 18px;
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ── Footer ── */
.login-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-top: 6px;
}

.footer-text {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

:deep(.register-link) {
  color: var(--accent) !important;
  font-size: 13px !important;
  font-weight: 600;
  padding: 0 !important;
  transition: opacity 0.2s;
}
:deep(.register-link:hover) {
  opacity: 0.8;
}

/* ── Responsive ── */
@media (max-width: 700px) {
  .brand-panel {
    display: none;
  }
  .login-card {
    width: 100%;
  }
  .login-wrapper {
    width: 100%;
    min-height: 100vh;
    border-radius: 0;
  }
  .card-inner {
    padding: 40px 28px;
  }
}
</style>

<!-- Global override: force dark theme on Element Plus inputs inside login page -->
<style>
.login-container .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.05) !important;
  box-shadow: none !important;
}
.login-container .el-input__inner {
  color: #f0f2f8 !important;
  background-color: transparent !important;
}
.login-container .el-input__inner::placeholder {
  color: #8892a4 !important;
}
.login-container .el-button.is-link {
  color: #FF6B35 !important;
}
</style>