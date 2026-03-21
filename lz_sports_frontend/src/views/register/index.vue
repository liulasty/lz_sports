<template>
  <div class="register-container">
    <!-- Animated background -->
    <div class="bg-layer">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="register-wrapper">
      <!-- Left brand panel -->
      <div class="brand-panel">
        <div class="brand-top">
          <div class="brand-badge">JOIN US</div>
          <h1 class="brand-title">加入<br /><span>赛场</span></h1>
          <p class="brand-sub">注册账号，开启您的专业体育管理之旅</p>
        </div>
        <div class="brand-steps">
          <div class="step" v-for="(s, i) in steps" :key="i">
            <div class="step-num">{{ i + 1 }}</div>
            <div class="step-info">
              <div class="step-title">{{ s.title }}</div>
              <div class="step-desc">{{ s.desc }}</div>
            </div>
          </div>
        </div>
        <div class="brand-deco">
          <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
            <circle cx="100" cy="100" r="80" stroke="rgba(255,107,53,0.2)" stroke-width="1" fill="none"/>
            <circle cx="100" cy="100" r="55" stroke="rgba(255,107,53,0.12)" stroke-width="1" fill="none"/>
            <circle cx="100" cy="100" r="30" stroke="rgba(255,107,53,0.3)" stroke-width="2" fill="none"/>
            <line x1="20" y1="100" x2="180" y2="100" stroke="rgba(255,107,53,0.15)" stroke-width="1"/>
            <line x1="100" y1="20" x2="100" y2="180" stroke="rgba(255,107,53,0.15)" stroke-width="1"/>
          </svg>
        </div>
      </div>

      <!-- Right form panel -->
      <div class="form-panel">
        <div class="form-inner">
          <div class="form-header">
            <h2 class="form-title">创建账号</h2>
            <p class="form-sub">LZ Sports · 欢迎加入</p>
          </div>

          <el-form
              :model="registerForm"
              :rules="rules"
              ref="registerFormRef"
              label-width="0"
              class="reg-form"
          >
            <!-- 用户名 -->
            <div class="field-label">用户名</div>
            <el-form-item prop="username">
              <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="User"
                  class="custom-input"
              />
            </el-form-item>

            <!-- QQ邮箱 -->
            <div class="field-label">QQ 邮箱</div>
            <el-form-item prop="email">
              <div class="qq-input-wrap">
                <el-input
                    v-model="registerForm.email"
                    placeholder="请输入 QQ 号"
                    prefix-icon="Message"
                    class="custom-input qq-input"
                />
                <div class="qq-suffix">@qq.com</div>
              </div>
            </el-form-item>

            <!-- 验证码 -->
            <div class="field-label">验证码</div>
            <el-form-item prop="code">
              <div class="code-row">
                <el-input
                    v-model="registerForm.code"
                    placeholder="6 位验证码"
                    prefix-icon="Key"
                    class="custom-input code-input"
                />
                <button
                    class="send-btn"
                    :class="{ disabled: isSending || countdown > 0 }"
                    :disabled="isSending || countdown > 0"
                    @click.prevent="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s 后重试` : '获取验证码' }}
                </button>
              </div>
            </el-form-item>

            <!-- 密码 -->
            <div class="field-label">密码</div>
            <el-form-item prop="password">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  show-password
                  class="custom-input"
              />
            </el-form-item>

            <!-- 确认密码 -->
            <div class="field-label">确认密码</div>
            <el-form-item prop="confirmPassword">
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  prefix-icon="Lock"
                  show-password
                  class="custom-input"
              />
            </el-form-item>

            <!-- 提交 -->
            <el-form-item style="margin-top: 8px;">
              <button
                  class="submit-btn"
                  :class="{ loading }"
                  :disabled="loading"
                  @click.prevent="handleRegister"
              >
                <span v-if="!loading" class="btn-text">
                  立即注册
                  <svg class="btn-arrow" viewBox="0 0 24 24" fill="none">
                    <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span v-else class="btn-spinner">
                  <svg class="spin" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" stroke-dasharray="32" stroke-dashoffset="10"/>
                  </svg>
                  注册中...
                </span>
              </button>
            </el-form-item>

            <div class="form-footer">
              <span class="footer-text">已有账号？</span>
              <el-button link class="login-link" @click="$router.push('/login')">去登录</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendCode } from '@/api/user'
