<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">401</div>
      <div class="error-graphic">
        <svg viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="65" y="70" width="70" height="55" rx="8" fill="rgba(255,107,53,0.15)" stroke="#FF6B35" stroke-width="2" />
          <path d="M80 70V52a20 20 0 0140 0v18" stroke="#FF6B35" stroke-width="2.5" stroke-linecap="round" fill="none" />
          <circle cx="100" cy="92" r="8" fill="rgba(255,107,53,0.3)" stroke="#FF6B35" stroke-width="1.5" />
          <rect x="97" y="95" width="6" height="12" rx="3" fill="#FF6B35" />
          <line x1="40" y1="30" x2="50" y2="30" stroke="rgba(255,107,53,0.4)" stroke-width="1.5" stroke-linecap="round" />
          <line x1="150" y1="50" x2="160" y2="50" stroke="rgba(255,107,53,0.3)" stroke-width="1.5" stroke-linecap="round" />
          <circle cx="35" cy="80" r="3" fill="rgba(255,107,53,0.25)" />
          <circle cx="168" cy="35" r="4" fill="rgba(255,107,53,0.2)" />
        </svg>
      </div>
      <h2 class="error-title">访问未授权</h2>
      <p class="error-desc">您的登录状态已过期，请重新登录后继续操作。</p>
      <div class="error-actions">
        <button class="btn-primary" @click="goLogin">重新登录</button>
        <button class="btn-ghost" @click="goHome">返回首页</button>
      </div>
    </div>
    <div class="error-bg">
      <div class="bg-num">401</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()

const goLogin = () => {
  store.logout()
  router.push('/login')
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
@import './error-shared.css';

.error-code {
  color: #ff6b35;
}
</style>