import { ElMessage } from 'element-plus'
import { isSuccess } from '@/utils/result'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const isSending = ref(false)
const countdown = ref(0)
let timer = null

const steps = [
  { title: '填写信息', desc: '输入用户名与 QQ 邮箱' },
  { title: '邮箱验证', desc: '获取并填写验证码' },
  { title: '等待审核', desc: '管理员审核后即可登录' },
]

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
    callback(new Error('两次输入密码不一致！'))
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
  if (!registerForm.email) { ElMessage.warning('请先输入QQ号'); return }
  if (!/^[1-9][0-9]{4,10}$/.test(registerForm.email)) { ElMessage.warning('QQ号格式不正确'); return }
  isSending.value = true
  try {
    const res = await sendCode(fullEmail.value)
    if (isSuccess(res)) {
      ElMessage.success('验证码已发送，请查收邮件')
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) clearInterval(timer)
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
        if (isSuccess(res)) {
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
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

/* ── Container ── */
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #0d0f14;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
}

/* ── Background ── */
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
.c1 { width: 500px; height: 500px; background: rgba(255,107,53,0.1); top: -150px; left: -150px; animation-duration: 10s; }
.c2 { width: 350px; height: 350px; background: rgba(99,140,255,0.07); bottom: -100px; right: -100px; animation-duration: 14s; animation-direction: alternate-reverse; }
.c3 { width: 250px; height: 250px; background: rgba(255,107,53,0.05); top: 50%; left: 50%; transform: translate(-50%,-50%); animation-duration: 18s; }

@keyframes drift {
  from { transform: translate(0,0) scale(1); }
  to   { transform: translate(30px,20px) scale(1.05); }
}

/* ── Wrapper ── */
.register-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: min(960px, 95vw);
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
      0 0 0 1px rgba(255,255,255,0.07),
      0 40px 80px rgba(0,0,0,0.5),
      0 0 60px rgba(255,107,53,0.07);
  animation: fadeUp 0.6s cubic-bezier(0.22,1,0.36,1) both;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Brand Panel ── */
.brand-panel {
  position: relative;
  flex: 1;
  background: linear-gradient(145deg, #1a1f2e 0%, #0f1420 100%);
  padding: 52px 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-right: 1px solid rgba(255,255,255,0.06);
  overflow: hidden;
}
.brand-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  color: #FF6B35;
  border: 1px solid rgba(255,107,53,0.4);
  padding: 4px 12px;
  border-radius: 20px;
  margin-bottom: 28px;
  background: rgba(255,107,53,0.06);
}
.brand-title {
  font-family: 'Bebas Neue', 'Noto Sans SC', sans-serif;
  font-size: 72px;
  line-height: 1;
  color: #f0f2f8;
  margin: 0 0 20px;
  letter-spacing: 0.02em;
}
.brand-title span { color: #FF6B35; }
.brand-sub {
  font-size: 13px;
  color: #8892a4;
  line-height: 1.8;
  max-width: 240px;
  margin: 0 0 44px;
}

/* Steps */
.brand-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  z-index: 2;
}
.step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255,107,53,0.15);
  border: 1px solid rgba(255,107,53,0.4);
  color: #FF6B35;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.step-title {
  font-size: 14px;
  font-weight: 600;
  color: #f0f2f8;
  margin-bottom: 2px;
}
.step-desc {
  font-size: 12px;
  color: #8892a4;
}

.brand-deco {
  position: absolute;
  bottom: -50px;
  right: -50px;
  width: 240px;
  opacity: 0.4;
  animation: rotate 30s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ── Form Panel ── */
.form-panel {
  width: 420px;
  flex-shrink: 0;
  background: #161921;
  border-left: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
}
.form-inner {
  width: 100%;
  padding: 44px 40px;
}
.form-header {
  margin-bottom: 28px;
}
.form-title {
  font-size: 22px;
  font-weight: 700;
  color: #f0f2f8;
  margin: 0 0 6px;
}
.form-sub {
  font-size: 13px;
  color: #8892a4;
  margin: 0;
}

/* ── Field Label ── */
.field-label {
  font-size: 12px;
  font-weight: 500;
  color: #8892a4;
  letter-spacing: 0.06em;
  margin-bottom: 6px;
  margin-top: 2px;
}

/* ── Form Overrides ── */
.reg-form {
  --el-color-primary: #FF6B35;
}

:deep(.reg-form .el-form-item) {
  margin-bottom: 14px;
}
:deep(.reg-form .el-form-item__error) {
  color: #ff7a7a;
  font-size: 11px;
  padding-top: 3px;
}

:deep(.custom-input .el-input__wrapper) {
  background-color: rgba(255,255,255,0.05) !important;
  border: 1px solid rgba(255,255,255,0.09) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  height: 44px;
  padding: 0 14px;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}
:deep(.custom-input .el-input__wrapper:hover) {
  border-color: rgba(255,107,53,0.4) !important;
  background-color: rgba(255,255,255,0.07) !important;
}
:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #FF6B35 !important;
  background-color: rgba(255,107,53,0.06) !important;
  box-shadow: 0 0 0 3px rgba(255,107,53,0.14) !important;
}
:deep(.custom-input .el-input__inner) {
  color: #f0f2f8 !important;
  background-color: transparent !important;
  font-size: 13px;
  font-family: 'Noto Sans SC', sans-serif;
}
:deep(.custom-input .el-input__inner::placeholder) {
  color: #4a5568 !important;
}
:deep(.custom-input .el-input__prefix-inner .el-icon),
:deep(.custom-input .el-input__suffix .el-icon) {
  color: #8892a4 !important;
}

/* QQ input */
.qq-input-wrap {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 0;
}
.qq-input {
  flex: 1;
}
:deep(.qq-input .el-input__wrapper) {
  border-radius: 10px 0 0 10px !important;
}
.qq-suffix {
  height: 44px;
  padding: 0 14px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.09);
  border-left: none;
  border-radius: 0 10px 10px 0;
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #8892a4;
  white-space: nowrap;
  flex-shrink: 0;
}

/* Code row */
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}
.code-input { flex: 1; }

.send-btn {
  height: 44px;
  padding: 0 16px;
  background: rgba(255,107,53,0.1);
  border: 1px solid rgba(255,107,53,0.35);
  border-radius: 10px;
  color: #FF6B35;
  font-size: 12px;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.15s;
  flex-shrink: 0;
}
.send-btn:hover:not(.disabled) {
  background: rgba(255,107,53,0.2);
  border-color: #FF6B35;
  transform: translateY(-1px);
}
.send-btn.disabled {
  color: #4a5568;
  background: rgba(255,255,255,0.03);
  border-color: rgba(255,255,255,0.08);
  cursor: not-allowed;
}

/* Submit button */
.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #FF6B35, #e0541e);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.12em;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.18s, box-shadow 0.18s, opacity 0.18s;
  box-shadow: 0 4px 20px rgba(255,107,53,0.3);
}
.submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.12) 0%, transparent 60%);
  pointer-events: none;
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(255,107,53,0.4);
}
.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}
.submit-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.btn-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;
}
.submit-btn:hover .btn-arrow { transform: translateX(4px); }

.btn-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.spin {
  width: 16px;
  height: 16px;
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* Footer */
.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 4px;
}
.footer-text {
  font-size: 13px;
  color: #8892a4;
}
:deep(.login-link) {
  color: #FF6B35 !important;
  font-size: 13px !important;
  font-weight: 600;
  padding: 0 !important;
}
:deep(.login-link:hover) { opacity: 0.8; }

/* Responsive */
@media (max-width: 720px) {
  .brand-panel { display: none; }
  .form-panel { width: 100%; }
  .register-wrapper { width: 100%; min-height: 100vh; border-radius: 0; }
  .form-inner { padding: 40px 28px; }
}
</style>

<style>
/* Global: force dark on El-input inside register page */
.register-container .el-input__wrapper {
  background-color: rgba(255,255,255,0.05) !important;
  box-shadow: none !important;
}
.register-container .el-input__inner {
  color: #f0f2f8 !important;
  background-color: transparent !important;
}
.register-container .el-input__inner::placeholder {
  color: #4a5568 !important;
}
.register-container .el-button.is-link {
  color: #FF6B35 !important;
}
</style>
